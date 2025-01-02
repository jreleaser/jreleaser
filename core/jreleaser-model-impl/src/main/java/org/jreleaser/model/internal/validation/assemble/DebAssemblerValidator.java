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
import org.jreleaser.model.internal.assemble.Assembler;
import org.jreleaser.model.internal.assemble.DebAssembler;
import org.jreleaser.model.internal.assemble.JavaArchiveAssembler;
import org.jreleaser.model.internal.assemble.JavaAssembler;
import org.jreleaser.model.internal.assemble.JpackageAssembler;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.assemble.AssemblersValidator.validateAssembler;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.16.0
 */
public final class DebAssemblerValidator {
    private DebAssemblerValidator() {
        // noop
    }

    public static void validateDeb(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, DebAssembler> deb = context.getModel().getAssemble().getDeb();
        if (!deb.isEmpty()) context.getLogger().debug("assemble.deb");

        for (Map.Entry<String, DebAssembler> e : deb.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig() || mode.validateAssembly()) {
                validateDeb(context, mode, e.getValue(), errors);
            }
        }
    }

    private static void validateDeb(JReleaserContext context, Mode mode, DebAssembler assembler, Errors errors) {
        context.getLogger().debug("assemble.deb.{}", assembler.getName());

        resolveActivatable(context, assembler,
            listOf("assemble.deb." + assembler.getName(), "assemble.deb"),
            "NEVER");
        Project project = context.getModel().getProject();
        if (!assembler.resolveEnabled(project)) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (isBlank(assembler.getName())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "deb.name"));
            context.getLogger().debug(RB.$("validation.disabled.error"));
            assembler.disable();
            return;
        }

        assembler.setPlatform(assembler.getPlatform().mergeValues(context.getModel().getPlatform()));

        boolean assemblerRefIsOk = false;
        if (isNotBlank(assembler.getAssemblerRef())) {
            String assemblerRef = assembler.getAssemblerRef().trim();
            Assembler<?> otherAssembler = context.getModel().getAssemble().findAssembler(assemblerRef);
            if (otherAssembler == null) {
                errors.configuration(RB.$("validation_not_configured", "assemble.deb." + assembler.getName()));
            } else if (!otherAssembler.isEnabled()) {
                errors.configuration(RB.$("validation_is_disabled", "assemble.deb." + assembler.getName()));
            } else if (otherAssembler instanceof DebAssembler || otherAssembler instanceof JpackageAssembler) {
                errors.configuration(RB.$("validation_is_invalid", "assemble.deb." + assembler.getName() + ".assemblerRef",
                    otherAssembler.getClass().getSimpleName()));
            } else {
                assemblerRefIsOk = true;
            }

            if (otherAssembler instanceof JavaAssembler && isBlank(assembler.getExecutable())) {
                assembler.setExecutable(((JavaAssembler) otherAssembler).getExecutable());
            } else if (otherAssembler instanceof JavaArchiveAssembler && isBlank(assembler.getExecutable())) {
                assembler.setExecutable(((JavaArchiveAssembler) otherAssembler).getExecutable().resolveExecutable("linux"));
            } else if (isBlank(assembler.getExecutable())) {
                errors.configuration(RB.$("validation_is_missing", "assemble.deb." + assembler.getName() + ".executable"));
            }
        }

        if (!assemblerRefIsOk && assembler.getFileSets().isEmpty()) {
            errors.configuration(RB.$("validation_deb_empty_fileset", assembler.getName()));
        }

        if (!assemblerRefIsOk && isBlank(assembler.getExecutable())) {
            assembler.setExecutable(assembler.getName());
        }

        if (isBlank(assembler.getAssemblerRef()) && isBlank(assembler.getArchitecture())) {
            errors.configuration(RB.$("validation_is_missing", "assemble.deb." + assembler.getName() + ".architecture"));
        }

        validateAssembler(context, mode, assembler, errors);

        if (isBlank(assembler.getInstallationPath())) {
            assembler.setInstallationPath("/opt/{{packageName}}");
        }

        DebAssembler.Control control = assembler.getControl();

        if (isBlank(control.getPackageName())) {
            control.setPackageName(assembler.getName());
        }

        if (isBlank(control.getPackageVersion())) {
            control.setPackageVersion(project.getResolvedVersion());
        }

        if (null == control.getPackageRevision()) {
            control.setPackageRevision(1);
        }
        if (control.getPackageRevision() < 0) {
            errors.configuration(RB.$("validation_is_invalid", "assemble.deb." + assembler.getName() + ".control.revision",
                control.getPackageRevision()));
        }

        if (isBlank(control.getProvides())) {
            control.setProvides(control.getPackageName());
        }

        if (isBlank(control.getMaintainer())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "assemble.deb." + assembler.getName() + ".control.maintainer"));
        }

        if (null == control.getSection()) {
            control.setSection(org.jreleaser.model.api.assemble.DebAssembler.Section.MISC);
        }

        if (null == control.getPriority()) {
            control.setPriority(org.jreleaser.model.api.assemble.DebAssembler.Priority.OPTIONAL);
        }

        if (!control.isEssentialSet()) {
            control.setEssential(false);
        }

        if (isBlank(control.getDescription())) {
            control.setDescription(project.getLongDescription());
        }
        if (isBlank(control.getDescription())) {
            control.setDescription(project.getDescription());
        }

        if (isBlank(control.getHomepage())) {
            control.setHomepage(project.getLinks().getHomepage());
        }
    }
}
