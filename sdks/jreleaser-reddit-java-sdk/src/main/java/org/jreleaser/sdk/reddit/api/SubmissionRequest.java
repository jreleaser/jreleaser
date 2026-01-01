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
package org.jreleaser.sdk.reddit.api;

import feign.form.FormProperty;

/**
 * @author Usman Shaikh
 * @since 1.21.0
 */
public class SubmissionRequest {
    @FormProperty("api_type")
    private String apiType;
    
    private String kind;
    
    @FormProperty("sr")
    private String subreddit;
    
    private String title;

    private String text;
    
    private String url;

    public SubmissionRequest() {
    }

    public SubmissionRequest(String apiType, String kind, String subreddit, String title, String text, String url) {
        this.apiType = apiType;
        this.kind = kind;
        this.subreddit = subreddit;
        this.title = title;
        this.text = text;
        this.url = url;
    }

    public String getApiType() {
        return apiType;
    }

    public void setApiType(String apiType) {
        this.apiType = apiType;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static SubmissionRequest forTextPost(String subreddit, String title, String text) {
        return new SubmissionRequest("json", "self", subreddit, title, text, null);
    }

    public static SubmissionRequest forLinkPost(String subreddit, String title, String url) {
        return new SubmissionRequest("json", "link", subreddit, title, null, url);
    }
}