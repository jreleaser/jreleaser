/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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

import org.jreleaser.util.Env;
import org.jreleaser.util.PlatformUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.model.Distribution.DistributionType.BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JLINK;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_IMAGE;
import static org.jreleaser.model.Distribution.DistributionType.NATIVE_PACKAGE;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.Constants.HIDE;
import static org.jreleaser.util.Constants.UNSET;
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
public class Chocolatey extends AbstractRepositoryPackager<Chocolatey> {
    public static final String CHOCOLATEY_API_KEY = "CHOCOLATEY_API_KEY";
    public static final String TYPE = "chocolatey";
    public static final String SKIP_CHOCOLATEY = "skipChocolatey";
    public static final String DEFAULT_CHOCOLATEY_PUSH_URL = "https://push.chocolatey.org/";

    private static final Map<Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();

    static {
        Set<String> extensions = setOf(ZIP.extension());
        SUPPORTED.put(BINARY, extensions);
        SUPPORTED.put(JAVA_BINARY, extensions);
        SUPPORTED.put(JLINK, extensions);
        SUPPORTED.put(NATIVE_IMAGE, extensions);
        SUPPORTED.put(NATIVE_PACKAGE, setOf(EXE.extension(), MSI.extension()));
    }

    private final ChocolateyBucket bucket = new ChocolateyBucket();
    private String packageName;
    private String packageVersion;
    private String username;
    private String apiKey;
    private String title;
    private String iconUrl;
    private String source;
    private Boolean remoteBuild;

    public Chocolatey() {
        super(TYPE);
    }

    @Override
    public void freeze() {
        super.freeze();
        bucket.freeze();
    }

    @Override
    public void merge(Chocolatey choco) {
        freezeCheck();
        super.merge(choco);
        this.packageName = merge(this.packageName, choco.packageName);
        this.packageVersion = merge(this.packageVersion, choco.packageVersion);
        this.username = merge(this.username, choco.username);
        this.apiKey = merge(this.apiKey, choco.apiKey);
        this.title = merge(this.title, choco.title);
        this.iconUrl = merge(this.iconUrl, choco.iconUrl);
        this.source = merge(this.source, choco.source);
        this.remoteBuild = merge(this.remoteBuild, choco.remoteBuild);
        setBucket(choco.bucket);
    }

    public String getResolvedApiKey() {
        return Env.env(CHOCOLATEY_API_KEY, apiKey);
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        freezeCheck();
        this.packageName = packageName;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        freezeCheck();
        this.packageVersion = packageVersion;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        freezeCheck();
        this.username = username;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        freezeCheck();
        this.apiKey = apiKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        freezeCheck();
        this.title = title;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        freezeCheck();
        this.iconUrl = iconUrl;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        freezeCheck();
        this.source = source;
    }

    public boolean isRemoteBuild() {
        return remoteBuild != null && remoteBuild;
    }

    public void setRemoteBuild(Boolean remoteBuild) {
        freezeCheck();
        this.remoteBuild = remoteBuild;
    }

    public boolean isRemoteBuildSet() {
        return remoteBuild != null;
    }

    public ChocolateyBucket getBucket() {
        return bucket;
    }

    public void setBucket(ChocolateyBucket bucket) {
        freezeCheck();
        this.bucket.merge(bucket);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("packageName", packageName);
        props.put("packageVersion", packageVersion);
        props.put("username", username);
        props.put("apiKey", isNotBlank(getResolvedApiKey()) ? HIDE : UNSET);
        props.put("remoteBuild", isRemoteBuild());
        props.put("title", title);
        props.put("iconUrl", iconUrl);
        props.put("source", source);
        props.put("bucket", bucket.asMap(full));
    }

    @Override
    public RepositoryTap getRepositoryTap() {
        return bucket;
    }

    @Override
    public boolean supportsPlatform(String platform) {
        return isBlank(platform) || (PlatformUtils.isWindows(platform) && PlatformUtils.isIntel(platform));
    }

    @Override
    public boolean supportsDistribution(Distribution distribution) {
        return SUPPORTED.containsKey(distribution.getType());
    }

    @Override
    public Set<String> getSupportedExtensions(Distribution distribution) {
        return Collections.unmodifiableSet(SUPPORTED.getOrDefault(distribution.getType(), Collections.emptySet()));
    }

    @Override
    protected boolean isNotSkipped(Artifact artifact) {
        return isFalse(artifact.getExtraProperties().get(SKIP_CHOCOLATEY));
    }

    public static class ChocolateyBucket extends AbstractRepositoryTap<ChocolateyBucket> {
        public ChocolateyBucket() {
            super("chocolatey", "chocolatey-bucket");
        }
    }
}
