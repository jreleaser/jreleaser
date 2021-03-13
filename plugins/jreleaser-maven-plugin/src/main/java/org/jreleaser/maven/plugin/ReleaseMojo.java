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
package org.jreleaser.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jreleaser.maven.plugin.internal.JReleaserModelConfigurer;
import org.jreleaser.maven.plugin.internal.JReleaserModelConverter;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.JReleaserModelValidator;
import org.jreleaser.model.releaser.spi.ReleaseException;
import org.jreleaser.model.releaser.spi.Releaser;
import org.jreleaser.releaser.Releasers;
import org.jreleaser.tools.Checksums;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

@Mojo(name = "release")
public class ReleaseMojo extends AbstractJReleaserMojo {
    /**
     * Skip execution.
     */
    @Parameter(property = "jreleaser.release.skip")
    private boolean skip;

    @Parameter(property = "jreleaser.release.dryrun")
    private boolean dryrun;

    @Parameter(required = true)
    private Jreleaser jreleaser;

    @Parameter(property = "jreleaser.checksum.directory", required = true)
    private File checksumDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Banner.display(project, getLog());
        if (skip) return;

        JReleaserModel jreleaserModel = JReleaserModelConverter.convert(jreleaser);
        JReleaserModelConfigurer.configure(jreleaserModel, project);
        List<String> errors = JReleaserModelValidator.validate(getLogger(), project.getBasedir().toPath(), jreleaserModel);
        if (!errors.isEmpty()) {
            getLog().error("== JReleaser ==");
            errors.forEach(getLog()::error);
            throw new MojoExecutionException("JReleaser for project " + project.getArtifactId() + " has not been properly configured.");
        }

        Path checksumsFilePath = checksumDirectory.toPath().resolve("checksums.txt");
        try {
            Checksums.collectAndWriteChecksums(getLogger(), jreleaserModel, checksumDirectory.toPath());
        } catch (JReleaserException e) {
            throw new MojoExecutionException("Unexpected error writing checksums to " + checksumsFilePath.toAbsolutePath(), e);
        }

        try {
            Releaser releaser = Releasers.findReleaser(getLogger(), jreleaserModel)
                .configureWith(project.getBasedir().toPath(), jreleaserModel)
                .build();
            releaser.release(dryrun);
        } catch (ReleaseException e) {
            throw new MojoExecutionException("Unexpected error", e);
        }
    }
}
