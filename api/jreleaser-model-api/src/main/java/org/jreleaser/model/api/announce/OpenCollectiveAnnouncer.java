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

/**
 * @author Andres Almiray
 * @since 1.6.0
 */
public interface OpenCollectiveAnnouncer extends Announcer {
    String TYPE = "opencollective";
    String OPENCOLLECTIVE_TOKEN = "OPENCOLLECTIVE_TOKEN";
    String OPENCOLLECTIVE_SLUG = "OPENCOLLECTIVE_SLUG";

    String getHost();

    String getToken();

    String getSlug();

    String getTitle();

    String getMessage();

    String getMessageTemplate();
}
