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
package org.jreleaser.model;

import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface RepositoryTap extends Domain, OwnerAware, Activatable {
    String getBasename();

    String getCanonicalRepoName();

    String getResolvedName();

    String getResolvedUsername(GitService service);

    String getResolvedToken(GitService service);

    String getName();

    void setName(String name);

    String getTagName();

    void setTagName(String tagName);

    String getBranch();

    void setBranch(String branch);

    String getUsername();

    void setUsername(String username);

    String getToken();

    void setToken(String token);

    String getCommitMessage();

    void setCommitMessage(String commitMessage);

    String getResolvedCommitMessage(Map<String, Object> props);

    String getResolvedTagName(Map<String, Object> props);
}
