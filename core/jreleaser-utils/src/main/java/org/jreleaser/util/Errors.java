/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class Errors {
    private final List<Error> assemblyErrors = new ArrayList<>();
    private final List<Error> configurationErrors = new ArrayList<>();

    public boolean hasErrors() {
        return !assemblyErrors.isEmpty() || !configurationErrors.isEmpty();
    }

    public boolean hasAssemblyErrors() {
        return !assemblyErrors.isEmpty();
    }

    public boolean hasConfigurationErrors() {
        return !configurationErrors.isEmpty();
    }

    public void assembly(String message) {
        assemblyErrors.add(new Error(Kind.ASSEMBLY, message));
    }

    public void configuration(String message) {
        configurationErrors.add(new Error(Kind.CONFIGURATION, message));
    }

    public void logErrors(JReleaserLogger logger) {
        assemblyErrors.forEach(e -> logger.error(e.message));
        configurationErrors.forEach(e -> logger.error(e.message));
    }

    public enum Kind {
        ASSEMBLY,
        CONFIGURATION
    }

    public static class Error {
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
            if (o == null || getClass() != o.getClass()) return false;
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
