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
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.Scoop;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import org.jreleaser.util.Constants;

import java.io.StringReader;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.MustacheUtils.applyTemplate;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ScoopToolProcessor extends AbstractRepositoryToolProcessor<Scoop> {
    public ScoopToolProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected boolean doPackageDistribution(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        copyPreparedFiles(distribution, props);
        return true;
    }

    @Override
    protected void fillToolProperties(Map<String, Object> props, Distribution distribution) throws ToolProcessingException {
        props.put(Constants.KEY_SCOOP_CHECKVER_URL, resolveCheckverUrl(props));
        props.put(Constants.KEY_SCOOP_AUTOUPDATE_URL, resolveAutoupdateUrl(props));
    }

    private Object resolveCheckverUrl(Map<String, Object> props) {
        if (!getTool().getCheckverUrl().contains("{{")) {
            return getTool().getCheckverUrl();
        }
        return applyTemplate(new StringReader(getTool().getCheckverUrl()), props);
    }

    private Object resolveAutoupdateUrl(Map<String, Object> props) {
        if (!getTool().getAutoupdateUrl().contains("{{")) {
            return getTool().getAutoupdateUrl();
        }

        Map<String, Object> copy = new LinkedHashMap<>(props);
        copy.put(Constants.KEY_PROJECT_VERSION, "$version");
        copy.put(Constants.KEY_ARTIFACT_FILE_NAME, copy.get("projectName") + "-$version.zip");
        return applyTemplate(new StringReader(getTool().getAutoupdateUrl()), copy);
    }

    @Override
    protected void writeFile(Project project, Distribution distribution, String content, Map<String, Object> props, String fileName)
        throws ToolProcessingException {
        Path outputDirectory = (Path) props.get(Constants.KEY_PREPARE_DIRECTORY);
        Path outputFile = outputDirectory.resolve(trimTplExtension(fileName));

        writeFile(content, outputFile);
    }
}
