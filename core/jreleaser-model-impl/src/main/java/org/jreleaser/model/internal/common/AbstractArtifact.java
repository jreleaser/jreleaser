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
package org.jreleaser.model.internal.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.JReleaserContext;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 1.6.0
 */
public abstract class AbstractArtifact<S extends AbstractArtifact<S>> extends AbstractActivatable<S> implements Domain, ExtraProperties {
    protected static final String GLOB_PREFIX = "glob:";
    protected static final String REGEX_PREFIX = "regex:";
    private static final long serialVersionUID = 8523164047987463127L;

    private final Map<String, Object> extraProperties = new LinkedHashMap<>();

    private String platform;
    @JsonIgnore
    private boolean selected;

    protected AbstractArtifact() {
        setActive(Active.ALWAYS);
    }

    @Override
    public void merge(S source) {
        super.merge(source);
        this.platform = merge(this.platform, source.getPlatform());
        this.selected = source.isSelected();
        setExtraProperties(merge(this.extraProperties, source.getExtraProperties()));
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean resolveActiveAndSelected(JReleaserContext context) {
        resolveEnabled(context.getModel().getProject());
        this.selected = context.isPlatformSelected(platform);
        return isActiveAndSelected();
    }

    public boolean isActiveAndSelected() {
        return isEnabled() && selected;
    }

    public void deactivateAndUnselect() {
        disable();
        this.selected = false;
    }

    public void select() {
        this.selected = true;
    }

    @Override
    public String prefix() {
        return "artifact";
    }

    @Override
    public Map<String, Object> getExtraProperties() {
        return extraProperties;
    }

    @Override
    public void setExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.clear();
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public void addExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.putAll(extraProperties);
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
