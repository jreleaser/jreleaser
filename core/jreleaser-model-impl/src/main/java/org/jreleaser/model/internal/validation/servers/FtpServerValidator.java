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
import org.jreleaser.model.internal.servers.FtpServer;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.ServerValidator.validateServer;
import static org.jreleaser.model.internal.validation.servers.ServersValidator.SERVERS;

/**
 * @author Andres Almiray
 * @author Jan Wloka
 * @since 1.11.0
 */
public final class FtpServerValidator {
    private FtpServerValidator() {
        // noop
    }

    public static void validateFtpServer(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, FtpServer> ftp = context.getModel().getServers().getFtp();
        if (!ftp.isEmpty()) context.getLogger().debug("servers.ftp");

        for (Map.Entry<String, FtpServer> e : ftp.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig() || mode.validateDownload()) {
                validateFtp(context, e.getValue(), errors);
            }
        }
    }

    private static void validateFtp(JReleaserContext context, FtpServer server, Errors errors) {
        context.getLogger().debug("servers.ftp.{}", server.getName());

        validateServer(context, server, SERVERS, "ftp", server.getName(), errors);
    }
}
