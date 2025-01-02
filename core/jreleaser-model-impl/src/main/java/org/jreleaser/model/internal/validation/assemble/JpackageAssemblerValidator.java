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
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.JlinkAssembler;
import org.jreleaser.model.internal.assemble.JpackageAssembler;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Java;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.util.Errors;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.version.SemanticVersion;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.groupingBy;
import static org.jreleaser.model.internal.validation.assemble.AssemblersValidator.validateJavaAssembler;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
public final class JpackageAssemblerValidator {
    private static final String MAC_IDENTIFIER = "[a-zA-Z0-9][a-zA-Z0-9\\.\\-]*";
    private static final Pattern MAC_IDENTIFIER_PATTERN = Pattern.compile(MAC_IDENTIFIER);

    private JpackageAssemblerValidator() {
        // noop
    }

    public static void validateJpackage(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, JpackageAssembler> jpackage = context.getModel().getAssemble().getJpackage();
        if (!jpackage.isEmpty()) context.getLogger().debug("assemble.jpackage");

        for (Map.Entry<String, JpackageAssembler> e : jpackage.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig() || mode.validateAssembly()) {
                validateJpackage(context, mode, e.getValue(), errors);
            }
        }
    }

    public static void postValidateJpackage(JReleaserContext context) {
        context.getLogger().debug("assemble.jpackage");
        Map<String, JpackageAssembler> jpackage = context.getModel().getAssemble().getJpackage();

        for (Map.Entry<String, JpackageAssembler> e : jpackage.entrySet()) {
            postValidateJpackage(context, e.getValue());
        }
    }

    private static void validateJpackage(JReleaserContext context, Mode mode, JpackageAssembler assembler, Errors errors) {
        context.getLogger().debug("assemble.jpackage.{}", assembler.getName());

        resolveActivatable(context, assembler,
            listOf("assemble.jpackage." + assembler.getName(), "assemble.jpackage"),
            "NEVER");

        Project project = context.getModel().getProject();
        if (!assembler.resolveEnabled(project)) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        JpackageAssembler.PlatformPackager packager = assembler.getResolvedPlatformPackager();
        JpackageAssembler.ApplicationPackage applicationPackage = assembler.getApplicationPackage();
        packager.enable();

        assembler.setPlatform(assembler.getPlatform().mergeValues(context.getModel().getPlatform()));

        boolean hasJavaArchive = false;
        if (isNotBlank(assembler.getJlink())) {
            JlinkAssembler jlink = context.getModel().getAssemble().findJlink(assembler.getJlink());
            hasJavaArchive = jlink.getJavaArchive().isSet();

            Path baseOutputDirectory = context.getAssembleDirectory()
                .resolve(jlink.getName())
                .resolve(jlink.getType());

            String imageName = jlink.getResolvedImageName(context);

            List<Artifact> candidateRuntimeImages = new ArrayList<>();
            for (Artifact targetJdk : jlink.getTargetJdks()) {
                if (!context.isPlatformSelected(targetJdk)) continue;

                String platform = targetJdk.getPlatform();
                String platformReplaced = assembler.getPlatform().applyReplacements(platform);
                Path path = baseOutputDirectory
                    .resolve("work-" + platform)
                    .resolve(imageName + "-" + platformReplaced)
                    .toAbsolutePath();
                candidateRuntimeImages.add(Artifact.of(path, platform));
            }

            if (!assembler.getRuntimeImages().isEmpty() && assembler.getRuntimeImages().size() != candidateRuntimeImages.size()) {
                errors.configuration(RB.$("validation_jpackage_jlink_application", assembler.getName()));
            }

            int count = 0;
            for (Artifact runtimeImage : assembler.getRuntimeImages()) {
                Path rp = runtimeImage.getResolvedPath(context, assembler);
                Path tp = runtimeImage.getResolvedTransform(context, assembler);
                Path path = null != tp ? tp : rp;
                if (candidateRuntimeImages.stream()
                    .anyMatch(a -> a.getPath().equals(path.toString()))) {
                    count++;
                }
            }

            if (!assembler.getRuntimeImages().isEmpty() && count != candidateRuntimeImages.size()) {
                errors.configuration(RB.$("validation_jpackage_jlink_application", assembler.getName()));
            }

            assembler.setJava(jlink.getJava());
            assembler.setMainJar(jlink.getMainJar());
            assembler.setJars(jlink.getJars());
            packager.setJdk(jlink.getJdk());
            if (isBlank(assembler.getExecutable())) {
                assembler.setExecutable(jlink.getExecutable());
            }

            for (Artifact runtimeImage : candidateRuntimeImages) {
                runtimeImage.resolveActiveAndSelected(context);
                assembler.addRuntimeImage(runtimeImage);
            }
        }

        if (!hasJavaArchive) {
            assembler.getMainJar().resolveActiveAndSelected(context);
        }

        context.getLogger().debug("assemble.jpackage.{}.java", assembler.getName());
        if (!validateJava(context, assembler, errors)) {
            context.getLogger().debug(RB.$("validation.disabled.error"));
            assembler.disable();
            return;
        }

        if (isBlank(assembler.getExecutable())) {
            assembler.setExecutable(assembler.getName());
        }

        if (assembler.getRuntimeImages().isEmpty()) {
            errors.configuration(RB.$("validation_jpackage_runtime_images_missing", assembler.getName()));
            return;
        }

        int i = 0;
        for (Artifact runtimeImage : assembler.getRuntimeImages()) {
            validateRuntimeImage(context, mode, assembler, runtimeImage, i++, errors);
        }

        // validate jdks.platform is unique
        Map<String, List<Artifact>> byPlatform = assembler.getRuntimeImages().stream()
            .collect(groupingBy(ri -> isBlank(ri.getPlatform()) ? "<nil>" : ri.getPlatform()));
        if (byPlatform.containsKey("<nil>")) {
            errors.configuration(RB.$("validation_jpackage_runtime_image_platform", assembler.getName()));
        }
        // check platforms
        byPlatform.forEach((p, jdks) -> {
            if (jdks.size() > 1) {
                errors.configuration(RB.$("validation_jpackage_runtime_image_multiple_platforms", assembler.getName(), p));
            }
        });

        if (isBlank(packager.getJdk().getPath())) {
            String javaHome = System.getProperty("java.home");
            if (isBlank(javaHome)) {
                // Can only happen when running as native-image, fail for now
                // TODO: native-image
                errors.configuration(RB.$("validation_java_home_missing"));
                return;
            }
            packager.getJdk().setPath(javaHome);
            packager.getJdk().setPlatform(PlatformUtils.getCurrentFull());
            packager.getJdk().resolveActiveAndSelected(context);
        }

        if (packager.getTypes().isEmpty()) {
            packager.setTypes(singletonList(packager.getValidTypes().get(0)));
        }

        if (isBlank(applicationPackage.getAppName())) {
            applicationPackage.setAppName(assembler.getName());
        }

        if (isBlank(applicationPackage.getAppVersion())) {
            applicationPackage.setAppVersion(project.getResolvedVersion());
        }

        // validate appVersion
        String appVersion = applicationPackage.getResolvedAppVersion(context, assembler);
        try {
            SemanticVersion v = SemanticVersion.of(appVersion);
            if (isNotBlank(v.getBuild()) || isNotBlank(v.getTag())) {
                errors.configuration(RB.$("validation_jpackage_invalid_appversion_t", appVersion));
            }
            if (PlatformUtils.isMac() && v.getMajor() <= 0) {
                errors.configuration(RB.$("validation_jpackage_invalid_appversion_n", appVersion));
            }
        } catch (IllegalArgumentException e) {
            // can't use this value
            errors.configuration(RB.$("validation_jpackage_invalid_appversion_n", appVersion));
        }

        if (isBlank(applicationPackage.getVendor())) {
            applicationPackage.setVendor(project.getVendor());
        }
        if (isBlank(applicationPackage.getVendor())) {
            errors.configuration(RB.$("validation_jpackage_missing_vendor", assembler.getName()));
        }
        if (isBlank(applicationPackage.getCopyright())) {
            applicationPackage.setCopyright(project.getCopyright());
        }

        validateJavaAssembler(context, mode, assembler, errors, false);

        if (isBlank(packager.getAppName())) {
            packager.setAppName(assembler.getApplicationPackage().getAppName());
        }

        if (packager instanceof JpackageAssembler.Linux) {
            validateLinux(context, assembler, (JpackageAssembler.Linux) packager, errors);
        }
        if (packager instanceof JpackageAssembler.Osx) {
            validateOsx(context, assembler, (JpackageAssembler.Osx) packager, errors);
        }
        if (packager instanceof JpackageAssembler.Windows) {
            validateWindows(context, assembler, (JpackageAssembler.Windows) packager, errors);
        }
    }

    private static void validateOsx(JReleaserContext context, JpackageAssembler jpackage, JpackageAssembler.Osx packager, Errors errors) {
        if (isNotBlank(packager.getPackageIdentifier()) && !MAC_IDENTIFIER_PATTERN.matcher(packager.getPackageIdentifier()).matches()) {
            errors.configuration(RB.$("validation_jpackage_invalid_mac_package_identifier",
                packager.getPackageIdentifier(), MAC_IDENTIFIER));
        }

        if (isBlank(packager.getPackageName())) {
            packager.setPackageName(packager.getAppName());
        }
        if (isNotBlank(packager.getPackageName()) && packager.getPackageName().length() > 16) {
            errors.configuration(RB.$("validation_jpackage_invalid_mac_package_name",
                packager.getPackageName()));
        }
    }

    private static void validateLinux(JReleaserContext context, JpackageAssembler jpackage, JpackageAssembler.Linux packager, Errors errors) {
        if (isBlank(packager.getLicense())) {
            packager.setLicense(context.getModel().getProject().getLicense());
        }
    }

    private static void validateWindows(JReleaserContext context, JpackageAssembler jpackage, JpackageAssembler.Windows packager, Errors errors) {
        // noop
    }

    private static boolean validateJava(JReleaserContext context, JpackageAssembler jpackage, Errors errors) {
        Project project = context.getModel().getProject();

        Java java = project.getLanguages().getJava();
        if (!jpackage.getJava().isEnabledSet() && java.isEnabledSet()) {
            jpackage.getJava().setEnabled(java.isEnabled());
        }
        if (!jpackage.getJava().isEnabledSet()) {
            jpackage.getJava().setEnabled(jpackage.getJava().isSet());
        }

        if (!jpackage.getJava().isEnabled()) return false;

        if (isBlank(jpackage.getJava().getArtifactId())) {
            jpackage.getJava().setArtifactId(java.getArtifactId());
        }
        if (isBlank(jpackage.getJava().getGroupId())) {
            jpackage.getJava().setGroupId(java.getGroupId());
        }
        if (isBlank(jpackage.getJava().getVersion())) {
            jpackage.getJava().setVersion(java.getVersion());
        }
        if (isBlank(jpackage.getJava().getMainModule())) {
            jpackage.getJava().setMainModule(java.getMainModule());
        }
        if (isBlank(jpackage.getJava().getMainClass())) {
            jpackage.getJava().setMainClass(java.getMainClass());
        }

        if (isBlank(jpackage.getJava().getGroupId())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "jpackage." + jpackage.getName() + ".java.groupId"));
        }

        if (isBlank(jpackage.getJava().getMainClass())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "jpackage." + jpackage.getName() + ".java.mainClass"));
        }

        return true;
    }

    private static void validateRuntimeImage(JReleaserContext context, Mode mode, JpackageAssembler jpackage, Artifact runtimeImage, int index, Errors errors) {
        if (mode == Mode.FULL) return;

        if (null == runtimeImage) {
            errors.configuration(RB.$("validation_is_null", "jpackage." + jpackage.getName() + ".runtimeImage[" + index + "]"));
            return;
        }
        if (!runtimeImage.resolveActiveAndSelected(context)) return;
        if (isBlank(runtimeImage.getPath())) {
            errors.configuration(RB.$("validation_must_not_be_null", "jpackage." + jpackage.getName() + ".runtimeImage[" + index + "].path"));
        }
        if (isNotBlank(runtimeImage.getPlatform()) && !PlatformUtils.isSupported(runtimeImage.getPlatform().trim())) {
            context.getLogger().warn(RB.$("validation_jpackage_platform",
                jpackage.getName(), index, runtimeImage.getPlatform(), System.lineSeparator(),
                PlatformUtils.getSupportedOsNames(), System.lineSeparator(), PlatformUtils.getSupportedOsArchs()));
        }
    }

    private static void postValidateJpackage(JReleaserContext context, JpackageAssembler jpackage) {
        Project project = context.getModel().getProject();
        if (!jpackage.resolveEnabled(project)) return;

        JpackageAssembler.ApplicationPackage applicationPackage = jpackage.getApplicationPackage();

        if (isBlank(applicationPackage.getCopyright())) {
            applicationPackage.setCopyright(project.getCopyright());
        }
    }
}
