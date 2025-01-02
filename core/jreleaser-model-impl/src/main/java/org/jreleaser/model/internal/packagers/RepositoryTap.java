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

import org.jreleaser.model.internal.common.Activatable;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.ExtraProperties;
import org.jreleaser.model.internal.common.OwnerAware;
import org.jreleaser.mustache.TemplateContext;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface RepositoryTap extends Domain, OwnerAware, Activatable, ExtraProperties {
    String getBasename();

    String getCanonicalRepoName();

    String getResolvedName();

    String getName();

    void setName(String name);

    String getTagName();

    void setTagName(String tagName);

    String getBranch();

    void setBranch(String branch);

    String getBranchPush();

    void setBranchPush(String branchPush);

    String getUsername();

    void setUsername(String username);

    String getToken();

    void setToken(String token);

    String getCommitMessage();

    void setCommitMessage(String commitMessage);

    String getResolvedCommitMessage(TemplateContext props);

    String getResolvedTagName(TemplateContext props);

    <T extends org.jreleaser.model.api.packagers.PackagerRepository> T asImmutable();
}
