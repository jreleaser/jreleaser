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

import org.jreleaser.util.Algorithm;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static java.nio.file.Files.exists;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Artifact implements Domain, ExtraProperties, Comparable<Artifact> {
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

    private void copyFile(JReleaserContext context, Path src, Path dest) throws JReleaserException {
        try {
            java.nio.file.Files.createDirectories(dest.getParent());
            java.nio.file.Files.copy(src, dest, REPLACE_EXISTING, COPY_ATTRIBUTES);
        } catch (IOException e) {
            throw new JReleaserException("Unexpected error copying " +
                context.relativizeToBasedir(src) + " to " +
                context.relativizeToBasedir(dest));
        }
    }

    private Path getResolvedPath(JReleaserContext context) {
        if (null == resolvedPath) {
            if (path.contains("{{")) {
                path = applyTemplate(path, context.props());
            }
            resolvedPath = context.getBasedir().resolve(Paths.get(path)).normalize();
            if (!exists(resolvedPath)) {
                throw new JReleaserException("Path does not exist. " + context.relativizeToBasedir(resolvedPath));
            }
        }
        return resolvedPath;
    }

    private Path getResolvedPath(JReleaserContext context, Distribution distribution) {
        if (null == resolvedPath) {
            if (path.contains("{{")) {
                Map<String, Object> props = context.props();
                props.putAll(distribution.props());
                path = applyTemplate(path, props);
            }
            resolvedPath = context.getBasedir().resolve(Paths.get(path)).normalize();
            if (!exists(resolvedPath)) {
                throw new JReleaserException("Artifact does not exist. " + context.relativizeToBasedir(resolvedPath));
            }
        }
        return resolvedPath;
    }

    private Path getResolvedPath(JReleaserContext context, Assembler assembler) {
        if (null == resolvedPath) {
            if (path.contains("{{")) {
                Map<String, Object> props = context.props();
                props.putAll(assembler.props());
                path = applyTemplate(path, props);
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

    private Path getResolvedTransform(JReleaserContext context) {
        if (null == resolvedTransform && isNotBlank(transform)) {
            if (transform.contains("{{")) {
                transform = applyTemplate(transform, context.props());
            }
            resolvedTransform = context.getArtifactsDirectory().resolve(Paths.get(transform)).normalize();
        }
        return resolvedTransform;
    }

    private Path getResolvedTransform(JReleaserContext context, Distribution distribution) {
        if (null == resolvedTransform && isNotBlank(transform)) {
            if (transform.contains("{{")) {
                Map<String, Object> props = context.props();
                props.putAll(distribution.props());
                transform = applyTemplate(transform, props);
            }
            resolvedTransform = context.getArtifactsDirectory().resolve(Paths.get(transform)).normalize();
        }
        return resolvedTransform;
    }

    private Path getResolvedTransform(JReleaserContext context, Assembler assembler) {
        if (null == resolvedTransform && isNotBlank(transform)) {
            if (transform.contains("{{")) {
                Map<String, Object> props = context.props();
                props.putAll(assembler.props());
                transform = applyTemplate(transform, props);
            }
            resolvedTransform = context.getArtifactsDirectory().resolve(Paths.get(transform)).normalize();
        }
        return resolvedTransform;
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
        Artifact artifact = (Artifact) o;
        return path.equals(artifact.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public int compareTo(Artifact that) {
        String p1 = this.platform;
        String p2 = that.platform;
        if (isBlank(p1)) p1 = "";
        if (isBlank(p2)) p2 = "";
        return p1.compareTo(p2);
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
