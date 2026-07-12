/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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

import dev.failsafe.FailsafeException;
import feign.Request;
import feign.RequestTemplate;
import feign.RetryableException;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.logging.SimpleJReleaserLoggerAdapter;
import org.jreleaser.sdk.mavencentral.api.MavenCentralAPIException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MavenCentralRetryTest {
    private final JReleaserLogger logger = new SimpleJReleaserLoggerAdapter();

    @Test
    void retrierRetriesWrappedTransientFailures() {
        MavenCentral.Retrier retrier = new MavenCentral.Retrier(logger, 1, 2);
        AtomicInteger attempts = new AtomicInteger();

        String result = retrier.retry(r -> false, () -> {
            if (attempts.incrementAndGet() == 1) {
                throw new MavenCentralException("transient failure", retryable503());
            }
            return "ok";
        });

        assertThat(result).isEqualTo("ok");
        assertThat(attempts.get()).isEqualTo(2);
    }

    @Test
    void retrierRetriesWrappedApiExceptions() {
        MavenCentral.Retrier retrier = new MavenCentral.Retrier(logger, 1, 2);
        AtomicInteger attempts = new AtomicInteger();

        String result = retrier.retry(r -> false, () -> {
            if (attempts.incrementAndGet() == 1) {
                throw new MavenCentralException("transient failure",
                    new MavenCentralAPIException(503, "Service Unavailable"));
            }
            return "ok";
        });

        assertThat(result).isEqualTo("ok");
        assertThat(attempts.get()).isEqualTo(2);
    }

    @Test
    void retrierDoesNotRetryUnauthorized() {
        MavenCentral.Retrier retrier = new MavenCentral.Retrier(logger, 1, 2);
        AtomicInteger attempts = new AtomicInteger();

        assertThatThrownBy(() -> retrier.retry(r -> false, () -> {
            attempts.incrementAndGet();
            throw new MavenCentralException("fatal failure",
                new MavenCentralAPIException(401, "Unauthorized"));
        })).isInstanceOf(FailsafeException.class)
            .hasRootCauseInstanceOf(MavenCentralAPIException.class);

        assertThat(attempts.get()).isEqualTo(1);
    }

    private RetryableException retryable503() {
        Request request = Request.create(Request.HttpMethod.HEAD, "http://localhost", Collections.emptyMap(), null,
            new RequestTemplate());
        return new RetryableException(503, "Service Unavailable", Request.HttpMethod.HEAD, (Long) null, request);
    }
}
