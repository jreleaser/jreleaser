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
import org.jreleaser.model.internal.assemble.ArchiveAssembler;
import org.jreleaser.model.internal.assemble.Assemble;
import org.jreleaser.model.internal.assemble.Assembler;
import org.jreleaser.model.internal.assemble.JavaArchiveAssembler;
import org.jreleaser.model.internal.assemble.JavaAssembler;
import org.jreleaser.model.internal.assemble.JlinkAssembler;
import org.jreleaser.model.internal.assemble.JpackageAssembler;
import org.jreleaser.model.internal.assemble.NativeImageAssembler;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.FileSet;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.util.Errors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.jreleaser.model.internal.validation.assemble.ArchiveAssemblerValidator.validateArchive;
import static org.jreleaser.model.internal.validation.assemble.JavaArchiveAssemblerValidator.validateJavaArchive;
import static org.jreleaser.model.internal.validation.assemble.JlinkAssemblerValidator.validateJlink;
import static org.jreleaser.model.internal.validation.assemble.JpackageAssemblerValidator.postValidateJpackage;
import static org.jreleaser.model.internal.validation.assemble.JpackageAssemblerValidator.validateJpackage;
import static org.jreleaser.model.internal.validation.assemble.NativeImageAssemblerValidator.validateNativeImage;
import static org.jreleaser.model.internal.validation.common.TemplateValidator.validateTemplate;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateFileSet;
import static org.jreleaser.model.internal.validation.common.Validator.validateGlobs;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public final class AssemblersValidator {
    private AssemblersValidator() {
        // noop
    }

    public static void validateAssemblers(JReleaserContext context, Mode mode, Errors errors) {
        Assemble assemble = context.getModel().getAssemble();
        context.getLogger().debug("assemble");

        validateArchive(context, mode, errors);
        validateJavaArchive(context, mode, errors);
        validateJlink(context, mode, errors);
        validateJpackage(context, mode, errors);
        validateNativeImage(context, mode, errors);

        if (!mode.validateConfig() && !mode.validateAssembly()) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        // validate unique distribution names between exported assemblers
        Map<String, List<String>> byDistributionName = new LinkedHashMap<>();
        for (ArchiveAssembler archive : assemble.getActiveArchives()) {
            List<String> types = byDistributionName.computeIfAbsent(archive.getName(), k -> new ArrayList<>());
            types.add(archive.getType());
        }
        for (JavaArchiveAssembler archive : assemble.getActiveJavaArchives()) {
            List<String> types = byDistributionName.computeIfAbsent(archive.getName(), k -> new ArrayList<>());
            types.add(archive.getType());
        }
        for (JlinkAssembler jlink : assemble.getActiveJlinks()) {
            List<String> types = byDistributionName.computeIfAbsent(jlink.getName(), k -> new ArrayList<>());
            if (jlink.isExported()) types.add(jlink.getType());
        }
        for (JpackageAssembler jpackage : assemble.getActiveJpackages()) {
            List<String> types = byDistributionName.computeIfAbsent(jpackage.getName(), k -> new ArrayList<>());
            if (jpackage.isExported()) types.add(jpackage.getType());
        }
        for (NativeImageAssembler nativeImage : assemble.getActiveNativeImages()) {
            List<String> types = byDistributionName.computeIfAbsent(nativeImage.getName(), k -> new ArrayList<>());
            if (nativeImage.isExported()) types.add(nativeImage.getType());
        }
        byDistributionName.forEach((name, types) -> {
            if (types.size() > 1) {
                errors.configuration(RB.$("validation_multiple_assemblers", "distribution." + name, types));
                context.getLogger().debug(RB.$("validation.disabled.error"));
                assemble.disable();
            }
        });

        boolean activeSet = assemble.isActiveSet();
        resolveActivatable(context, assemble, "assemble", "ALWAYS");
        assemble.resolveEnabled(context.getModel().getProject());

        if (assemble.isEnabled()) {
            boolean enabled = !assemble.getActiveArchives().isEmpty() ||
                !assemble.getActiveJavaArchives().isEmpty() ||
                !assemble.getActiveJlinks().isEmpty() ||
                !assemble.getActiveJpackages().isEmpty() ||
                !assemble.getActiveNativeImages().isEmpty();

            if (!activeSet && !enabled) {
                context.getLogger().debug(RB.$("validation.disabled"));
                assemble.disable();
            }
        }
    }

    public static void postValidateAssemblers(JReleaserContext context) {
        context.getLogger().debug("assemble");

        postValidateJpackage(context);
    }

    public static void validateAssembler(JReleaserContext context, Mode mode, Assembler<?> assembler, Errors errors) {
        if (null == assembler.getStereotype()) {
            assembler.setStereotype(context.getModel().getProject().getStereotype());
        }

        for (Artifact artifact : assembler.getArtifacts()) {
            artifact.resolveActiveAndSelected(context);
        }

        validateGlobs(
            context, assembler.getFiles(),
            assembler.getType() + "." + assembler.getName() + ".files",
            errors);

        int i = 0;
        for (FileSet fileSet : assembler.getFileSets()) {
            validateFileSet(context, mode, assembler, fileSet, i++, errors);
        }

        if (mode == Mode.ASSEMBLE) {
            validateTemplate(context, assembler, errors);
        }
    }

    public static boolean validateJavaAssembler(JReleaserContext context, Mode mode, JavaAssembler<?> assembler, Errors errors, boolean checkMainJar) {
        validateAssembler(context, mode, assembler, errors);

        if (checkMainJar) {
            if (null == assembler.getMainJar()) {
                errors.configuration(RB.$("validation_is_null", assembler.getType() + "." + assembler.getName() + ".mainJar"));
                return false;
            }

            if (isBlank(assembler.getMainJar().getPath())) {
                errors.configuration(RB.$("validation_must_not_be_null", assembler.getType() + "." + assembler.getName() + ".mainJar.path"));
            }
        }

        validateGlobs(
            context, assembler.getJars(),
            assembler.getType() + "." + assembler.getName() + ".jars",
            errors);

        return true;
    }

    public static boolean validateJava(JReleaserContext context, JavaAssembler<?> assembler, Errors errors) {
        Project project = context.getModel().getProject();

        if (!assembler.getJava().isEnabledSet() && project.getJava().isEnabledSet()) {
            assembler.getJava().setEnabled(project.getJava().isEnabled());
        }
        if (!assembler.getJava().isEnabledSet()) {
            assembler.getJava().setEnabled(assembler.getJava().isSet());
        }

        if (!assembler.getJava().isEnabled()) return false;

        if (isBlank(assembler.getJava().getArtifactId())) {
            assembler.getJava().setArtifactId(project.getJava().getArtifactId());
        }
        if (isBlank(assembler.getJava().getGroupId())) {
            assembler.getJava().setGroupId(project.getJava().getGroupId());
        }
        if (isBlank(assembler.getJava().getVersion())) {
            assembler.getJava().setVersion(project.getJava().getVersion());
        }
        if (isBlank(assembler.getJava().getMainModule())) {
            assembler.getJava().setMainModule(project.getJava().getMainModule());
        }
        if (isBlank(assembler.getJava().getMainClass())) {
            assembler.getJava().setMainClass(project.getJava().getMainClass());
        }

        return true;
    }
}