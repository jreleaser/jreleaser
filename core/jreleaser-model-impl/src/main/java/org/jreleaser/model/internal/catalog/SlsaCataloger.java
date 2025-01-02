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
package org.jreleaser.model.internal.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.mustache.TemplateContext;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.7.0
 */
public final class SlsaCataloger extends AbstractCataloger<SlsaCataloger, org.jreleaser.model.api.catalog.SlsaCataloger> {
    private static final long serialVersionUID = -4942594674615727875L;
    private static final String ATTESTATION_INTOTO = "-attestation.intoto";

    private final Set<String> includes = new LinkedHashSet<>();
    private final Set<String> excludes = new LinkedHashSet<>();

    private String attestationName;
    private Boolean artifacts;
    private Boolean files;
    private Boolean deployables;

    @JsonIgnore
    private final org.jreleaser.model.api.catalog.SlsaCataloger immutable = new org.jreleaser.model.api.catalog.SlsaCataloger() {
        private static final long serialVersionUID = -7836277468968815227L;

        @Override
        public String getAttestationName() {
            return attestationName;
        }

        @Override
        public boolean isArtifacts() {
            return SlsaCataloger.this.isArtifacts();
        }

        @Override
        public boolean isFiles() {
            return SlsaCataloger.this.isFiles();
        }

        @Override
        public boolean isDeployables() {
            return SlsaCataloger.this.isDeployables();
        }

        @Override
        public Set<String> getIncludes() {
            return unmodifiableSet(includes);
        }

        @Override
        public Set<String> getExcludes() {
            return unmodifiableSet(excludes);
        }

        @Override
        public String getGroup() {
            return SlsaCataloger.this.getType();
        }

        @Override
        public String getType() {
            return SlsaCataloger.this.getType();
        }

        @Override
        public Active getActive() {
            return SlsaCataloger.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return SlsaCataloger.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return SlsaCataloger.this.asMap(full);
        }

        @Override
        public String getPrefix() {
            return SlsaCataloger.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(SlsaCataloger.this.getExtraProperties());
        }
    };

    public SlsaCataloger() {
        super("slsa");
    }

    public org.jreleaser.model.api.catalog.SlsaCataloger asImmutable() {
        return immutable;
    }

    @Override
    public void merge(SlsaCataloger source) {
        super.merge(source);
        this.attestationName = merge(this.attestationName, source.attestationName);
        this.artifacts = merge(this.artifacts, source.artifacts);
        this.files = merge(this.files, source.files);
        this.deployables = merge(this.deployables, source.deployables);
        setIncludes(merge(this.includes, source.includes));
        setExcludes(merge(this.excludes, source.excludes));
    }

    @Override
    public boolean isSet() {
        return super.isSet() ||
            isNotBlank(attestationName) ||
            isArtifactsSet() ||
            isFilesSet() ||
            isDeployablesSet() ||
            !includes.isEmpty() ||
            !excludes.isEmpty();
    }

    public String getResolvedAttestationName(JReleaserContext context) {
        TemplateContext props = context.fullProps();
        context.getChangelog().apply(props);
        applyTemplates(props, resolvedExtraProperties());
        String name = resolveTemplate(attestationName, props);
        if (!name.endsWith(ATTESTATION_INTOTO)) {
            name += ATTESTATION_INTOTO;
        }
        return name;
    }

    public String getAttestationName() {
        return attestationName;
    }

    public void setAttestationName(String attestationName) {
        this.attestationName = attestationName;
    }

    public boolean isArtifactsSet() {
        return null != artifacts;
    }

    public boolean isArtifacts() {
        return null == artifacts || artifacts;
    }

    public void setArtifacts(Boolean artifacts) {
        this.artifacts = artifacts;
    }

    public boolean isFiles() {
        return null == files || files;
    }

    public void setFiles(Boolean files) {
        this.files = files;
    }

    public boolean isFilesSet() {
        return null != files;
    }

    public boolean isDeployablesSet() {
        return null != deployables;
    }

    public boolean isDeployables() {
        return null == deployables || deployables;
    }

    public void setDeployables(Boolean deployables) {
        this.deployables = deployables;
    }

    public Set<String> getIncludes() {
        return includes;
    }

    public void setIncludes(Set<String> includes) {
        this.includes.clear();
        this.includes.addAll(includes);
    }

    public Set<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes.clear();
        this.excludes.addAll(excludes);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("attestationName", attestationName);
        props.put("artifacts", isArtifacts());
        props.put("files", isFiles());
        props.put("deployables", isDeployables());
        props.put("includes", includes);
        props.put("excludes", excludes);
    }
}
