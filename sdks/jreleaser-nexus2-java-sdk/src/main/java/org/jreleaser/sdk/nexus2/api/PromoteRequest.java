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
package org.jreleaser.sdk.nexus2.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PromoteRequest {
    private String stagedRepositoryId;
    private String description;

    public static PromoteRequest ofDescription(String description) {
        PromoteRequest promoteRequest = new PromoteRequest();
        promoteRequest.description = description;
        return promoteRequest;
    }

    public static PromoteRequest of(String stagedRepositoryId, String description) {
        PromoteRequest promoteRequest = new PromoteRequest();
        promoteRequest.stagedRepositoryId = stagedRepositoryId;
        promoteRequest.description = description;
        return promoteRequest;
    }

    public String getStagedRepositoryId() {
        return stagedRepositoryId;
    }

    public void setStagedRepositoryId(String stagedRepositoryId) {
        this.stagedRepositoryId = stagedRepositoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
