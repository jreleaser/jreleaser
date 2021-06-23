/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
package org.jreleaser.config.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jreleaser.config.JReleaserConfigParser;
import org.jreleaser.model.JReleaserModel;
import org.kordamp.jipsy.annotations.ServiceProviderFor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@ServiceProviderFor(JReleaserConfigParser.class)
public class JsonJReleaserConfigParser implements JReleaserConfigParser {
    @Override
    public String getPreferredFileExtension() {
        return "json";
    }

    @Override
    public boolean supports(Path configFile) {
        return configFile.getFileName().toString().endsWith(".json");
    }

    @Override
    public void validate(Path configFile) throws IOException {
        // noop
    }

    @Override
    public JReleaserModel parse(InputStream inputStream) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(inputStream, JReleaserModel.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> properties(InputStream inputStream) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(inputStream, Map.class);
    }
}
