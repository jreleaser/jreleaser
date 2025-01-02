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
package org.jreleaser.model.internal.upload;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Activatable;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.ExtraProperties;
import org.jreleaser.model.internal.common.TimeoutAware;
import org.jreleaser.mustache.TemplateContext;

import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public interface Uploader<A extends org.jreleaser.model.api.upload.Uploader> extends Domain, Activatable, TimeoutAware, ExtraProperties {
    String getType();

    String getName();

    void setName(String name);

    boolean isSnapshotSupported();

    boolean isArtifacts();

    void setArtifacts(Boolean artifacts);

    boolean isArtifactsSet();

    boolean isFiles();

    void setFiles(Boolean files);

    boolean isFilesSet();

    boolean isSignatures();

    void setSignatures(Boolean signatures);

    boolean isSignaturesSet();

    boolean isChecksumsSet();

    boolean isChecksums();

    void setChecksums(Boolean checksums);

    boolean isCatalogs();

    void setCatalogs(Boolean catalogs);

    boolean isCatalogsSet();

    List<String> resolveSkipKeys();

    A asImmutable();

    TemplateContext artifactProps(JReleaserContext context, Artifact artifact);

    TemplateContext artifactProps(TemplateContext props, Artifact artifact);

    String getResolvedDownloadUrl(JReleaserContext context, Artifact artifact);

    String getResolvedDownloadUrl(TemplateContext props, Artifact artifact);
}
