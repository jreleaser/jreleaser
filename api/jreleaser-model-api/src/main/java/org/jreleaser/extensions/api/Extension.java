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
package org.jreleaser.extensions.api;

import java.util.Set;

/**
 * Provides a collection of {@code ExtensionPoints}.
 * <p>
 * Every extension must define a unique name.
 * <p>
 * Extensions are loaded using the standard ServiceProvider mechanism,
 * (see <a href="https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html">
 * https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html</a>
 * for more details).
 *
 * @author Andres Almiray
 * @since 1.3.0
 */
public interface Extension {
    /**
     * The given name of the extension.
     *
     * @return a non {@code null} String.
     */
    String getName();

    /**
     * A collection of {@code ExtensionPoint} instances.
     * <p>
     *
     * @return a non {@code null} collection.
     */
    Set<ExtensionPoint> provides();
}
