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
package org.jreleaser.sdk.command;

import org.jreleaser.bundle.RB;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.util.IoUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public class CommandExecutor {
    private final JReleaserLogger logger;
    private final Output output;
    private final Map<String, String> environment = new LinkedHashMap<>();

    public enum Output {
        QUIET,
        DEBUG,
        VERBOSE
    }

    public CommandExecutor(JReleaserLogger logger) {
        this(logger, Output.DEBUG);
    }

    public CommandExecutor(JReleaserLogger logger, Output output) {
        this.logger = logger;
        this.output = output;
    }

    public CommandExecutor environment(Map<String, String> env) {
        environment.putAll(env);
        return this;
    }

    public CommandExecutor environment(String name, String value) {
        environment.put(name, value);
        return this;
    }

    private Command.Result executeCommand(ProcessExecutor processExecutor) throws CommandException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();

            int exitValue = processExecutor
                .execute(logger, output, out, err);

            return Command.Result.of(IoUtils.toString(out), IoUtils.toString(err), exitValue);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommandException(RB.$("ERROR_unexpected_error"), e);
        } catch (Exception e) {
            throw new CommandException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    public Command.Result executeCommand(Command command) throws CommandException {
        return executeCommand(createProcessExecutor(command));
    }

    public Command.Result executeCommand(Path directory, Command command) throws CommandException {
        return executeCommand(createProcessExecutor(command)
            .directory(directory.toFile()));
    }

    public Command.Result executeCommand(Command command, InputStream in) throws CommandException {
        return executeCommand(createProcessExecutor(command)
            .redirectInput(in));
    }

    public Command.Result executeCommand(Path directory, Command command, InputStream in) throws CommandException {
        return executeCommand(createProcessExecutor(command)
            .redirectInput(in)
            .directory(directory.toFile()));
    }

    private ProcessExecutor createProcessExecutor(Command command) throws CommandException {
        try {
            return new ProcessExecutor(command, environment);
        } catch (IOException e) {
            throw new CommandException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    private static class ProcessExecutor {
        private final ProcessBuilder builder;
        private InputStream input;

        private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(2, new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                t.setName("jreleaser-command-executor-" + counter.getAndIncrement());
                return t;
            }
        });

        private ProcessExecutor(Command command, Map<String, String> environment) throws IOException {
            this.builder = new ProcessBuilder(command.asCommandLine());
            this.builder.environment().putAll(environment);
        }

        private ProcessExecutor directory(File directory) {
            builder.directory(directory);
            return this;
        }

        private ProcessExecutor redirectInput(InputStream input) {
            this.input = input;
            return this;
        }

        private int execute(JReleaserLogger logger, Output output, OutputStream out, OutputStream err) throws IOException, InterruptedException {
            Process process = builder.start();

            if (null != input) {
                PrintWriter writer = IoUtils.newPrintWriter(process.getOutputStream(), true);
                IoUtils.withInputStream(input, writer::write);
                writer.println();
            }

            IOException[] outException = handleStream(process.getInputStream(), out, s -> {
                switch (output) {
                    case DEBUG:
                        logger.debug(s);
                        break;
                    case VERBOSE:
                        logger.plain(s);
                        break;
                    default:
                        // noop
                }
            });
            IOException[] errException = handleStream(process.getErrorStream(), err, s -> {
                switch (output) {
                    case DEBUG:
                        // fall-through
                    case VERBOSE:
                        logger.error(s);
                        break;
                    default:
                        // noop
                }
            });

            int exitValue = process.waitFor();

            if (null != outException[0]) throw outException[0];
            if (null != errException[0]) throw errException[0];

            return exitValue;
        }

        private IOException[] handleStream(InputStream input, OutputStream target, Consumer<? super String> log) {
            IOException[] capture = new IOException[1];

            EXECUTOR_SERVICE.submit(() -> {
                try {
                    PrintWriter writer = IoUtils.newPrintWriter(target, true);
                    IoUtils.withLines(input, s -> {
                        log.accept(s);
                        writer.println(s);
                    });
                } catch (IOException e) {
                    capture[0] = e;
                }
            });

            return capture;
        }
    }
}
