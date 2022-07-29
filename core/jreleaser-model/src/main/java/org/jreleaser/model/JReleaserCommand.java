/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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

import java.util.Locale;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
public enum JReleaserCommand {
    DOWNLOAD,
    ASSEMBLE,
    CHANGELOG,
    CHECKSUM,
    SIGN,
    UPLOAD,
    RELEASE,
    PREPARE,
    PACKAGE,
    PUBLISH,
    ANNOUNCE,
    FULL_RELEASE;

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