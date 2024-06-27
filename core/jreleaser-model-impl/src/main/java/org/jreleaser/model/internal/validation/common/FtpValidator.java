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
package org.jreleaser.model.internal.validation.common;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Ftp;
import org.jreleaser.model.internal.servers.FtpServer;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.common.AuthenticatableValidator.validateAuthenticatable;
import static org.jreleaser.model.internal.validation.common.ServerValidator.validateHost;
import static org.jreleaser.model.internal.validation.common.ServerValidator.validatePort;
import static org.jreleaser.model.internal.validation.common.ServerValidator.validateTimeout;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class FtpValidator {
    private static final String FTP = "ftp";

    private FtpValidator() {
        // noop
    }

    public static void validateFtp(JReleaserContext context, Ftp ftp, String prefix, String name, Errors errors, boolean anonymousAccess) {
        validateFtp(context, ftp, null, prefix, name, errors, anonymousAccess);
    }

    public static void validateFtp(JReleaserContext context, Ftp ftp, FtpServer server, String prefix, String name, Errors errors, boolean anonymousAccess) {
        validateAuthenticatable(context, ftp, server, prefix, FTP, name, errors, anonymousAccess);
        validateHost(context, ftp, server, prefix, FTP, name, errors, context.isDryrun());
        validatePort(context, ftp, server, prefix, FTP, name, errors, context.isDryrun());
        validateTimeout(context, ftp, server, prefix, FTP, name, errors, true);
    }
}
