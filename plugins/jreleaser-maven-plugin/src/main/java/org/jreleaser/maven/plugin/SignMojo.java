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
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.signer.Signer;
import org.jreleaser.signer.SigningException;
import org.jreleaser.util.Logger;

import java.io.File;

import static org.jreleaser.maven.plugin.ChecksumsMojo.checksums;

@Mojo(name = "sign")
public class SignMojo extends AbstractJReleaserMojo {
    /**
     * Skip execution.
     */
    @Parameter(property = "jreleaser.sign.skip")
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Banner.display(project, getLog());
        if (skip) return;

        JReleaserModel jreleaserModel = convertAndValidateModel();
        checksums(getLogger(), jreleaserModel, outputDirectory);
        sign(getLogger(), jreleaserModel, outputDirectory);
    }

    static void sign(Logger logger, JReleaserModel jreleaserModel, File outputDirectory) throws MojoExecutionException {
        try {
            Signer.sign(logger,
                jreleaserModel,
                outputDirectory.toPath());
        } catch (SigningException e) {
            throw new MojoExecutionException("Unexpected error when signing artifacts", e);
        }
    }
}
