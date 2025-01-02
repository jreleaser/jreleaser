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
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.Distribution.DistributionType.BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JLINK;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_IMAGE;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_PACKAGE;
import static org.jreleaser.model.JReleaserOutput.nag;
import static org.jreleaser.model.api.packagers.ChocolateyPackager.SKIP_CHOCOLATEY;
import static org.jreleaser.model.api.packagers.ChocolateyPackager.TYPE;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.FileType.EXE;
import static org.jreleaser.util.FileType.MSI;
import static org.jreleaser.util.FileType.ZIP;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isFalse;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class ChocolateyPackager extends AbstractRepositoryPackager<org.jreleaser.model.api.packagers.ChocolateyPackager, ChocolateyPackager> {
    private static final Map<org.jreleaser.model.Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();
    private static final long serialVersionUID = 7817273679163722060L;

    static {
        Set<String> extensions = setOf(ZIP.extension());
        SUPPORTED.put(NATIVE_IMAGE, extensions);
        SUPPORTED.put(BINARY, extensions);
        SUPPORTED.put(JAVA_BINARY, extensions);
        SUPPORTED.put(JLINK, extensions);
        SUPPORTED.put(NATIVE_PACKAGE, setOf(EXE.extension(), MSI.extension()));
        // SUPPORTED.put(FLAT_BINARY, emptySet());
    }

    private final ChocolateyRepository repository = new ChocolateyRepository();
    private String packageName;
    private String packageVersion;
    private String username;
    private String apiKey;
    private String title;
    private String iconUrl;
    private String source;
    private Boolean remoteBuild;

    @JsonIgnore
    private final org.jreleaser.model.api.packagers.ChocolateyPackager immutable = new org.jreleaser.model.api.packagers.ChocolateyPackager() {
        private static final long serialVersionUID = 288367150761378503L;

        @Override
        public String getPackageName() {
            return packageName;
        }

        @Override
        public String getPackageVersion() {
            return packageVersion;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getApiKey() {
            return apiKey;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getIconUrl() {
            return iconUrl;
        }

        @Override
        public String getSource() {
            return source;
        }

        @Override
        public boolean isRemoteBuild() {
            return ChocolateyPackager.this.isRemoteBuild();
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
            return ChocolateyPackager.this.getCommitAuthor().asImmutable();
        }

        @Override
        public String getTemplateDirectory() {
            return ChocolateyPackager.this.getTemplateDirectory();
        }

        @Override
        public List<String> getSkipTemplates() {
            return unmodifiableList(ChocolateyPackager.this.getSkipTemplates());
        }

        @Override
        public String getType() {
            return ChocolateyPackager.this.getType();
        }

        @Override
        public String getDownloadUrl() {
            return ChocolateyPackager.this.getDownloadUrl();
        }

        @Override
        public boolean supportsPlatform(String platform) {
            return ChocolateyPackager.this.supportsPlatform(platform);
        }

        @Override
        public boolean supportsDistribution(Distribution.DistributionType distributionType) {
            return ChocolateyPackager.this.supportsDistribution(distributionType);
        }

        @Override
        public Set<String> getSupportedFileExtensions(Distribution.DistributionType distributionType) {
            return ChocolateyPackager.this.getSupportedFileExtensions(distributionType);
        }

        @Override
        public Set<Stereotype> getSupportedStereotypes() {
            return ChocolateyPackager.this.getSupportedStereotypes();
        }

        @Override
        public boolean isSnapshotSupported() {
            return ChocolateyPackager.this.isSnapshotSupported();
        }

        @Override
        public boolean isContinueOnError() {
            return ChocolateyPackager.this.isContinueOnError();
        }

        @Override
        public Active getActive() {
            return ChocolateyPackager.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return ChocolateyPackager.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(ChocolateyPackager.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return ChocolateyPackager.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(ChocolateyPackager.this.getExtraProperties());
        }
    };

    public ChocolateyPackager() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.packagers.ChocolateyPackager asImmutable() {
        return immutable;
    }

    @Override
    public void merge(ChocolateyPackager source) {
        super.merge(source);
        this.packageName = merge(this.packageName, source.packageName);
        this.packageVersion = merge(this.packageVersion, source.packageVersion);
        this.username = merge(this.username, source.username);
        this.apiKey = merge(this.apiKey, source.apiKey);
        this.title = merge(this.title, source.title);
        this.iconUrl = merge(this.iconUrl, source.iconUrl);
        this.source = merge(this.source, source.source);
        this.remoteBuild = merge(this.remoteBuild, source.remoteBuild);
        setRepository(source.repository);
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isRemoteBuild() {
        return null != remoteBuild && remoteBuild;
    }

    public void setRemoteBuild(Boolean remoteBuild) {
        this.remoteBuild = remoteBuild;
    }

    public boolean isRemoteBuildSet() {
        return null != remoteBuild;
    }

    public ChocolateyRepository getRepository() {
        return repository;
    }

    public void setRepository(ChocolateyRepository repository) {
        this.repository.merge(repository);
    }

    @Deprecated
    public ChocolateyRepository getBucket() {
        return getRepository();
    }

    @Deprecated
    public void setBucket(ChocolateyRepository repository) {
        nag("chocolatey.bucket is deprecated since 1.8.0 and will be removed in 2.0.0. Use chocolatey.repository instead");
        setRepository(repository);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("packageName", packageName);
        props.put("packageVersion", packageVersion);
        props.put("username", username);
        props.put("apiKey", isNotBlank(apiKey) ? HIDE : UNSET);
        props.put("remoteBuild", isRemoteBuild());
        props.put("title", title);
        props.put("iconUrl", iconUrl);
        props.put("source", source);
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
        return isBlank(platform) ||
            PlatformUtils.isWindows(platform) && PlatformUtils.isIntel(platform);
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
        return isFalse(artifact.getExtraProperties().get(SKIP_CHOCOLATEY));
    }

    public static final class ChocolateyRepository extends PackagerRepository {
        private static final long serialVersionUID = 3536191707611666696L;

        public ChocolateyRepository() {
            super("chocolatey", "chocolatey-bucket");
        }
    }
}
