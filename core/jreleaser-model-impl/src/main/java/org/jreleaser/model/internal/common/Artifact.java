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
import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.Assembler;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.util.Artifacts;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.util.Algorithm;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.file.Files.exists;
import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.OPTIONAL;
import static org.jreleaser.model.internal.util.Artifacts.checkAndCopyFile;
import static org.jreleaser.model.internal.util.Artifacts.resolveForArtifact;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.isTrue;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class Artifact extends AbstractArtifact<Artifact> implements Domain, ExtraProperties, Comparable<Artifact> {
    private static final long serialVersionUID = 4590895350785446198L;

    @JsonIgnore
    private final Map<Algorithm, String> hashes = new LinkedHashMap<>();

    private String path;
    private String transform;
    @JsonIgnore
    private Path effectivePath;
    @JsonIgnore
    private Path resolvedPath;
    @JsonIgnore
    private Path resolvedTransform;

    @JsonIgnore
    private final org.jreleaser.model.api.common.Artifact immutable = new org.jreleaser.model.api.common.Artifact() {
        private static final long serialVersionUID = -5286060454190216979L;

        @Override
        public Active getActive() {
            return Artifact.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return Artifact.this.isEnabled();
        }

        @Override
        public boolean isSelected() {
            return Artifact.this.isSelected();
        }

        @Override
        public Path getEffectivePath() {
            return effectivePath;
        }

        @Override
        public Path getResolvedPath() {
            return resolvedPath;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public String getHash() {
            return Artifact.this.getHash();
        }

        @Override
        public String getHash(Algorithm algorithm) {
            return Artifact.this.getHash(algorithm);
        }

        @Override
        public String getPlatform() {
            return Artifact.this.getPlatform();
        }

        @Override
        public String getTransform() {
            return transform;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Artifact.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return Artifact.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(Artifact.this.getExtraProperties());
        }
    };

    public org.jreleaser.model.api.common.Artifact asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Artifact source) {
        super.merge(source);
        this.effectivePath = merge(this.effectivePath, source.effectivePath);
        this.path = merge(this.path, source.path);
        this.transform = merge(this.transform, source.transform);
        this.resolvedPath = merge(this.resolvedPath, source.resolvedPath);
        this.resolvedTransform = merge(this.resolvedTransform, source.resolvedTransform);

        // do not merge
        setHashes(source.hashes);
    }

    public boolean isOptional(JReleaserContext context) {
        Object value = getExtraProperties().get(OPTIONAL);

        if (value instanceof CharSequence && value.toString().contains("{{")) {
            value = resolveTemplate(value.toString(), context.fullProps());
            getExtraProperties().put(OPTIONAL, value);
        }

        return isTrue(value);
    }

    public Path getEffectivePath() {
        return effectivePath;
    }

    public Path getResolvedPath() {
        return resolvedPath;
    }

    public Path getResolvedTransform() {
        return resolvedTransform;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
        this.resolvedPath = null;
        this.effectivePath = null;
        this.resolvedTransform = null;
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
        return unmodifiableMap(hashes);
    }

    void setHashes(Map<Algorithm, String> hashes) {
        this.hashes.clear();
        this.hashes.putAll(hashes);
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
        map.put("enabled", isEnabled());
        map.put("active", getActive());
        map.put("path", path);
        map.put("transform", transform);
        map.put("platform", getPlatform());
        map.put("extraProperties", getExtraProperties());
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;
        Artifact that = (Artifact) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public int compareTo(Artifact o) {
        if (null == o) return -1;
        return path.compareTo(o.path);
    }

    public void mergeExtraProperties(Map<String, Object> extraProperties) {
        extraProperties.forEach((k, v) -> {
            if (!getExtraProperties().containsKey(k)) {
                getExtraProperties().put(k, v);
            }
        });
    }

    public Path getEffectivePath(JReleaserContext context) {
        return getEffectivePath(context, (TemplateContext) null);
    }

    public Path getEffectivePath(JReleaserContext context, TemplateContext additionalContext) {
        if (null == effectivePath) {
            Path rp = getResolvedPath(context, additionalContext);
            Path tp = getResolvedTransform(context, additionalContext);
            effectivePath = checkAndCopyFile(context, rp, tp, isOptional(context));
        }
        return effectivePath;
    }

    public Path getEffectivePath(JReleaserContext context, Distribution distribution) {
        return getEffectivePath(context, null, distribution);
    }

    public Path getEffectivePath(JReleaserContext context, TemplateContext additionalContext, Distribution distribution) {
        if (null == effectivePath) {
            Path rp = getResolvedPath(context, additionalContext, distribution);
            Path tp = getResolvedTransform(context, additionalContext, distribution);
            effectivePath = checkAndCopyFile(context, rp, tp, isOptional(context));
        }
        return effectivePath;
    }

    public Path getEffectivePath(JReleaserContext context, Assembler<?> assembler) {
        return getEffectivePath(context, null, assembler);
    }

    public Path getEffectivePath(JReleaserContext context, TemplateContext additionalContext, Assembler<?> assembler) {
        if (null == effectivePath) {
            Path rp = getResolvedPath(context, additionalContext, assembler);
            Path tp = getResolvedTransform(context, additionalContext, assembler);
            effectivePath = checkAndCopyFile(context, rp, tp, isOptional(context));
        }
        return effectivePath;
    }

    public Path getResolvedPath(JReleaserContext context, Path basedir, boolean checkIfExists) {
        return getResolvedPath(context, null, basedir, checkIfExists);
    }

    public Path getResolvedPath(JReleaserContext context, TemplateContext additionalContext, Path basedir, boolean checkIfExists) {
        if (null == resolvedPath) {
            path = resolveForArtifact(path, context, additionalContext, this);
            resolvedPath = basedir.resolve(Paths.get(path)).normalize();
            if (checkIfExists && !isOptional(context) && !exists(resolvedPath)) {
                throw new JReleaserException(RB.$("ERROR_path_does_not_exist", context.relativizeToBasedir(resolvedPath)));
            }
        }
        return resolvedPath;
    }

    public Path getResolvedPath(JReleaserContext context) {
        return getResolvedPath(context, (TemplateContext) null);
    }

    public Path getResolvedPath(JReleaserContext context, TemplateContext additionalContext) {
        return getResolvedPath(context, additionalContext, context.getBasedir(), context.getMode().validatePaths());
    }

    public Path getResolvedPath(JReleaserContext context, Distribution distribution) {
        return getResolvedPath(context, null, distribution);
    }

    public Path getResolvedPath(JReleaserContext context, TemplateContext additionalContext, Distribution distribution) {
        if (null == resolvedPath) {
            path = Artifacts.resolveForArtifact(path, context, additionalContext, this, distribution);
            resolvedPath = context.getBasedir().resolve(Paths.get(path)).normalize();
            if (context.getMode().validatePaths() && !isOptional(context) && !exists(resolvedPath)) {
                throw new JReleaserException(RB.$("ERROR_path_does_not_exist", context.relativizeToBasedir(resolvedPath)));
            }
        }
        return resolvedPath;
    }

    public Path getResolvedPath(JReleaserContext context, Assembler<?> assembler) {
        return getResolvedPath(context, null, assembler);
    }

    public Path getResolvedPath(JReleaserContext context, TemplateContext additionalContext, Assembler<?> assembler) {
        if (null == resolvedPath) {
            path = Artifacts.resolveForArtifact(path, context, additionalContext, this, assembler);
            resolvedPath = context.getBasedir().resolve(Paths.get(path)).normalize();
            if (context.getMode().validatePaths() && !isOptional(context) && !exists(resolvedPath)) {
                throw new JReleaserException(RB.$("ERROR_path_does_not_exist", context.relativizeToBasedir(resolvedPath)));
            }
        }
        return resolvedPath;
    }

    public boolean resolvedPathExists() {
        return null != resolvedPath && exists(resolvedPath);
    }

    public Path getResolvedTransform(JReleaserContext context, Path basedir) {
        return getResolvedTransform(context, null, basedir);
    }

    public Path getResolvedTransform(JReleaserContext context, TemplateContext additionalContext, Path basedir) {
        if (null == resolvedTransform && isNotBlank(transform)) {
            transform = resolveForArtifact(transform, context, additionalContext, this);
            resolvedTransform = basedir.resolve(Paths.get(transform)).normalize();
        }
        return resolvedTransform;
    }

    public Path getResolvedTransform(JReleaserContext context) {
        return getResolvedTransform(context, (TemplateContext) null);
    }

    public Path getResolvedTransform(JReleaserContext context, TemplateContext additionalContext) {
        return getResolvedTransform(context, additionalContext, context.getArtifactsDirectory());
    }

    public Path getResolvedTransform(JReleaserContext context, Distribution distribution) {
        return getResolvedTransform(context, null, distribution);
    }

    public Path getResolvedTransform(JReleaserContext context, TemplateContext additionalContext, Distribution distribution) {
        if (null == resolvedTransform && isNotBlank(transform)) {
            transform = Artifacts.resolveForArtifact(transform, context, additionalContext, this, distribution);
            resolvedTransform = context.getArtifactsDirectory().resolve(Paths.get(transform)).normalize();
        }
        return resolvedTransform;
    }

    public Path getResolvedTransform(JReleaserContext context, Assembler<?> assembler) {
        return getResolvedTransform(context, null, assembler);
    }

    public Path getResolvedTransform(JReleaserContext context, TemplateContext additionalContext, Assembler<?> assembler) {
        if (null == resolvedTransform && isNotBlank(transform)) {
            transform = Artifacts.resolveForArtifact(transform, context, additionalContext, this, assembler);
            resolvedTransform = context.getArtifactsDirectory().resolve(Paths.get(transform)).normalize();
        }
        return resolvedTransform;
    }

    public void mergeWith(Artifact other) {
        if (this == other) return;
        if (isBlank(this.getPlatform())) this.setPlatform(other.getPlatform());
        if (isBlank(this.transform)) this.transform = other.transform;
        mergeExtraProperties(other.getExtraProperties());
    }

    public Artifact copy() {
        Artifact copy = new Artifact();
        copy.mergeWith(this);
        return copy;
    }

    public static Set<Artifact> sortArtifacts(Set<Artifact> artifacts) {
        return artifacts.stream()
            .sorted(Artifact.comparatorByPlatform())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static Comparator<Artifact> comparatorByPlatform() {
        return (a1, a2) -> {
            String p1 = a1.getPlatform();
            String p2 = a2.getPlatform();
            if (isBlank(p1)) p1 = "";
            if (isBlank(p2)) p2 = "";
            return p1.compareTo(p2);
        };
    }

    public static Artifact of(Path resolvedPath, String platform, Map<String, Object> props) {
        Artifact artifact = new Artifact();
        artifact.path = resolvedPath.toAbsolutePath().toString();
        artifact.setPlatform(platform);
        artifact.resolvedPath = resolvedPath;
        artifact.effectivePath = resolvedPath;
        artifact.setExtraProperties(props);
        return artifact;
    }

    public static Artifact of(Path resolvedPath, Map<String, Object> props) {
        Artifact artifact = new Artifact();
        artifact.path = resolvedPath.toAbsolutePath().toString();
        artifact.resolvedPath = resolvedPath;
        artifact.effectivePath = resolvedPath;
        artifact.setExtraProperties(props);
        return artifact;
    }

    public static Artifact of(Path resolvedPath) {
        Artifact artifact = new Artifact();
        artifact.path = resolvedPath.toAbsolutePath().toString();
        artifact.resolvedPath = resolvedPath;
        artifact.effectivePath = resolvedPath;
        return artifact;
    }

    public static Artifact of(Path resolvedPath, String platform) {
        Artifact artifact = new Artifact();
        artifact.path = resolvedPath.toAbsolutePath().toString();
        artifact.setPlatform(platform);
        artifact.resolvedPath = resolvedPath;
        artifact.effectivePath = resolvedPath;
        return artifact;
    }
}
