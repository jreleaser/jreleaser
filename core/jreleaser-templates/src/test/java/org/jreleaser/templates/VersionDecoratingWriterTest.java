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
package org.jreleaser.templates;

import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.util.IoUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

class VersionDecoratingWriterTest {
    private final ByteArrayOutputStream sink = new ByteArrayOutputStream();
    private final VersionDecoratingWriter writer = new VersionDecoratingWriter(new OutputStreamWriter(sink, UTF_8));

    @Test
    void should_ignore_content_without_marker() throws IOException {
        writer.write("cask \"{{brewCaskName}}\" do");
        writer.flush();

        assertThat(IoUtils.toString(sink), is("cask \"{{brewCaskName}}\" do"));
    }

    @Test
    void should_replace_marker() throws IOException {
        writer.write("# {{jreleaserCreationStamp}}");
        writer.flush();

        assertThat(IoUtils.toString(sink), not(containsString("{{jreleaserCreationStamp}}")));
        assertThat(IoUtils.toString(sink), containsString(JReleaserVersion.getPlainVersion()));
    }
}