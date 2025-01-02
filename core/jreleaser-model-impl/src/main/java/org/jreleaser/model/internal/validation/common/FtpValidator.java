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
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.util.CollectionUtils.listOf;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class FtpValidator {
    private static final String USERNAME = ".username";
    private static final String PASSWORD = ".password";
    private static final String HOST = ".host";
    private static final String PORT = ".port";

    private FtpValidator() {
        // noop
    }

    public static void validateFtp(JReleaserContext context, Ftp ftp, String prefix, String name, Errors errors, boolean anonymousAccess) {
        String baseKey1 = prefix + ".ftp." + name;
        String baseKey2 = prefix + ".ftp";
        String baseKey3 = "ftp." + name;
        String baseKey4 = "ftp";

        ftp.setUsername(
            checkProperty(context,
                listOf(
                    baseKey1 + USERNAME,
                    baseKey2 + USERNAME,
                    baseKey3 + USERNAME,
                    baseKey4 + USERNAME),
                baseKey1 + USERNAME,
                ftp.getUsername(),
                errors,
                anonymousAccess));

        ftp.setPassword(
            checkProperty(context,
                listOf(
                    baseKey1 + PASSWORD,
                    baseKey2 + PASSWORD,
                    baseKey3 + PASSWORD,
                    baseKey4 + PASSWORD),
                baseKey1 + PASSWORD,
                ftp.getPassword(),
                errors,
                anonymousAccess));

        ftp.setHost(
            checkProperty(context,
                listOf(
                    baseKey1 + HOST,
                    baseKey2 + HOST,
                    baseKey3 + HOST,
                    baseKey4 + HOST),
                baseKey1 + HOST,
                ftp.getHost(),
                errors,
                context.isDryrun()));

        ftp.setPort(
            checkProperty(context,
                listOf(
                    baseKey1 + PORT,
                    baseKey2 + PORT,
                    baseKey3 + PORT,
                    baseKey4 + PORT),
                baseKey1 + PORT,
                ftp.getPort(),
                errors,
                context.isDryrun()));
    }
}
