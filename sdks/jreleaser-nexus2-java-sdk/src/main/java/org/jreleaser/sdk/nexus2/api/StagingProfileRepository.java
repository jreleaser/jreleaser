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

import java.time.Instant;
import java.util.Locale;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StagingProfileRepository {
    private String profileId;
    private String profileName;
    private String repositoryId;
    private String type;
    private boolean transitioning;
    private Instant created;
    private Instant updated;

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isTransitioning() {
        return transitioning;
    }

    public void setTransitioning(boolean transitioning) {
        this.transitioning = transitioning;
    }

    public State getState() {
        return State.of(type);
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public Instant getUpdated() {
        return updated;
    }

    public void setUpdated(Instant updated) {
        this.updated = updated;
    }

    @Override
    public String toString() {
        return "StagingProfileRepository{" +
            "profileId='" + profileId + '\'' +
            ", profileName='" + profileName + '\'' +
            ", repositoryId='" + repositoryId + '\'' +
            ", type='" + type + '\'' +
            ", created='" + created + '\'' +
            ", updated='" + updated + '\'' +
            ", transitioning=" + transitioning +
            '}';
    }

    public static StagingProfileRepository notFound(String repositoryId) {
        StagingProfileRepository repository = new StagingProfileRepository();
        repository.repositoryId = repositoryId;
        repository.type = "not_found";
        return repository;
    }

    public enum State {
        OPEN,
        CLOSED,
        RELEASED,
        NOT_FOUND;

        public static State of(String str) {
            if (isBlank(str)) return null;
            return State.valueOf(str.toUpperCase(Locale.ENGLISH).trim()
                .replace("-", "_")
                .replace(" ", "_"));
        }
    }
}
