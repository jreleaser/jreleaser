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
package org.jreleaser.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jreleaser.ant.tasks.internal.JReleaserLoggerAdapter;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.templates.TemplateGenerationException;
import org.jreleaser.templates.TemplateGenerator;
import org.jreleaser.util.JReleaserLogger;

import java.nio.file.Path;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserGenerateTemplateTask extends Task {
    private boolean skip;
    private String distributionName;
    private Distribution.DistributionType distributionType = Distribution.DistributionType.JAVA_BINARY;
    private String toolName;
    private boolean overwrite;
    private boolean snapshot;

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public void setDistributionName(String distributionName) {
        this.distributionName = distributionName;
    }

    public void setDistributionType(Distribution.DistributionType distributionType) {
        this.distributionType = distributionType;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public void setSnapshot(boolean snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public void execute() throws BuildException {
        Banner.display(getLogger());
        if (skip) return;

        try {
            Path outputDirectory = getProject().getBaseDir().toPath()
                .resolve("src")
                .resolve("distributions");

            Path output = TemplateGenerator.builder()
                .logger(getLogger())
                .distributionName(distributionName)
                .distributionType(distributionType)
                .toolName(toolName)
                .outputDirectory(outputDirectory)
                .overwrite(overwrite)
                .snapshot(snapshot)
                .build()
                .generate();

            if (null != output) {
                getLogger().info("Template generated at {}", output.toAbsolutePath());
            }
        } catch (TemplateGenerationException e) {
            throw new JReleaserException("Unexpected error", e);
        }
    }

    private JReleaserLogger getLogger() {
        return new JReleaserLoggerAdapter(getProject());
    }
}
