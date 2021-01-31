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
package org.kordamp.jreleaser.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.kordamp.jreleaser.maven.plugin.internal.JReleaserModelConfigurer;
import org.kordamp.jreleaser.maven.plugin.internal.JReleaserModelConverter;
import org.kordamp.jreleaser.maven.plugin.internal.JReleaserModelPrinter;
import org.kordamp.jreleaser.model.JReleaserModel;
import org.kordamp.jreleaser.model.JReleaserModelValidator;

import java.io.PrintWriter;
import java.util.List;

@Mojo(name = "config")
public class ConfigMojo extends AbstractJReleaserMojo {
    @Parameter(required = true)
    private Jreleaser jreleaser;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Banner.display(project, getLog());

        JReleaserModel jreleaserModel = JReleaserModelConverter.convert(jreleaser);
        JReleaserModelConfigurer.configure(jreleaserModel, project);
        List<String> errors = JReleaserModelValidator.validate(getLogger(), project.getBasedir().toPath(), jreleaserModel);
        if (!errors.isEmpty()) {
            getLog().error("== JReleaser ==");
            errors.forEach(getLog()::error);
            throw new MojoExecutionException("JReleaser for project " + project.getArtifactId() + " has not been properly configured.");
        }

        new JReleaserModelPrinter(new PrintWriter(System.out, true)).print(jreleaserModel.asMap());
    }
}
