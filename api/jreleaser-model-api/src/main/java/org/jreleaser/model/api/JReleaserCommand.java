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
package org.jreleaser.model.api;

import java.util.Locale;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
public enum JReleaserCommand {
    CONFIG(true),
    DOWNLOAD(false),
    ASSEMBLE(false),
    CHANGELOG(true),
    CHECKSUM(false),
    CATALOG(false),
    SIGN(false),
    DEPLOY(false),
    UPLOAD(false),
    RELEASE(true),
    PREPARE(false),
    PACKAGE(false),
    PUBLISH(false),
    ANNOUNCE(true),
    FULL_RELEASE(true);

    private final boolean git;

    JReleaserCommand(boolean git) {
        this.git = git;
    }

    public boolean requiresGit() {
        return git;
    }

    public String toStep() {
        return name().toLowerCase(Locale.ENGLISH)
            .replace("_", "-");
    }

    public static boolean supportsAssemble(JReleaserCommand command) {
        return ASSEMBLE == command;
    }

    public static boolean supportsChangelog(JReleaserCommand command) {
        return CHANGELOG == command ||
            RELEASE == command ||
            ANNOUNCE == command ||
            FULL_RELEASE == command;
    }

    public static boolean supportsChecksum(JReleaserCommand command) {
        return CHECKSUM == command ||
            CATALOG == command ||
            SIGN == command ||
            UPLOAD == command ||
            RELEASE == command ||
            PREPARE == command ||
            PACKAGE == command ||
            PUBLISH == command ||
            FULL_RELEASE == command;
    }

    public static boolean supportsSign(JReleaserCommand command) {
        return SIGN == command ||
            UPLOAD == command ||
            RELEASE == command ||
            FULL_RELEASE == command;
    }

    public static boolean supportsDeploy(JReleaserCommand command) {
        return DEPLOY == command ||
            RELEASE == command ||
            FULL_RELEASE == command;
    }

    public static boolean supportsUpload(JReleaserCommand command) {
        return UPLOAD == command ||
            RELEASE == command ||
            FULL_RELEASE == command;
    }

    public static boolean supportsRelease(JReleaserCommand command) {
        return RELEASE == command ||
            FULL_RELEASE == command;
    }

    public static boolean supportsPrepare(JReleaserCommand command) {
        return PREPARE == command ||
            PACKAGE == command ||
            PUBLISH == command ||
            FULL_RELEASE == command;
    }

    public static boolean supportsPackage(JReleaserCommand command) {
        return PACKAGE == command ||
            PUBLISH == command ||
            FULL_RELEASE == command;
    }

    public static boolean supportsPublish(JReleaserCommand command) {
        return PUBLISH == command ||
            FULL_RELEASE == command;
    }

    public static boolean supportsAnnounce(JReleaserCommand command) {
        return ANNOUNCE == command ||
            FULL_RELEASE == command;
    }
}