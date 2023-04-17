/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jreleaser.sdk.nexus2;

import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import dev.failsafe.function.CheckedPredicate;
import dev.failsafe.function.CheckedSupplier;
import feign.Feign;
import feign.FeignException;
import feign.Request;
import feign.Response;
import feign.RetryableException;
import feign.Util;
import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import feign.form.FormData;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.apache.commons.io.IOUtils;
import org.jreleaser.bundle.RB;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.model.spi.upload.UploadException;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.nexus2.api.Data;
import org.jreleaser.sdk.nexus2.api.NexusAPI;
import org.jreleaser.sdk.nexus2.api.NexusAPIException;
import org.jreleaser.sdk.nexus2.api.PromoteRequest;
import org.jreleaser.sdk.nexus2.api.StagedRepository;
import org.jreleaser.sdk.nexus2.api.StagingProfile;
import org.jreleaser.sdk.nexus2.api.StagingProfileRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;
import static org.jreleaser.util.StringUtils.uncapitalize;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public class Nexus2 {
    private final JReleaserLogger logger;
    private final NexusAPI api;
    private final boolean dryrun;
    private final String apiHost;
    private final String username;
    private final String password;
    private final int connectTimeout;
    private final int readTimeout;
    private final Retrier retrier;

    public Nexus2(JReleaserLogger logger,
                  String apiHost,
                  String username,
                  String password,
                  int connectTimeout,
                  int readTimeout,
                  boolean dryrun,
                  int transitionDelay,
                  int transitionMaxRetries) {
        requireNonNull(logger, "'logger' must not be blank");
        requireNonBlank(apiHost, "'apiHost' must not be blank");
        requireNonBlank(username, "'username' must not be blank");
        requireNonBlank(password, "'password' must not be blank");

        this.logger = logger;
        this.dryrun = dryrun;
        this.apiHost = apiHost;
        this.username = username;
        this.password = password;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.retrier = new Retrier(logger, transitionDelay, transitionMaxRetries);
        this.api = Feign.builder()
            .encoder(new JacksonEncoder())
            .decoder(new ContentNegotiationDecoder())
            .requestInterceptor(new BasicAuthRequestInterceptor(username, password))
            .requestInterceptor(template -> template.header("User-Agent", "JReleaser/" + JReleaserVersion.getPlainVersion()))
            .errorDecoder(new NexusErrorDecoder(logger))
            .options(new Request.Options(connectTimeout, TimeUnit.SECONDS, readTimeout, TimeUnit.SECONDS, true))
            .target(NexusAPI.class, apiHost);
    }

    public String findStagingProfileId(String groupId) throws Nexus2Exception {
        return wrapNoDryrun(() -> {
            Data<List<StagingProfile>> data = api.getStagingProfiles();
            if (null == data || null == data.getData() || data.getData().isEmpty()) {
                throw fail(RB.$("ERROR_nexus_find_staging_profile", groupId));
            }

            return data.getData().stream()
                .filter(profile -> groupId.startsWith(profile.getName()) &&
                    (groupId.length() == profile.getName().length() ||
                        groupId.charAt(profile.getName().length()) == '.'))
                .max(Comparator.comparingInt(profile -> profile.getName().length()))
                .map(StagingProfile::getId)
                .orElseThrow(() -> fail(RB.$("ERROR_nexus_find_staging_profile", groupId)));
        });
    }

    public String createStagingRepository(String profileId, String groupId) throws Nexus2Exception {
        logger.debug(RB.$("nexus.create.staging.repository2", groupId, profileId));
        return wrap(() -> {
            Data<StagedRepository> data = api.startStagingRepository(
                new Data<>(PromoteRequest.ofDescription("Staging repository for " + groupId)),
                profileId);
            if (null == data || null == data.getData()) {
                throw fail(RB.$("ERROR_nexus_create_staging_repository", groupId));
            }

            return data.getData().getStagedRepositoryId();
        });
    }

    public void dropStagingRepository(String profileId, String stagingRepositoryId, String groupId) throws Nexus2Exception {
        logger.debug(uncapitalize(RB.$("nexus.drop.repository", stagingRepositoryId)));
        wrap(() -> {
            api.dropStagingRepository(
                new Data<>(PromoteRequest.of(stagingRepositoryId, "Staging repository for " + groupId)),
                profileId);
            waitForState(stagingRepositoryId, StagingProfileRepository.State.NOT_FOUND);
        });
    }

    public void releaseStagingRepository(String profileId, String stagingRepositoryId, String groupId) throws Nexus2Exception {
        wrap(() -> {
            api.releaseStagingRepository(
                new Data<>(PromoteRequest.of(stagingRepositoryId, "Staging repository for " + groupId)),
                profileId);
            waitForState(stagingRepositoryId, StagingProfileRepository.State.RELEASED, StagingProfileRepository.State.NOT_FOUND);
        });
    }

    public void closeStagingRepository(String profileId, String stagingRepositoryId, String groupId) throws Nexus2Exception {
        wrap(() -> {
            api.closeStagingRepository(
                new Data<>(PromoteRequest.of(stagingRepositoryId, "Staging repository for " + groupId)),
                profileId);
            waitForState(stagingRepositoryId, StagingProfileRepository.State.CLOSED);
        });
    }

    private void waitForState(String stagingRepositoryId, StagingProfileRepository.State... states) {
        logger.debug(RB.$("nexus.wait.repository.state", stagingRepositoryId, Arrays.asList(states)));

        StagingProfileRepository repository = retrier.retry(StagingProfileRepository::isTransitioning,
            () -> getStagingRepository(stagingRepositoryId));

        if (repository.isTransitioning()) {
            throw new IllegalStateException(RB.$("nexus.wait.repository.transitioning", stagingRepositoryId));
        }

        if (Arrays.binarySearch(states, repository.getState()) < 0) {
            throw new IllegalStateException(RB.$("nexus.wait.repository.invalid.state", stagingRepositoryId, Arrays.asList(states), repository.getState()));
        }
    }

    private StagingProfileRepository getStagingRepository(String stagingRepositoryId) {
        logger.debug(RB.$("nexus.get.staging.repository", stagingRepositoryId));

        try {
            return api.getStagingRepository(stagingRepositoryId);
        } catch (NexusAPIException apiException) {
            if (apiException.isNotFound()) {
                return StagingProfileRepository.notFound(stagingRepositoryId);
            } else {
                throw apiException;
            }
        }
    }

    public void deploy(String stagingRepositoryId, String path, Path file) throws Nexus2Exception {
        String filename = file.getFileName().toString();
        logger.debug(" - " + RB.$("nexus.deploy.artifact", filename, path, filename));

        try {
            FormData data = ClientUtils.toFormData(file);

            Map<String, String> headers = new LinkedHashMap<>();

            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(UTF_8));
            auth = new String(encodedAuth, UTF_8);
            headers.put("Authorization", "Basic " + auth);

            StringBuilder url = new StringBuilder(apiHost);
            if (isNotBlank(stagingRepositoryId)) {
                url.append("/staging/deployByRepositoryId/")
                    .append(stagingRepositoryId);
            }
            url.append(path)
                .append("/")
                .append(filename);

            ClientUtils.putFile(logger,
                url.toString(),
                connectTimeout,
                readTimeout,
                data,
                headers);
        } catch (UploadException | IOException e) {
            logger.error(" x {}", filename, e);
            throw fail(RB.$("ERROR_nexus_deploy_artifact", filename), e);
        }
    }

    private Nexus2Exception fail(String message) {
        return new Nexus2Exception(message);
    }

    private Nexus2Exception fail(String message, Exception e) {
        return new Nexus2Exception(message, e);
    }

    private void wrap(NexusOperation operation) throws Nexus2Exception {
        try {
            if (!dryrun) operation.execute();
        } catch (Nexus2Exception e) {
            logger.trace(e);
            throw e;
        } catch (RuntimeException e) {
            logger.trace(e);
            throw new Nexus2Exception(RB.$("ERROR_unexpected_error"), e);
        }
    }

    private <T> T wrap(Callable<T> callable) throws Nexus2Exception {
        try {
            if (!dryrun) {
                return callable.call();
            }
            return null;
        } catch (Nexus2Exception e) {
            logger.trace(e);
            throw e;
        } catch (Exception e) {
            logger.trace(e);
            throw new Nexus2Exception(RB.$("ERROR_unexpected_error"), e);
        }
    }

    private <T> T wrapNoDryrun(Callable<T> callable) throws Nexus2Exception {
        try {
            return callable.call();
        } catch (Nexus2Exception e) {
            logger.trace(e);
            throw e;
        } catch (Exception e) {
            logger.trace(e);
            throw new Nexus2Exception(RB.$("ERROR_unexpected_error"), e);
        }
    }

    interface NexusOperation {
        void execute() throws Nexus2Exception;
    }

    public static class Retrier {
        private final JReleaserLogger logger;
        private final int delay;
        private final int maxRetries;

        public Retrier(JReleaserLogger logger, int delay, int maxRetries) {
            this.logger = logger;
            this.delay = delay;
            this.maxRetries = maxRetries;
        }

        public <R> R retry(CheckedPredicate<R> stopFunction, CheckedSupplier<R> retriableOperation) {
            final int maxAttempts = maxRetries + 1;

            RetryPolicy<R> policy = RetryPolicy.<R>builder()
                .handle(IllegalStateException.class, NexusAPIException.class)
                .handleResultIf(stopFunction)
                .withDelay(Duration.ofSeconds(delay))
                .withMaxRetries(maxRetries)
                .onFailedAttempt(event -> {
                    logger.info(RB.$("nexus.retry.attempt"), event.getAttemptCount(), maxAttempts);
                    logger.debug(RB.$("nexus.retry.failed.attempt"), event.getAttemptCount(), maxAttempts, event.getLastResult());
                }).build();

            return Failsafe.with(policy).get(retriableOperation);
        }
    }

    static class ContentNegotiationDecoder implements Decoder {
        private final XmlDecoder xml = new XmlDecoder();
        private final JacksonDecoder json = new JacksonDecoder();

        @Override
        public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
            try {
                return xml.decode(response, type);
            } catch (NotXml e) {
                return json.decode(response, type);
            }
        }
    }

    static class XmlDecoder implements Decoder {
        private final XmlMapper mapper;

        public XmlDecoder() {
            this(new XmlMapper());
        }

        public XmlDecoder(XmlMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public Object decode(Response response, Type type) throws IOException {
            if (null == response.body()) {
                throw new NotXml();
            }

            Collection<String> value = response.headers().get("Content-Type");
            if (null == value || value.size() != 1) {
                throw new NotXml();
            }

            String contentType = value.iterator().next();
            if (!contentType.contains("application/xml")) {
                throw new NotXml();
            }

            Reader reader = response.body().asReader(Util.UTF_8);
            if (!reader.markSupported()) {
                reader = new BufferedReader(reader, 1);
            }
            try {
                // Read the first byte to see if we have any data
                reader.mark(1);
                if (reader.read() == -1) {
                    return null; // Eagerly returning null avoids "No content to map due to end-of-input"
                }
                reader.reset();
                return mapper.readValue(reader, mapper.constructType(type));
            } catch (RuntimeJsonMappingException e) {
                if (e.getCause() instanceof IOException) {
                    throw (IOException) e.getCause();
                }
                throw e;
            }
        }
    }

    static class NotXml extends IOException {
        private static final long serialVersionUID = -6458245950020411953L;
    }

    static class NexusErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultErrorDecoder = new Default();
        private final JReleaserLogger logger;

        public NexusErrorDecoder(JReleaserLogger logger) {
            this.logger = logger;
        }

        @Override
        public Exception decode(String methodKey, Response response) {
            Exception exception = defaultErrorDecoder.decode(methodKey, response);

            if (exception instanceof RetryableException) {
                return exception;
            }

            if (response.status() >= 500) {
                logger.trace(response.request().httpMethod() + " " + response.request().url());
                logger.trace(response.status() + " " + response.reason());
                if (null != response.body() && response.body().length() > 0) {
                    try (Reader reader = new InputStreamReader(response.body().asInputStream(), UTF_8)) {
                        logger.trace(IOUtils.toString(reader));
                    } catch (IOException e) {
                        logger.trace(e);
                    }
                }

                return new RetryableException(
                    response.status(),
                    response.reason(),
                    response.request().httpMethod(),
                    null,
                    response.request());
            }

            return new NexusAPIException(response.status(), response.reason(), response.headers());
        }
    }
}
