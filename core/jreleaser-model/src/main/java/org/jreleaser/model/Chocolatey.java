/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import org.jreleaser.util.PlatformUtils;

import java.util.Map;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Chocolatey extends AbstractTool {
    public static final String NAME = "chocolatey";

    private String username;
    private Boolean remoteBuild;
    private ChocolateyBucket bucket = new ChocolateyBucket();

    public Chocolatey() {
        super(NAME);
    }

    void setAll(Chocolatey choco) {
        super.setAll(choco);
        this.username = choco.username;
        this.remoteBuild = choco.remoteBuild;
        this.bucket.setAll(choco.bucket);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean isRemoteBuild() {
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
        this.bucket = bucket;
    }

    @Override
    protected void asMap(Map<String, Object> props) {
        props.put("username", username);
        props.put("remoteBuild", isRemoteBuild());
        props.put("bucket", bucket.asMap());
    }

    @Override
    public RepositoryTap getRepositoryTap() {
        return bucket;
    }

    @Override
    public boolean supportsPlatform(String platform) {
        return isBlank(platform) || PlatformUtils.isWindows(platform);
    }
}
