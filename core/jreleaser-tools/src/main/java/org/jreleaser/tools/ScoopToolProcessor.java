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

import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Project;
import org.jreleaser.model.Scoop;
import org.jreleaser.util.Constants;
import org.jreleaser.util.Logger;

import java.io.StringReader;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.MustacheUtils.applyTemplate;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ScoopToolProcessor extends AbstractToolProcessor<Scoop> {
    public ScoopToolProcessor(Logger logger, JReleaserModel model, Scoop scoop) {
        super(logger, model, scoop);
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
        context.put(Constants.KEY_SCOOP_CHECKVER_URL, resolveCheckverUrl(context));
        context.put(Constants.KEY_SCOOP_AUTOUPDATE_URL, resolveAutoupdateUrl(context));
    }

    private Object resolveCheckverUrl(Map<String, Object> context) {
        if (!getTool().getCheckverUrl().contains("{{")) {
            return getTool().getCheckverUrl();
        }
        return applyTemplate(new StringReader(getTool().getCheckverUrl()), context);
    }

    private Object resolveAutoupdateUrl(Map<String, Object> context) {
        if (!getTool().getAutoupdateUrl().contains("{{")) {
            return getTool().getAutoupdateUrl();
        }

        Map<String, Object> copy = new LinkedHashMap<>(context);
        copy.put(Constants.KEY_PROJECT_VERSION, "$version");
        copy.put(Constants.KEY_ARTIFACT_FILE_NAME, copy.get("projectName") + "-$version.zip");
        return applyTemplate(new StringReader(getTool().getAutoupdateUrl()), copy);
    }

    @Override
    protected void writeFile(Project project, Distribution distribution, String content, Map<String, Object> context, String fileName)
        throws ToolProcessingException {
        Path outputDirectory = (Path) context.get(Constants.KEY_PREPARE_DIRECTORY);
        Path outputFile = outputDirectory.resolve(trimTplExtension(fileName));

        writeFile(content, outputFile);
    }
}
