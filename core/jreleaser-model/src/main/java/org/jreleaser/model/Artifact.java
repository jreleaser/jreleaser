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

import org.jreleaser.util.Constants;
import org.jreleaser.util.Version;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static java.nio.file.Files.exists;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Artifact implements Domain {
    private String path;
    private String hash;
    private String platform;
    private String transform;
    private Path resolvedPath;
    private Path resolvedTransform;

    void setAll(Artifact artifact) {
        this.path = artifact.path;
        this.hash = artifact.hash;
        this.platform = artifact.platform;
        this.transform = artifact.transform;
        this.resolvedPath = artifact.resolvedPath;
        this.resolvedTransform = artifact.resolvedTransform;
    }

    public Path getEffectivePath(JReleaserContext context) {
        Path rp = getResolvedPath(context);
        Path tp = getResolvedTransform(context);

        if (null == tp) return rp;

        if (!java.nio.file.Files.exists(tp)) {
            context.getLogger().debug("transformed artifact does not exist: {}",
                context.getBasedir().relativize(tp));
            copyFile(context, rp, tp);
        } else if (rp.toFile().lastModified() > tp.toFile().lastModified()) {
            context.getLogger().debug("{} is newer than {}",
                context.getBasedir().relativize(rp),
                context.getBasedir().relativize(tp));
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
                context.getBasedir().relativize(tp));
            copyFile(context, rp, tp);
        } else if (rp.toFile().lastModified() > tp.toFile().lastModified()) {
            context.getLogger().debug("{} is newer than {}",
                context.getBasedir().relativize(rp),
                context.getBasedir().relativize(tp));
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
                context.getBasedir().relativize(tp));
            copyFile(context, rp, tp);
        } else if (rp.toFile().lastModified() > tp.toFile().lastModified()) {
            context.getLogger().debug("{} is newer than {}",
                context.getBasedir().relativize(rp),
                context.getBasedir().relativize(tp));
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
                context.getBasedir().relativize(src) + " to " +
                context.getBasedir().relativize(dest));
        }
    }

    private Path getResolvedPath(JReleaserContext context) {
        if (null == resolvedPath) {
            if (path.contains("{{")) {
                path = applyTemplate(new StringReader(path), context.props());
            }
            resolvedPath = context.getBasedir().resolve(Paths.get(path)).normalize();
            if (!exists(resolvedPath)) {
                throw new JReleaserException("Path does not exist. " + context.getBasedir().relativize(resolvedPath));
            }
        }
        return resolvedPath;
    }

    private Path getResolvedPath(JReleaserContext context, Distribution distribution) {
        if (null == resolvedPath) {
            if (path.contains("{{")) {
                Map<String, Object> props = context.props();
                fillDistributionProps(props, distribution);
                path = applyTemplate(new StringReader(path), props);
            }
            resolvedPath = context.getBasedir().resolve(Paths.get(path)).normalize();
            if (!exists(resolvedPath)) {
                throw new JReleaserException("Artifact does not exist. " + context.getBasedir().relativize(resolvedPath));
            }
        }
        return resolvedPath;
    }

    private Path getResolvedPath(JReleaserContext context, Assembler assembler) {
        if (null == resolvedPath) {
            if (path.contains("{{")) {
                Map<String, Object> props = context.props();
                fillAssemblerProps(props, assembler);
                path = applyTemplate(new StringReader(path), props);
            }
            resolvedPath = context.getBasedir().resolve(Paths.get(path)).normalize();
            if (!exists(resolvedPath)) {
                throw new JReleaserException("Path does not exist. " + context.getBasedir().relativize(resolvedPath));
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
                transform = applyTemplate(new StringReader(transform), context.props());
            }
            resolvedTransform = context.getArtifactsDirectory().resolve(Paths.get(transform)).normalize();
        }
        return resolvedTransform;
    }

    private Path getResolvedTransform(JReleaserContext context, Distribution distribution) {
        if (null == resolvedTransform && isNotBlank(transform)) {
            if (transform.contains("{{")) {
                Map<String, Object> props = context.props();
                fillDistributionProps(props, distribution);
                transform = applyTemplate(new StringReader(transform), props);
            }
            resolvedTransform = context.getArtifactsDirectory().resolve(Paths.get(transform)).normalize();
        }
        return resolvedTransform;
    }

    private Path getResolvedTransform(JReleaserContext context, Assembler assembler) {
        if (null == resolvedTransform && isNotBlank(transform)) {
            if (transform.contains("{{")) {
                Map<String, Object> props = context.props();
                fillAssemblerProps(props, assembler);
                transform = applyTemplate(new StringReader(transform), props);
            }
            resolvedTransform = context.getArtifactsDirectory().resolve(Paths.get(transform)).normalize();
        }
        return resolvedTransform;
    }

    private void fillDistributionProps(Map<String, Object> props, Distribution distribution) {
        props.put(Constants.KEY_DISTRIBUTION_NAME, distribution.getName());
        props.put(Constants.KEY_DISTRIBUTION_EXECUTABLE, distribution.getExecutable());
        if (distribution.getJava().isEnabled()) {
            props.putAll(distribution.getJava().getResolvedExtraProperties());
            props.put(Constants.KEY_DISTRIBUTION_JAVA_GROUP_ID, distribution.getJava().getGroupId());
            props.put(Constants.KEY_DISTRIBUTION_JAVA_ARTIFACT_ID, distribution.getJava().getArtifactId());
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION, distribution.getJava().getVersion());
            Version jv = Version.of(distribution.getJava().getVersion());
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MAJOR, jv.getMajor());
            if (jv.hasMinor()) props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MINOR, jv.getMinor());
            if (jv.hasPatch()) props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_PATCH, jv.getPatch());
            if (jv.hasTag()) props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_TAG, jv.getTag());
            if (jv.hasBuild()) props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_BUILD, jv.getBuild());
        }
        props.putAll(distribution.getResolvedExtraProperties());
    }

    private void fillAssemblerProps(Map<String, Object> props, Assembler assembler) {
        props.put(Constants.KEY_DISTRIBUTION_NAME, assembler.getName());
        props.put(Constants.KEY_DISTRIBUTION_EXECUTABLE, assembler.getExecutable());
        props.putAll(assembler.getJava().getResolvedExtraProperties());
        props.put(Constants.KEY_DISTRIBUTION_JAVA_GROUP_ID, assembler.getJava().getGroupId());
        props.put(Constants.KEY_DISTRIBUTION_JAVA_ARTIFACT_ID, assembler.getJava().getArtifactId());
        if (isNotBlank(assembler.getJava().getVersion())) {
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION, assembler.getJava().getVersion());
            Version jv = Version.of(assembler.getJava().getVersion());
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MAJOR, jv.getMajor());
            if (jv.hasMinor()) props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MINOR, jv.getMinor());
            if (jv.hasPatch()) props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_PATCH, jv.getPatch());
            if (jv.hasTag()) props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_TAG, jv.getTag());
            if (jv.hasBuild()) props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_BUILD, jv.getBuild());
        } else {
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MAJOR, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MINOR, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_PATCH, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_TAG, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_BUILD, "");
        }
        props.putAll(assembler.getResolvedExtraProperties());
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
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
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
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("path", path);
        map.put("hash", hash);
        map.put("platform", platform);
        map.put("transform", transform);
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
