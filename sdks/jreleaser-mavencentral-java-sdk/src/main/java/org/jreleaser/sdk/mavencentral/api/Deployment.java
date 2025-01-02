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
package org.jreleaser.sdk.mavencentral.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 1.12.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Deployment {
    private String deploymentId;
    private String deploymentName;
    private State deploymentState;
    private List<String> purls = new ArrayList<>();
    private Instant createTimestamp;
    private List<DeployedComponentVersion> deployedComponentVersions = new ArrayList<>();
    private Map<String, List<String>> errors = new LinkedHashMap<>();

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }

    public State getDeploymentState() {
        return deploymentState;
    }

    public void setDeploymentState(State deploymentState) {
        this.deploymentState = deploymentState;
    }

    public List<String> getPurls() {
        return purls;
    }

    public void setPurls(List<String> purls) {
        this.purls = purls;
    }

    public Instant getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(Instant createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public List<DeployedComponentVersion> getDeployedComponentVersions() {
        return deployedComponentVersions;
    }

    public void setDeployedComponentVersions(List<DeployedComponentVersion> deployedComponentVersions) {
        this.deployedComponentVersions = deployedComponentVersions;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, List<String>> errors) {
        this.errors = errors;
    }

    public boolean isTransitioning() {
        return deploymentState == State.PENDING ||
            deploymentState == State.VALIDATING ||
            deploymentState == State.PUBLISHING;
    }
}
