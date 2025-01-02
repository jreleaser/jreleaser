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
package org.jreleaser.cli;

import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.util.IoUtils;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;

import static org.jreleaser.util.IoUtils.newPrintStream;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Versions implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JReleaserVersion.banner(newPrintStream(baos));
        return IoUtils.toString(baos).split(System.lineSeparator());
    }
}
