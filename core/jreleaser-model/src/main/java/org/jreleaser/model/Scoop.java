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
package org.jreleaser.model;

import org.jreleaser.util.FileType;
import org.jreleaser.util.PlatformUtils;

import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Scoop extends AbstractRepositoryTool {
    public static final String NAME = "scoop";
    private final ScoopBucket bucket = new ScoopBucket();
    private String packageName;
    private String checkverUrl;
    private String autoupdateUrl;

    public Scoop() {
        super(NAME);
    }

    void setAll(Scoop scoop) {
        super.setAll(scoop);
        this.packageName = scoop.packageName;
        this.checkverUrl = scoop.checkverUrl;
        this.autoupdateUrl = scoop.autoupdateUrl;
        setBucket(scoop.bucket);
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

    public ScoopBucket getBucket() {
        return bucket;
    }

    public void setBucket(ScoopBucket bucket) {
        this.bucket.setAll(bucket);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("packageName", packageName);
        props.put("checkverUrl", checkverUrl);
        props.put("autoupdateUrl", autoupdateUrl);
        props.put("bucket", bucket.asMap(full));
    }

    @Override
    public RepositoryTap getRepositoryTap() {
        return bucket;
    }

    @Override
    public boolean supportsPlatform(String platform) {
        return isBlank(platform) || PlatformUtils.isWindows(platform);
    }

    @Override
    public Set<String> getSupportedExtensions() {
        Set<String> set = super.getSupportedExtensions();
        set.add(FileType.JAR.extension());
        return set;
    }

    public static class ScoopBucket extends AbstractRepositoryTap {
        public ScoopBucket() {
            super("scoop", "scoop");
        }
    }
}
