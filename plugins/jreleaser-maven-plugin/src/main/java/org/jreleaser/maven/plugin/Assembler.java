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
package org.jreleaser.maven.plugin;

import java.util.List;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public interface Assembler extends Activatable, ExtraProperties {
    Platform getPlatform();

    void setPlatform(Platform platform);

    String getName();

    void setName(String name);

    boolean isExported();

    void setExported(boolean exported);

    Set<Artifact> getOutputs();

    void setOutputs(Set<Artifact> output);

    void addOutput(Artifact artifact);

    List<FileSet> getFileSets();

    void setFileSets(List<FileSet> fileSets);
}
