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
package org.jreleaser.sdk.command;

import org.jreleaser.bundle.RB;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.util.IoUtils;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessInitException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public class CommandExecutor {
    private final JReleaserLogger logger;
    private final boolean quiet;
    private final Map<String, String> environment = new LinkedHashMap<>();

    public CommandExecutor(JReleaserLogger logger) {
        this(logger, false);
    }

    public CommandExecutor(JReleaserLogger logger, boolean quiet) {
        this.logger = logger;
        this.quiet = quiet;
    }

    public CommandExecutor environment(Map<String, String> env) {
        environment.putAll(env);
        return this;
    }

    public CommandExecutor environment(String name, String value) {
        environment.put(name, value);
        return this;
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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommandException(RB.$("ERROR_unexpected_error"), e);
        } catch (Exception e) {
            throw new CommandException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    public int executeCommand(Command command) throws CommandException {
        return executeCommand(createProcessExecutor(command));
    }

    public int executeCommand(Path directory, Command command) throws CommandException {
        return executeCommand(createProcessExecutor(command)
            .directory(directory.toFile()));
    }

    public int executeCommandCapturing(Command command, OutputStream out) throws CommandException {
        return executeCommandCapturing(createProcessExecutor(command), out);
    }

    public int executeCommandCapturing(Command command, OutputStream out, OutputStream err) throws CommandException {
        return executeCommandCapturing(createProcessExecutor(command), out, err);
    }

    public int executeCommandCapturing(Path directory, Command command, OutputStream out) throws CommandException {
        return executeCommandCapturing(createProcessExecutor(command)
            .directory(directory.toFile()), out);
    }

    public int executeCommandCapturing(Path directory, Command command, OutputStream out, OutputStream err) throws CommandException {
        return executeCommandCapturing(createProcessExecutor(command)
            .directory(directory.toFile()), out, err);
    }

    public int executeCommandWithInput(Command command, InputStream in) throws CommandException {
        return executeCommand(createProcessExecutor(command)
            .redirectInput(in));
    }

    public int executeCommandWithInputCapturing(Command command, InputStream in, OutputStream out) throws CommandException {
        return executeCommandCapturing(createProcessExecutor(command)
            .redirectInput(in), out);
    }

    public int executeCommandWithInput(Path directory, Command command, InputStream in) throws CommandException {
        return executeCommand(createProcessExecutor(command)
            .redirectInput(in)
            .directory(directory.toFile()));
    }

    public int executeCommandWithInputCapturing(Path directory, Command command, InputStream in, OutputStream out) throws CommandException {
        return executeCommandCapturing(createProcessExecutor(command)
            .directory(directory.toFile())
            .redirectInput(in), out);
    }

    private ProcessExecutor createProcessExecutor(Command command) throws CommandException {
        try {
            return new ProcessExecutor(command.asCommandLine())
                .environment(environment);
        } catch (IOException e) {
            throw new CommandException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    private int executeCommandCapturing(ProcessExecutor processor, OutputStream out) throws CommandException {
        return executeCommandCapturing(processor, out, null);
    }

    private int executeCommandCapturing(ProcessExecutor processor, OutputStream out, OutputStream err) throws CommandException {
        try {
            ByteArrayOutputStream errLocal = new ByteArrayOutputStream();

            int exitValue = processor
                .redirectOutput(out)
                .redirectError(err)
                .execute()
                .getExitValue();

            if (!quiet) {
                error(errLocal);
            }

            if (err != null) {
                err.write(errLocal.toByteArray());
            }

            return exitValue;
        } catch (ProcessInitException e) {
            throw new CommandException(RB.$("ERROR_unexpected_error"), e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommandException(RB.$("ERROR_unexpected_error"), e);
        } catch (Exception e) {
            throw new CommandException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    private void info(ByteArrayOutputStream out) {
        log(out, logger::info);
    }

    private void error(ByteArrayOutputStream err) {
        log(err, logger::error);
    }

    private void log(ByteArrayOutputStream stream, Consumer<? super String> consumer) {
        String str = IoUtils.toString(stream);
        if (isBlank(str)) return;

        Arrays.stream(str.split(System.lineSeparator()))
            .forEach(consumer);
    }
}
