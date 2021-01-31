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
package org.kordamp.jreleaser.tools;

import org.kordamp.jreleaser.model.Brew;
import org.kordamp.jreleaser.model.Distribution;
import org.kordamp.jreleaser.model.JReleaserModel;
import org.kordamp.jreleaser.model.Project;
import org.kordamp.jreleaser.util.Logger;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.kordamp.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.kordamp.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class BrewToolProcessor extends AbstractToolProcessor<Brew> {
    public BrewToolProcessor(Logger logger, JReleaserModel model, Brew brew) {
        super(logger, model, brew);
    }

    @Override
    protected boolean doPackageDistribution(Distribution distribution, Map<String, Object> context) throws ToolProcessingException {
        getLogger().debug("Tool {} does not require additional packaging", getToolName());
        return true;
    }

    @Override
    protected Set<String> resolveByExtensionsFor(Distribution.DistributionType type) {
        Set<String> set = new LinkedHashSet<>();
        set.add(".zip");
        return set;
    }

    @Override
    protected void fillToolProperties(Map<String, Object> context, Distribution distribution) throws ToolProcessingException {
        if (!getTool().getDependencies().containsKey(Constants.KEY_JAVA_VERSION)) {
            getTool().getDependencies().put(":java", (String) context.get(Constants.KEY_DISTRIBUTION_JAVA_VERSION));
        }

        context.put(Constants.KEY_BREW_DEPENDENCIES, getTool().getDependencies()
            .entrySet().stream()
            .map(entry -> new Dependency(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList()));
    }

    @Override
    protected void writeFile(Project project, Distribution distribution, String content, Map<String, Object> context, String fileName)
        throws ToolProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputDirectory = (Path) context.get(Constants.KEY_PREPARE_DIRECTORY);
        Path outputFile = "formula.rb".equals(fileName) ?
            outputDirectory.resolve("Formula").resolve(distribution.getExecutable().concat(".rb")) :
            outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }

    public static class Dependency {
        private final String key;
        private final String value;

        public Dependency(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getFormattedDependency() {
            StringBuilder formatted = new StringBuilder();
            if (key.startsWith(":")) {
                formatted.append(key);
            } else {
                formatted.append("\"")
                    .append(key)
                    .append("\"");
            }
            if (isNotBlank(value)) {
                formatted.append(" => \"")
                    .append(value)
                    .append("\"");
            }
            return "!!" + formatted.toString() + "!!";
        }
    }
}
