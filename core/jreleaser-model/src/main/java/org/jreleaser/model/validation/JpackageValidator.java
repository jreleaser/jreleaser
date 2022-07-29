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
import org.jreleaser.model.Artifact;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Jlink;
import org.jreleaser.model.Jpackage;
import org.jreleaser.model.Project;
import org.jreleaser.util.Errors;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.SemVer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.groupingBy;
import static org.jreleaser.model.validation.TemplateValidator.validateTemplate;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
public abstract class JpackageValidator extends Validator {
    private static final String MAC_IDENTIFIER = "[a-zA-Z0-9][a-zA-Z0-9\\.\\-]*";
    private static final Pattern MAC_IDENTIFIER_PATTERN = Pattern.compile(MAC_IDENTIFIER);

    public static void validateJpackage(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        context.getLogger().debug("jpackage");
        Map<String, Jpackage> jpackage = context.getModel().getAssemble().getJpackage();

        for (Map.Entry<String, Jpackage> e : jpackage.entrySet()) {
            e.getValue().setName(e.getKey());
            validateJpackage(context, mode, e.getValue(), errors);
        }
    }

    public static void postValidateJpackage(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        context.getLogger().debug("jpackage");
        Map<String, Jpackage> jpackage = context.getModel().getAssemble().getJpackage();

        for (Map.Entry<String, Jpackage> e : jpackage.entrySet()) {
            postValidateJpackage(context, mode, e.getValue(), errors);
        }
    }

    private static void validateJpackage(JReleaserContext context, JReleaserContext.Mode mode, Jpackage jpackage, Errors errors) {
        context.getLogger().debug("jpackage.{}", jpackage.getName());

        if (!jpackage.isActiveSet()) {
            jpackage.setActive(Active.NEVER);
        }

        Project project = context.getModel().getProject();
        if (!jpackage.resolveEnabled(project)) return;

        if (null == jpackage.getStereotype()) {
            jpackage.setStereotype(context.getModel().getProject().getStereotype());
        }

        Jpackage.PlatformPackager packager = jpackage.getResolvedPlatformPackager();
        Jpackage.ApplicationPackage applicationPackage = jpackage.getApplicationPackage();
        packager.enable();

        if (isNotBlank(jpackage.getJlink())) {
            Jlink jlink = context.getModel().getAssemble().findJlink(jpackage.getJlink());

            Path baseOutputDirectory = context.getAssembleDirectory()
                .resolve(jlink.getName())
                .resolve(jlink.getType());

            String imageName = jlink.getResolvedImageName(context);

            List<Artifact> candidateRuntimeImages = new ArrayList<>();
            for (Artifact targetJdk : jlink.getTargetJdks()) {
                if (!context.isPlatformSelected(targetJdk)) continue;

                String platform = targetJdk.getPlatform();
                Path path = baseOutputDirectory
                    .resolve("work-" + platform)
                    .resolve(imageName + "-" + platform)
                    .toAbsolutePath();
                candidateRuntimeImages.add(Artifact.of(path, platform));
            }

            if (jpackage.getRuntimeImages().size() > 0 && jpackage.getRuntimeImages().size() != candidateRuntimeImages.size()) {
                errors.configuration(RB.$("validation_jpackage_jlink_application", jpackage.getName()));
            }

            int count = 0;
            for (Artifact runtimeImage : jpackage.getRuntimeImages()) {
                Path rp = runtimeImage.getResolvedPath(context, jpackage);
                Path tp = runtimeImage.getResolvedTransform(context, jpackage);
                Path path = tp != null ? tp : rp;
                if (candidateRuntimeImages.stream()
                    .anyMatch(a -> a.getPath().equals(path.toString()))) {
                    count++;
                }
            }

            if (jpackage.getRuntimeImages().size() > 0 && count != candidateRuntimeImages.size()) {
                errors.configuration(RB.$("validation_jpackage_jlink_application", jpackage.getName()));
            }

            jpackage.setJava(jlink.getJava());
            jpackage.setMainJar(jlink.getMainJar());
            jpackage.setJars(jlink.getJars());
            packager.setJdk(jlink.getJdk());
            if (isBlank(jpackage.getExecutable())) {
                jpackage.setExecutable(jlink.getExecutable());
            }

            for (Artifact runtimeImage : candidateRuntimeImages) {
                runtimeImage.activate();
                jpackage.addRuntimeImage(runtimeImage);
            }
        }

        context.getLogger().debug("jpackage.{}.java", jpackage.getName());
        if (!validateJava(context, jpackage, errors)) {
            return;
        }

        if (isBlank(jpackage.getExecutable())) {
            jpackage.setExecutable(jpackage.getName());
        }

        if (jpackage.getRuntimeImages().size() == 0) {
            errors.configuration(RB.$("validation_jpackage_runtime_images_missing", jpackage.getName()));
            return;
        }

        int i = 0;
        for (Artifact runtimeImage : jpackage.getRuntimeImages()) {
            validateRuntimeImage(context, mode, jpackage, runtimeImage, i++, errors);
        }

        // validate jdks.platform is unique
        Map<String, List<Artifact>> byPlatform = jpackage.getRuntimeImages().stream()
            .collect(groupingBy(ri -> isBlank(ri.getPlatform()) ? "<nil>" : ri.getPlatform()));
        if (byPlatform.containsKey("<nil>")) {
            errors.configuration(RB.$("validation_jpackage_runtime_image_platform", jpackage.getName()));
        }
        // check platforms
        byPlatform.forEach((p, jdks) -> {
            if (jdks.size() > 1) {
                errors.configuration(RB.$("validation_jpackage_runtime_image_multiple_platforms", jpackage.getName(), p));
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
        }

        if (packager.getTypes().isEmpty()) {
            packager.setTypes(singletonList(packager.getValidTypes().get(0)));
        }

        if (isBlank(applicationPackage.getAppName())) {
            applicationPackage.setAppName(jpackage.getName());
        }

        if (isBlank(applicationPackage.getAppVersion())) {
            applicationPackage.setAppVersion(project.getResolvedVersion());
        }

        // validate appVersion
        String appVersion = applicationPackage.getResolvedAppVersion(context, jpackage);
        try {
            SemVer v = SemVer.of(appVersion);
            if (isNotBlank(v.getBuild()) && isNotBlank(v.getTag()) &&
                v.getMajor() <= 0) {
                errors.configuration(RB.$("validation_jpackage_invalid_appversion", appVersion));
            }
        } catch (IllegalArgumentException e) {
            // can't use this value
            errors.configuration(RB.$("validation_jpackage_invalid_appversion", appVersion));
        }

        if (isBlank(applicationPackage.getVendor())) {
            applicationPackage.setVendor(project.getVendor());
        }
        if (isBlank(applicationPackage.getVendor())) {
            errors.configuration(RB.$("validation_jpackage_missing_vendor", jpackage.getName()));
        }
        if (isBlank(applicationPackage.getCopyright())) {
            applicationPackage.setCopyright(project.getCopyright());
        }

        if (mode == JReleaserContext.Mode.ASSEMBLE) {
            validateTemplate(context, jpackage, errors);
        }

        if (isBlank(packager.getAppName())) {
            packager.setAppName(jpackage.getApplicationPackage().getAppName());
        }

        if (packager instanceof Jpackage.Linux) {
            validateLinux(context, jpackage, (Jpackage.Linux) packager, errors);
        }
        if (packager instanceof Jpackage.Osx) {
            validateOsx(context, jpackage, (Jpackage.Osx) packager, errors);
        }
        if (packager instanceof Jpackage.Windows) {
            validateWindows(context, jpackage, (Jpackage.Windows) packager, errors);
        }
    }

    private static void validateOsx(JReleaserContext context, Jpackage jpackage, Jpackage.Osx packager, Errors errors) {
        if (isNotBlank(packager.getPackageIdentifier())) {
            if (!MAC_IDENTIFIER_PATTERN.matcher(packager.getPackageIdentifier()).matches()) {
                errors.configuration(RB.$("validation_jpackage_invalid_mac_package_identifier",
                    packager.getPackageIdentifier(), MAC_IDENTIFIER));
            }
        }

        if (isBlank(packager.getPackageName())) {
            packager.setPackageName(packager.getAppName());
        }
        if (isNotBlank(packager.getPackageName()) && packager.getPackageName().length() > 16) {
            errors.configuration(RB.$("validation_jpackage_invalid_mac_package_name",
                packager.getPackageName()));
        }
    }

    private static void validateLinux(JReleaserContext context, Jpackage jpackage, Jpackage.Linux packager, Errors errors) {
        if (isBlank(packager.getLicense())) {
            packager.setLicense(context.getModel().getProject().getLicense());
        }
    }

    private static void validateWindows(JReleaserContext context, Jpackage jpackage, Jpackage.Windows packager, Errors errors) {

    }

    private static boolean validateJava(JReleaserContext context, Jpackage jpackage, Errors errors) {
        Project project = context.getModel().getProject();

        if (!jpackage.getJava().isEnabledSet() && project.getJava().isEnabledSet()) {
            jpackage.getJava().setEnabled(project.getJava().isEnabled());
        }
        if (!jpackage.getJava().isEnabledSet()) {
            jpackage.getJava().setEnabled(jpackage.getJava().isSet());
        }

        if (!jpackage.getJava().isEnabled()) return true;

        if (isBlank(jpackage.getJava().getArtifactId())) {
            jpackage.getJava().setArtifactId(project.getJava().getArtifactId());
        }
        if (isBlank(jpackage.getJava().getGroupId())) {
            jpackage.getJava().setGroupId(project.getJava().getGroupId());
        }
        if (isBlank(jpackage.getJava().getVersion())) {
            jpackage.getJava().setVersion(project.getJava().getVersion());
        }
        if (isBlank(jpackage.getJava().getMainModule())) {
            jpackage.getJava().setMainModule(project.getJava().getMainModule());
        }
        if (isBlank(jpackage.getJava().getMainClass())) {
            jpackage.getJava().setMainClass(project.getJava().getMainClass());
        }

        if (isBlank(jpackage.getJava().getGroupId())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "jpackage." + jpackage.getName() + ".java.groupId"));
        }

        if (isBlank(jpackage.getJava().getMainClass())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "jpackage." + jpackage.getName() + ".java.mainClass"));
        }

        return true;
    }

    private static void validateRuntimeImage(JReleaserContext context, JReleaserContext.Mode mode, Jpackage jpackage, Artifact runtimeImage, int index, Errors errors) {
        if (mode == JReleaserContext.Mode.FULL) return;

        if (null == runtimeImage) {
            errors.configuration(RB.$("validation_is_null", "jpackage." + jpackage.getName() + ".runtimeImage[" + index + "]"));
            return;
        }
        if (isBlank(runtimeImage.getPath())) {
            errors.configuration(RB.$("validation_must_not_be_null", "jpackage." + jpackage.getName() + ".runtimeImage[" + index + "].path"));
        }
        if (isNotBlank(runtimeImage.getPlatform()) && !PlatformUtils.isSupported(runtimeImage.getPlatform().trim())) {
            context.getLogger().warn(RB.$("validation_jpackage_platform",
                jpackage.getName(), index, runtimeImage.getPlatform(), System.lineSeparator(),
                PlatformUtils.getSupportedOsNames(), System.lineSeparator(), PlatformUtils.getSupportedOsArchs()));
        }
    }

    private static void postValidateJpackage(JReleaserContext context, JReleaserContext.Mode mode, Jpackage jpackage, Errors errors) {
        Project project = context.getModel().getProject();
        if (!jpackage.resolveEnabled(project)) return;

        Jpackage.ApplicationPackage applicationPackage = jpackage.getApplicationPackage();

        if (isBlank(applicationPackage.getCopyright())) {
            applicationPackage.setCopyright(project.getCopyright());
        }
    }
}
