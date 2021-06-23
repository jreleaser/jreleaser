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
package org.jreleaser.config.yaml;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.sbaudoin.yamllint.Format;
import com.github.sbaudoin.yamllint.LintProblem;
import com.github.sbaudoin.yamllint.Linter;
import com.github.sbaudoin.yamllint.YamlLintConfig;
import com.github.sbaudoin.yamllint.YamlLintConfigException;
import org.jreleaser.config.JReleaserConfigParser;
import org.jreleaser.model.JReleaserModel;
import org.kordamp.jipsy.annotations.ServiceProviderFor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.lang.System.lineSeparator;
import static java.util.Arrays.asList;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@ServiceProviderFor(JReleaserConfigParser.class)
public class YamlJReleaserConfigParser implements JReleaserConfigParser {
    private static final String YAML_LINT_CONFIG = String.join(lineSeparator(), asList(
        "---",
        "rules:",
        "  indentation:",
        "    spaces: consistent",
        "    check-multi-line-strings: true",
        "    indent-sequences: true",
        "  comments-indentation: {}")) + lineSeparator();

    @Override
    public String getPreferredFileExtension() {
        return "yml";
    }

    @Override
    public boolean supports(Path configFile) {
        String fileName = configFile.getFileName().toString();
        return fileName.endsWith(".yml") || fileName.endsWith(".yaml");
    }

    @Override
    public void validate(Path configFile) throws IOException {
        YamlLintConfig config = null;
        try {
            config = new YamlLintConfig(YAML_LINT_CONFIG);
        } catch (YamlLintConfigException e) {
            return;
        }

        List<LintProblem> problems = Linter.run(config, configFile.toFile());

        if (!problems.isEmpty()) {
            throw new IOException(Format.format(configFile.toAbsolutePath().toString(),
                problems,
                Format.OutputFormat.AUTO));
        }
    }

    @Override
    public JReleaserModel parse(InputStream inputStream) throws IOException {
        YAMLMapper mapper = YAMLMapper.builder().build();
        return mapper.readValue(inputStream, JReleaserModel.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> properties(InputStream inputStream) throws IOException {
        YAMLMapper mapper = YAMLMapper.builder().build();
        return mapper.readValue(inputStream, Map.class);
    }
}
