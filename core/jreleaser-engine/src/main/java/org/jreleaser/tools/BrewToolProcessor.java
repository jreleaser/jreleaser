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
package org.jreleaser.tools;

import org.jreleaser.model.Brew;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import org.jreleaser.util.Constants;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jreleaser.templates.TemplateUtils.trimTplExtension;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class BrewToolProcessor extends AbstractToolProcessor<Brew> {
    public BrewToolProcessor(JReleaserContext context, Brew brew) {
        super(context, brew);
    }

    @Override
    protected boolean doPackageDistribution(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        copyPreparedFiles(distribution, props);
        return true;
    }

    @Override
    protected Set<String> resolveByExtensionsFor(Distribution.DistributionType type) {
        Set<String> set = new LinkedHashSet<>();
        set.add(".zip");
        return set;
    }

    @Override
    protected void fillToolProperties(Map<String, Object> props, Distribution distribution) throws ToolProcessingException {
        getTool().addDependency(":java", (String) props.get(Constants.KEY_DISTRIBUTION_JAVA_VERSION));

        props.put(Constants.KEY_BREW_DEPENDENCIES, getTool().getDependenciesAsList()
            .stream()
            // prevent Mustache from converting quotes into &quot;
            .map(dependency -> "!!" + dependency.toString() + "!!")
            .collect(Collectors.toList()));
    }

    @Override
    protected void writeFile(Project project, Distribution distribution, String content, Map<String, Object> props, String fileName)
        throws ToolProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputDirectory = (Path) props.get(Constants.KEY_PREPARE_DIRECTORY);
        Path outputFile = "formula.rb".equals(fileName) ?
            outputDirectory.resolve("Formula").resolve(distribution.getExecutable().concat(".rb")) :
            outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }
}
