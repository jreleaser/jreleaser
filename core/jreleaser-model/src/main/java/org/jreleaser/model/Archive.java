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

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public class Archive extends AbstractAssembler<Archive> {
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
        freezeCheck();
        this.distributionType = distributionType;
    }

    public void setDistributionType(String distributionType) {
        freezeCheck();
        this.distributionType = Distribution.DistributionType.of(distributionType);
    }

    @Override
    public void merge(Archive archive) {
        freezeCheck();
        super.merge(archive);
        this.archiveName = merge(archive.archiveName, archive.archiveName);
        this.distributionType = merge(archive.distributionType, archive.distributionType);
        this.attachPlatform = merge(archive.attachPlatform, archive.attachPlatform);
        setFormats(merge(this.formats, archive.formats));
    }

    public String getResolvedArchiveName(JReleaserContext context) {
        Map<String, Object> props = context.fullProps();
        props.putAll(props());
        String result = resolveTemplate(archiveName, props);
        if (isAttachPlatform()) {
            result += "-" + getPlatform().applyReplacements(PlatformUtils.getCurrentFull());
        }
        return result;
    }

    public String getArchiveName() {
        return archiveName;
    }

    public void setArchiveName(String archiveName) {
        freezeCheck();
        this.archiveName = archiveName;
    }

    public boolean isAttachPlatformSet() {
        return attachPlatform != null;
    }

    public boolean isAttachPlatform() {
        return attachPlatform != null && attachPlatform;
    }

    public void setAttachPlatform(Boolean attachPlatform) {
        freezeCheck();
        this.attachPlatform = attachPlatform;
    }

    public Set<Format> getFormats() {
        return freezeWrap(formats);
    }

    public void setFormats(Set<Format> formats) {
        freezeCheck();
        this.formats.clear();
        this.formats.addAll(formats);
    }

    public void addFormat(Format format) {
        freezeCheck();
        this.formats.add(format);
    }

    public void addFormat(String str) {
        freezeCheck();
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
        TAR_BZ2("tar.bz2"),
        TAR_GZ("tar.gz"),
        TAR_XZ("tar.xz"),
        TBZ2("tbz2"),
        TGZ("tgz"),
        TXZ("txz");

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
                .valueOf(str.toUpperCase(Locale.ENGLISH).trim()
                    .replace(".", "_"));
        }
    }
}
