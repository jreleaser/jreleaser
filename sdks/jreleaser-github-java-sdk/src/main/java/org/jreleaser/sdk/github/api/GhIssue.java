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
package org.jreleaser.sdk.github.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GhIssue {
    private final List<GhLabel> labels = new ArrayList<>();
    private Long id;
    private Long number;
    private String title;
    private String state;
    private GhMilestone milestone;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<GhLabel> getLabels() {
        return labels;
    }

    public void setLabels(List<GhLabel> labels) {
        if (null != labels) {
            this.labels.clear();
            this.labels.addAll(labels);
        }
    }

    public GhMilestone getMilestone() {
        return milestone;
    }

    public void setMilestone(GhMilestone milestone) {
        this.milestone = milestone;
    }
}