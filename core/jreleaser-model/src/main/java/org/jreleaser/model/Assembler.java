/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public interface Assembler extends Domain, Activatable, ExtraProperties {
    Distribution.DistributionType getDistributionType();

    String getType();

    String getName();

    void setName(String name);

    String getExecutable();

    void setExecutable(String executable);

    String getTemplateDirectory();

    void setTemplateDirectory(String templateDirectory);

    Java getJava();

    void setJava(Java java);

    Set<Artifact> getOutputs();

    void setOutputs(Set<Artifact> output);

    void addOutput(Artifact artifact);
}
