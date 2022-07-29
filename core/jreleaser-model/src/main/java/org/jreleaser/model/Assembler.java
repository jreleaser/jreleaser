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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public interface Assembler extends Domain, Activatable, ExtraProperties {
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

    Map<String, Object> props();

    List<FileSet> getFileSets();

    void setFileSets(List<FileSet> fileSets);

    void addFileSets(List<FileSet> files);

    void addFileSet(FileSet file);
}
