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
package org.jreleaser.sdk.sdkman;

import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.sdk.sdkman.api.Announce;
import org.jreleaser.sdk.sdkman.api.Candidate;
import org.jreleaser.sdk.sdkman.api.Release;
import org.jreleaser.sdk.sdkman.api.SdkmanAPI;
import org.jreleaser.util.JReleaserLogger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Sdkman {
    private static final String KEY_UNIVERSAL = "UNIVERSAL";

    private final JReleaserLogger logger;
    private final SdkmanAPI api;
    private final boolean dryrun;

    public Sdkman(JReleaserLogger logger,
                  String apiHost,
                  int connectTimeout,
                  int readTimeout,
                  String consumerKey,
                  String consumerToken,
                  boolean dryrun) {
        requireNonNull(logger, "'logger' must not be null");
        requireNonBlank(apiHost, "'apiHost' must not be blank");
        requireNonBlank(consumerKey, "'consumerKey' must not be blank");
        requireNonBlank(consumerToken, "'consumerToken' must not be blank");

        this.logger = logger;
        this.dryrun = dryrun;
        this.api = Feign.builder()
            .encoder(new JacksonEncoder())
            .decoder(new JacksonDecoder())
            .requestInterceptor(template -> {
                template.header("User-Agent", "JReleaser/" + JReleaserVersion.getPlainVersion());
                template.header("Consumer-Key", consumerKey);
                template.header("Consumer-Token", consumerToken);
                template.header("Content-Type", "application/json");
                template.header("Accept", "application/json");
            })
            .errorDecoder((methodKey, response) -> new IllegalStateException("Server returned error " + response.reason()))
            .options(new Request.Options(connectTimeout, TimeUnit.SECONDS, readTimeout, TimeUnit.SECONDS, true))
            .target(SdkmanAPI.class, apiHost);

        this.logger.debug(RB.$("workflow.dryrun"), dryrun);
    }

    public void announce(String candidate,
                         String version) throws SdkmanException {
        announce(candidate, version, null, null);
    }

    public void announce(String candidate,
                         String version,
                         String hashtag,
                         String releaseNotesUrl) throws SdkmanException {
        Announce payload = Announce.of(candidate, version, hashtag, releaseNotesUrl);
        logger.debug("sdkman.announce: " + payload.toString());
        wrap(() -> api.announce(payload));
    }

    public void setDefault(String candidate,
                           String version) throws SdkmanException {
        Candidate payload = Candidate.of(candidate, version);
        logger.debug("sdkman.default: " + payload.toString());
        wrap(() -> api.setDefault(payload));
    }

    public void release(String candidate,
                        String version,
                        String url) throws SdkmanException {
        Map<String, String> platforms = new LinkedHashMap<>();
        platforms.put(KEY_UNIVERSAL, url);
        release(candidate, version, platforms);
    }

    public void release(String candidate,
                        String version,
                        String platform,
                        String url) throws SdkmanException {
        Map<String, String> platforms = new LinkedHashMap<>();
        platforms.put(platform, url);
        release(candidate, version, platforms);
    }

    public void release(String candidate,
                        String version,
                        Map<String, String> platforms) throws SdkmanException {
        for (Map.Entry<String, String> entry : platforms.entrySet()) {
            Release payload = Release.of(candidate, version, entry.getKey(), entry.getValue());
            logger.debug("sdkman.release: " + payload.toString());
            wrap(() -> api.release(payload));
        }
    }

    public void majorRelease(String candidate,
                             String version,
                             String url,
                             String hashtag,
                             String releaseNotesUrl,
                             boolean skipAnnounce) throws SdkmanException {
        Map<String, String> platforms = new LinkedHashMap<>();
        platforms.put(KEY_UNIVERSAL, url);
        majorRelease(candidate, version, platforms, hashtag, releaseNotesUrl, skipAnnounce);
    }

    public void majorRelease(String candidate,
                             String version,
                             String platform,
                             String url,
                             String hashtag,
                             String releaseNotesUrl,
                             boolean skipAnnounce) throws SdkmanException {
        Map<String, String> platforms = new LinkedHashMap<>();
        platforms.put(platform, url);
        majorRelease(candidate, version, platforms, hashtag, releaseNotesUrl, skipAnnounce);
    }

    public void majorRelease(String candidate,
                             String version,
                             Map<String, String> platforms,
                             String hashtag,
                             String releaseNotesUrl,
                             boolean skipAnnounce) throws SdkmanException {
        release(candidate, version, platforms);
        if (!skipAnnounce) announce(candidate, version, hashtag, releaseNotesUrl);
        setDefault(candidate, version);
    }

    public void minorRelease(String candidate,
                             String version,
                             String url,
                             String hashtag,
                             String releaseNotesUrl,
                             boolean skipAnnounce) throws SdkmanException {
        Map<String, String> platforms = new LinkedHashMap<>();
        platforms.put(KEY_UNIVERSAL, url);
        minorRelease(candidate, version, platforms, hashtag, releaseNotesUrl, skipAnnounce);
    }

    public void minorRelease(String candidate,
                             String version,
                             String platform,
                             String url,
                             String hashtag,
                             String releaseNotesUrl,
                             boolean skipAnnounce) throws SdkmanException {
        Map<String, String> platforms = new LinkedHashMap<>();
        platforms.put(platform, url);
        minorRelease(candidate, version, platforms, hashtag, releaseNotesUrl, skipAnnounce);
    }

    public void minorRelease(String candidate,
                             String version,
                             Map<String, String> platforms,
                             String hashtag,
                             String releaseNotesUrl,
                             boolean skipAnnounce) throws SdkmanException {
        release(candidate, version, platforms);
        if (!skipAnnounce) announce(candidate, version, hashtag, releaseNotesUrl);
    }

    private void wrap(Runnable runnable) throws SdkmanException {
        try {
            if (!dryrun) runnable.run();
        } catch (RuntimeException e) {
            logger.trace(e);
            throw new SdkmanException(RB.$("sdk.operation.failed", "Sdkman"), e);
        }
    }
}
