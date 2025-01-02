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

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public interface JReleaserLogger {
    PrintWriter getTracer();

    void close();

    void reset();

    void increaseIndent();

    void decreaseIndent();

    void setPrefix(String prefix);

    void restorePrefix();

    void plain(String message);

    void debug(String message);

    void info(String message);

    void warn(String message);

    void error(String message);

    void trace(String message);

    void plain(String message, Object... args);

    void debug(String message, Object... args);

    void info(String message, Object... args);

    void warn(String message, Object... args);

    void error(String message, Object... args);

    void plain(String message, Throwable throwable);

    void debug(String message, Throwable throwable);

    void info(String message, Throwable throwable);

    void warn(String message, Throwable throwable);

    void error(String message, Throwable throwable);

    void trace(String message, Throwable throwable);

    void trace(Throwable throwable);
}
