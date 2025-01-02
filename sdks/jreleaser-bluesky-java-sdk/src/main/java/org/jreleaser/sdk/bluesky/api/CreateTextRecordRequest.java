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
package org.jreleaser.sdk.bluesky.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


/**
 * @author Simon Verhoeven
 * @author Tom Cools
 * @since 1.7.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateTextRecordRequest {

    private String repo;

    private String collection = "app.bsky.feed.post";

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

        private ReplyReference reply;

        @JsonProperty("$type")
        private String type;

        private List<Facet> facets;

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

        public ReplyReference getReply() {
            return reply;
        }

        public void setReply(ReplyReference reply) {
            this.reply = reply;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<Facet> getFacets() {
            return facets;
        }

        public void setFacets(List<Facet> facets) {
            this.facets = facets;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReplyReference {
        private CreateRecordResponse root;

        private CreateRecordResponse parent;

        public CreateRecordResponse getRoot() {
            return root;
        }

        public void setRoot(CreateRecordResponse root) {
            this.root = root;
        }

        public CreateRecordResponse getParent() {
            return parent;
        }

        public void setParent(CreateRecordResponse parent) {
            this.parent = parent;
        }
    }
}
