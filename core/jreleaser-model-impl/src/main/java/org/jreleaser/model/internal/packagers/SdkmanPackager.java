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
import org.jreleaser.model.Sdkman;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.TimeoutAware;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.model.Distribution.DistributionType.BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JLINK;
import static org.jreleaser.model.api.packagers.SdkmanPackager.SKIP_SDKMAN;
import static org.jreleaser.model.api.packagers.SdkmanPackager.TYPE;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.FileType.ZIP;
import static org.jreleaser.util.StringUtils.isFalse;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
public final class SdkmanPackager extends AbstractPackager<org.jreleaser.model.api.packagers.SdkmanPackager, SdkmanPackager> implements TimeoutAware {
    private static final Map<org.jreleaser.model.Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();
    private static final long serialVersionUID = 3632422575248447545L;

    static {
        Set<String> extensions = setOf(ZIP.extension());
        SUPPORTED.put(JAVA_BINARY, extensions);
        SUPPORTED.put(JLINK, extensions);
        SUPPORTED.put(BINARY, extensions);
    }

    private Sdkman.Command command;
    private String candidate;
    private String releaseNotesUrl;
    private String consumerKey;
    private String consumerToken;
    private int connectTimeout;
    private int readTimeout;
    @JsonIgnore
    private boolean published;

    @JsonIgnore
    private final org.jreleaser.model.api.packagers.SdkmanPackager immutable = new org.jreleaser.model.api.packagers.SdkmanPackager() {
        private static final long serialVersionUID = 3123151880557373320L;

        @Override
        public String getCandidate() {
            return candidate;
        }

        @Override
        public String getReleaseNotesUrl() {
            return releaseNotesUrl;
        }

        @Override
        public Sdkman.Command getCommand() {
            return command;
        }

        @Override
        public String getConsumerKey() {
            return consumerKey;
        }

        @Override
        public String getConsumerToken() {
            return consumerToken;
        }

        @Override
        public boolean isPublished() {
            return SdkmanPackager.this.isPublished();
        }

        @Override
        public Integer getConnectTimeout() {
            return connectTimeout;
        }

        @Override
        public Integer getReadTimeout() {
            return readTimeout;
        }

        @Override
        public String getType() {
            return SdkmanPackager.this.getType();
        }

        @Override
        public String getDownloadUrl() {
            return SdkmanPackager.this.getDownloadUrl();
        }

        @Override
        public boolean supportsPlatform(String platform) {
            return SdkmanPackager.this.supportsPlatform(platform);
        }

        @Override
        public boolean supportsDistribution(Distribution.DistributionType distributionType) {
            return SdkmanPackager.this.supportsDistribution(distributionType);
        }

        @Override
        public Set<String> getSupportedFileExtensions(Distribution.DistributionType distributionType) {
            return SdkmanPackager.this.getSupportedFileExtensions(distributionType);
        }

        @Override
        public Set<Stereotype> getSupportedStereotypes() {
            return SdkmanPackager.this.getSupportedStereotypes();
        }

        @Override
        public boolean isSnapshotSupported() {
            return SdkmanPackager.this.isSnapshotSupported();
        }

        @Override
        public boolean isContinueOnError() {
            return SdkmanPackager.this.isContinueOnError();
        }

        @Override
        public Active getActive() {
            return SdkmanPackager.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return SdkmanPackager.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(SdkmanPackager.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return SdkmanPackager.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(SdkmanPackager.this.getExtraProperties());
        }
    };

    public SdkmanPackager() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.packagers.SdkmanPackager asImmutable() {
        return immutable;
    }

    @Override
    public void merge(SdkmanPackager source) {
        super.merge(source);
        this.candidate = merge(this.candidate, source.candidate);
        this.releaseNotesUrl = merge(this.releaseNotesUrl, source.releaseNotesUrl);
        this.command = merge(this.command, source.command);
        this.consumerKey = merge(this.consumerKey, source.consumerKey);
        this.consumerToken = merge(this.consumerToken, source.consumerToken);
        this.connectTimeout = merge(this.connectTimeout, source.connectTimeout);
        this.readTimeout = merge(this.readTimeout, source.readTimeout);
        this.published = merge(this.published, source.published);
    }

    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }

    public String getReleaseNotesUrl() {
        return releaseNotesUrl;
    }

    public void setReleaseNotesUrl(String releaseNotesUrl) {
        this.releaseNotesUrl = releaseNotesUrl;
    }

    public Sdkman.Command getCommand() {
        return command;
    }

    public void setCommand(Sdkman.Command command) {
        this.command = command;
    }

    public void setCommand(String str) {
        setCommand(Sdkman.Command.of(str));
    }

    public boolean isCommandSet() {
        return null != command;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getConsumerToken() {
        return consumerToken;
    }

    public void setConsumerToken(String consumerToken) {
        this.consumerToken = consumerToken;
    }

    @Override
    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public Integer getReadTimeout() {
        return readTimeout;
    }

    @Override
    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("candidate", candidate);
        props.put("command", command);
        props.put("releaseNotesUrl", releaseNotesUrl);
        props.put("connectTimeout", connectTimeout);
        props.put("readTimeout", readTimeout);
        props.put("consumerKey", isNotBlank(consumerKey) ? HIDE : UNSET);
        props.put("consumerToken", isNotBlank(consumerToken) ? HIDE : UNSET);
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
        return isFalse(artifact.getExtraProperties().get(SKIP_SDKMAN));
    }
}
