/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
package org.jreleaser.model.internal.validation;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.NativeImageAssembler;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.util.Errors;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public abstract class NativeImageAssemblerResolver extends Validator {
    public static void resolveNativeImageOutputs(JReleaserContext context, Errors errors) {
        List<NativeImageAssembler> activeNativeImages = context.getModel().getAssemble().getActiveNativeImages();
        if (!activeNativeImages.isEmpty()) context.getLogger().debug("assemble.nativeImage");

        for (NativeImageAssembler nativeImage : activeNativeImages) {
            if (nativeImage.isExported()) resolveNativeImageOutputs(context, nativeImage, errors);
        }
    }

    private static void resolveNativeImageOutputs(JReleaserContext context, NativeImageAssembler nativeImage, Errors errors) {
        if (!context.isPlatformSelected(nativeImage.getGraal())) return;

        String imageName = nativeImage.getResolvedImageName(context);
        if (isNotBlank(nativeImage.getImageNameTransform())) {
            imageName = nativeImage.getResolvedImageNameTransform(context);
        }

        String platform = nativeImage.getGraal().getPlatform();
        String platformReplaced = nativeImage.getPlatform().applyReplacements(platform);

        Path image = context.getAssembleDirectory()
            .resolve(nativeImage.getName())
            .resolve(nativeImage.getType())
            .resolve(imageName + "-" +
                platformReplaced + "." +
                nativeImage.getArchiveFormat().extension());

        if (!Files.exists(image)) {
            errors.assembly(RB.$("validation_missing_assembly",
                nativeImage.getType(), nativeImage.getName(), nativeImage.getName()));
        } else {
            Artifact artifact = Artifact.of(image, platform);
            artifact.setExtraProperties(nativeImage.getExtraProperties());
            artifact.activate();
            nativeImage.addOutput(artifact);
        }
    }
}
