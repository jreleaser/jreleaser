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
import org.jreleaser.model.internal.servers.GiteaServer;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.ServerValidator.validateServer;
import static org.jreleaser.model.internal.validation.servers.ServersValidator.SERVERS;

/**
 * @author Andres Almiray
 * @author Jan Wloka
 * @since 1.11.0
 */
public final class GiteaServerValidator {
    private GiteaServerValidator() {
        // noop
    }

    public static void validateGiteaServer(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, GiteaServer> gitea = context.getModel().getServers().getGitea();
        if (!gitea.isEmpty()) context.getLogger().debug("servers.gitea");

        for (Map.Entry<String, GiteaServer> e : gitea.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig() || mode.validateDownload()) {
                validateGitea(context, e.getValue(), errors);
            }
        }
    }

    private static void validateGitea(JReleaserContext context, GiteaServer server, Errors errors) {
        context.getLogger().debug("servers.gitea.{}", server.getName());

        validateServer(context, server, SERVERS, "gitea", server.getName(), errors);
    }
}
