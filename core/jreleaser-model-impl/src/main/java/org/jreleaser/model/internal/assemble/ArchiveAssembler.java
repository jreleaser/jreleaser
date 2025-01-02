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
package org.jreleaser.model.internal.assemble;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.Archive;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.catalog.swid.SwidTagAware;
import org.jreleaser.model.internal.common.ArchiveOptions;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.FileSet;
import org.jreleaser.model.internal.common.Glob;
import org.jreleaser.model.internal.common.Matrix;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.util.PlatformUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.jreleaser.model.api.assemble.ArchiveAssembler.TYPE;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.CollectionUtils.mapOf;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public final class ArchiveAssembler extends AbstractAssembler<ArchiveAssembler, org.jreleaser.model.api.assemble.ArchiveAssembler> implements SwidTagAware {
    private static final long serialVersionUID = 7763149116887864287L;

    private final Set<Archive.Format> formats = new LinkedHashSet<>();
    private final ArchiveOptions options = new ArchiveOptions();
    private final Matrix matrix = new Matrix();

    private String archiveName;
    private Boolean applyDefaultMatrix;
    private Boolean attachPlatform;
    private Distribution.DistributionType distributionType;

    @JsonIgnore
    private final org.jreleaser.model.api.assemble.ArchiveAssembler immutable = new org.jreleaser.model.api.assemble.ArchiveAssembler() {
        private static final long serialVersionUID = 3508112065751072495L;

        private Set<? extends org.jreleaser.model.api.common.Artifact> artifacts;
        private List<? extends org.jreleaser.model.api.common.FileSet> fileSets;
        private List<? extends org.jreleaser.model.api.common.Glob> files;
        private Set<? extends org.jreleaser.model.api.common.Artifact> outputs;

        @Override
        public String getArchiveName() {
            return archiveName;
        }

        @Override
        public boolean isApplyDefaultMatrix() {
            return ArchiveAssembler.this.isApplyDefaultMatrix();
        }

        @Override
        public boolean isAttachPlatform() {
            return ArchiveAssembler.this.isAttachPlatform();
        }

        @Override
        public Set<Archive.Format> getFormats() {
            return unmodifiableSet(formats);
        }

        @Override
        public org.jreleaser.model.api.common.ArchiveOptions getOptions() {
            return options.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.common.Matrix getMatrix() {
            return matrix.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.platform.Platform getPlatform() {
            return ArchiveAssembler.this.getPlatform().asImmutable();
        }

        @Override
        public org.jreleaser.model.api.catalog.swid.SwidTag getSwid() {
            return ArchiveAssembler.this.getSwid().asImmutable();
        }

        @Override
        public Distribution.DistributionType getDistributionType() {
            return ArchiveAssembler.this.getDistributionType();
        }

        @Override
        public String getType() {
            return ArchiveAssembler.this.getType();
        }

        @Override
        public Stereotype getStereotype() {
            return ArchiveAssembler.this.getStereotype();
        }

        @Override
        public boolean isExported() {
            return ArchiveAssembler.this.isExported();
        }

        @Override
        public String getName() {
            return ArchiveAssembler.this.getName();
        }

        @Override
        public String getTemplateDirectory() {
            return ArchiveAssembler.this.getTemplateDirectory();
        }

        @Override
        public Set<String> getSkipTemplates() {
            return unmodifiableSet(ArchiveAssembler.this.getSkipTemplates());
        }

        @Override
        public Set<? extends org.jreleaser.model.api.common.Artifact> getArtifacts() {
            if (null == artifacts) {
                artifacts = ArchiveAssembler.this.getArtifacts().stream()
                    .map(Artifact::asImmutable)
                    .collect(toSet());
            }
            return artifacts;
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.FileSet> getFileSets() {
            if (null == fileSets) {
                fileSets = ArchiveAssembler.this.getFileSets().stream()
                    .map(FileSet::asImmutable)
                    .collect(toList());
            }
            return fileSets;
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.Glob> getFiles() {
            if (null == files) {
                files = ArchiveAssembler.this.getFiles().stream()
                    .map(Glob::asImmutable)
                    .collect(toList());
            }
            return files;
        }

        @Override
        public Set<? extends org.jreleaser.model.api.common.Artifact> getOutputs() {
            if (null == outputs) {
                outputs = ArchiveAssembler.this.getOutputs().stream()
                    .map(Artifact::asImmutable)
                    .collect(toSet());
            }
            return outputs;
        }

        @Override
        public Active getActive() {
            return ArchiveAssembler.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return ArchiveAssembler.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(ArchiveAssembler.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return ArchiveAssembler.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(ArchiveAssembler.this.getExtraProperties());
        }
    };

    public ArchiveAssembler() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.assemble.ArchiveAssembler asImmutable() {
        return immutable;
    }

    @Override
    public Distribution.DistributionType getDistributionType() {
        return distributionType;
    }

    public void setDistributionType(Distribution.DistributionType distributionType) {
        this.distributionType = distributionType;
    }

    public void setDistributionType(String distributionType) {
        this.distributionType = Distribution.DistributionType.of(distributionType);
    }

    @Override
    public void merge(ArchiveAssembler source) {
        super.merge(source);
        this.archiveName = merge(this.archiveName, source.archiveName);
        this.distributionType = merge(this.distributionType, source.distributionType);
        this.applyDefaultMatrix = merge(this.applyDefaultMatrix, source.applyDefaultMatrix);
        this.attachPlatform = merge(this.attachPlatform, source.attachPlatform);
        setFormats(merge(this.formats, source.formats));
        setOptions(source.options);
        setMatrix(source.matrix);
    }

    public String getResolvedArchiveName(JReleaserContext context, Map<String, String> matrix) {
        TemplateContext props = context.fullProps();
        props.setAll(props());
        if (null != matrix) {
            props.setAll(mapOf("matrix", matrix));
        }
        String result = resolveTemplate(archiveName, props);
        if (isAttachPlatform()) {
            result += "-" + getPlatform().applyReplacements(PlatformUtils.getCurrentFull());
        }
        return result;
    }

    public String getArchiveName() {
        return archiveName;
    }

    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
    }

    public boolean isApplyDefaultMatrixSet() {
        return null != applyDefaultMatrix;
    }

    public boolean isApplyDefaultMatrix() {
        return null != applyDefaultMatrix && applyDefaultMatrix;
    }

    public void setApplyDefaultMatrix(Boolean applyDefaultMatrix) {
        this.applyDefaultMatrix = applyDefaultMatrix;
    }

    public boolean isAttachPlatformSet() {
        return null != attachPlatform;
    }

    public boolean isAttachPlatform() {
        return null != attachPlatform && attachPlatform;
    }

    public void setAttachPlatform(Boolean attachPlatform) {
        this.attachPlatform = attachPlatform;
    }

    public Set<Archive.Format> getFormats() {
        return formats;
    }

    public void setFormats(Set<Archive.Format> formats) {
        this.formats.clear();
        this.formats.addAll(formats);
    }

    public void addFormat(Archive.Format format) {
        this.formats.add(format);
    }

    public void addFormat(String str) {
        this.formats.add(Archive.Format.of(str));
    }

    public ArchiveOptions getOptions() {
        return options;
    }

    public void setOptions(ArchiveOptions options) {
        this.options.merge(options);
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public void setMatrix(Matrix matrix) {
        this.matrix.merge(matrix);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("archiveName", archiveName);
        props.put("distributionType", distributionType);
        props.put("applyDefaultMatrix", isApplyDefaultMatrix());
        props.put("attachPlatform", isAttachPlatform());
        props.put("formats", formats);
        props.put("options", options.asMap(full));
        matrix.asMap(props);
    }
}
