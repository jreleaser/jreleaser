/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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

import org.jreleaser.util.Algorithm;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.file.Files.exists;
import static org.jreleaser.model.util.Artifacts.copyFile;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Artifact implements Domain, ExtraProperties {
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    private final Map<Algorithm, String> hashes = new LinkedHashMap<>();

    private String path;
    private String platform;
    private String transform;
    private Path resolvedPath;
    private Path resolvedTransform;

    void setAll(Artifact artifact) {
        this.path = artifact.path;
        this.platform = artifact.platform;
        this.transform = artifact.transform;
        this.resolvedPath = artifact.resolvedPath;
        this.resolvedTransform = artifact.resolvedTransform;
        setExtraProperties(artifact.extraProperties);
        setHashes(artifact.hashes);
    }

    public Path getEffectivePath(JReleaserContext context) {
        Path rp = getResolvedPath(context);
        Path tp = getResolvedTransform(context);

        if (null == tp) return rp;

        if (!java.nio.file.Files.exists(tp)) {
            context.getLogger().debug("transformed artifact does not exist: {}",
                context.relativizeToBasedir(tp));
            copyFile(context, rp, tp);
        } else if (rp.toFile().lastModified() > tp.toFile().lastModified()) {
            context.getLogger().debug("{} is newer than {}",
                context.relativizeToBasedir(rp),
                context.relativizeToBasedir(tp));
            copyFile(context, rp, tp);
        }

        return tp;
    }

    public Path getEffectivePath(JReleaserContext context, Distribution distribution) {
        Path rp = getResolvedPath(context, distribution);
        Path tp = getResolvedTransform(context, distribution);

        if (null == tp) return rp;

        if (!java.nio.file.Files.exists(tp)) {
            context.getLogger().debug("transformed artifact does not exist: {}",
                context.relativizeToBasedir(tp));
            copyFile(context, rp, tp);
        } else if (rp.toFile().lastModified() > tp.toFile().lastModified()) {
            context.getLogger().debug("{} is newer than {}",
                context.relativizeToBasedir(rp),
                context.relativizeToBasedir(tp));
            copyFile(context, rp, tp);
        }

        return tp;
    }

    public Path getEffectivePath(JReleaserContext context, Assembler assembler) {
        Path rp = getResolvedPath(context, assembler);
        Path tp = getResolvedTransform(context, assembler);

        if (null == tp) return rp;

        if (!java.nio.file.Files.exists(tp)) {
            context.getLogger().debug("transformed artifact does not exist: {}",
                context.relativizeToBasedir(tp));
            copyFile(context, rp, tp);
        } else if (rp.toFile().lastModified() > tp.toFile().lastModified()) {
            context.getLogger().debug("{} is newer than {}",
                context.relativizeToBasedir(rp),
                context.relativizeToBasedir(tp));
            copyFile(context, rp, tp);
        }

        return tp;
    }

    public Path getResolvedPath(JReleaserContext context) {
        if (null == resolvedPath) {
            if (path.contains("{{")) {
                path = applyTemplate(path, artifactProps(context.props()));
            }
            resolvedPath = context.getBasedir().resolve(Paths.get(path)).normalize();
            if (!exists(resolvedPath)) {
                throw new JReleaserException("Path does not exist. " + context.relativizeToBasedir(resolvedPath));
            }
        }
        return resolvedPath;
    }

    public Path getResolvedPath(JReleaserContext context, Distribution distribution) {
        if (null == resolvedPath) {
            if (path.contains("{{")) {
                Map<String, Object> props = context.props();
                props.putAll(distribution.props());
                path = applyTemplate(path, artifactProps(props));
            }
            resolvedPath = context.getBasedir().resolve(Paths.get(path)).normalize();
            if (!exists(resolvedPath)) {
                throw new JReleaserException("Artifact does not exist. " + context.relativizeToBasedir(resolvedPath));
            }
        }
        return resolvedPath;
    }

    public Path getResolvedPath(JReleaserContext context, Assembler assembler) {
        if (null == resolvedPath) {
            if (path.contains("{{")) {
                Map<String, Object> props = context.props();
                props.putAll(assembler.props());
                path = applyTemplate(path, artifactProps(props));
            }
            resolvedPath = context.getBasedir().resolve(Paths.get(path)).normalize();
            if (!exists(resolvedPath)) {
                throw new JReleaserException("Path does not exist. " + context.relativizeToBasedir(resolvedPath));
            }
        }
        return resolvedPath;
    }

    public Path getResolvedPath() {
        return resolvedPath;
    }

    public Path getResolvedTransform(JReleaserContext context) {
        if (null == resolvedTransform && isNotBlank(transform)) {
            if (transform.contains("{{")) {
                transform = applyTemplate(transform, artifactProps(context.props()));
            }
            resolvedTransform = context.getArtifactsDirectory().resolve(Paths.get(transform)).normalize();
        }
        return resolvedTransform;
    }

    public Path getResolvedTransform(JReleaserContext context, Distribution distribution) {
        if (null == resolvedTransform && isNotBlank(transform)) {
            if (transform.contains("{{")) {
                Map<String, Object> props = context.props();
                props.putAll(distribution.props());
                transform = applyTemplate(transform, artifactProps(props));
            }
            resolvedTransform = context.getArtifactsDirectory().resolve(Paths.get(transform)).normalize();
        }
        return resolvedTransform;
    }

    public Path getResolvedTransform(JReleaserContext context, Assembler assembler) {
        if (null == resolvedTransform && isNotBlank(transform)) {
            if (transform.contains("{{")) {
                Map<String, Object> props = context.props();
                props.putAll(assembler.props());
                transform = applyTemplate(transform, artifactProps(props));
            }
            resolvedTransform = context.getArtifactsDirectory().resolve(Paths.get(transform)).normalize();
        }
        return resolvedTransform;
    }

    private Map<String,Object> artifactProps(Map<String, Object> props) {
        props.putAll(getExtraProperties());
        props.putAll(getResolvedExtraProperties());
        props.put("platform", platform);
        props.put("artifactPlatform", platform);
        return props;
    }

    public Path getResolvedTransform() {
        return resolvedTransform;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHash() {
        return getHash(Algorithm.SHA_256);
    }

    public void setHash(String hash) {
        setHash(Algorithm.SHA_256, hash);
    }

    public String getHash(Algorithm algorithm) {
        return hashes.get(algorithm);
    }

    public void setHash(Algorithm algorithm, String hash) {
        if (isNotBlank(hash)) {
            this.hashes.put(algorithm, hash.trim());
        }
    }

    public Map<Algorithm, String> getHashes() {
        return Collections.unmodifiableMap(hashes);
    }

    void setHashes(Map<Algorithm, String> hashes) {
        this.hashes.clear();
        this.hashes.putAll(hashes);
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getTransform() {
        return transform;
    }

    public void setTransform(String transform) {
        this.transform = transform;
    }

    @Override
    public String getPrefix() {
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

    public void mergeExtraProperties(Map<String, Object> extraProperties) {
        extraProperties.forEach((k, v) -> {
            if (!this.extraProperties.containsKey(k)) {
                this.extraProperties.put(k, v);
            }
        });
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("path", path);
        map.put("platform", platform);
        map.put("transform", transform);
        map.put("extraProperties", getResolvedExtraProperties());
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artifact that = (Artifact) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    public void merge(Artifact other) {
        if (this == other) return;
        if (isBlank(this.platform)) this.platform = other.platform;
        if (isBlank(this.transform)) this.transform = other.transform;
        mergeExtraProperties(other.extraProperties);
    }

    public static Set<Artifact> sortArtifacts(Set<Artifact> artifacts) {
        return artifacts.stream()
            .sorted(Artifact.comparatorByPlatform())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static Comparator<Artifact> comparatorByPlatform() {
        return (a1, a2) -> {
            String p1 = a1.platform;
            String p2 = a2.platform;
            if (isBlank(p1)) p1 = "";
            if (isBlank(p2)) p2 = "";
            return p1.compareTo(p2);
        };
    }

    public static Artifact of(Path resolvedPath) {
        Artifact artifact = new Artifact();
        artifact.path = resolvedPath.toAbsolutePath().toString();
        artifact.resolvedPath = resolvedPath;
        return artifact;
    }

    public static Artifact of(Path resolvedPath, String platform) {
        Artifact artifact = new Artifact();
        artifact.path = resolvedPath.toAbsolutePath().toString();
        artifact.resolvedPath = resolvedPath;
        artifact.platform = platform;
        return artifact;
    }
}
