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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public class Archive extends AbstractAssembler {
    private final Set<Format> formats = new LinkedHashSet<>();
    private final List<FileSet> fileSets = new ArrayList<>();

    private String archiveName;
    private Boolean attachPlatform;
    private Distribution.DistributionType distributionType;

    public Distribution.DistributionType getDistributionType() {
        return distributionType;
    }

    public void setDistributionType(Distribution.DistributionType distributionType) {
        this.distributionType = distributionType;
    }

    void setAll(Archive archive) {
        super.setAll(archive);
        this.archiveName = archive.archiveName;
        this.distributionType = archive.distributionType;
        this.attachPlatform = archive.attachPlatform;
        setFormats(archive.formats);
        setFileSets(archive.fileSets);
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

    public List<FileSet> getFileSets() {
        return fileSets;
    }

    public void setFileSets(List<FileSet> fileSets) {
        this.fileSets.clear();
        this.fileSets.addAll(fileSets);
    }

    public void addFiles(List<FileSet> files) {
        this.fileSets.addAll(files);
    }

    public void addFile(FileSet file) {
        if (null != file) {
            this.fileSets.add(file);
        }
    }

    public enum Format {
        ZIP,
        TAR,
        TGZ,
        TAR_GZ
    }
}
