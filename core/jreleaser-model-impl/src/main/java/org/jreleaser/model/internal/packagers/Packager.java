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

import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Activatable;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.ExtraProperties;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.project.Project;

import java.util.List;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface Packager<A extends org.jreleaser.model.api.packagers.Packager> extends Domain, ExtraProperties, Activatable {
    String getType();

    String getDownloadUrl();

    void setDownloadUrl(String downloadUrl);

    boolean supportsPlatform(String platform);

    boolean supportsDistribution(org.jreleaser.model.Distribution.DistributionType distributionType);

    Set<String> getSupportedFileExtensions(org.jreleaser.model.Distribution.DistributionType distributionType);

    Set<Stereotype> getSupportedStereotypes();

    List<Artifact> resolveCandidateArtifacts(JReleaserContext context, Distribution distribution);

    List<Artifact> resolveArtifacts(JReleaserContext context, Distribution distribution);

    boolean isSnapshotSupported();

    boolean isContinueOnError();

    void setContinueOnError(Boolean continueOnError);

    boolean isContinueOnErrorSet();

    boolean isFailed();

    void fail();

    A asImmutable();

    boolean resolveEnabled(Project project, Distribution distribution);
}
