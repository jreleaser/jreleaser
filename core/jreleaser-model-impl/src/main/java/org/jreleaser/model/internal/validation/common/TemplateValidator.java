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
package org.jreleaser.model.internal.validation.common;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.Assembler;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.DockerPackager;
import org.jreleaser.model.internal.packagers.DockerSpec;
import org.jreleaser.model.internal.packagers.JibPackager;
import org.jreleaser.model.internal.packagers.JibSpec;
import org.jreleaser.model.internal.packagers.TemplatePackager;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class TemplateValidator {
    private TemplateValidator() {
        // noop
    }

    public static void validateTemplate(JReleaserContext context, Distribution distribution,
                                        TemplatePackager<?> packager, TemplatePackager<?> parentPackager, Errors errors) {
        if (packager.getSkipTemplates().isEmpty()) {
            packager.setSkipTemplates(parentPackager.getSkipTemplates());
        }

        String defaultTemplateDirectory = "src/jreleaser/distributions/" + distribution.getName() + "/" + packager.getType();

        if (isBlank(packager.getTemplateDirectory())) {
            packager.setTemplateDirectory(parentPackager.getTemplateDirectory());
            if (isNotBlank(packager.getTemplateDirectory()) &&
                !defaultTemplateDirectory.equals(packager.getTemplateDirectory().trim()) &&
                !Files.exists(context.getBasedir().resolve(packager.getTemplateDirectory().trim()))) {
                errors.configuration(RB.$("validation_directory_not_exist",
                    "distribution." + distribution.getName() + "." + packager.getType() + ".template", packager.getTemplateDirectory()));
            }
            if (isBlank(packager.getTemplateDirectory())) {
                packager.setTemplateDirectory(defaultTemplateDirectory);
            }
            return;
        }

        if (isNotBlank(packager.getTemplateDirectory()) &&
            !defaultTemplateDirectory.equals(packager.getTemplateDirectory().trim()) &&
            !Files.exists(context.getBasedir().resolve(packager.getTemplateDirectory().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist",
                "distribution." + distribution.getName() + "." + packager.getType() + ".template", packager.getTemplateDirectory()));
        }
        if (isBlank(packager.getTemplateDirectory())) {
            packager.setTemplateDirectory(defaultTemplateDirectory);
        }
    }

    public static void validateTemplate(JReleaserContext context, Assembler<?> assembler, Errors errors) {
        String defaultTemplateDirectory = "src/jreleaser/assemblers/" + assembler.getName() + "/" + assembler.getType();
        if (isNotBlank(assembler.getTemplateDirectory()) &&
            !defaultTemplateDirectory.equals(assembler.getTemplateDirectory().trim()) &&
            !Files.exists(context.getBasedir().resolve(assembler.getTemplateDirectory().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist",
                assembler.getType() + "." + assembler.getName() + ".template", assembler.getTemplateDirectory()));
        }
        if (isBlank(assembler.getTemplateDirectory())) {
            assembler.setTemplateDirectory(defaultTemplateDirectory);
        }
    }

    public static void validateTemplate(JReleaserContext context, Distribution distribution,
                                        DockerSpec spec, DockerPackager docker, Errors errors) {
        String defaultTemplateDirectory = "src/jreleaser/distributions/" + distribution.getName() + "/" + docker.getType() + "/" + spec.getName();
        if (isNotBlank(spec.getTemplateDirectory()) &&
            !defaultTemplateDirectory.equals(spec.getTemplateDirectory().trim()) &&
            !Files.exists(context.getBasedir().resolve(spec.getTemplateDirectory().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist",
                "distribution." + distribution.getName() + ".docker." + spec.getName() + ".template", spec.getTemplateDirectory()));
        }
        if (isBlank(spec.getTemplateDirectory())) {
            spec.setTemplateDirectory(defaultTemplateDirectory);
        }
    }

    public static void validateTemplate(JReleaserContext context, Distribution distribution,
                                        JibSpec spec, JibPackager jib, Errors errors) {
        String defaultTemplateDirectory = "src/jreleaser/distributions/" + distribution.getName() + "/" + jib.getType() + "/" + spec.getName();
        if (isNotBlank(spec.getTemplateDirectory()) &&
            !defaultTemplateDirectory.equals(spec.getTemplateDirectory().trim()) &&
            !Files.exists(context.getBasedir().resolve(spec.getTemplateDirectory().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist",
                "distribution." + distribution.getName() + ".jib." + spec.getName() + ".template", spec.getTemplateDirectory()));
        }
        if (isBlank(spec.getTemplateDirectory())) {
            spec.setTemplateDirectory(defaultTemplateDirectory);
        }
    }
}
