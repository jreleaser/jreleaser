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

import picocli.CommandLine;

import java.io.PrintWriter;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
@CommandLine.Command(name = "template",
    subcommands = {TemplateGenerate.class, TemplateEval.class})
public class Template extends AbstractCommand<Main> implements IO {
    @Override
    public PrintWriter getOut() {
        return parent().getOut();
    }

    @Override
    public void setOut(PrintWriter out) {
        parent().setOut(out);
    }

    @Override
    public PrintWriter getErr() {
        return parent().getErr();
    }

    @Override
    public void setErr(PrintWriter err) {
        parent().setErr(err);
    }

    @Override
    protected void execute() {
        spec.commandLine().usage(parent().getOut());
    }
}
