/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
package org.jreleaser.model;

import org.jreleaser.util.PlatformUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Docker extends AbstractTool {
    public static final String NAME = "docker";
    public static final String LABEL_OCI_IMAGE_TITLE = "org.opencontainers.image.title";
    public static final String LABEL_OCI_IMAGE_DESCRIPTION = "org.opencontainers.image.description";
    public static final String LABEL_OCI_IMAGE_REVISION = "org.opencontainers.image.revision";
    public static final String LABEL_OCI_IMAGE_VERSION = "org.opencontainers.image.version";
    public static final String LABEL_OCI_IMAGE_LICENSES = "org.opencontainers.image.licenses";
    public static final String LABEL_OCI_IMAGE_URL = "org.opencontainers.image.url";

    private final Map<String, String> labels = new LinkedHashMap<>();
    private final Set<String> imageNames = new LinkedHashSet<>();
    private final List<String> buildArgs = new ArrayList<>();

    private String baseImage;

    public Docker() {
        super(NAME);
    }

    void setAll(Docker docker) {
        super.setAll(docker);
        this.baseImage = docker.baseImage;
        setImageNames(docker.imageNames);
        setBuildArgs(docker.buildArgs);
        setLabels(docker.labels);
    }

    public String getBaseImage() {
        return baseImage;
    }

    public void setBaseImage(String baseImage) {
        this.baseImage = baseImage;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels.clear();
        this.labels.putAll(labels);
    }

    public void addLabels(Map<String, String> labels) {
        this.labels.putAll(labels);
    }

    public void addLabel(String key, String value) {
        if (isNotBlank(value)) {
            this.labels.put(key, value);
        }
    }

    public Set<String> getImageNames() {
        return imageNames;
    }

    public void setImageNames(Set<String> imageNames) {
        if (imageNames != null) {
            this.imageNames.clear();
            this.imageNames.addAll(imageNames);
        }
    }

    public void addImageName(String imageName) {
        if (isNotBlank(imageName)) {
            this.imageNames.add(imageName);
        }
    }

    public List<String> getBuildArgs() {
        return buildArgs;
    }

    public void setBuildArgs(List<String> buildArgs) {
        if (buildArgs != null) {
            this.buildArgs.clear();
            this.buildArgs.addAll(buildArgs);
        }
    }

    public void addBuildArg(String buildArg) {
        if (isNotBlank(buildArg)) {
            this.buildArgs.add(buildArg);
        }
    }

    @Override
    protected void asMap(Map<String, Object> props) {
        props.put("baseImage", baseImage);
        props.put("imageNames", imageNames);
        props.put("buildArgs", buildArgs);
        props.put("labels", labels);
    }

    @Override
    public boolean supportsPlatform(String platform) {
        return isBlank(platform) || !PlatformUtils.isMac(platform);
    }
}
