package org.jreleaser.sdk.mavencentral;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.sdk.mavencentral.api.MavenCentralAPIException;
import org.junit.jupiter.api.Test;

import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import feign.RetryableException;

class MavenCentralErrorDecoderTest {
    private final JReleaserLogger logger = new JReleaserLogger() {
        @Override
        public java.io.PrintWriter getTracer() {
            return null;
        }

        @Override
        public void close() {
        }

        @Override
        public void reset() {
        }

        @Override
        public void increaseIndent() {
        }

        @Override
        public void decreaseIndent() {
        }

        @Override
        public void setPrefix(String prefix) {
        }

        @Override
        public void restorePrefix() {
        }

        @Override
        public void plain(String message) {
        }

        @Override
        public void debug(String message) {
        }

        @Override
        public void info(String message) {
        }

        @Override
        public void warn(String message) {
        }

        @Override
        public void error(String message) {
        }

        @Override
        public void trace(String message) {
        }

        @Override
        public void plain(String message, Object... args) {
        }

        @Override
        public void debug(String message, Object... args) {
        }

        @Override
        public void info(String message, Object... args) {
        }

        @Override
        public void warn(String message, Object... args) {
        }

        @Override
        public void error(String message, Object... args) {
        }

        @Override
        public void plain(String message, Throwable throwable) {
        }

        @Override
        public void debug(String message, Throwable throwable) {
        }

        @Override
        public void info(String message, Throwable throwable) {
        }

        @Override
        public void warn(String message, Throwable throwable) {
        }

        @Override
        public void error(String message, Throwable throwable) {
        }

        @Override
        public void trace(String message, Throwable throwable) {
        }

        @Override
        public void trace(Throwable throwable) {
        }
    };

    private Response buildResponse(int status, String reason) {
        Request request = Request.create(Request.HttpMethod.GET, "http://localhost", Collections.emptyMap(), null,
                new RequestTemplate());
        return Response.builder()
                .status(status)
                .reason(reason)
                .request(request)
                .headers(Collections.emptyMap())
                .build();
    }

    @Test
    void returnsUnauthorizedExceptionFor401() {
        MavenCentral.MavenCentralErrorDecoder decoder = new MavenCentral.MavenCentralErrorDecoder(logger);
        Response response = buildResponse(401, "Unauthorized");
        Exception ex = decoder.decode("method", response);
        assertThat(ex).isInstanceOf(MavenCentralAPIException.class);
        assertThat(((MavenCentralAPIException) ex).getStatus()).isEqualTo(401);
        assertThat(ex.getMessage()).contains("Unauthorized");
    }

    @Test
    void returnsForbiddenExceptionFor403() {
        MavenCentral.MavenCentralErrorDecoder decoder = new MavenCentral.MavenCentralErrorDecoder(logger);
        Response response = buildResponse(403, "Forbidden");
        Exception ex = decoder.decode("method", response);
        assertThat(ex).isInstanceOf(MavenCentralAPIException.class);
        assertThat(((MavenCentralAPIException) ex).getStatus()).isEqualTo(403);
        assertThat(ex.getMessage()).contains("Forbidden");
    }

    @Test
    void returnsRetryableExceptionFor500() {
        MavenCentral.MavenCentralErrorDecoder decoder = new MavenCentral.MavenCentralErrorDecoder(logger);
        Response response = buildResponse(500, "Internal Server Error");
        Exception ex = decoder.decode("method", response);
        assertThat(ex).isInstanceOf(RetryableException.class);
    }

    @Test
    void returnsDefaultExceptionForOtherStatus() {
        MavenCentral.MavenCentralErrorDecoder decoder = new MavenCentral.MavenCentralErrorDecoder(logger);
        Response response = buildResponse(404, "Not Found");
        Exception ex = decoder.decode("method", response);
        assertThat(ex).isInstanceOf(MavenCentralAPIException.class);
        assertThat(((MavenCentralAPIException) ex).getStatus()).isEqualTo(404);
        assertThat(ex.getMessage()).contains("Not Found");
    }
}
