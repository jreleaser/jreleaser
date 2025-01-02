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
package org.jreleaser.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jreleaser.engine.schema.JsonSchemaGenerator;
import org.jreleaser.util.IoUtils;

import static org.jreleaser.util.IoUtils.newPrintWriter;

/**
 * @author Andres Almiray
 * @since 1.4.0
 */
public class JReleaserJsonSchemaTask extends Task {
    @Override
    public void execute() throws BuildException {
        Banner.display(newPrintWriter(System.err));
        JsonSchemaGenerator.generate(IoUtils.newPrintWriter(System.out));
    }
}
