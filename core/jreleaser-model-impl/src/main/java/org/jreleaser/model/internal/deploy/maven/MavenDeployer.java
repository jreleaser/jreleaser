/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
package org.jreleaser.model.internal.deploy.maven;

import org.jreleaser.model.Http;
import org.jreleaser.model.internal.common.TimeoutAware;
import org.jreleaser.model.internal.deploy.Deployer;

import java.util.List;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public interface MavenDeployer<A extends org.jreleaser.model.api.deploy.maven.MavenDeployer> extends Deployer<A>, TimeoutAware {
    String getUrl();

    void setUrl(String url);

    String getUsername();

    void setUsername(String username);

    String getPassword();

    void setPassword(String password);

    Http.Authorization getAuthorization();

    void setAuthorization(Http.Authorization authorization);

    void setAuthorization(String authorization);

    boolean isSign();

    void setSign(Boolean sign);

    boolean isSignSet();

    boolean isVerifyPom();

    void setVerifyPom(Boolean verifyPom);

    boolean isVerifyPomSet();

    boolean isApplyMavenCentralRules();

    void setApplyMavenCentralRules(Boolean applyMavenCentralRules);

    boolean isApplyMavenCentralRulesSet();

    List<String> getStagingRepositories();

    void setStagingRepositories(List<String> stagingRepositories);

    Http.Authorization resolveAuthorization();

    boolean isSnapshotAllowed();
}
