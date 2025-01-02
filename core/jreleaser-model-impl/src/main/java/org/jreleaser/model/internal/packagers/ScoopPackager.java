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
package org.jreleaser.model.internal.packagers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.util.PlatformUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.jreleaser.model.Distribution.DistributionType.BINARY;
import static org.jreleaser.model.Distribution.DistributionType.FLAT_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JLINK;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_IMAGE;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_PACKAGE;
import static org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR;
import static org.jreleaser.model.JReleaserOutput.nag;
import static org.jreleaser.model.api.packagers.ScoopPackager.SKIP_SCOOP;
import static org.jreleaser.model.api.packagers.ScoopPackager.TYPE;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.FileType.BAT;
import static org.jreleaser.util.FileType.CMD;
import static org.jreleaser.util.FileType.EXE;
import static org.jreleaser.util.FileType.JAR;
import static org.jreleaser.util.FileType.MSI;
import static org.jreleaser.util.FileType.PS1;
import static org.jreleaser.util.FileType.ZIP;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isFalse;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class ScoopPackager extends AbstractRepositoryPackager<org.jreleaser.model.api.packagers.ScoopPackager, ScoopPackager> {
    private static final Map<org.jreleaser.model.Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();
    private static final long serialVersionUID = 7277751575218470373L;

    static {
        Set<String> extensions = setOf(ZIP.extension());
        SUPPORTED.put(NATIVE_IMAGE, extensions);
        SUPPORTED.put(BINARY, extensions);
        SUPPORTED.put(JAVA_BINARY, extensions);
        SUPPORTED.put(JLINK, extensions);
        SUPPORTED.put(NATIVE_PACKAGE, setOf(MSI.extension()));
        SUPPORTED.put(SINGLE_JAR, setOf(JAR.extension()));
        SUPPORTED.put(FLAT_BINARY, setOf(BAT.extension(), CMD.extension(), EXE.extension(), PS1.extension()));
    }

    private final ScoopRepository repository = new ScoopRepository();
    private String packageName;
    private String checkverUrl;
    private String autoupdateUrl;

    @JsonIgnore
    private final org.jreleaser.model.api.packagers.ScoopPackager immutable = new org.jreleaser.model.api.packagers.ScoopPackager() {
        private static final long serialVersionUID = -3131943938630790438L;

        @Override
        public String getPackageName() {
            return packageName;
        }

        @Override
        public String getCheckverUrl() {
            return checkverUrl;
        }

        @Override
        public String getAutoupdateUrl() {
            return autoupdateUrl;
        }

        @Override
        public org.jreleaser.model.api.packagers.PackagerRepository getRepository() {
            return repository.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.packagers.PackagerRepository getBucket() {
            return getRepository();
        }

        @Override
        public org.jreleaser.model.api.packagers.PackagerRepository getPackagerRepository() {
            return getRepository();
        }

        @Override
        public org.jreleaser.model.api.common.CommitAuthor getCommitAuthor() {
            return ScoopPackager.this.getCommitAuthor().asImmutable();
        }

        @Override
        public String getTemplateDirectory() {
            return ScoopPackager.this.getTemplateDirectory();
        }

        @Override
        public List<String> getSkipTemplates() {
            return unmodifiableList(ScoopPackager.this.getSkipTemplates());
        }

        @Override
        public String getType() {
            return ScoopPackager.this.getType();
        }

        @Override
        public String getDownloadUrl() {
            return ScoopPackager.this.getDownloadUrl();
        }

        @Override
        public boolean supportsPlatform(String platform) {
            return ScoopPackager.this.supportsPlatform(platform);
        }

        @Override
        public boolean supportsDistribution(Distribution.DistributionType distributionType) {
            return ScoopPackager.this.supportsDistribution(distributionType);
        }

        @Override
        public Set<String> getSupportedFileExtensions(Distribution.DistributionType distributionType) {
            return ScoopPackager.this.getSupportedFileExtensions(distributionType);
        }

        @Override
        public Set<Stereotype> getSupportedStereotypes() {
            return ScoopPackager.this.getSupportedStereotypes();
        }

        @Override
        public boolean isSnapshotSupported() {
            return ScoopPackager.this.isSnapshotSupported();
        }

        @Override
        public boolean isContinueOnError() {
            return ScoopPackager.this.isContinueOnError();
        }

        @Override
        public Active getActive() {
            return ScoopPackager.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return ScoopPackager.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(ScoopPackager.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return ScoopPackager.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(ScoopPackager.this.getExtraProperties());
        }
    };

    public ScoopPackager() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.packagers.ScoopPackager asImmutable() {
        return immutable;
    }

    @Override
    public void merge(ScoopPackager source) {
        super.merge(source);
        this.packageName = merge(this.packageName, source.packageName);
        this.checkverUrl = merge(this.checkverUrl, source.checkverUrl);
        this.autoupdateUrl = merge(this.autoupdateUrl, source.autoupdateUrl);
        setRepository(source.repository);
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getCheckverUrl() {
        return checkverUrl;
    }

    public void setCheckverUrl(String checkverUrl) {
        this.checkverUrl = checkverUrl;
    }

    public String getAutoupdateUrl() {
        return autoupdateUrl;
    }

    public void setAutoupdateUrl(String autoupdateUrl) {
        this.autoupdateUrl = autoupdateUrl;
    }

    public ScoopRepository getRepository() {
        return repository;
    }

    public void setRepository(ScoopRepository repository) {
        this.repository.merge(repository);
    }

    @Deprecated
    public ScoopRepository getBucket() {
        return getRepository();
    }

    @Deprecated
    public void setBucket(ScoopRepository repository) {
        nag("scoop.bucket is deprecated since 1.8.0 and will be removed in 2.0.0. Use scoop.repository instead");
        setRepository(repository);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("packageName", packageName);
        props.put("checkverUrl", checkverUrl);
        props.put("autoupdateUrl", autoupdateUrl);
        props.put("repository", repository.asMap(full));
    }

    @Override
    public RepositoryTap getRepositoryTap() {
        return getRepository();
    }

    public PackagerRepository getPackagerRepository() {
        return getRepository();
    }

    @Override
    public boolean supportsPlatform(String platform) {
        return isBlank(platform) || PlatformUtils.isWindows(platform);
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
        return isFalse(artifact.getExtraProperties().get(SKIP_SCOOP));
    }

    public static final class ScoopRepository extends PackagerRepository {
        private static final long serialVersionUID = 5693815301518572010L;

        public ScoopRepository() {
            super("scoop", "scoop");
        }
    }
}
