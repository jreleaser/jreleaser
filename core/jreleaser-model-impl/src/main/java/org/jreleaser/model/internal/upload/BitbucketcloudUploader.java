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
package org.jreleaser.model.internal.upload;

import org.jreleaser.model.Active;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.mustache.TemplateContext;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.mustache.Templates.resolveTemplate;

/**
 * @author Hasnae Rehioui
 * @since 1.7.0
 */
public class BitbucketcloudUploader extends AbstractGitPackageUploader<org.jreleaser.model.api.upload.BitbucketcloudUploader, BitbucketcloudUploader> {
    private static final long serialVersionUID = -2516884046138936300L;
    private static final String DOWNLOAD_URL = "https://{{host}}/2.0/repositories/{{projectIdentifier}}/{{packageName}}/downloads/{{artifactFile}}";

    private String projectIdentifier;

    private final org.jreleaser.model.api.upload.BitbucketcloudUploader immutable = new org.jreleaser.model.api.upload.BitbucketcloudUploader() {
        private static final long serialVersionUID = 3571755247298525266L;

        @Override
        public String getHost() {
            return BitbucketcloudUploader.this.getHost();
        }

        @Override
        public String getToken() {
            return BitbucketcloudUploader.this.getToken();
        }

        @Override
        public String getPackageName() {
            return BitbucketcloudUploader.this.getPackageName();
        }

        @Override
        public String getPackageVersion() {
            return BitbucketcloudUploader.this.getPackageVersion();
        }

        @Override
        public String getProjectIdentifier() {
            return BitbucketcloudUploader.this.getProjectIdentifier();
        }

        @Override
        public String getType() {
            return BitbucketcloudUploader.this.getType();
        }

        @Override
        public String getName() {
            return BitbucketcloudUploader.this.getName();
        }

        @Override
        public boolean isSnapshotSupported() {
            return BitbucketcloudUploader.this.isSnapshotSupported();
        }

        @Override
        public boolean isArtifacts() {
            return BitbucketcloudUploader.this.isArtifacts();
        }

        @Override
        public boolean isFiles() {
            return BitbucketcloudUploader.this.isFiles();
        }

        @Override
        public boolean isSignatures() {
            return BitbucketcloudUploader.this.isSignatures();
        }

        @Override
        public boolean isChecksums() {
            return BitbucketcloudUploader.this.isChecksums();
        }

        @Override
        public boolean isCatalogs() {
            return BitbucketcloudUploader.this.isCatalogs();
        }

        @Override
        public Active getActive() {
            return BitbucketcloudUploader.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return BitbucketcloudUploader.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(BitbucketcloudUploader.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return BitbucketcloudUploader.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(BitbucketcloudUploader.this.getExtraProperties());
        }

        @Override
        public Integer getConnectTimeout() {
            return BitbucketcloudUploader.this.getConnectTimeout();
        }

        @Override
        public Integer getReadTimeout() {
            return BitbucketcloudUploader.this.getReadTimeout();
        }
    };

    public BitbucketcloudUploader() {
        super(org.jreleaser.model.api.upload.BitbucketcloudUploader.TYPE);
        setHost("api.bitbucket.org");
    }

    public String getProjectIdentifier() {
        return projectIdentifier;
    }

    public void setProjectIdentifier(String projectIdentifier) {
        this.projectIdentifier = projectIdentifier;
    }

    @Override
    public org.jreleaser.model.api.upload.BitbucketcloudUploader asImmutable() {
        return immutable;
    }

    @Override
    public String getResolvedDownloadUrl(TemplateContext props, Artifact artifact) {
        TemplateContext p = new TemplateContext(artifactProps(props, artifact));
        p.setAll(resolvedExtraProperties());
        p.set("host", getHost());
        p.set("projectIdentifier", getProjectIdentifier());
        p.set("packageName", getPackageName());
        return resolveTemplate(DOWNLOAD_URL, p);
    }
}
