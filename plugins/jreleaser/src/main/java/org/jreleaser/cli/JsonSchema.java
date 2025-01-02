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

import org.jreleaser.engine.schema.JsonSchemaGenerator;
import picocli.CommandLine;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
@CommandLine.Command(name = "json-schema")
public class JsonSchema extends AbstractCommand<Main> {
    @CommandLine.ParentCommand
    Main parent;

    @Override
    protected Main parent() {
        return parent;
    }

    @Override
    protected void execute() {
        JsonSchemaGenerator.generate(parent().getOut());
    }
}
