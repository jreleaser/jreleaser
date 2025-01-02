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
package org.jreleaser.model.internal.assemble;

import org.jreleaser.model.Distribution;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.catalog.swid.SwidTag;
import org.jreleaser.model.internal.common.Activatable;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.ExtraProperties;
import org.jreleaser.model.internal.common.FileSet;
import org.jreleaser.model.internal.common.Glob;
import org.jreleaser.model.internal.platform.Platform;
import org.jreleaser.mustache.TemplateContext;

import java.util.List;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public interface Assembler<A extends org.jreleaser.model.api.assemble.Assembler> extends Domain, Activatable, ExtraProperties {
    Platform getPlatform();

    void setPlatform(Platform platform);

    Distribution.DistributionType getDistributionType();

    String getType();

    Stereotype getStereotype();

    void setStereotype(Stereotype stereotype);

    void setStereotype(String str);

    boolean isExported();

    void setExported(Boolean exported);

    String getName();

    void setName(String name);

    Set<Artifact> getOutputs();

    void setOutputs(Set<Artifact> output);

    void addOutput(Artifact artifact);

    TemplateContext props();

    String getTemplateDirectory();

    void setTemplateDirectory(String templateDirectory);

    Set<String> getSkipTemplates();

    void setSkipTemplates(Set<String> skipTemplates);

    void addSkipTemplates(Set<String> templates);

    void addSkipTemplate(String template);

    Set<? extends Artifact> getArtifacts();

    void setArtifacts(Set<Artifact> artifacts);

    void addArtifacts(Set<Artifact> artifacts);

    void addArtifact(Artifact artifact);

    List<FileSet> getFileSets();

    void setFileSets(List<FileSet> fileSets);

    void addFileSets(List<FileSet> files);

    void addFileSet(FileSet file);

    List<Glob> getFiles();

    void setFiles(List<Glob> files);

    void addFiles(List<Glob> files);

    void addFile(Glob file);

    void setSwid(SwidTag swid);

    SwidTag getSwid();

    A asImmutable();
}
