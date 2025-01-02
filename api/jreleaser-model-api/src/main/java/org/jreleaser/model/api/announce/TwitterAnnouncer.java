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

import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface TwitterAnnouncer extends Announcer {
    String TYPE = "twitter";
    String TWITTER_CONSUMER_KEY = "TWITTER_CONSUMER_KEY";
    String TWITTER_CONSUMER_SECRET = "TWITTER_CONSUMER_SECRET";
    String TWITTER_ACCESS_TOKEN = "TWITTER_ACCESS_TOKEN";
    String TWITTER_ACCESS_TOKEN_SECRET = "TWITTER_ACCESS_TOKEN_SECRET";

    String getConsumerKey();

    String getConsumerSecret();

    String getAccessToken();

    String getAccessTokenSecret();

    String getStatus();

    List<String> getStatuses();

    String getStatusTemplate();
}
