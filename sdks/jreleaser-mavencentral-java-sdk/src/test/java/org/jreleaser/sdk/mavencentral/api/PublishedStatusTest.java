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
package org.jreleaser.sdk.mavencentral.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PublishedStatusTest {
    @Test
    void deserializesPublishedResponse() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        assertThat(mapper.readValue("{\"published\":true}", PublishedStatus.class).isPublished()).isTrue();
        assertThat(mapper.readValue("{\"published\":false}", PublishedStatus.class).isPublished()).isFalse();
        assertThat(mapper.readValue("{\"published\":true,\"unknown\":1}", PublishedStatus.class).isPublished()).isTrue();
    }
}
