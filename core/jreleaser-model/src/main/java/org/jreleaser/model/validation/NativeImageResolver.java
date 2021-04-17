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
package org.jreleaser.model.validation;

import org.jreleaser.model.Artifact;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.NativeImage;
import org.jreleaser.util.Errors;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public abstract class NativeImageResolver extends Validator {
    public static void resolveNativeImageOutputs(JReleaserContext context, Errors errors) {
        context.getLogger().debug("nativeImage");

        for (NativeImage nativeImage : context.getModel().getAssemble().getActiveNativeImages()) {
            resolveNativeImageOutputs(context, nativeImage, errors);
        }
    }

    private static void resolveNativeImageOutputs(JReleaserContext context, NativeImage nativeImage, Errors errors) {
        Path image = context.getOutputDirectory()
            .resolve(nativeImage.getName())
            .resolve("assemble")
            .resolve(nativeImage.getType())
            .resolve(nativeImage.getExecutable());

        if (!Files.exists(image)) {
            errors.assembly("Missing outputs for " + nativeImage.getType() + "." + nativeImage.getName() +
                ". Distribution " + nativeImage.getName() + " has not been assembled.");
        } else {
            nativeImage.addOutput(Artifact.of(image, nativeImage.getGraal().getPlatform()));
        }
    }
}
