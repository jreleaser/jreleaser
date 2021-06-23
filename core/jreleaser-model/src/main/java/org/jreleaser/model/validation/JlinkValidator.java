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

import org.jreleaser.model.Active;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.Glob;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Jlink;
import org.jreleaser.model.Project;
import org.jreleaser.util.Errors;
import org.jreleaser.util.PlatformUtils;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static org.jreleaser.model.validation.TemplateValidator.validateTemplate;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public abstract class JlinkValidator extends Validator {
    public static void validateJlink(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        context.getLogger().debug("jlink");
        Map<String, Jlink> jlink = context.getModel().getAssemble().getJlink();

        for (Map.Entry<String, Jlink> e : jlink.entrySet()) {
            e.getValue().setName(e.getKey());
            validateJlink(context, mode, e.getValue(), errors);
        }
    }

    private static void validateJlink(JReleaserContext context, JReleaserContext.Mode mode, Jlink jlink, Errors errors) {
        context.getLogger().debug("jlink.{}", jlink.getName());

        if (!jlink.isActiveSet()) {
            jlink.setActive(Active.NEVER);
        }
        if (!jlink.resolveEnabled(context.getModel().getProject())) return;

        if (isBlank(jlink.getName())) {
            errors.configuration("jlink.name must not be blank");
            return;
        }

        context.getLogger().debug("jlink.{}.java", jlink.getName());
        if (!validateJava(context, jlink, errors)) {
            return;
        }

        if (isBlank(jlink.getImageName())) {
            jlink.setImageName(jlink.getJava().getGroupId() + "." +
                jlink.getJava().getArtifactId() + "-" +
                context.getModel().getProject().getResolvedVersion());
        }
        if (isBlank(jlink.getExecutable())) {
            jlink.setExecutable(jlink.getName());
        }

        if (isBlank(jlink.getJdk().getPath())) {
            jlink.getJdk().setPath(System.getProperty("java.home"));
            jlink.getJdk().setPlatform(PlatformUtils.getCurrentFull());
        }

        if (jlink.getTargetJdks().isEmpty()) {
            jlink.addTargetJdk(jlink.getJdk());
        }

        int i = 0;
        for (Artifact targetJdk : jlink.getTargetJdks()) {
            validateJdk(context, mode, jlink, targetJdk, i, errors);
        }

        // validate jdks.platform is unique
        Map<String, List<Artifact>> byPlatform = jlink.getTargetJdks().stream()
            .collect(groupingBy(jdk -> isBlank(jdk.getPlatform()) ? "<nil>" : jdk.getPlatform()));
        if (byPlatform.containsKey("<nil>")) {
            errors.configuration("jlink." + jlink.getName() +
                " defines JDKs without platform");
        }
        // check platforms
        byPlatform.forEach((platform, jdks) -> {
            if (jdks.size() > 1) {
                errors.configuration("jlink." + jlink.getName() +
                    " has more than one JDK for " + platform);
            }
        });

        if (jlink.getArgs().isEmpty()) {
            jlink.getArgs().add("--no-header-files");
            jlink.getArgs().add("--no-man-pages");
            jlink.getArgs().add("--compress=2");
            jlink.getArgs().add("--strip-debug");
        }

        if (null == jlink.getMainJar()) {
            errors.configuration("jlink." + jlink.getName() + ".mainJar is null");
            return;
        }
        if (isBlank(jlink.getMainJar().getPath())) {
            errors.configuration("jlink." + jlink.getName() + ".mainJar.path must not be null");
        }

        i = 0;
        for (Glob glob : jlink.getJars()) {
            boolean isBaseDir = false;

            if (isBlank(glob.getDirectory())) {
                glob.setDirectory(".");
                isBaseDir = true;
            }

            boolean includeAll = false;
            if (isBlank(glob.getInclude())) {
                glob.setInclude("*");
                includeAll = true;
            }

            if (isBlank(glob.getExclude()) &&
                includeAll && isBaseDir) {
                // too broad!
                errors.configuration("jlink." + jlink.getName() + ".jars[" + i + "] must define either a directory or an include/exclude pattern");
            }
        }

        if (mode != JReleaserContext.Mode.FULL) {
            validateTemplate(context, jlink, errors);
        }
    }

    private static boolean validateJava(JReleaserContext context, Jlink jlink, Errors errors) {
        Project project = context.getModel().getProject();

        if (!jlink.getJava().isEnabledSet() && project.getJava().isEnabledSet()) {
            jlink.getJava().setEnabled(project.getJava().isEnabled());
        }
        if (!jlink.getJava().isEnabledSet()) {
            jlink.getJava().setEnabled(jlink.getJava().isSet());
        }

        if (!jlink.getJava().isEnabled()) return true;

        if (isBlank(jlink.getJava().getArtifactId())) {
            jlink.getJava().setArtifactId(project.getJava().getArtifactId());
        }
        if (isBlank(jlink.getJava().getGroupId())) {
            jlink.getJava().setGroupId(project.getJava().getGroupId());
        }
        if (isBlank(jlink.getJava().getVersion())) {
            jlink.getJava().setVersion(project.getJava().getVersion());
        }
        if (isBlank(jlink.getJava().getMainClass())) {
            jlink.getJava().setMainClass(project.getJava().getMainClass());
        }

        if (isBlank(jlink.getJava().getGroupId())) {
            errors.configuration("jlink." + jlink.getName() + ".java.groupId must not be blank");
        }

        return true;
    }

    private static void validateJdk(JReleaserContext context, JReleaserContext.Mode mode, Jlink jlink, Artifact jdk, int index, Errors errors) {
        if (mode == JReleaserContext.Mode.FULL) return;

        if (null == jdk) {
            errors.configuration("jlink." + jlink.getName() + ".targetJdk[" + index + "] is null");
            return;
        }
        if (isBlank(jdk.getPath())) {
            errors.configuration("jlink." + jlink.getName() + ".targetJdk[" + index + "].path must not be null");
        }
        if (isNotBlank(jdk.getPlatform()) && !PlatformUtils.isSupported(jdk.getPlatform().trim())) {
            context.getLogger().warn("jlink.{}.targetJdk[{}].platform ({}) is not supported. Please use `${name}` or `${name}-${arch}` from{}       name = {}{}       arch = {}",
                jlink.getName(), index, jdk.getPlatform(), System.lineSeparator(),
                PlatformUtils.getSupportedOsNames(), System.lineSeparator(), PlatformUtils.getSupportedOsArchs());
        }
    }
}
