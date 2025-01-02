/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 The JReleaser authors.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import dev.failsafe.function.CheckedPredicate;
import dev.failsafe.function.CheckedSupplier;
import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import feign.Util;
import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import feign.form.FormData;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.apache.commons.io.IOUtils;
import org.jreleaser.bundle.RB;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.api.JReleaserContext;
import org.jreleaser.model.spi.deploy.maven.Deployable;
import org.jreleaser.model.spi.upload.UploadException;
import org.jreleaser.mustache.Templates;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.nexus2.api.Data;
import org.jreleaser.sdk.nexus2.api.NexusAPI;
import org.jreleaser.sdk.nexus2.api.NexusAPIException;
import org.jreleaser.sdk.nexus2.api.PromoteRequest;
import org.jreleaser.sdk.nexus2.api.StagedRepository;
import org.jreleaser.sdk.nexus2.api.StagingActivity;
import org.jreleaser.sdk.nexus2.api.StagingProfile;
import org.jreleaser.sdk.nexus2.api.StagingProfileRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.util.IoUtils.newInputStreamReader;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;
import static org.jreleaser.util.StringUtils.uncapitalize;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public class Nexus2 {
    private final JReleaserContext context;
    private final NexusAPI api;
    private final boolean dryrun;
    private final String apiHost;
    private final String username;
    private final String password;
    private final int connectTimeout;
    private final int readTimeout;
    private final Retrier retrier;

    public Nexus2(JReleaserContext context,
                  String apiHost,
                  String username,
                  String password,
                  int connectTimeout,
                  int readTimeout,
                  boolean dryrun,
                  int transitionDelay,
                  int transitionMaxRetries) {
        this.context = requireNonNull(context, "'context' must not be blank");
        this.apiHost = requireNonBlank(apiHost, "'apiHost' must not be blank").trim();
        this.username = requireNonBlank(username, "'username' must not be blank").trim();
        this.password = requireNonBlank(password, "'password' must not be blank").trim();

        this.dryrun = dryrun;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.retrier = new Retrier(context.getLogger(), transitionDelay, transitionMaxRetries);
        this.api = ClientUtils.builder(context, connectTimeout, readTimeout)
            .encoder(new JacksonEncoder())
            .decoder(new ContentNegotiationDecoder())
            .requestInterceptor(new BasicAuthRequestInterceptor(username, password))
            .errorDecoder(new NexusErrorDecoder(context.getLogger()))
            .target(NexusAPI.class, apiHost);
    }

    public List<StagingProfileRepository> findStagingProfileRepositories(String profileId, String groupId) throws Nexus2Exception {
        return wrap(() -> {
            Data<List<StagingProfileRepository>> data = api.getStagingProfileRepositories(profileId);
            if (null == data || null == data.getData()) {
                throw fail(RB.$("ERROR_nexus_find_staging_repository", groupId));
            }

            return data.getData().stream()
                .filter(r -> r.getProfileName().equals(groupId))
                .sorted(comparing(StagingProfileRepository::getUpdated).reversed())
                .collect(toList());
        }, emptyList());
    }

    public List<StagingProfile> findStagingProfiles(String groupId) throws Nexus2Exception {
        return wrap(() -> {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("t", "maven2");
            params.put("g", groupId);
            params.put("a", "whatever");
            params.put("v", "whatever");
            Data<List<StagingProfile>> data = api.evalStagingProfile(params);
            if (null == data || null == data.getData()) {
                throw fail(RB.$("ERROR_nexus_find_staging_repository", groupId));
            }

            return data.getData().stream()
                .sorted(comparing(StagingProfile::getName).reversed())
                .collect(toList());
        }, emptyList());
    }

    public String createStagingRepository(String profileId, String groupId) throws Nexus2Exception {
        context.getLogger().debug(RB.$("nexus.create.staging.repository2", groupId, profileId));
        return wrap(() -> {
            Data<StagedRepository> data = api.startStagingRepository(
                new Data<>(PromoteRequest.ofDescription("Staging repository for " + groupId)),
                profileId);
            if (null == data || null == data.getData()) {
                throw fail(RB.$("ERROR_nexus_create_staging_repository", groupId));
            }

            return data.getData().getStagedRepositoryId();
        }, null);
    }

    public void dropStagingRepository(String profileId, String stagingRepositoryId, String groupId) throws Nexus2Exception {
        context.getLogger().debug(uncapitalize(RB.$("nexus.drop.repository", stagingRepositoryId)));
        wrap(() -> {
            api.dropStagingRepository(
                new Data<>(PromoteRequest.of(stagingRepositoryId, "Staging repository for " + groupId)),
                profileId);
            waitForState(stagingRepositoryId, "drop", StagingProfileRepository.State.NOT_FOUND);
        });
    }

    public void releaseStagingRepository(String profileId, String stagingRepositoryId, String groupId) throws Nexus2Exception {
        wrap(() -> {
            api.releaseStagingRepository(
                new Data<>(PromoteRequest.of(stagingRepositoryId, "Staging repository for " + groupId)),
                profileId);
            waitForState(stagingRepositoryId, "release", StagingProfileRepository.State.RELEASED, StagingProfileRepository.State.NOT_FOUND);
        });
    }

    public void closeStagingRepository(String profileId, String stagingRepositoryId, String groupId) throws Nexus2Exception {
        wrap(() -> {
            api.closeStagingRepository(
                new Data<>(PromoteRequest.of(stagingRepositoryId, "Staging repository for " + groupId)),
                profileId);
            waitForState(stagingRepositoryId, "close", StagingProfileRepository.State.CLOSED);
        });
    }

    private void waitForState(String stagingRepositoryId, String activity, StagingProfileRepository.State... states) throws Nexus2Exception {
        context.getLogger().debug(RB.$("nexus.wait.repository.state", stagingRepositoryId, Arrays.asList(states)));

        StagingProfileRepository repository = retrier.retry(StagingProfileRepository::isTransitioning,
            () -> getStagingRepository(stagingRepositoryId));

        if (repository.isTransitioning()) {
            throw new IllegalStateException(RB.$("nexus.wait.repository.transitioning", stagingRepositoryId));
        }

        if (Arrays.binarySearch(states, repository.getState()) < 0) {
            Set<String> messages = resolveActivityMessages(stagingRepositoryId, activity);
            String title = RB.$("nexus.wait.repository.invalid.state", stagingRepositoryId, Arrays.asList(states), repository.getState());
            throw new Nexus2Exception(title + lineSeparator() + String.join(lineSeparator(), messages));
        }
    }

    private Set<String> resolveActivityMessages(String stagingRepositoryId, String activityName) throws Nexus2Exception {
        List<StagingActivity> data = api.getActivities(stagingRepositoryId);
        if (null == data || data.isEmpty()) {
            throw fail(RB.$("ERROR_nexus_find_staging_activities", stagingRepositoryId));
        }

        Optional<StagingActivity> activity = data.stream()
            .filter(a -> activityName.equals(a.getName()))
            .findFirst();

        if (!activity.isPresent()) {
            throw fail(RB.$("ERROR_nexus_find_staging_activity", activityName, stagingRepositoryId));
        }

        Set<String> messages = new LinkedHashSet<>();
        for (StagingActivity.StagingActivityEvent event : activity.get().getEvents()) {
            if (event.getName().endsWith("Failed")) {
                for (StagingActivity.StagingProperty property : event.getProperties()) {
                    if ("failureMessage".equals(property.getName()) ||
                        "cause".equals(property.getName())) {
                        messages.add(property.getValue());
                    }
                }
            }
        }

        return messages;
    }

    private StagingProfileRepository getStagingRepository(String stagingRepositoryId) {
        context.getLogger().debug(RB.$("nexus.get.staging.repository", stagingRepositoryId));

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

    public boolean artifactExists(Deployable deployable, String verifyUrl) {
        if (isNotBlank(verifyUrl)) {
            verifyUrl = Templates.resolveTemplate(verifyUrl, deployable.props());
            if (ClientUtils.head(context.getLogger(), verifyUrl, connectTimeout, readTimeout)) {
                context.getLogger().warn(" ! " + RB.$("nexus.deploy.artifact.exists",
                    deployable.getDeployPath(),
                    deployable.getLocalPath().getFileName().toString()));
                return true;
            }
        }

        return false;
    }

    public void deploy(String stagingRepositoryId, String path, Path file) throws Nexus2Exception {
        String filename = file.getFileName().toString();
        context.getLogger().debug(" - " + RB.$("nexus.deploy.artifact", filename, path, filename));

        try {
            FormData data = ClientUtils.toFormData(file);

            Map<String, String> headers = new LinkedHashMap<>();

            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(UTF_8));
            auth = new String(encodedAuth, UTF_8);
            headers.put("Authorization", "Basic " + auth);

            StringBuilder url = new StringBuilder(apiHost);
            if (!apiHost.endsWith("/")) {
                url.append("/");
            }

            if (isNotBlank(stagingRepositoryId)) {
                url.append("staging/deployByRepositoryId/")
                    .append(stagingRepositoryId)
                    .append("/");
            }

            url.append(path);

            if (!path.endsWith("/")) {
                url.append("/");
            }
            url.append(filename);

            ClientUtils.putFile(context.getLogger(),
                url.toString(),
                connectTimeout,
                readTimeout,
                data,
                headers);
        } catch (UploadException | IOException e) {
            context.getLogger().error(" x {}", filename, e);
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
            context.getLogger().trace(e);
            throw e;
        } catch (RuntimeException e) {
            context.getLogger().trace(e);
            throw new Nexus2Exception(RB.$("ERROR_unexpected_error"), e);
        }
    }

    private <T> T wrap(Callable<T> callable, T defaultValue) throws Nexus2Exception {
        try {
            if (!dryrun) {
                return callable.call();
            }
            return defaultValue;
        } catch (Nexus2Exception e) {
            context.getLogger().trace(e);
            throw e;
        } catch (Exception e) {
            context.getLogger().trace(e);
            throw new Nexus2Exception(RB.$("ERROR_unexpected_error"), e);
        }
    }

    private <T> T wrapNoDryrun(Callable<T> callable) throws Nexus2Exception {
        try {
            return callable.call();
        } catch (Nexus2Exception e) {
            context.getLogger().trace(e);
            throw e;
        } catch (Exception e) {
            context.getLogger().trace(e);
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
                    logger.debug(RB.$("nexus.retry.failed.attempt", event.getAttemptCount(), maxAttempts, event.getLastResult()), event.getLastException());
                }).build();

            return Failsafe.with(policy).get(retriableOperation);
        }
    }

    static class ContentNegotiationDecoder implements Decoder {
        private final XmlDecoder xml = new XmlDecoder((XmlMapper) new XmlMapper()
            .registerModule(new JavaTimeModule()));

        private final JacksonDecoder json = new JacksonDecoder(new ObjectMapper()
            .registerModule(new JavaTimeModule()));

        @Override
        public Object decode(Response response, Type type) throws IOException, FeignException {
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
                    try (Reader reader = newInputStreamReader(response.body().asInputStream())) {
                        logger.trace(IOUtils.toString(reader));
                    } catch (IOException e) {
                        logger.trace(e);
                    }
                }

                return new RetryableException(
                    response.status(),
                    response.reason(),
                    response.request().httpMethod(),
                    (Long) null,
                    response.request());
            }

            return new NexusAPIException(response.status(), response.reason(), response.headers());
        }
    }
}
