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

import picocli.AutoComplete;
import picocli.CommandLine;

import java.io.PrintWriter;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "jreleaser",
    subcommands = {Env.class, Init.class, Config.class, Template.class,
        Download.class, Assemble.class, Changelog.class,
        Catalog.class, Checksum.class, Sign.class,
        Deploy.class, Upload.class,
        Release.class, Prepare.class, Package.class,
        Publish.class, Announce.class, FullRelease.class,
        AutoComplete.GenerateCompletion.class,
        JsonSchema.class})
public class Main extends BaseCommand implements Runnable, IO {
    private PrintWriter out;
    private PrintWriter err;

    @Override
    public PrintWriter getOut() {
        return out;
    }

    @Override
    public void setOut(PrintWriter out) {
        this.out = out;
    }

    @Override
    public PrintWriter getErr() {
        return err;
    }

    @Override
    public void setErr(PrintWriter err) {
        this.err = err;
    }

    @Override
    public void run() {
        Banner.display(err);

        spec.commandLine().usage(out);
    }

    public static void main(String[] args) {
        System.exit(run(args));
    }

    public static int run(String... args) {
        Main cmd = new Main();
        CommandLine commandLine = new CommandLine(cmd);
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);
        commandLine.setUsageHelpWidth(110);
        commandLine.setUsageHelpLongOptionsMaxWidth(42);
        cmd.out = commandLine.getOut();
        cmd.err = commandLine.getErr();
        return execute(commandLine, args);
    }

    public static int run(PrintWriter out, PrintWriter err, String... args) {
        Main cmd = new Main();
        CommandLine commandLine = new CommandLine(cmd);
        commandLine.setCaseInsensitiveEnumValuesAllowed(true);
        commandLine.setUsageHelpWidth(110);
        commandLine.setUsageHelpLongOptionsMaxWidth(42);
        commandLine.setOut(out);
        commandLine.setErr(err);
        cmd.out = out;
        cmd.err = err;
        return execute(commandLine, args);
    }

    private static int execute(CommandLine commandLine, String[] args) {
        return commandLine.execute(args);
    }
}
