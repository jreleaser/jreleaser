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
package org.jreleaser.model.api.announce;

import java.util.Locale;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Usman Shaikh
 * @since 1.21.0
 */
public interface RedditAnnouncer extends Announcer {
    String TYPE = "reddit";
    String REDDIT_CLIENT_ID = "REDDIT_CLIENT_ID";
    String REDDIT_CLIENT_SECRET = "REDDIT_CLIENT_SECRET";
    String REDDIT_USERNAME = "REDDIT_USERNAME";
    String REDDIT_PASSWORD = "REDDIT_PASSWORD";

    String getClientId();

    String getClientSecret();

    String getUsername();

    String getPassword();

    String getSubreddit();

    String getTitle();

    String getText();

    String getTextTemplate();

    String getUrl();

    SubmissionType getSubmissionType();

    /**
     * Reddit submission types
     */
    enum SubmissionType {
        SELF,       // Text post with body content
        LINK;       // Link post pointing to URLs

        public String formatted() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public static SubmissionType of(String str) {
            if (isBlank(str)) return null;
            return SubmissionType.valueOf(str.toUpperCase(Locale.ENGLISH).trim());
        }
    }
}