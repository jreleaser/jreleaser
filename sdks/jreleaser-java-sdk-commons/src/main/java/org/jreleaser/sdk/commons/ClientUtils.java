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
package org.jreleaser.sdk.commons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Client;
import feign.Feign;
import feign.RedirectionInterceptor;
import feign.Request;
import feign.Response;
import feign.form.FormData;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.jreleaser.bundle.RB;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.Constants;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.model.api.JReleaserContext;
import org.jreleaser.model.internal.JReleaserModelPrinter;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.upload.UploadException;
import org.jreleaser.sdk.commons.feign.FeignLogger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.IoUtils.newInputStreamReader;
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

    public static FormData toFormData(String fileName, String contentType, String content) {
        return toFormData(fileName, contentType, content.getBytes(UTF_8));
    }

    public static FormData toFormData(String fileName, String contentType, byte[] content) {
        return FormData.builder()
            .fileName(fileName)
            .contentType(contentType)
            .data(content)
            .build();
    }

    public static FormData toFormData(Path asset) throws IOException {
        return toFormData(asset.getFileName().toString(),
            MediaType.parse(TIKA.detect(asset)).toString(),
            Files.readAllBytes(asset));
    }

    public static Feign.Builder builder(JReleaserContext context,
                                        int connectTimeout,
                                        int readTimeout) {
        requireNonNull(context, "'logger' must not be null");

        Feign.Builder builder = Feign.builder();

        if (Boolean.getBoolean("jreleaser.disableSslValidation")) {
            context.getLogger().warn(RB.$("warn_ssl_disabled"));
            builder = builder.client(
                new Client.Default(nonValidatingSSLSocketFactory(),
                    new NonValidatingHostnameVerifier()));
        }

        return builder
            .logger(new FeignLogger(context.getLogger()))
            .logLevel(FeignLogger.resolveLevel(context))
            .encoder(new FormEncoder(new JacksonEncoder()))
            .decoder(new JacksonDecoder())
            .responseInterceptor(new RedirectionInterceptor())
            .requestInterceptor(template -> template.header("User-Agent", "JReleaser/" + JReleaserVersion.getPlainVersion()))
            .errorDecoder((methodKey, response) -> new RestAPIException(response.request(), response.status(), response.reason(), toString(context.getLogger(), response.body()), response.headers()))
            .options(new Request.Options(connectTimeout, TimeUnit.SECONDS, readTimeout, TimeUnit.SECONDS, true));
    }

    private static String toString(JReleaserLogger logger, Response.Body body) {
        if (null == body) return "";

        try (Reader reader = body.asReader(UTF_8)) {
            return IOUtils.toString(reader);
        } catch (IOException e) {
            logger.trace(e);
            return "";
        }
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
        post(logger, webhookUrl, connectTimeout, readTimeout, message, emptyMap());
    }

    public static void post(JReleaserLogger logger,
                            String theUrl,
                            int connectTimeout,
                            int readTimeout,
                            String message,
                            Map<String, String> headers) throws AnnounceException {
        try {
            // create URL
            URL url = new URI(theUrl).toURL();
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
            connection.addRequestProperty("User-Agent", "JReleaser/" + JReleaserVersion.getPlainVersion());
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("Accept", "application/json");
            headers.forEach(connection::addRequestProperty);
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
                StringBuilder b = new StringBuilder("Webhook replied with: ")
                    .append(status);
                if (isNotBlank(reason)) {
                    b.append(" reason: ")
                        .append(reason);
                }
                try (Reader reader = newInputStreamReader(connection.getErrorStream())) {
                    message = IOUtils.toString(reader);
                    if (isNotBlank(message)) {
                        b.append(",")
                            .append(message);
                    }
                }
                throw new AnnounceException(b.toString());
            }
        } catch (URISyntaxException | IOException e) {
            logger.trace(e);
            throw new AnnounceException(e);
        }
    }

    public static Reader postFile(JReleaserLogger logger,
                                  URI uri,
                                  int connectTimeout,
                                  int readTimeout,
                                  FormData data,
                                  Map<String, String> headers) throws UploadException {
        headers.put("METHOD", "POST");
        return uploadFile(logger, uri, connectTimeout, readTimeout, data, headers);
    }

    public static Reader postFile(JReleaserLogger logger,
                                  String url,
                                  int connectTimeout,
                                  int readTimeout,
                                  FormData data,
                                  Map<String, String> headers) throws UploadException {
        headers.put("METHOD", "POST");
        try {
            return uploadFile(logger, new URI(url), connectTimeout, readTimeout, data, headers);
        } catch (URISyntaxException e) {
            logger.trace(e);
            throw new UploadException(e);
        }
    }

    public static Reader putFile(JReleaserLogger logger,
                                 String url,
                                 int connectTimeout,
                                 int readTimeout,
                                 FormData data,
                                 Map<String, String> headers) throws UploadException {
        headers.put("METHOD", "PUT");
        headers.put("Expect", "100-continue");
        try {
            return uploadFile(logger, new URI(url), connectTimeout, readTimeout, data, headers);
        } catch (URISyntaxException e) {
            logger.trace(e);
            throw new UploadException(e);
        }
    }

    private static Reader uploadFile(JReleaserLogger logger,
                                     URI uri,
                                     int connectTimeout,
                                     int readTimeout,
                                     FormData data,
                                     Map<String, String> headers) throws UploadException {
        try {
            // create URL
            URL theUrl = uri.toURL();
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
            if (!headers.containsKey("Accept")) {
                connection.addRequestProperty("Accept", "*/*");
            }
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
                StringBuilder b = new StringBuilder("Got ")
                    .append(status);
                if (isNotBlank(reason)) {
                    b.append(" reason: ")
                        .append(reason);
                }
                logger.trace(RB.$("webhook.server.reply", status, reason));

                try (Reader reader = newInputStreamReader(connection.getErrorStream())) {
                    String message = IOUtils.toString(reader);
                    if (isNotBlank(message)) {
                        b.append(", ")
                            .append(message);
                    }
                }
                throw new UploadException(b.toString());
            }

            return newInputStreamReader(connection.getInputStream());
        } catch (IOException e) {
            logger.trace(e);
            throw new UploadException(e);
        }
    }

    public static boolean head(JReleaserLogger logger,
                               String theUrl,
                               int connectTimeout,
                               int readTimeout) throws RestAPIException {
        try {
            // create URL
            URL url = new URI(theUrl).toURL();
            // open connection
            logger.debug(RB.$("webhook.connection.open"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // set options
            logger.debug(RB.$("webhook.connection.configure"));
            connection.setConnectTimeout(connectTimeout * 1000);
            connection.setReadTimeout(readTimeout * 1000);
            connection.setAllowUserInteraction(false);
            connection.setInstanceFollowRedirects(true);

            connection.setRequestMethod("HEAD");
            connection.addRequestProperty("User-Agent", "JReleaser/" + JReleaserVersion.getPlainVersion());

            // handle response
            logger.debug(RB.$("webhook.response.handle"));
            int status = connection.getResponseCode();
            if (status == 200) return true;
            if (status == 404) return false;

            String reason = connection.getResponseMessage();
            StringBuilder b = new StringBuilder("Request replied with: ")
                .append(status);
            if (isNotBlank(reason)) {
                b.append(" reason: ")
                    .append(reason);
            }
            throw new RestAPIException(status, b.toString());
        } catch (URISyntaxException | IOException e) {
            logger.trace(e);
            throw new RestAPIException(500, e);
        }
    }

    private static SSLSocketFactory nonValidatingSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{new NonValidatingTrustManager()}, null); // lgtm [java/insecure-trustmanager]
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
