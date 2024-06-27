/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 The JReleaser authors.
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
package org.jreleaser.model.internal.validation.servers;

import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.servers.FtpServerValidator.validateFtpServer;
import static org.jreleaser.model.internal.validation.servers.GenericServerValidator.validateGenericServer;
import static org.jreleaser.model.internal.validation.servers.GiteaServerValidator.validateGiteaServer;
import static org.jreleaser.model.internal.validation.servers.GithubServerValidator.validateGithubServer;
import static org.jreleaser.model.internal.validation.servers.GitlabServerValidator.validateGitlabServer;
import static org.jreleaser.model.internal.validation.servers.HttpServerValidator.validateHttpServer;
import static org.jreleaser.model.internal.validation.servers.SshServerValidator.validateSshServer;

/**
 * @author Andres Almiray
 * @author Jan Wloka
 * @since 1.11.0
 */
public final class ServersValidator {
    public static final String SERVERS = "servers";

    private ServersValidator() {
        // noop
    }

    public static void validateServers(JReleaserContext context, Mode mode, Errors errors) {
        context.getLogger().debug(SERVERS);

        validateGenericServer(context, mode, errors);
        validateFtpServer(context, mode, errors);
        validateHttpServer(context, mode, errors);
        validateSshServer(context, mode, errors);
        validateGithubServer(context, mode, errors);
        validateGitlabServer(context, mode, errors);
        validateGiteaServer(context, mode, errors);
    }
}