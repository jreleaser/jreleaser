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

import java.util.Map;

import static org.jreleaser.util.Constants.HIDE;
import static org.jreleaser.util.Constants.UNSET;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Chocolatey extends AbstractRepositoryTool {
    public static final String NAME = "chocolatey";
    public static final String CHOCOLATEY_API_KEY = "CHOCOLATEY_API_KEY";
    public static final String SKIP_CHOCOLATEY = "skipChocolatey";
    public static final String DEFAULT_CHOCOLATEY_PUSH_URL = "https://push.chocolatey.org/";

    private final ChocolateyBucket bucket = new ChocolateyBucket();
    private String packageName;
    private String username;
    private String apiKey;
    private String title;
    private String iconUrl;
    private String source;
    private Boolean remoteBuild;

    public Chocolatey() {
        super(NAME);
    }

    void setAll(Chocolatey choco) {
        super.setAll(choco);
        this.packageName = choco.packageName;
        this.username = choco.username;
        this.apiKey = choco.apiKey;
        this.title = choco.title;
        this.iconUrl = choco.iconUrl;
        this.source = choco.source;
        this.remoteBuild = choco.remoteBuild;
        setBucket(choco.bucket);
    }

    public String getResolvedApiKey() {
        return Env.resolve(CHOCOLATEY_API_KEY, apiKey);
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
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
        return remoteBuild != null && remoteBuild;
    }

    public void setRemoteBuild(Boolean remoteBuild) {
        this.remoteBuild = remoteBuild;
    }

    public boolean isRemoteBuildSet() {
        return remoteBuild != null;
    }

    public ChocolateyBucket getBucket() {
        return bucket;
    }

    public void setBucket(ChocolateyBucket bucket) {
        this.bucket.setAll(bucket);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("packageName", packageName);
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
        return distribution.getType() != Distribution.DistributionType.SINGLE_JAR &&
            distribution.getType() != Distribution.DistributionType.NATIVE_PACKAGE;
    }

    public static class ChocolateyBucket extends AbstractRepositoryTap {
        public ChocolateyBucket() {
            super("chocolatey", "chocolatey-bucket");
        }
    }
}
