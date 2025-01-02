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
package org.jreleaser.util;

import org.jreleaser.logging.JReleaserLogger;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class Errors implements Serializable {
    private static final long serialVersionUID = -7835875752041439694L;

    private final Set<Error> assemblyErrors = new LinkedHashSet<>();
    private final Set<Error> configurationErrors = new LinkedHashSet<>();
    private final Set<Error> warnings = new LinkedHashSet<>();

    public boolean hasErrors() {
        return !assemblyErrors.isEmpty() || !configurationErrors.isEmpty();
    }

    public boolean hasAssemblyErrors() {
        return !assemblyErrors.isEmpty();
    }

    public boolean hasConfigurationErrors() {
        return !configurationErrors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public void assembly(String message) {
        assemblyErrors.add(new Error(Kind.ASSEMBLY, message));
    }

    public void configuration(String message) {
        configurationErrors.add(new Error(Kind.CONFIGURATION, message));
    }

    public void warning(String message) {
        warnings.add(new Error(Kind.CONFIGURATION, message));
    }

    public void logWarnings(JReleaserLogger logger) {
        warnings.forEach(e -> logger.warn(e.message));
    }

    public void logWarnings(PrintWriter writer) {
        warnings.forEach(e -> writer.println(e.message));
    }

    public void logErrors(JReleaserLogger logger) {
        assemblyErrors.forEach(e -> logger.error(e.message));
        configurationErrors.forEach(e -> logger.error(e.message));
    }

    public void logErrors(PrintWriter writer) {
        assemblyErrors.forEach(e -> writer.println(e.message));
        configurationErrors.forEach(e -> writer.println(e.message));
    }

    public String asString() {
        StringWriter writer = new StringWriter();
        logErrors(new PrintWriter(writer));
        return writer.toString();
    }

    public String warningsAsString() {
        StringWriter writer = new StringWriter();
        logWarnings(new PrintWriter(writer));
        return writer.toString();
    }

    public void addAll(Errors errors) {
        this.assemblyErrors.addAll(errors.assemblyErrors);
        this.configurationErrors.addAll(errors.configurationErrors);
        this.warnings.addAll(errors.warnings);
    }

    public enum Kind {
        ASSEMBLY,
        CONFIGURATION,
        WARNING
    }

    public static class Error implements Serializable {
        private static final long serialVersionUID = -9011553489507569322L;

        private final Kind kind;
        private final String message;

        public Error(Kind kind, String message) {
            this.kind = kind;
            this.message = message;
        }

        public Kind getKind() {
            return kind;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (null == o || getClass() != o.getClass()) return false;
            Error error = (Error) o;
            return kind == error.kind &&
                message.equals(error.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(kind, message);
        }

        @Override
        public String toString() {
            return message;
        }
    }
}
