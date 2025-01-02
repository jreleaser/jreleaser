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
import org.jreleaser.model.internal.assemble.JlinkAssembler;
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
public final class JlinkAssemblerValidator {
    private JlinkAssemblerValidator() {
        // noop
    }

    public static void validateJlink(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, JlinkAssembler> jlink = context.getModel().getAssemble().getJlink();
        if (!jlink.isEmpty()) context.getLogger().debug("assemble.jlink");

        for (Map.Entry<String, JlinkAssembler> e : jlink.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig() || mode.validateAssembly()) {
                validateJlink(context, mode, e.getValue(), errors);
            }
        }
    }

    private static void validateJlink(JReleaserContext context, Mode mode, JlinkAssembler assembler, Errors errors) {
        context.getLogger().debug("assemble.jlink.{}", assembler.getName());

        resolveActivatable(context, assembler,
            listOf("assemble.jlink." + assembler.getName(), "assemble.jlink"),
            "NEVER");
        if (!assembler.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (isBlank(assembler.getName())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "jlink.name"));
            return;
        }

        context.getLogger().debug("assemble.jlink.{}.java", assembler.getName());
        if (!validateJava(context, assembler, errors)) {
            context.getLogger().debug(RB.$("validation.disabled.error"));
            assembler.disable();
            return;
        }
        if (isBlank(assembler.getJava().getGroupId())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "assembler." + assembler.getName() + ".java.groupId"));
        }

        assembler.setPlatform(assembler.getPlatform().mergeValues(context.getModel().getPlatform()));

        if (isBlank(assembler.getImageName())) {
            assembler.setImageName(assembler.getJava().getGroupId() + "." +
                assembler.getJava().getArtifactId() + "-" +
                context.getModel().getProject().getResolvedVersion());
        }
        if (isBlank(assembler.getExecutable())) {
            assembler.setExecutable(assembler.getName());
        }

        int i = 0;
        for (Artifact targetJdk : assembler.getTargetJdks()) {
            validateJdk(context, mode, assembler, targetJdk, i++, errors);
        }

        // validate jdks.platform is unique
        Map<String, List<Artifact>> byPlatform = assembler.getTargetJdks().stream()
            .filter(Artifact::isActiveAndSelected)
            .collect(groupingBy(jdk -> isBlank(jdk.getPlatform()) ? "<nil>" : jdk.getPlatform()));
        if (byPlatform.containsKey("<nil>")) {
            errors.configuration(RB.$("validation_jlink_jdk_platform", assembler.getName()));
        }
        // check platforms
        byPlatform.forEach((p, jdks) -> {
            if (jdks.size() > 1) {
                errors.configuration(RB.$("validation_jlink_jdk_multiple_platforms", assembler.getName(), p));
            }
        });

        if (isBlank(assembler.getJdk().getPath())) {
            String currentPlatform = PlatformUtils.getCurrentFull();
            String javaHome = System.getProperty("java.home");

            if (assembler.getTargetJdks().isEmpty()) {
                if (isBlank(javaHome)) {
                    // Can only happen when running as native-image, fail for now
                    // TODO: native-image
                    errors.configuration(RB.$("validation_java_home_missing"));
                    return;
                }
                // Use current
                assembler.getJdk().resolveEnabled(context.getModel().getProject());
                assembler.getJdk().setPath(javaHome);
                assembler.getJdk().setPlatform(currentPlatform);
                assembler.addTargetJdk(assembler.getJdk());
            } else {
                // find a compatible JDK in targets
                Optional<Artifact> jdk = assembler.getTargetJdks().stream()
                    .filter(Artifact::isActiveAndSelected)
                    .filter(j -> PlatformUtils.isCompatible(currentPlatform, j.getPlatform()))
                    .findFirst();

                if (jdk.isPresent()) {
                    assembler.setJdk(jdk.get());
                } else {
                    if (isBlank(javaHome)) {
                        // Can only happen when running as native-image, fail for now
                        // TODO: native-image
                        errors.configuration(RB.$("validation_java_home_missing"));
                        return;
                    }
                    // Can't tell if the current JDK will work but might as well use it
                    assembler.getJdk().setPath(javaHome);
                    assembler.getJdk().setPlatform(currentPlatform);
                }
            }
            assembler.getJdk().select();
        }

        if (assembler.getArgs().isEmpty()) {
            assembler.getArgs().add("--no-header-files");
            assembler.getArgs().add("--no-man-pages");
            assembler.getArgs().add("--strip-debug");
        }

        boolean hasJavaArchive = assembler.getJavaArchive().isSet();
        if (hasJavaArchive) {
            if (isBlank(assembler.getJavaArchive().getMainJarName())) {
                assembler.getJavaArchive().setMainJarName("{{projectName}}-{{projectVersion}}.jar");
            }
            if (isBlank(assembler.getJavaArchive().getLibDirectoryName())) {
                assembler.getJavaArchive().setLibDirectoryName("lib");
            }
        }

        if (!validateJavaAssembler(context, mode, assembler, errors, !hasJavaArchive)) {
            return;
        }

        if (!hasJavaArchive) {
            assembler.getMainJar().resolveActiveAndSelected(context);
        }

        if (null == assembler.getArchiveFormat()) {
            assembler.setArchiveFormat(Archive.Format.ZIP);
        }

        if (null == assembler.getOptions().getTimestamp()) {
            assembler.getOptions().setTimestamp(context.getModel().resolveArchiveTimestamp());
        }

        if (!assembler.getJdeps().isEnabledSet()) {
            assembler.getJdeps().setEnabled(true);
        }

        if (!assembler.getJdeps().isEnabled() && assembler.getModuleNames().isEmpty()) {
            assembler.getModuleNames().add("java.base");
        }

        if (!assembler.getModuleNames().isEmpty()) {
            assembler.getJdeps().setEnabled(false);
        }
    }

    private static void validateJdk(JReleaserContext context, Mode mode, JlinkAssembler jlink, Artifact jdk, int index, Errors errors) {
        if (mode == Mode.FULL) return;

        if (null == jdk) {
            errors.configuration(RB.$("validation_is_null", "jlink." + jlink.getName() + ".targetJdk[" + index + "]"));
            return;
        }
        if (!jdk.resolveActiveAndSelected(context)) return;
        if (isBlank(jdk.getPath())) {
            errors.configuration(RB.$("validation_must_not_be_null", "jlink." + jlink.getName() + ".targetJdk[" + index + "].path"));
        }

        if (isBlank(jdk.getPlatform())) {
            errors.configuration(RB.$("validation_is_missing", "jlink." + jlink.getName() + ".targetJdk[" + index + "].platform"));
        }

        if (isNotBlank(jdk.getPlatform()) && !PlatformUtils.isSupported(jdk.getPlatform().trim())) {
            context.getLogger().warn(RB.$("validation_jlink_platform",
                jlink.getName(), index, jdk.getPlatform(), System.lineSeparator(),
                PlatformUtils.getSupportedOsNames(), System.lineSeparator(), PlatformUtils.getSupportedOsArchs()));
        }
    }
}
