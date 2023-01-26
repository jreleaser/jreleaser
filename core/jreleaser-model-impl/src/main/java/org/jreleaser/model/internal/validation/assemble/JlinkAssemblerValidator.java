/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.JlinkAssembler;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.FileSet;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.util.Errors;
import org.jreleaser.util.PlatformUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;
import static org.jreleaser.model.internal.validation.common.TemplateValidator.validateTemplate;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateFileSet;
import static org.jreleaser.model.internal.validation.common.Validator.validateGlobs;
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

    private static void validateJlink(JReleaserContext context, Mode mode, JlinkAssembler jlink, Errors errors) {
        context.getLogger().debug("assemble.jlink.{}", jlink.getName());

        resolveActivatable(context, jlink,
            listOf("assemble.jlink." + jlink.getName(), "assemble.jlink"),
            "NEVER");
        if (!jlink.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (isBlank(jlink.getName())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "jlink.name"));
            return;
        }
        if (null == jlink.getStereotype()) {
            jlink.setStereotype(context.getModel().getProject().getStereotype());
        }

        context.getLogger().debug("assemble.jlink.{}.java", jlink.getName());
        if (!validateJava(context, jlink, errors)) {
            context.getLogger().debug(RB.$("validation.disabled.error"));
            jlink.disable();
            return;
        }

        jlink.setPlatform(jlink.getPlatform().mergeValues(context.getModel().getPlatform()));

        if (isBlank(jlink.getImageName())) {
            jlink.setImageName(jlink.getJava().getGroupId() + "." +
                jlink.getJava().getArtifactId() + "-" +
                context.getModel().getProject().getResolvedVersion());
        }
        if (isBlank(jlink.getExecutable())) {
            jlink.setExecutable(jlink.getName());
        }

        int i = 0;
        for (Artifact targetJdk : jlink.getTargetJdks()) {
            validateJdk(context, mode, jlink, targetJdk, i++, errors);
        }

        // validate jdks.platform is unique
        Map<String, List<Artifact>> byPlatform = jlink.getTargetJdks().stream()
            .collect(groupingBy(jdk -> isBlank(jdk.getPlatform()) ? "<nil>" : jdk.getPlatform()));
        if (byPlatform.containsKey("<nil>")) {
            errors.configuration(RB.$("validation_jlink_jdk_platform", jlink.getName()));
        }
        // check platforms
        byPlatform.forEach((p, jdks) -> {
            if (jdks.size() > 1) {
                errors.configuration(RB.$("validation_jlink_jdk_multiple_platforms", jlink.getName(), p));
            }
        });

        if (isBlank(jlink.getJdk().getPath())) {
            String currentPlatform = PlatformUtils.getCurrentFull();
            String javaHome = System.getProperty("java.home");

            if (jlink.getTargetJdks().isEmpty()) {
                if (isBlank(javaHome)) {
                    // Can only happen when running as native-image, fail for now
                    // TODO: native-image
                    errors.configuration(RB.$("validation_java_home_missing"));
                    return;
                }
                // Use current
                jlink.getJdk().setPath(javaHome);
                jlink.getJdk().setPlatform(currentPlatform);
                jlink.addTargetJdk(jlink.getJdk());
            } else {
                // find a compatible JDK in targets
                Optional<Artifact> jdk = jlink.getTargetJdks().stream()
                    .filter(j -> PlatformUtils.isCompatible(currentPlatform, j.getPlatform()))
                    .findFirst();

                if (jdk.isPresent()) {
                    jlink.setJdk(jdk.get());
                } else {
                    if (isBlank(javaHome)) {
                        // Can only happen when running as native-image, fail for now
                        // TODO: native-image
                        errors.configuration(RB.$("validation_java_home_missing"));
                        return;
                    }
                    // Can't tell if the current JDK will work but might as well use it
                    jlink.getJdk().setPath(javaHome);
                    jlink.getJdk().setPlatform(currentPlatform);
                }
            }
        }

        if (jlink.getArgs().isEmpty()) {
            jlink.getArgs().add("--no-header-files");
            jlink.getArgs().add("--no-man-pages");
            jlink.getArgs().add("--compress=2");
            jlink.getArgs().add("--strip-debug");
        }

        if (null == jlink.getMainJar()) {
            errors.configuration(RB.$("validation_is_null", "jlink." + jlink.getName() + ".mainJar"));
            return;
        }
        if (isBlank(jlink.getMainJar().getPath())) {
            errors.configuration(RB.$("validation_must_not_be_null", "jlink." + jlink.getName() + ".mainJar.path"));
        }

        validateGlobs(
            jlink.getJars(),
            "jlink." + jlink.getName() + ".jars",
            errors);

        validateGlobs(
            jlink.getFiles(),
            "jlink." + jlink.getName() + ".files",
            errors);

        if (mode == Mode.ASSEMBLE) {
            validateTemplate(context, jlink, errors);
        }

        if (!jlink.getFileSets().isEmpty()) {
            i = 0;
            for (FileSet fileSet : jlink.getFileSets()) {
                validateFileSet(mode, jlink, fileSet, i++, errors);
            }
        }

        if (!jlink.getJdeps().isEnabledSet()) {
            jlink.getJdeps().setEnabled(true);
        }

        if (!jlink.getJdeps().isEnabled() && jlink.getModuleNames().isEmpty()) {
            jlink.getModuleNames().add("java.base");
        }

        if (!jlink.getModuleNames().isEmpty()) {
            jlink.getJdeps().setEnabled(false);
        }
    }

    private static boolean validateJava(JReleaserContext context, JlinkAssembler jlink, Errors errors) {
        Project project = context.getModel().getProject();

        if (!jlink.getJava().isEnabledSet() && project.getJava().isEnabledSet()) {
            jlink.getJava().setEnabled(project.getJava().isEnabled());
        }
        if (!jlink.getJava().isEnabledSet()) {
            jlink.getJava().setEnabled(jlink.getJava().isSet());
        }

        if (!jlink.getJava().isEnabled()) return false;

        if (isBlank(jlink.getJava().getArtifactId())) {
            jlink.getJava().setArtifactId(project.getJava().getArtifactId());
        }
        if (isBlank(jlink.getJava().getGroupId())) {
            jlink.getJava().setGroupId(project.getJava().getGroupId());
        }
        if (isBlank(jlink.getJava().getVersion())) {
            jlink.getJava().setVersion(project.getJava().getVersion());
        }
        if (isBlank(jlink.getJava().getMainModule())) {
            jlink.getJava().setMainModule(project.getJava().getMainModule());
        }
        if (isBlank(jlink.getJava().getMainClass())) {
            jlink.getJava().setMainClass(project.getJava().getMainClass());
        }

        if (isBlank(jlink.getJava().getGroupId())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "jlink." + jlink.getName() + ".java.groupId"));
        }

        return true;
    }

    private static void validateJdk(JReleaserContext context, Mode mode, JlinkAssembler jlink, Artifact jdk, int index, Errors errors) {
        if (mode == Mode.FULL) return;

        if (null == jdk) {
            errors.configuration(RB.$("validation_is_null", "jlink." + jlink.getName() + ".targetJdk[" + index + "]"));
            return;
        }
        if (isBlank(jdk.getPath())) {
            errors.configuration(RB.$("validation_must_not_be_null", "jlink." + jlink.getName() + ".targetJdk[" + index + "].path"));
        }
        if (isNotBlank(jdk.getPlatform()) && !PlatformUtils.isSupported(jdk.getPlatform().trim())) {
            context.getLogger().warn(RB.$("validation_jlink_platform",
                jlink.getName(), index, jdk.getPlatform(), System.lineSeparator(),
                PlatformUtils.getSupportedOsNames(), System.lineSeparator(), PlatformUtils.getSupportedOsArchs()));
        }
    }
}
