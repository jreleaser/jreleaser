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
import static org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.FileType.JAR;
import static org.jreleaser.util.FileType.MSI;
import static org.jreleaser.util.FileType.ZIP;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isFalse;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Scoop extends AbstractRepositoryPackager<Scoop> {
    public static final String TYPE = "scoop";
    public static final String SKIP_SCOOP = "skipScoop";

    private static final Map<Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();

    static {
        Set<String> extensions = setOf(ZIP.extension());
        SUPPORTED.put(BINARY, extensions);
        SUPPORTED.put(JAVA_BINARY, extensions);
        SUPPORTED.put(JLINK, extensions);
        SUPPORTED.put(NATIVE_IMAGE, extensions);
        SUPPORTED.put(NATIVE_PACKAGE, setOf(MSI.extension()));
        SUPPORTED.put(SINGLE_JAR, setOf(JAR.extension()));
    }

    private final ScoopBucket bucket = new ScoopBucket();
    private String packageName;
    private String checkverUrl;
    private String autoupdateUrl;

    public Scoop() {
        super(TYPE);
    }

    @Override
    public void freeze() {
        super.freeze();
        bucket.freeze();
    }

    @Override
    public void merge(Scoop scoop) {
        freezeCheck();
        super.merge(scoop);
        this.packageName = merge(this.packageName, scoop.packageName);
        this.checkverUrl = merge(this.checkverUrl, scoop.checkverUrl);
        this.autoupdateUrl = merge(this.autoupdateUrl, scoop.autoupdateUrl);
        setBucket(scoop.bucket);
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        freezeCheck();
        this.packageName = packageName;
    }

    public String getCheckverUrl() {
        return checkverUrl;
    }

    public void setCheckverUrl(String checkverUrl) {
        freezeCheck();
        this.checkverUrl = checkverUrl;
    }

    public String getAutoupdateUrl() {
        return autoupdateUrl;
    }

    public void setAutoupdateUrl(String autoupdateUrl) {
        freezeCheck();
        this.autoupdateUrl = autoupdateUrl;
    }

    public ScoopBucket getBucket() {
        return bucket;
    }

    public void setBucket(ScoopBucket bucket) {
        this.bucket.merge(bucket);
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
    public boolean supportsDistribution(Distribution distribution) {
        return SUPPORTED.containsKey(distribution.getType());
    }

    @Override
    public Set<String> getSupportedExtensions(Distribution distribution) {
        return Collections.unmodifiableSet(SUPPORTED.getOrDefault(distribution.getType(), Collections.emptySet()));
    }

    @Override
    protected boolean isNotSkipped(Artifact artifact) {
        return isFalse(artifact.getExtraProperties().get(SKIP_SCOOP));
    }

    public static class ScoopBucket extends AbstractRepositoryTap<ScoopBucket> {
        public ScoopBucket() {
            super("scoop", "scoop");
        }
    }
}
