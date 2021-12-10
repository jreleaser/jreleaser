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
package org.jreleaser.templates;

import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.util.JReleaserLogger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class VersionDecoratingWriter extends BufferedWriter {
    private static final String VERSION_MARKER = "[JRELEASER_VERSION]";

    private static final String JRELEASER_VERSION = JReleaserVersion.getPlainVersion();

    private static final String VERSION_BANNER = String.format("Generated with JReleaser %s at %s",
            JRELEASER_VERSION, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now())
    );

    public VersionDecoratingWriter(final Writer out) {
        super(out);
    }

    @Override
    public void write(final String str) throws IOException {
        if (needsVersionReplacement(str)) {
            super.write(str.replace(VERSION_MARKER, VERSION_BANNER));
        } else {
            super.write(str);
        }
    }

    private boolean needsVersionReplacement(final String str) {
        return str.contains(VERSION_MARKER);
    }
}
