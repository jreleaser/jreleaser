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
package org.jreleaser.config;

import java.io.IOException;
import java.nio.file.Path;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * Base implementation of the {@code JReleaserConfigParser} interface.
 *
 * @author Andres Almiray
 * @since 1.5.0
 */
public abstract class AbstractJReleaserConfigParser implements JReleaserConfigParser {
    private final String fileExtension;

    protected AbstractJReleaserConfigParser(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    @Override
    public String getPreferredFileExtension() {
        return fileExtension;
    }

    @Override
    public boolean supports(Path configFile) {
        return supports(configFile.getFileName().toString());
    }

    @Override
    public boolean supports(String resource) {
        return isNotBlank(resource) && resource.endsWith("." + fileExtension);
    }

    @Override
    public void validate(Path configFile) throws IOException {
        // noop
    }
}
