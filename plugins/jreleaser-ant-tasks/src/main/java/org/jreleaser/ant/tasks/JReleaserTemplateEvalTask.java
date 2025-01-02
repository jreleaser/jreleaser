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
package org.jreleaser.ant.tasks;

import org.jreleaser.engine.context.ModelValidator;
import org.jreleaser.engine.templates.TemplateEvaluator;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;

import java.nio.file.Path;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public class JReleaserTemplateEvalTask extends AbstractPlatformAwareJReleaserTask {
    private boolean assembly;
    private boolean download;
    private boolean changelog;
    private boolean announce;
    private boolean overwrite;
    private Path inputFile;
    private Path inputDir;
    private Path targetDir;

    public void setAssembly(boolean assembly) {
        this.assembly = assembly;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    public void setChangelog(boolean changelog) {
        this.changelog = changelog;
    }

    public void setAnnounce(boolean announce) {
        this.announce = announce;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public void setInputFile(Path inputFile) {
        this.inputFile = inputFile;
    }

    public void setInputDir(Path inputDir) {
        this.inputDir = inputDir;
    }

    public void setTargetDir(Path targetDir) {
        this.targetDir = targetDir;
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        ModelValidator.validate(context);

        if (null != inputFile) {
            TemplateEvaluator.generateTemplate(context, inputFile,
                context.relativizeToBasedir(targetDir), overwrite);
        } else if (null != inputDir) {
            TemplateEvaluator.generateTemplates(context, inputDir,
                context.relativizeToBasedir(targetDir), overwrite);
        }

        context.report();
    }

    @Override
    protected Mode getMode() {
        if (download) return Mode.DOWNLOAD;
        if (assembly) return Mode.ASSEMBLE;
        if (changelog) return Mode.CHANGELOG;
        if (announce) return Mode.ANNOUNCE;
        return Mode.CONFIG;
    }

    @Override
    protected JReleaserCommand getCommand() {
        return JReleaserCommand.CONFIG;
    }
}
