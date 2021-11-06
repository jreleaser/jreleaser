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
import org.jreleaser.model.Active;
import org.jreleaser.model.FileSet;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.NativeImage;
import org.jreleaser.util.Errors;
import org.jreleaser.util.PlatformUtils;

import java.util.Map;

import static org.jreleaser.model.validation.TemplateValidator.validateTemplate;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public abstract class NativeImageValidator extends Validator {
    public static void validateNativeImage(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        context.getLogger().debug("nativeImage");
        Map<String, NativeImage> nativeImage = context.getModel().getAssemble().getNativeImage();

        for (Map.Entry<String, NativeImage> e : nativeImage.entrySet()) {
            e.getValue().setName(e.getKey());
            validateNativeImage(context, mode, e.getValue(), errors);
        }
    }

    private static void validateNativeImage(JReleaserContext context, JReleaserContext.Mode mode, NativeImage nativeImage, Errors errors) {
        context.getLogger().debug("nativeImage.{}", nativeImage.getName());

        if (!nativeImage.isActiveSet()) {
            nativeImage.setActive(Active.NEVER);
        }
        if (!nativeImage.resolveEnabled(context.getModel().getProject())) return;

        if (isBlank(nativeImage.getName())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "nativeImage.name"));
            return;
        }

        if (isBlank(nativeImage.getExecutable())) {
            nativeImage.setExecutable(nativeImage.getName());
        }
        if (isBlank(nativeImage.getImageName())) {
            nativeImage.setImageName(nativeImage.getExecutable() + "-" +
                context.getModel().getProject().getResolvedVersion());
        }

        if (isBlank(nativeImage.getGraal().getPath())) {
            nativeImage.getGraal().setPath(System.getProperty("java.home"));
        }
        if (isBlank(nativeImage.getGraal().getPlatform())) {
            nativeImage.getGraal().setPlatform(PlatformUtils.getCurrentFull());
        }

        if (null == nativeImage.getMainJar()) {
            errors.configuration(RB.$("validation_is_null", "nativeImage." + nativeImage.getName() + ".mainJar"));
            return;
        }
        if (isBlank(nativeImage.getMainJar().getPath())) {
            errors.configuration(RB.$("validation_must_not_be_null", "nativeImage." + nativeImage.getName() + ".mainJar.path"));
        }

        validateGlobs(context,
            nativeImage.getJars(),
            "nativeImage." + nativeImage.getName() + ".jars",
            errors);

        if (mode == JReleaserContext.Mode.ASSEMBLE) {
            validateTemplate(context, nativeImage, errors);
        }

        if (!nativeImage.getFileSets().isEmpty()) {
            int i = 0;
            for (FileSet fileSet : nativeImage.getFileSets()) {
                validateFileSet(context, mode, nativeImage, fileSet, i++, errors);
            }
        }
    }
}
