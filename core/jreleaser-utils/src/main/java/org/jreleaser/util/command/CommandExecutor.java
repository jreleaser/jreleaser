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
package org.jreleaser.util.command;

import org.jreleaser.bundle.RB;
import org.jreleaser.util.JReleaserLogger;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessInitException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Consumer;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public class CommandExecutor {
    private final JReleaserLogger logger;
    private final boolean quiet;

    public CommandExecutor(JReleaserLogger logger) {
        this(logger, false);
    }

    public CommandExecutor(JReleaserLogger logger, boolean quiet) {
        this.logger = logger;
        this.quiet = quiet;
    }

    public int executeCommand(ProcessExecutor processExecutor) throws CommandException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();

            int exitValue = processExecutor
                .redirectOutput(out)
                .redirectError(err)
                .execute()
                .getExitValue();

            if (!quiet) {
                info(out);
                error(err);
            }

            return exitValue;
        } catch (ProcessInitException e) {
            throw new CommandException(RB.$("ERROR_unexpected_error"), e.getCause());
        } catch (Exception e) {
            throw new CommandException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    public int executeCommand(Path directory, Command command) throws CommandException {
        return executeCommand(new ProcessExecutor(command.getArgs())
            .directory(directory.toFile()));
    }

    public int executeCommand(Command command) throws CommandException {
        return executeCommand(new ProcessExecutor(command.getArgs()));
    }

    public int executeCommandCapturing(Command command, OutputStream out) throws CommandException {
        try {
            ByteArrayOutputStream err = new ByteArrayOutputStream();

            int exitValue = new ProcessExecutor(command.getArgs())
                .redirectOutput(out)
                .redirectError(err)
                .execute()
                .getExitValue();

            if (!quiet) {
                error(err);
            }

            return exitValue;
        } catch (ProcessInitException e) {
            throw new CommandException(RB.$("ERROR_unexpected_error"), e.getCause());
        } catch (Exception e) {
            throw new CommandException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    public int executeCommandWithInput(Command command, InputStream in) throws CommandException {
        return executeCommand(new ProcessExecutor(command.getArgs())
            .redirectInput(in));
    }

    private void info(ByteArrayOutputStream out) {
        log(out, logger::info);
    }

    private void error(ByteArrayOutputStream err) {
        log(err, logger::error);
    }

    private void log(ByteArrayOutputStream stream, Consumer<? super String> consumer) {
        String str = stream.toString();
        if (isBlank(str)) return;

        Arrays.stream(str.split(System.lineSeparator()))
            .forEach(consumer);
    }
}
