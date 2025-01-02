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
package org.jreleaser.model.internal.validation.assemble;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Archive;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.NativeImageAssembler;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.util.Errors;
import org.jreleaser.util.PlatformUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;
import static org.jreleaser.model.internal.validation.assemble.AssemblersValidator.validateJava;
import static org.jreleaser.model.internal.validation.assemble.AssemblersValidator.validateJavaAssembler;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public final class NativeImageAssemblerValidator {
    private NativeImageAssemblerValidator() {
        // noop
    }

    public static void validateNativeImage(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, NativeImageAssembler> nativeImage = context.getModel().getAssemble().getNativeImage();
        if (!nativeImage.isEmpty()) context.getLogger().debug("assemble.nativeImage");

        for (Map.Entry<String, NativeImageAssembler> e : nativeImage.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig() || mode.validateAssembly()) {
                validateNativeImage(context, mode, e.getValue(), errors);
            }
        }
    }

    private static void validateNativeImage(JReleaserContext context, Mode mode, NativeImageAssembler assembler, Errors errors) {
        context.getLogger().debug("assemble.nativeImage.{}", assembler.getName());

        resolveActivatable(context, assembler,
            listOf("assemble.native.image." + assembler.getName(), "assemble.native.image"),
            "NEVER");
        if (!assembler.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (null == assembler.getStereotype()) {
            assembler.setStereotype(context.getModel().getProject().getStereotype());
        }

        if (isBlank(assembler.getName())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "nativeImage.name"));
            context.getLogger().debug(RB.$("validation.disabled.error"));
            assembler.disable();
            return;
        }

        assembler.getMainJar().resolveActiveAndSelected(context);

        context.getLogger().debug("assemble.nativeImage.{}.java", assembler.getName());
        validateJava(context, assembler, errors);

        assembler.setPlatform(assembler.getPlatform().mergeValues(context.getModel().getPlatform()));

        if (isBlank(assembler.getExecutable())) {
            assembler.setExecutable(assembler.getName());
        }
        if (isBlank(assembler.getImageName())) {
            assembler.setImageName(assembler.getExecutable() + "-" +
                context.getModel().getProject().getResolvedVersion());
        }

        int i = 0;
        for (Artifact graalJdk : assembler.getGraalJdks()) {
            validateJdk(context, mode, assembler, graalJdk, i++, errors);
        }

        // validate jdks.platform is unique
        Map<String, List<Artifact>> byPlatform = assembler.getGraalJdks().stream()
            .filter(Artifact::isActiveAndSelected)
            .collect(groupingBy(jdk -> isBlank(jdk.getPlatform()) ? "<nil>" : jdk.getPlatform()));
        if (byPlatform.containsKey("<nil>")) {
            errors.configuration(RB.$("validation_nativeimage_jdk_platform", assembler.getName()));
        }
        // check platforms
        byPlatform.forEach((p, jdks) -> {
            if (jdks.size() > 1) {
                errors.configuration(RB.$("validation_nativeimage_jdk_multiple_platforms", assembler.getName(), p));
            }
        });

        if (isBlank(assembler.getGraal().getPath())) {
            String currentPlatform = PlatformUtils.getCurrentFull();
            String javaHome = System.getProperty("java.home");
            if (assembler.getGraalJdks().isEmpty()) {
                if (isBlank(javaHome)) {
                    // Can only happen when running as native-image, fail for now
                    // TODO: native-image
                    errors.configuration(RB.$("validation_java_home_missing"));
                    return;
                }
                // Use current
                assembler.getGraal().setPath(javaHome);
                assembler.getGraal().setPlatform(currentPlatform);
                assembler.getGraal().resolveActiveAndSelected(context);
            } else {
                // find a compatible JDK in targets
                Optional<Artifact> jdk = assembler.getGraalJdks().stream()
                    .filter(Artifact::isActiveAndSelected)
                    .filter(j -> PlatformUtils.isCompatible(currentPlatform, j.getPlatform()))
                    .findFirst();

                if (jdk.isPresent()) {
                    assembler.setGraal(jdk.get());
                } else {
                    if (isBlank(javaHome)) {
                        // Can only happen when running as native-image, fail for now
                        // TODO: native-image
                        errors.configuration(RB.$("validation_java_home_missing"));
                        return;
                    }
                    // Can't tell if the current JDK will work but might as well use it
                    assembler.getGraal().setPath(javaHome);
                    assembler.getGraal().setPlatform(currentPlatform);
                    assembler.getGraal().resolveActiveAndSelected(context);
                }
            }
        }

        if (null == assembler.getArchiveFormat()) {
            assembler.setArchiveFormat(Archive.Format.ZIP);
        }

        if (null == assembler.getOptions().getTimestamp()) {
            assembler.getOptions().setTimestamp(context.getModel().resolveArchiveTimestamp());
        }

        validateJavaAssembler(context, mode, assembler, errors, true);

        assembler.getComponents().remove("native-image");

        NativeImageAssembler.Upx upx = assembler.getUpx();
        resolveActivatable(context, upx,
            listOf("assemble.native.image." + assembler.getName() + ".upx", "assemble.native.image.upx"),
            "NEVER");
        if (!upx.resolveEnabled(context.getModel().getProject())) return;

        if (isBlank(upx.getVersion())) {
            errors.configuration(RB.$("validation_is_missing", "nativeImage." + assembler.getName() + ".upx.version"));
        }
    }

    private static void validateJdk(JReleaserContext context, Mode mode, NativeImageAssembler nativeImage, Artifact jdk, int index, Errors errors) {
        if (mode == Mode.FULL) return;

        if (null == jdk) {
            errors.configuration(RB.$("validation_is_null", "nativeImage." + nativeImage.getName() + ".graalJdk[" + index + "]"));
            return;
        }
        if (!jdk.resolveActiveAndSelected(context)) return;
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
