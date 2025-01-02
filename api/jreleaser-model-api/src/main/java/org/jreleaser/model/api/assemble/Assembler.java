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
package org.jreleaser.model.api.assemble;

import org.jreleaser.model.Distribution;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.api.common.Activatable;
import org.jreleaser.model.api.common.Artifact;
import org.jreleaser.model.api.common.Domain;
import org.jreleaser.model.api.common.ExtraProperties;
import org.jreleaser.model.api.common.FileSet;
import org.jreleaser.model.api.common.Glob;
import org.jreleaser.model.api.platform.Platform;

import java.util.List;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public interface Assembler extends Domain, Activatable, ExtraProperties {
    Platform getPlatform();

    Distribution.DistributionType getDistributionType();

    String getType();

    Stereotype getStereotype();

    boolean isExported();

    String getName();

    String getTemplateDirectory();

    Set<String> getSkipTemplates();

    Set<? extends Artifact> getArtifacts();

    List<? extends Glob> getFiles();

    List<? extends FileSet> getFileSets();

    Set<? extends Artifact> getOutputs();
}
