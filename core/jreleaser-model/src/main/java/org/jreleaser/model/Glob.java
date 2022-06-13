/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.util.Artifacts;
import org.jreleaser.util.JReleaserException;
import org.jreleaser.util.PlatformUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static java.nio.file.Files.exists;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Glob extends AbstractModelObject<Glob> implements Domain, ExtraProperties {
    private static final String GLOB_PREFIX = "glob:";
    private static final String REGEX_PREFIX = "regex:";

    private final Map<String, Object> extraProperties = new LinkedHashMap<>();

    private String pattern;
    private String platform;
    @JsonIgnore
    private Set<Artifact> artifacts;

    private String directory;

    @Override
    public void merge(Glob glob) {
        freezeCheck();
        this.pattern = merge(this.pattern, glob.pattern);
        this.platform = merge(this.platform, glob.platform);
        setExtraProperties(merge(this.extraProperties, glob.extraProperties));
    }

    @Override
    public String getPrefix() {
        return "artifact";
    }

    @Override
    public Map<String, Object> getExtraProperties() {
        return freezeWrap(extraProperties);
    }

    @Override
    public void setExtraProperties(Map<String, Object> extraProperties) {
        freezeCheck();
        this.extraProperties.clear();
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public void addExtraProperties(Map<String, Object> extraProperties) {
        freezeCheck();
        this.extraProperties.putAll(extraProperties);
    }

    public Set<Artifact> getResolvedArtifacts(JReleaserContext context) {
        return getResolvedArtifactsPattern(context);
    }

    public Set<Artifact> getResolvedArtifactsPattern(JReleaserContext context) {
        if (null == artifacts) {
            mutate(() -> setPattern(Artifacts.resolveForGlob(getPattern(), context, this)));
            normalizePattern(resolveDirectory(context));
            artifacts = Artifacts.resolveFiles(context, resolveDirectory(context), Collections.singletonList(pattern));
            artifacts.forEach(artifact -> {
                artifact.setPlatform(platform);
                if (context.isPlatformSelected(artifact)) artifact.activate();
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

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        freezeCheck();
        if (isBlank(pattern)) return;
        this.pattern = pattern.trim();
    }

    private void normalizePattern(Path basedir) {
        if (!pattern.startsWith(GLOB_PREFIX) && !pattern.startsWith(REGEX_PREFIX)) {
            this.pattern = GLOB_PREFIX + pattern;
        }

        if (this.pattern.startsWith(GLOB_PREFIX)) {
            String path = this.pattern.substring(GLOB_PREFIX.length());
            String test = path;
            if (PlatformUtils.isWindows()) {
                test = test.replace("*", "x");
            }
            if (!Paths.get(test).isAbsolute()) {
                this.pattern = GLOB_PREFIX + "**" + File.separator + path;
            } else {
                this.pattern = GLOB_PREFIX + "**" + relativize(basedir, Paths.get(path));
            }
        } else {
            String path = this.pattern.substring(REGEX_PREFIX.length());
            String test = path;
            if (PlatformUtils.isWindows()) {
                test = test.replace("*", "x");
            }
            if (!Paths.get(test).isAbsolute()) {
                this.pattern = REGEX_PREFIX + ".*" + File.separator + path;
            } else {
                this.pattern = REGEX_PREFIX + ".*" + relativize(basedir, Paths.get(path));
            }
        }

        if (PlatformUtils.isWindows()) {
            this.pattern = pattern.replace("/", "\\\\");
        }
    }

    public Path relativize(Path base, Path other) {
        Path p1 = base.toAbsolutePath();
        Path p2 = other.toAbsolutePath();
        Path p = p1.relativize(p2).normalize();
        while (p != null && !p.endsWith("..")) {
            p = p.getParent();
        }

        return base.resolve(p).resolve(p2).normalize();
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        freezeCheck();
        this.platform = platform;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        freezeCheck();
        this.directory = directory;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("pattern", pattern);
        props.put("platform", platform);
        props.put("extraProperties", getResolvedExtraProperties());
        props.put("directory", directory);
        return props;
    }
}
