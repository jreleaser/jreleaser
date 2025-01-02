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
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.catalog.Cataloger;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.EnabledAware;
import org.jreleaser.model.internal.common.ExtraProperties;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public interface SbomCataloger<A extends org.jreleaser.model.api.catalog.sbom.SbomCataloger> extends Cataloger<A> {
    boolean isDistributions();

    void setDistributions(Boolean distributions);

    boolean isDistributionsSet();

    boolean isFiles();

    void setFiles(Boolean files);

    boolean isFilesSet();

    Pack getPack();

    void setPack(Pack pack);

    Set<Artifact> resolveArtifacts(JReleaserContext context, Set<Artifact> candidates);

    Path resolveArchivePath(JReleaserContext context);

    boolean isSkipped(ExtraProperties target);

    class Pack extends AbstractModelObject<SbomCataloger.Pack> implements Domain, EnabledAware {
        private static final long serialVersionUID = -4077304033224132950L;

        private Boolean enabled;
        private String name;

        @JsonIgnore
        private final org.jreleaser.model.api.catalog.sbom.SbomCataloger.Pack immutable = new org.jreleaser.model.api.catalog.sbom.SbomCataloger.Pack() {
            private static final long serialVersionUID = -6963070679384259298L;

            @Override
            public boolean isEnabled() {
                return SbomCataloger.Pack.this.isEnabled();
            }

            @Override
            public String getName() {
                return SbomCataloger.Pack.this.getName();
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return SbomCataloger.Pack.this.asMap(full);
            }
        };

        public org.jreleaser.model.api.catalog.sbom.SbomCataloger.Pack asImmutable() {
            return immutable;
        }

        @Override
        public void merge(SbomCataloger.Pack source) {
            this.enabled = merge(this.enabled, source.enabled);
            this.name = merge(this.name, source.name);
        }

        public boolean isSet() {
            return isEnabledSet();
        }

        @Override
        public boolean isEnabled() {
            return null != enabled && enabled;
        }

        @Override
        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public boolean isEnabledSet() {
            return null != enabled;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            if (!full && !isEnabled()) return Collections.emptyMap();

            Map<String, Object> props = new LinkedHashMap<>();
            props.put("enabled", isEnabled());
            props.put("name", name);

            return props;
        }
    }
}
