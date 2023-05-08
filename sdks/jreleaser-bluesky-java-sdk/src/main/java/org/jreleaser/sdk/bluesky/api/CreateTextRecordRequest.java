/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
package org.jreleaser.sdk.bluesky.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Simon Verhoeven
 * @author Tom Cools
 * @since 1.7.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateTextRecordRequest {
    private final static String BLUESKY_POST_COLLECTION = "app.bsky.feed.post";
    public static CreateTextRecordRequest of(String repo, String text) {
        CreateTextRecordRequest o = new CreateTextRecordRequest();
        o.repo = requireNonBlank(repo, "'repo' must not be blank").trim();
        o.collection = BLUESKY_POST_COLLECTION;

        TextRecord textRecord = new TextRecord();
        textRecord.text = requireNonBlank(text, "'text' must not be blank").trim();
        textRecord.createdAt = LocalDateTime.now().toString();
        o.record = textRecord;
        return o;
    }

    private String repo;

    private String collection;

    /**
     * The record to create.
     */
    private TextRecord record;

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public TextRecord getRecord() {
        return record;
    }

    public void setRecord(TextRecord record) {
        this.record = record;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextRecord {
        private String text;

        private String createdAt;

        @JsonProperty("$type")
        private String type;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }


}
