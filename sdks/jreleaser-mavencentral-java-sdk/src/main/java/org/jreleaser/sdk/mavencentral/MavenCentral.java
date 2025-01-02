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
package org.jreleaser.sdk.mavencentral;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import dev.failsafe.function.CheckedPredicate;
import dev.failsafe.function.CheckedSupplier;
import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import feign.form.FormData;
import feign.jackson.JacksonDecoder;
import org.apache.commons.io.IOUtils;
import org.jreleaser.bundle.RB;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.api.JReleaserContext;
import org.jreleaser.model.spi.deploy.maven.Deployable;
import org.jreleaser.mustache.Templates;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.commons.feign.TokenAuthRequestInterceptor;
import org.jreleaser.sdk.mavencentral.api.Deployment;
import org.jreleaser.sdk.mavencentral.api.MavenCentralAPI;
import org.jreleaser.sdk.mavencentral.api.MavenCentralAPIException;
import org.jreleaser.sdk.mavencentral.api.State;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

import static java.lang.System.lineSeparator;
import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.IoUtils.newInputStreamReader;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 1.12.0
 */
public class MavenCentral {
    private final JReleaserContext context;
    private final MavenCentralAPI api;
    private final boolean dryrun;
    private final int connectTimeout;
    private final int readTimeout;
    private final Retrier retrier;

    public MavenCentral(JReleaserContext context,
                        String apiHost,
                        String username,
                        String password,
                        int connectTimeout,
                        int readTimeout,
                        boolean dryrun,
                        int retryDelay,
                        int maxRetries) {
        requireNonNull(context, "'context' must not be blank");
        requireNonBlank(apiHost, "'apiHost' must not be blank");
        requireNonBlank(username, "'username' must not be blank");
        requireNonBlank(password, "'password' must not be blank");

        this.context = context;
        this.dryrun = dryrun;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.retrier = new Retrier(context.getLogger(), retryDelay, maxRetries);
        this.api = ClientUtils.builder(context, connectTimeout, readTimeout)
            .decoder(new MavenCentralDecoder())
            .requestInterceptor(new TokenAuthRequestInterceptor("Bearer", username, password))
            .errorDecoder(new MavenCentralErrorDecoder(context.getLogger()))
            .target(MavenCentralAPI.class, apiHost);
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

    public Optional<Deployment> status(String deploymentId) throws MavenCentralException {
        return wrap(() -> {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("id", deploymentId);
            return Optional.ofNullable(api.status(params));
        });
    }

    public void publish(String deploymentId) throws MavenCentralException {
        wrap(() -> {
            api.publish(deploymentId);
            waitForState(deploymentId, State.PUBLISHED, State.FAILED);
        });
    }

    public String upload(Path bundle) throws MavenCentralException {
        return wrap(() -> {
            FormData formData = ClientUtils.toFormData(bundle);
            String deploymentId = api.upload(formData);
            waitForState(deploymentId, State.VALIDATED, State.FAILED);
            return deploymentId;
        });
    }

    private void wrap(MavenCentralOperation operation) throws MavenCentralException {
        try {
            if (!dryrun) operation.execute();
        } catch (MavenCentralException e) {
            context.getLogger().trace(e);
            throw e;
        } catch (RuntimeException e) {
            context.getLogger().trace(e);
            throw new MavenCentralException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    private <T> T wrap(Callable<T> callable) throws MavenCentralException {
        try {
            if (!dryrun) {
                return callable.call();
            }
            return null;
        } catch (MavenCentralException e) {
            context.getLogger().trace(e);
            throw e;
        } catch (Exception e) {
            context.getLogger().trace(e);
            throw new MavenCentralException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    private void waitForState(String deploymentId, State... states) throws MavenCentralException {
        context.getLogger().debug(RB.$("maven.central.wait.deployment.state", deploymentId, Arrays.asList(states)));

        Optional<Deployment> deployment = retrier.retry(o -> o.map(Deployment::isTransitioning).orElse(false),
            () -> status(deploymentId));

        if (deployment.isPresent()) {
            if (deployment.get().isTransitioning()) {
                throw new IllegalStateException(RB.$("maven.central.wait.deployment.transitioning", deploymentId));
            }

            if (Arrays.binarySearch(states, deployment.get().getDeploymentState()) < 0) {
                Set<String> messages = resolveErrorMessages(deployment.get());
                String title = RB.$("maven.central.wait.deployment.invalid.state", deploymentId, Arrays.asList(states), deployment.get().getDeploymentState());
                if (!messages.isEmpty()) {
                    throw new MavenCentralException(title + lineSeparator() + String.join(lineSeparator(), messages));
                } else {
                    throw new MavenCentralException(title);
                }
            }
        }
    }

    private Set<String> resolveErrorMessages(Deployment deployment) {
        Set<String> messages = new LinkedHashSet<>();

        for (Map.Entry<String, List<String>> e : deployment.getErrors().entrySet()) {
            for (String error : e.getValue()) {
                messages.add(e.getKey() + " " + error);
            }
        }

        return messages;
    }

    interface MavenCentralOperation {
        void execute() throws MavenCentralException;
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
                .handle(IllegalStateException.class)
                .handleIf(exception -> {
                    if (exception instanceof MavenCentralAPIException) {
                        MavenCentralAPIException mavenCentralException = (MavenCentralAPIException) exception;
                        return !mavenCentralException.isUnauthorized();
                    }

                    return false;
                })
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

    static class MavenCentralErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultErrorDecoder = new Default();
        private final JReleaserLogger logger;

        public MavenCentralErrorDecoder(JReleaserLogger logger) {
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
                if (null != response.body() && null != response.body().length() && response.body().length() > 0) {
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

            return new MavenCentralAPIException(response.status(), response.reason(), response.headers());
        }
    }

    static class MavenCentralDecoder implements Decoder {
        private final JacksonDecoder json = new JacksonDecoder(new ObjectMapper()
            .registerModule(new JavaTimeModule()));

        @Override
        public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
            if (response.request().url().endsWith("/upload")) {
                try (Reader reader = newInputStreamReader(response.body().asInputStream())) {
                    return IOUtils.toString(reader);
                }
            }
            return json.decode(response, type);
        }
    }
}
