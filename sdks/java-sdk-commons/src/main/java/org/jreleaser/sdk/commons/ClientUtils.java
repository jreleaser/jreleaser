/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
package org.jreleaser.sdk.commons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Client;
import feign.Feign;
import feign.Request;
import feign.form.FormData;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserModelPrinter;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.model.uploader.spi.UploadException;
import org.jreleaser.util.Constants;
import org.jreleaser.util.JReleaserLogger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public final class ClientUtils {
    private static final Tika TIKA = new Tika();

    private ClientUtils() {
        // noop
    }

    public static FormData toFormData(Path asset) throws IOException {
        return FormData.builder()
            .fileName(asset.getFileName().toString())
            .contentType(MediaType.parse(TIKA.detect(asset)).toString())
            .data(Files.readAllBytes(asset))
            .build();
    }

    public static Feign.Builder builder(JReleaserLogger logger,
                                        int connectTimeout,
                                        int readTimeout) {
        requireNonNull(logger, "'logger' must not be null");

        Feign.Builder builder = Feign.builder();

        if (Boolean.getBoolean("jreleaser.disableSslValidation")) {
            logger.warn(RB.$("warn_ssl_disabled"));
            builder = builder.client(
                new Client.Default(nonValidatingSSLSocketFactory(),
                    new NonValidatingHostnameVerifier()));
        }

        return builder
            .encoder(new FormEncoder(new JacksonEncoder()))
            .decoder(new JacksonDecoder())
            .requestInterceptor(template -> template.header("User-Agent", "JReleaser/" + JReleaserVersion.getPlainVersion()))
            .errorDecoder((methodKey, response) -> new RestAPIException(response.request(), response.status(), response.reason(), response.headers()))
            .options(new Request.Options(connectTimeout, TimeUnit.SECONDS, readTimeout, TimeUnit.SECONDS, true));
    }

    public static void webhook(JReleaserLogger logger,
                               String webhookUrl,
                               int connectTimeout,
                               int readTimeout,
                               Object message) throws AnnounceException {
        if (message instanceof String) {
            webhook(logger, webhookUrl, connectTimeout, readTimeout, (String) message);
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            webhook(logger, webhookUrl, connectTimeout, readTimeout, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            throw new AnnounceException(e);
        }
    }

    public static void webhook(JReleaserLogger logger,
                               String webhookUrl,
                               int connectTimeout,
                               int readTimeout,
                               String message) throws AnnounceException {
        try {
            // create URL
            URL url = new URL(webhookUrl);
            // open connection
            logger.debug(RB.$("webhook.connection.open"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // set options
            logger.debug(RB.$("webhook.connection.configure"));
            connection.setConnectTimeout(connectTimeout * 1000);
            connection.setReadTimeout(readTimeout * 1000);
            connection.setAllowUserInteraction(false);
            connection.setInstanceFollowRedirects(true);

            connection.setRequestMethod("POST");
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("Accept", "application/json");
            connection.addRequestProperty("User-Agent", "JReleaser/" + JReleaserVersion.getPlainVersion());
            connection.setDoOutput(true);

            // write message
            logger.debug(RB.$("webhook.message.send"));
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = message.getBytes(UTF_8);
                os.write(input, 0, input.length);
            }

            // handle response
            logger.debug(RB.$("webhook.response.handle"));
            int status = connection.getResponseCode();
            if (status >= 400) {
                String reason = connection.getResponseMessage();
                Reader reader = new InputStreamReader(connection.getErrorStream(), UTF_8);
                message = IOUtils.toString(reader);
                StringBuilder b = new StringBuilder("Webhook replied with: ")
                    .append(status);
                if (isNotBlank(reason)) {
                    b.append(" reason: ")
                        .append(reason)
                        .append(",");
                }
                if (isNotBlank(message)) {
                    b.append(message);
                }
                throw new AnnounceException(b.toString());
            }
        } catch (IOException e) {
            logger.trace(e);
            throw new AnnounceException(e);
        }
    }

    public static void postFile(JReleaserLogger logger,
                                String url,
                                int connectTimeout,
                                int readTimeout,
                                FormData data,
                                Map<String, String> headers) throws UploadException {
        headers.put("METHOD", "POST");
        uploadFile(logger, url, connectTimeout, readTimeout, data, headers);
    }

    public static void putFile(JReleaserLogger logger,
                               String url,
                               int connectTimeout,
                               int readTimeout,
                               FormData data,
                               Map<String, String> headers) throws UploadException {
        headers.put("METHOD", "PUT");
        headers.put("Expect", "100-continue");
        uploadFile(logger, url, connectTimeout, readTimeout, data, headers);
    }

    private static void uploadFile(JReleaserLogger logger,
                                   String url,
                                   int connectTimeout,
                                   int readTimeout,
                                   FormData data,
                                   Map<String, String> headers) throws UploadException {
        try {
            // create URL
            URL theUrl = new URL(url);
            logger.debug("url: {}", theUrl);

            // open connection
            logger.debug(RB.$("webhook.connection.open"));
            HttpURLConnection connection = (HttpURLConnection) theUrl.openConnection();
            // set options
            logger.debug(RB.$("webhook.connection.configure"));
            connection.setConnectTimeout(connectTimeout * 1000);
            connection.setReadTimeout(readTimeout * 1000);
            connection.setAllowUserInteraction(false);
            connection.setInstanceFollowRedirects(true);

            connection.setRequestMethod(headers.remove("METHOD"));
            connection.addRequestProperty("Accept", "*/*");
            connection.addRequestProperty("User-Agent", "JReleaser/" + JReleaserVersion.getPlainVersion());
            connection.addRequestProperty("Content-Length", data.getData().length + "");
            connection.setRequestProperty("Content-Type", data.getContentType());
            headers.forEach(connection::setRequestProperty);

            connection.getRequestProperties().forEach((k, v) -> {
                if (JReleaserModelPrinter.isSecret(k)) {
                    logger.debug("{}: {}", k, Constants.HIDE);
                } else {
                    logger.debug("{}: {}", k, v);
                }
            });

            connection.setDoOutput(true);

            // write message
            logger.debug(RB.$("webhook.data.send"));
            try (OutputStream os = connection.getOutputStream()) {
                os.write(data.getData(), 0, data.getData().length);
                os.flush();
            }

            // handle response
            logger.debug(RB.$("webhook.response.handle"));
            int status = connection.getResponseCode();
            if (status >= 400) {
                String reason = connection.getResponseMessage();
                Reader reader = new InputStreamReader(connection.getErrorStream(), UTF_8);
                String message = IOUtils.toString(reader);
                StringBuilder b = new StringBuilder("Got ")
                    .append(status);
                if (isNotBlank(reason)) {
                    b.append(" reason: ")
                        .append(reason)
                        .append(",");
                }
                if (isNotBlank(message)) {
                    b.append(message);
                }
                throw new UploadException(b.toString());
            }
        } catch (IOException e) {
            logger.trace(e);
            throw new UploadException(e);
        }
    }

    private static SSLSocketFactory nonValidatingSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{new NonValidatingTrustManager()}, null);
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static class NonValidatingTrustManager implements X509TrustManager {
        private static final X509Certificate[] EMPTY_CERTIFICATES = new X509Certificate[0];

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
            // noop
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
            // noop
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return EMPTY_CERTIFICATES;
        }
    }

    private static class NonValidatingHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
