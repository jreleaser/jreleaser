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

import org.jreleaser.util.PlatformUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class Command {
    private final List<String> args = new ArrayList<>();
    private final boolean supportsArgsfile;

    public Command() {
        this.supportsArgsfile = false;
    }

    public Command(String arg) {
        this.supportsArgsfile = false;
        this.args.add(arg);
    }

    public Command(String arg, boolean supportsArgsfile) {
        this.supportsArgsfile = supportsArgsfile;
        this.args.add(arg);
    }

    public Command(List<String> args) {
        this.supportsArgsfile = false;
        this.args.addAll(args);
    }

    public boolean isSupportsArgsfile() {
        return supportsArgsfile;
    }

    public List<String> getArgs() {
        return Collections.unmodifiableList(args);
    }

    public boolean hasArg(String arg) {
        return args.contains(arg);
    }

    public Command arg(String arg) {
        this.args.add(arg);
        return this;
    }

    public Command args(Collection<String> args) {
        this.args.addAll(args);
        return this;
    }

    public List<String> asCommandLine() throws IOException {
        if (!supportsArgsfile) return getArgs();

        if (PlatformUtils.isWindows()) {
            File argsFile = File.createTempFile("jreleaser-command", "args");
            argsFile.deleteOnExit();
            Files.write(argsFile.toPath(), (Iterable<String>) args.stream().skip(1)::iterator);

            String cmd = args.get(0);
            args.clear();
            args.add(cmd);
            args.add("@" + argsFile);
        }

        return getArgs();
    }

    public static class Result {
        private final String out;
        private final String err;
        private final int exitValue;

        public static Result empty() {
            return new Result("", "", -1);
        }

        public static Result of(String err, int exitValue) {
            return new Result("", err, exitValue);
        }

        public static Result of(String out, String err, int exitValue) {
            return new Result(out, err, exitValue);
        }

        private Result(String out, String err, int exitValue) {
            this.out = out.trim();
            this.err = err.trim();
            this.exitValue = exitValue;
        }

        public String getOut() {
            return out;
        }

        public String getErr() {
            return err;
        }

        public int getExitValue() {
            return exitValue;
        }
    }
}
