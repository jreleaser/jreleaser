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
package org.jreleaser.templates;

import java.io.InputStream;
import java.io.Reader;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public class InputStreamTemplateResource implements TemplateResource {
    private final InputStream inputStream;

    public InputStreamTemplateResource(InputStream inputStream) {
        this.inputStream = requireNonNull(inputStream, "inputStream");
    }

    @Override
    public Reader getReader() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public boolean isReader() {
        return false;
    }

    @Override
    public boolean isInputStream() {
        return true;
    }

    @Override
    public void close() throws Exception {
        inputStream.close();
    }
}
