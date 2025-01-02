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
package org.jreleaser.logging;

import java.io.PrintWriter;
import java.util.ArrayDeque;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AbstractJReleaserLogger implements JReleaserLogger {
    private final ArrayDeque<String> prefix = new ArrayDeque<>();
    private final PrintWriter tracer;
    private String indent = "";

    protected AbstractJReleaserLogger(PrintWriter tracer) {
        this.tracer = tracer;
    }

    protected boolean isIndented() {
        return !"".equals(indent);
    }

    @Override
    public PrintWriter getTracer() {
        return tracer;
    }

    @Override
    public void close() {
        if (null == tracer) return;
        tracer.flush();
        tracer.close();
    }

    @Override
    public void reset() {
        this.prefix.clear();
        this.indent = "";
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix.push(prefix);
    }

    @Override
    public void restorePrefix() {
        if (!this.prefix.isEmpty()) {
            this.prefix.pop();
        }
    }

    @Override
    public void increaseIndent() {
        indent += "  ";
    }

    @Override
    public void decreaseIndent() {
        if (indent.length() > 0) {
            indent = indent.substring(0, indent.length() - 2);
        }
    }

    protected String formatMessage(String message) {
        return indent + (!prefix.isEmpty() ? "[" + prefix.peek() + "] " : "") + message;
    }

    @Override
    public void trace(String message) {
        tracer.println(message);
        tracer.flush();
    }

    @Override
    public void trace(String message, Throwable throwable) {
        tracer.println(message);
        printThrowable(throwable);
        tracer.flush();
    }

    @Override
    public void trace(Throwable throwable) {
        printThrowable(throwable);
    }

    private void printThrowable(Throwable throwable) {
        if (null != throwable) {
            throwable.printStackTrace(tracer);
            tracer.flush();
        }
    }
}
