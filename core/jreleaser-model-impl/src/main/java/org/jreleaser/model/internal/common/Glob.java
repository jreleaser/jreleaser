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
package org.jreleaser.model.internal.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.util.Artifacts;
import org.jreleaser.util.PlatformUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static java.nio.file.Files.exists;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class Glob extends AbstractActivatable<Glob> implements Domain, ExtraProperties {
    private static final String GLOB_PREFIX = "glob:";
    private static final String REGEX_PREFIX = "regex:";
    private static final long serialVersionUID = 3229164223309181365L;

    private final Map<String, Object> extraProperties = new LinkedHashMap<>();

    private String pattern;
    private String platform;
    @JsonIgnore
    private boolean selected;
    @JsonIgnore
    private Set<Artifact> artifacts;
    private String directory;

    @JsonIgnore
    private final org.jreleaser.model.api.common.Glob immutable = new org.jreleaser.model.api.common.Glob() {
        private static final long serialVersionUID = 7275219810370004662L;

        @Override
        public Active getActive() {
            return Glob.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return Glob.this.isEnabled();
        }

        @Override
        public boolean isSelected() {
            return Glob.this.isSelected();
        }

        @Override
        public String getPattern() {
            return pattern;
        }

        @Override
        public String getPlatform() {
            return platform;
        }

        @Override
        public String getDirectory() {
            return directory;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Glob.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return Glob.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(extraProperties);
        }
    };

    public Glob() {
        setActive(Active.ALWAYS);
    }

    public org.jreleaser.model.api.common.Glob asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Glob source) {
        super.merge(source);
        this.pattern = merge(this.pattern, source.pattern);
        this.platform = merge(this.platform, source.platform);
        this.selected = source.selected;
        setExtraProperties(merge(this.extraProperties, source.extraProperties));
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

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        if (isBlank(pattern)) return;
        this.pattern = pattern.trim();
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("active", getActive());
        props.put("pattern", pattern);
        props.put("platform", platform);
        props.put("directory", directory);
        props.put("extraProperties", getExtraProperties());
        return props;
    }

    public Set<Artifact> getResolvedArtifacts(JReleaserContext context) {
        return getResolvedArtifactsPattern(context);
    }

    public Set<Artifact> getResolvedArtifactsPattern(JReleaserContext context) {
        if (!isActiveAndSelected()) return emptySet();

        if (null == artifacts) {
            setPattern(Artifacts.resolveForGlob(getPattern(), context, this));
            normalizePattern();
            artifacts = Artifacts.resolveFiles(context, resolveDirectory(context), singletonList(pattern));
            artifacts.forEach(artifact -> {
                artifact.setPlatform(platform);
                artifact.setActive(getActive());
                if (isSelected()) artifact.select();
                artifact.setExtraProperties(getExtraProperties());
            });
        }

        return artifacts;
    }

    private Path resolveDirectory(JReleaserContext context) {
        // resolve directory
        Path path = context.getBasedir();
        if (isNotBlank(directory)) {
            directory = Artifacts.resolveForGlob(directory, context, this);
            path = context.getBasedir().resolve(Paths.get(directory)).normalize();
            if (context.getMode().validatePaths() && !exists(path)) {
                throw new JReleaserException(RB.$("ERROR_path_does_not_exist", context.relativizeToBasedir(path)));
            }
        }

        return path;
    }

    private void normalizePattern() {
        if (!pattern.startsWith(GLOB_PREFIX) && !pattern.startsWith(REGEX_PREFIX)) {
            this.pattern = GLOB_PREFIX + pattern;
        }

        String prefix = this.pattern.startsWith(GLOB_PREFIX) ? GLOB_PREFIX : REGEX_PREFIX;
        String root = this.pattern.startsWith(GLOB_PREFIX) ? "**" : ".*";
        String path = this.pattern.substring(prefix.length());

        String test = path;
        if (PlatformUtils.isWindows()) {
            test = test.replace("*", "x");
        }

        if (!Paths.get(test).isAbsolute()) {
            this.pattern = prefix + root + File.separator + path;
        } else {
            this.pattern = prefix + path;
        }

        if (PlatformUtils.isWindows()) {
            this.pattern = pattern.replace("/", "\\\\");
        }
    }
}
