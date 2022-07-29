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
package org.jreleaser.model.validation;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.Archive;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.FileSet;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.NativeImage;
import org.jreleaser.util.Errors;
import org.jreleaser.util.PlatformUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;
import static org.jreleaser.model.validation.TemplateValidator.validateTemplate;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

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

        if (null == nativeImage.getStereotype()) {
            nativeImage.setStereotype(context.getModel().getProject().getStereotype());
        }

        if (isBlank(nativeImage.getName())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "nativeImage.name"));
            return;
        }

        if (null == nativeImage.getMainJar()) {
            errors.configuration(RB.$("validation_is_null", "nativeImage." + nativeImage.getName() + ".mainJar"));
            return;
        }

        nativeImage.setPlatform(nativeImage.getPlatform().mergeValues(context.getModel().getPlatform()));

        if (isBlank(nativeImage.getExecutable())) {
            nativeImage.setExecutable(nativeImage.getName());
        }
        if (isBlank(nativeImage.getImageName())) {
            nativeImage.setImageName(nativeImage.getExecutable() + "-" +
                context.getModel().getProject().getResolvedVersion());
        }

        int i = 0;
        for (Artifact graalJdk : nativeImage.getGraalJdks()) {
            validateJdk(context, mode, nativeImage, graalJdk, i++, errors);
        }

        // validate jdks.platform is unique
        Map<String, List<Artifact>> byPlatform = nativeImage.getGraalJdks().stream()
            .collect(groupingBy(jdk -> isBlank(jdk.getPlatform()) ? "<nil>" : jdk.getPlatform()));
        if (byPlatform.containsKey("<nil>")) {
            errors.configuration(RB.$("validation_nativeimage_jdk_platform", nativeImage.getName()));
        }
        // check platforms
        byPlatform.forEach((p, jdks) -> {
            if (jdks.size() > 1) {
                errors.configuration(RB.$("validation_nativeimage_jdk_multiple_platforms", nativeImage.getName(), p));
            }
        });

        if (isBlank(nativeImage.getGraal().getPath())) {
            String currentPlatform = PlatformUtils.getCurrentFull();
            String javaHome = System.getProperty("java.home");
            if (nativeImage.getGraalJdks().isEmpty()) {
                if (isBlank(javaHome)) {
                    // Can only happen when running as native-image, fail for now
                    // TODO: native-image
                    errors.configuration(RB.$("validation_java_home_missing"));
                    return;
                }
                // Use current
                nativeImage.getGraal().setPath(javaHome);
                nativeImage.getGraal().setPlatform(currentPlatform);
            } else {
                // find a compatible JDK in targets
                Optional<Artifact> jdk = nativeImage.getGraalJdks().stream()
                    .filter(j -> PlatformUtils.isCompatible(currentPlatform, j.getPlatform()))
                    .findFirst();

                if (jdk.isPresent()) {
                    nativeImage.setGraal(jdk.get());
                } else {
                    if (isBlank(javaHome)) {
                        // Can only happen when running as native-image, fail for now
                        // TODO: native-image
                        errors.configuration(RB.$("validation_java_home_missing"));
                        return;
                    }
                    // Can't tell if the current JDK will work but might as well use it
                    nativeImage.getGraal().setPath(javaHome);
                    nativeImage.getGraal().setPlatform(currentPlatform);
                }
            }
        }

        if (isBlank(nativeImage.getMainJar().getPath())) {
            errors.configuration(RB.$("validation_must_not_be_null", "nativeImage." + nativeImage.getName() + ".mainJar.path"));
        }
        if (null == nativeImage.getArchiveFormat()) {
            nativeImage.setArchiveFormat(Archive.Format.ZIP);
        }

        validateGlobs(context,
            nativeImage.getJars(),
            "nativeImage." + nativeImage.getName() + ".jars",
            errors);

        if (mode == JReleaserContext.Mode.ASSEMBLE) {
            validateTemplate(context, nativeImage, errors);
        }

        if (!nativeImage.getFileSets().isEmpty()) {
            i = 0;
            for (FileSet fileSet : nativeImage.getFileSets()) {
                validateFileSet(context, mode, nativeImage, fileSet, i++, errors);
            }
        }

        NativeImage.Upx upx = nativeImage.getUpx();
        if (!upx.isActiveSet()) {
            upx.setActive(Active.NEVER);
        }
        if (!upx.resolveEnabled(context.getModel().getProject())) return;

        if (isBlank(upx.getVersion())) {
            errors.configuration(RB.$("validation_is_missing", "nativeImage." + nativeImage.getName() + ".upx.version"));
        }
    }

    private static void validateJdk(JReleaserContext context, JReleaserContext.Mode mode, NativeImage nativeImage, Artifact jdk, int index, Errors errors) {
        if (mode == JReleaserContext.Mode.FULL) return;

        if (null == jdk) {
            errors.configuration(RB.$("validation_is_null", "nativeImage." + nativeImage.getName() + ".graalJdk[" + index + "]"));
            return;
        }
        if (isBlank(jdk.getPath())) {
            errors.configuration(RB.$("validation_must_not_be_null", "nativeImage." + nativeImage.getName() + ".graalJdk[" + index + "].path"));
        }
        if (isNotBlank(jdk.getPlatform()) && !PlatformUtils.isSupported(jdk.getPlatform().trim())) {
            context.getLogger().warn(RB.$("validation_nativeimage_platform",
                nativeImage.getName(), index, jdk.getPlatform(), System.lineSeparator(),
                PlatformUtils.getSupportedOsNames(), System.lineSeparator(), PlatformUtils.getSupportedOsArchs()));
        }
    }
}
