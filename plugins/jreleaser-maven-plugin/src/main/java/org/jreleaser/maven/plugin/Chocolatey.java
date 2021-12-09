/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
package org.jreleaser.maven.plugin;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Chocolatey extends AbstractRepositoryTool {
    private final Bucket bucket = new Bucket();
    private String packageName;
    private String username;
    private String apiKey;
    private String title;
    private String iconUrl;
    private Boolean remoteBuild;

    void setAll(Chocolatey choco) {
        super.setAll(choco);
        this.packageName = choco.packageName;
        this.username = choco.username;
        this.apiKey = choco.apiKey;
        this.title = choco.title;
        this.iconUrl = choco.iconUrl;
        this.remoteBuild = choco.remoteBuild;
        setBucket(choco.bucket);
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

    public Boolean getRemoteBuild() {
        return remoteBuild;
    }

    public boolean isRemoteBuild() {
        return remoteBuild != null && remoteBuild;
    }

    public void setRemoteBuild(Boolean remoteBuild) {
        this.remoteBuild = remoteBuild;
    }

    public Bucket getBucket() {
        return bucket;
    }

    public void setBucket(Bucket bucket) {
        this.bucket.setAll(bucket);
    }

    @Override
    public boolean isSet() {
        return super.isSet() ||
            isNotBlank(packageName) ||
            isNotBlank(username) ||
            isNotBlank(apiKey) ||
            isNotBlank(title) ||
            isNotBlank(iconUrl) ||
            null != remoteBuild ||
            bucket.isSet();
    }
}
