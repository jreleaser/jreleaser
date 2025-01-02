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
import org.jreleaser.model.internal.assemble.JavaArchiveAssembler;
import org.jreleaser.model.internal.common.Java;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.assemble.AssemblersValidator.validateAssembler;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateGlobs;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.4.0
 */
public final class JavaArchiveAssemblerValidator {
    private JavaArchiveAssemblerValidator() {
        // noop
    }

    public static void validateJavaArchive(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, JavaArchiveAssembler> archive = context.getModel().getAssemble().getJavaArchive();
        if (!archive.isEmpty()) context.getLogger().debug("assemble.java-archive");

        for (Map.Entry<String, JavaArchiveAssembler> e : archive.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig() || mode.validateAssembly()) {
                validateJavaArchive(context, mode, e.getValue(), errors);
            }
        }
    }

    private static void validateJavaArchive(JReleaserContext context, Mode mode, JavaArchiveAssembler assembler, Errors errors) {
        context.getLogger().debug("assemble.java-archive.{}", assembler.getName());

        resolveActivatable(context, assembler,
            listOf("assemble.java.archive." + assembler.getName(), "assemble.java.archive"),
            "NEVER");
        if (!assembler.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (isBlank(assembler.getName())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "java-archive.name"));
            context.getLogger().debug(RB.$("validation.disabled.error"));
            assembler.disable();
            return;
        }

        if (isBlank(assembler.getArchiveName())) {
            assembler.setArchiveName("{{distributionName}}-{{projectVersion}}");
        }

        if (isBlank(assembler.getExecutable().getName())) {
            assembler.getExecutable().setName(assembler.getName());
        }

        if (isBlank(assembler.getExecutable().getWindowsExtension())) {
            assembler.getExecutable().setWindowsExtension("bat");
        }

        if (assembler.getFormats().isEmpty()) {
            assembler.addFormat(Archive.Format.ZIP);
        }

        if (null == assembler.getOptions().getTimestamp()) {
            assembler.getOptions().setTimestamp(context.getModel().resolveArchiveTimestamp());
        }

        if (assembler.getJars().isEmpty() && isBlank(assembler.getMainJar().getPath())) {
            errors.configuration(RB.$("validation_java_archive_empty_jars", assembler.getName()));
        } else {
            validateGlobs(
                context, assembler.getJars(),
                "java-archive." + assembler.getName() + ".jars",
                errors);
        }

        validateAssembler(context, mode, assembler, errors);

        context.getLogger().debug("assemble.java-archive.{}.java", assembler.getName());

        validateJava(context, mode, assembler, errors);

        assembler.getMainJar().resolveActiveAndSelected(context);
        boolean mainJarIsSet = isNotBlank(assembler.getMainJar().getPath());
        boolean mainClassIsSet = isNotBlank(assembler.getJava().getMainClass());

        if (!mainJarIsSet && !mainClassIsSet) {
            errors.configuration(RB.$("validation_java_archive_main_jar_or_class_missing", assembler.getName(), assembler.getName()));
        }
    }

    private static void validateJava(JReleaserContext context, Mode mode, JavaArchiveAssembler assembler, Errors errors) {
        JavaArchiveAssembler.Java java = assembler.getJava();
        Java projectJava = context.getModel().getProject().getLanguages().getJava();

        if (isBlank(java.getMainModule())) {
            java.setMainModule(projectJava.getMainModule());
        }
        if (isBlank(java.getMainClass())) {
            java.setMainClass(projectJava.getMainClass());
        }
        if (java.getOptions().isEmpty()) {
            java.setOptions(projectJava.getOptions());
        } else {
            java.addOptions(projectJava.getOptions());
        }

        java.getJvmOptions().merge(projectJava.getOptions());
        java.getJvmOptions().merge(projectJava.getJvmOptions());
        java.getEnvironmentVariables().merge(projectJava.getEnvironmentVariables());
    }
}
