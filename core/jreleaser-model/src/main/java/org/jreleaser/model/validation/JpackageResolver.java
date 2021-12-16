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
package org.jreleaser.model.validation;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.Jpackage;
import org.jreleaser.util.Errors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
public abstract class JpackageResolver extends Validator {
    public static void resolveJpackageOutputs(JReleaserContext context, Errors errors) {
        context.getLogger().debug("jpackage");

        for (Jpackage jpackage : context.getModel().getAssemble().getActiveJpackages()) {
            if (jpackage.isExported()) resolveJpackageOutputs(context, jpackage, errors);
        }
    }

    private static void resolveJpackageOutputs(JReleaserContext context, Jpackage jpackage, Errors errors) {
        Path baseOutputDirectory = context.getAssembleDirectory()
            .resolve(jpackage.getName())
            .resolve(jpackage.getType());

        Artifact jdk = jpackage.getResolvedPlatformPackager().getJdk();
        if (!context.isPlatformSelected(jdk)) return;

        Jpackage.PlatformPackager packager = jpackage.getResolvedPlatformPackager();
        String platform = jdk.getPlatform();

        for (String type : packager.getTypes()) {
            try {
                Optional<Path> file = Files.list(baseOutputDirectory)
                    .filter(path -> path.getFileName().toString().endsWith(type))
                    .findFirst();

                if (!file.isPresent()) {
                    errors.assembly("Missing outputs for " + jpackage.getType() + "." + jpackage.getName() +
                        ". Distribution " + jpackage.getName() + " has not been assembled.");
                } else {
                    Artifact artifact = Artifact.of(file.get(), platform);
                    artifact.setExtraProperties(jpackage.getExtraProperties());
                    artifact.activate();
                    jpackage.addOutput(artifact);
                }
            } catch (IOException e) {
                throw new JReleaserException(RB.$("ERROR_unexpected_error"), e);
            }
        }
    }
}
