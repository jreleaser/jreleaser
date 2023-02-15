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
package org.jreleaser.model.internal.packagers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.project.Project;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JLINK;
import static org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR;
import static org.jreleaser.model.api.packagers.JbangPackager.TYPE;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class JbangPackager extends AbstractRepositoryPackager<org.jreleaser.model.api.packagers.JbangPackager, JbangPackager> {
    private static final Map<org.jreleaser.model.Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();
    private static final long serialVersionUID = 7475917126983967203L;

    static {
        SUPPORTED.put(JAVA_BINARY, emptySet());
        SUPPORTED.put(JLINK, emptySet());
        SUPPORTED.put(SINGLE_JAR, emptySet());
    }

    private final JbangRepository catalog = new JbangRepository();
    private String alias;

    @JsonIgnore
    private final org.jreleaser.model.api.packagers.JbangPackager immutable = new org.jreleaser.model.api.packagers.JbangPackager() {
        private static final long serialVersionUID = -3477595656026714700L;

        @Override
        public String getAlias() {
            return alias;
        }

        @Override
        public org.jreleaser.model.api.packagers.PackagerRepository getCatalog() {
            return catalog.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.packagers.PackagerRepository getPackagerRepository() {
            return getCatalog();
        }

        @Override
        public org.jreleaser.model.api.common.CommitAuthor getCommitAuthor() {
            return JbangPackager.this.getCommitAuthor().asImmutable();
        }

        @Override
        public String getTemplateDirectory() {
            return JbangPackager.this.getTemplateDirectory();
        }

        @Override
        public List<String> getSkipTemplates() {
            return unmodifiableList(JbangPackager.this.getSkipTemplates());
        }

        @Override
        public String getType() {
            return JbangPackager.this.getType();
        }

        @Override
        public String getDownloadUrl() {
            return JbangPackager.this.getDownloadUrl();
        }

        @Override
        public boolean supportsPlatform(String platform) {
            return JbangPackager.this.supportsPlatform(platform);
        }

        @Override
        public boolean supportsDistribution(Distribution.DistributionType distributionType) {
            return JbangPackager.this.supportsDistribution(distributionType);
        }

        @Override
        public Set<String> getSupportedFileExtensions(Distribution.DistributionType distributionType) {
            return JbangPackager.this.getSupportedFileExtensions(distributionType);
        }

        @Override
        public Set<Stereotype> getSupportedStereotypes() {
            return JbangPackager.this.getSupportedStereotypes();
        }

        @Override
        public boolean isSnapshotSupported() {
            return JbangPackager.this.isSnapshotSupported();
        }

        @Override
        public boolean isContinueOnError() {
            return JbangPackager.this.isContinueOnError();
        }

        @Override
        public Active getActive() {
            return JbangPackager.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return JbangPackager.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(JbangPackager.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return JbangPackager.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(JbangPackager.this.getExtraProperties());
        }
    };

    public JbangPackager() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.packagers.JbangPackager asImmutable() {
        return immutable;
    }

    @Override
    public void merge(JbangPackager source) {
        super.merge(source);
        this.alias = merge(this.alias, source.alias);
        setCatalog(source.catalog);
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public JbangRepository getCatalog() {
        return catalog;
    }

    public void setCatalog(JbangRepository tap) {
        this.catalog.merge(tap);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("alias", alias);
        props.put("catalog", catalog.asMap(full));
    }

    @Override
    public RepositoryTap getRepositoryTap() {
        return getPackagerRepository();
    }

    public PackagerRepository getPackagerRepository() {
        return getCatalog();
    }

    @Override
    public boolean supportsPlatform(String platform) {
        return true;
    }

    @Override
    public boolean supportsDistribution(org.jreleaser.model.Distribution.DistributionType distributionType) {
        return SUPPORTED.containsKey(distributionType);
    }

    @Override
    public Set<String> getSupportedFileExtensions(org.jreleaser.model.Distribution.DistributionType distributionType) {
        return unmodifiableSet(SUPPORTED.getOrDefault(distributionType, emptySet()));
    }

    @Override
    protected boolean isNotSkipped(Artifact artifact) {
        return true;
    }

    @Override
    public boolean isSnapshotSupported() {
        return true;
    }

    public static final class JbangRepository extends PackagerRepository {
        private static final long serialVersionUID = -800139841211856966L;

        public JbangRepository() {
            super("jbang", "jbang-catalog");
        }

        @Override
        public String getResolvedName() {
            return getTapName();
        }

        @Override
        public boolean resolveEnabledWithSnapshot(Project project) {
            enabledSet(null != getActive() && getActive().check(project));
            return isEnabled();
        }
    }
}
