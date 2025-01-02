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
package org.jreleaser.model.api.hooks;

import org.jreleaser.mustache.MustacheUtils;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Locale;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public class ExecutionEvent implements Serializable {
    private static final long serialVersionUID = -3014674027923578251L;

    private final Type type;
    private final String name;
    private final Throwable failure;

    private ExecutionEvent(Type type, String name) {
        this(type, name, null);
    }

    private ExecutionEvent(Type type, String name, Throwable failure) {
        this.type = requireNonNull(type, "'type' must not be null");
        this.name = requireNonBlank(name, "'name' must not be blank");
        this.failure = failure;
    }

    @Override
    public String toString() {
        return type + ":" + name;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Throwable getFailure() {
        return failure;
    }

    public String getStacktrace() {
        if (null == failure) return "";

        StringWriter sw = new StringWriter();
        failure.printStackTrace(new PrintWriter(sw, true));
        return MustacheUtils.passThrough(sw.toString());
    }

    public static ExecutionEvent before(String name) {
        return new ExecutionEvent(Type.BEFORE, name);
    }

    public static ExecutionEvent success(String name) {
        return new ExecutionEvent(Type.SUCCESS, name);
    }

    public static ExecutionEvent failure(String name, Throwable failure) {
        return new ExecutionEvent(Type.FAILURE, name, failure);
    }

    public enum Type {
        BEFORE,
        SUCCESS,
        FAILURE;

        public String formatted() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public static Type of(String str) {
            if (isBlank(str)) return null;
            return Type.valueOf(str.replace(" ", "_")
                .replace("-", "_")
                .toUpperCase(Locale.ENGLISH).trim());
        }
    }
}
