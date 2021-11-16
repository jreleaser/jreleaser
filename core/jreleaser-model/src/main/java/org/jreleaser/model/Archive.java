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

import org.jreleaser.util.PlatformUtils;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public class Archive extends AbstractAssembler {
    public static final String TYPE = "archive";

    private final Set<Format> formats = new LinkedHashSet<>();

    private String archiveName;
    private Boolean attachPlatform;
    private Distribution.DistributionType distributionType;

    public Archive() {
        super(TYPE);
    }

    @Override
    public Distribution.DistributionType getDistributionType() {
        return distributionType;
    }

    public void setDistributionType(Distribution.DistributionType distributionType) {
        this.distributionType = distributionType;
    }

    public void setDistributionType(String distributionType) {
        this.distributionType = Distribution.DistributionType.of(distributionType);
    }

    void setAll(Archive archive) {
        super.setAll(archive);
        this.archiveName = archive.archiveName;
        this.distributionType = archive.distributionType;
        this.attachPlatform = archive.attachPlatform;
        setFormats(archive.formats);
    }

    public String getResolvedArchiveName(JReleaserContext context) {
        Map<String, Object> props = context.props();
        props.putAll(props());
        String result = applyTemplate(archiveName, props);
        if (isAttachPlatform()) {
            result += "-" + PlatformUtils.getCurrentFull();
        }
        return result;
    }

    public String getArchiveName() {
        return archiveName;
    }

    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
    }

    public boolean isAttachPlatformSet() {
        return attachPlatform;
    }

    public boolean isAttachPlatform() {
        return attachPlatform != null && attachPlatform;
    }

    public void setAttachPlatform(Boolean attachPlatform) {
        this.attachPlatform = attachPlatform;
    }

    public Set<Format> getFormats() {
        return formats;
    }

    public void setFormats(Set<Format> formats) {
        this.formats.clear();
        this.formats.addAll(formats);
    }

    public void addFormat(Format format) {
        this.formats.add(format);
    }

    public void addFormat(String str) {
        this.formats.add(Format.of(str));
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("archiveName", archiveName);
        props.put("distributionType", distributionType);
        props.put("attachPlatform", isAttachPlatform());
        props.put("formats", formats);
    }

    public enum Format {
        ZIP("zip"),
        TAR("tar"),
        TGZ("tgz"),
        TAR_GZ("tar.gz");

        private final String extension;

        Format(String extension) {
            this.extension = extension;
        }

        public String extension() {
            return this.extension;
        }

        @Override
        public String toString() {
            return extension();
        }

        public static org.jreleaser.model.Archive.Format of(String str) {
            if (isBlank(str)) return null;
            return org.jreleaser.model.Archive.Format
                .valueOf(str.toUpperCase().trim()
                    .replace(".", "_"));
        }
    }
}
