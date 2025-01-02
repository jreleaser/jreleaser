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
package org.jreleaser.model.internal.catalog.sbom;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.ExtraProperties;
import org.jreleaser.mustache.TemplateContext;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.jreleaser.model.Constants.SKIP_SBOM;
import static org.jreleaser.model.Constants.SKIP_SBOM_SYFT;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.FileType.ZIP;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.isTrue;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public final class SyftSbomCataloger extends AbstractSbomCataloger<SyftSbomCataloger, org.jreleaser.model.api.catalog.sbom.SyftSbomCataloger> {
    private static final long serialVersionUID = 167717390683692845L;

    private final Set<org.jreleaser.model.api.catalog.sbom.SyftSbomCataloger.Format> formats = new LinkedHashSet<>();
    private String version;

    @JsonIgnore
    private final org.jreleaser.model.api.catalog.sbom.SyftSbomCataloger immutable = new org.jreleaser.model.api.catalog.sbom.SyftSbomCataloger() {
        private static final long serialVersionUID = 6622561901807746751L;

        @Override
        public String getVersion() {
            return SyftSbomCataloger.this.getVersion();
        }

        @Override
        public Set<Format> getFormats() {
            return unmodifiableSet(SyftSbomCataloger.this.getFormats());
        }

        @Override
        public boolean isDistributions() {
            return SyftSbomCataloger.this.isDistributions();
        }

        @Override
        public boolean isFiles() {
            return SyftSbomCataloger.this.isFiles();
        }

        @Override
        public Pack getPack() {
            return SyftSbomCataloger.this.getPack().asImmutable();
        }

        @Override
        public String getGroup() {
            return org.jreleaser.model.api.catalog.sbom.SbomCataloger.GROUP;
        }

        @Override
        public String getType() {
            return SyftSbomCataloger.this.getType();
        }

        @Override
        public Active getActive() {
            return SyftSbomCataloger.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return SyftSbomCataloger.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(SyftSbomCataloger.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return SyftSbomCataloger.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(SyftSbomCataloger.this.getExtraProperties());
        }
    };

    public SyftSbomCataloger() {
        super(org.jreleaser.model.api.catalog.sbom.SyftSbomCataloger.TYPE);
    }

    @Override
    public org.jreleaser.model.api.catalog.sbom.SyftSbomCataloger asImmutable() {
        return immutable;
    }

    @Override
    public void merge(SyftSbomCataloger source) {
        super.merge(source);
        this.version = merge(this.version, source.version);
        setFormats(merge(this.formats, source.formats));
    }

    @Override
    protected boolean isSet() {
        return super.isSet() ||
            isNotBlank(version) ||
            !formats.isEmpty();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Set<org.jreleaser.model.api.catalog.sbom.SyftSbomCataloger.Format> getFormats() {
        return formats;
    }

    public void setFormats(Set<org.jreleaser.model.api.catalog.sbom.SyftSbomCataloger.Format> formats) {
        this.formats.clear();
        this.formats.addAll(formats);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("version", version);
        props.put("formats", formats);
    }

    @Override
    public Set<Artifact> resolveArtifacts(JReleaserContext context, Set<Artifact> candidates) {
        Set<Artifact> artifacts = new LinkedHashSet<>();

        if (getPack().isEnabled()) {
            artifacts.add(Artifact.of(resolveArchivePath(context)));
        } else {
            artifacts.addAll(doResolveArtifacts(context, candidates));
        }

        return artifacts;
    }

    @Override
    public boolean isSkipped(ExtraProperties target) {
        return isTrue(target.getExtraProperties().get(SKIP_SBOM)) ||
            isTrue(target.getExtraProperties().get(SKIP_SBOM_SYFT));
    }

    @Override
    public Path resolveArchivePath(JReleaserContext context) {
        Path catalogDirectory = context.getCatalogsDirectory()
            .resolve("sbom").resolve(getType());
        String archiveName = resolveArchiveName(context);
        return catalogDirectory.resolve(archiveName);
    }

    private String resolveArchiveName(JReleaserContext context) {
        String archiveName = getPack().getName();
        TemplateContext props = context.fullProps();
        props.setAll(resolvedExtraProperties());
        return resolveTemplate(archiveName, props) + ZIP.extension();
    }

    private Set<Artifact> doResolveArtifacts(JReleaserContext context, Set<Artifact> candidates) {
        Set<Artifact> artifacts = new LinkedHashSet<>();

        Path catalogDirectory = context.getCatalogsDirectory()
            .resolve("sbom").resolve(getType());

        for (org.jreleaser.model.api.catalog.sbom.SyftSbomCataloger.Format format : formats) {
            for (Artifact artifact : candidates) {
                String artifactFile = artifact.getEffectivePath().getFileName().toString();
                Path targetPath = catalogDirectory.resolve(artifactFile + format.extension());
                artifacts.add(Artifact.of(targetPath));
            }
        }

        return artifacts;
    }
}
