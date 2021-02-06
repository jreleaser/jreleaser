/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class AnnounceSdkmanCommand extends AbstractSingleSdkmanCommand {
    private final String hashtag;
    private final String releaseNotesUrl;

    private AnnounceSdkmanCommand(String consumerKey,
                                  String consumerToken,
                                  String candidate,
                                  String version,
                                  String apiHost,
                                  boolean https,
                                  String hashtag,
                                  String releaseNotesUrl) {
        super(consumerKey,
            consumerToken,
            candidate,
            version,
            apiHost,
            https);
        this.hashtag = hashtag;
        this.releaseNotesUrl = releaseNotesUrl;
    }

    @Override
    protected Map<String, String> getPayload() {
        Map<String, String> payload = super.getPayload();
        if (isNotBlank(hashtag)) payload.put("hashtag", hashtag.trim());
        if (isNotBlank(releaseNotesUrl)) payload.put("url", releaseNotesUrl.trim());
        return payload;
    }

    @Override
    protected Request createRequest(Map<String, String> payload) {
        RequestBody body = RequestBody.create(JSON, toJson(payload));
        return new Request.Builder()
            .url(createURL(ApiEndpoints.ANNOUNCE_ENDPOINT))
            .post(body)
            .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractSdkmanCommand.Builder<Builder> {
        private String hashtag;
        private String releaseNotesUrl;

        /**
         * The hashtag to use (legacy)
         */
        public Builder hashtag(String hashtag) {
            this.hashtag = hashtag;
            return this;
        }

        /**
         * The URL where the release notes can be found
         */
        public Builder releaseNotesUrl(String releaseNotesUrl) {
            this.releaseNotesUrl = releaseNotesUrl;
            return this;
        }

        public AnnounceSdkmanCommand build() {
            requireNonBlank(consumerKey, "'consumerKey' must not be blank");
            requireNonBlank(consumerToken, "'consumerToken' must not be blank");
            requireNonBlank(candidate, "'candidate' must not be blank");
            requireNonBlank(version, "'version' must not be blank");
            requireNonBlank(apiHost, "'apiHost' must not be blank");

            return new AnnounceSdkmanCommand(
                consumerKey,
                consumerToken,
                candidate,
                version,
                apiHost,
                https,
                hashtag,
                releaseNotesUrl);
        }
    }
}
