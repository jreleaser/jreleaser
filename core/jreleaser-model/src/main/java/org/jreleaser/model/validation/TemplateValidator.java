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

import org.jreleaser.model.Assembler;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.Docker;
import org.jreleaser.model.DockerSpec;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.TemplateTool;
import org.jreleaser.model.Tool;
import org.jreleaser.util.Errors;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class TemplateValidator extends Validator {
    public static void validateTemplate(JReleaserContext context, Distribution distribution,
                                        TemplateTool tool, TemplateTool parentTool, Errors errors) {
        if (isBlank(tool.getTemplateDirectory())) {
            tool.setTemplateDirectory(parentTool.getTemplateDirectory());
            if (isNotBlank(tool.getTemplateDirectory()) &&
                !(context.getBasedir().resolve(tool.getTemplateDirectory().trim()).toFile().exists())) {
                errors.configuration("distribution." + distribution.getName() + "." + tool.getName() + ".template does not exist. " + tool.getTemplateDirectory());
            } else {
                tool.setTemplateDirectory("src/jreleaser/distributions/" + distribution.getName() + "/" + tool.getName());
            }
            return;
        }

        if (isNotBlank(tool.getTemplateDirectory()) &&
            !(context.getBasedir().resolve(tool.getTemplateDirectory().trim()).toFile().exists())) {
            errors.configuration("distribution." + distribution.getName() + "." + tool.getName() + ".template does not exist. " + tool.getTemplateDirectory());
        } else {
            tool.setTemplateDirectory("src/jreleaser/distributions/" + distribution.getName() + "/" + tool.getName());
        }
    }

    public static void validateTemplate(JReleaserContext context, Assembler assembler, Errors errors) {
        if (isNotBlank(assembler.getTemplateDirectory()) &&
            !(context.getBasedir().resolve(assembler.getTemplateDirectory().trim()).toFile().exists())) {
            errors.configuration(assembler.getType() + "." + assembler.getName() + ".template does not exist. " + assembler.getTemplateDirectory());
        } else {
            assembler.setTemplateDirectory("src/jreleaser/assemblers/" + assembler.getName() + "/" + assembler.getType());
        }
    }

    public static void validateTemplate(JReleaserContext context, Distribution distribution,
                                        DockerSpec spec, Docker docker, Errors errors) {
        if (isNotBlank(spec.getTemplateDirectory()) &&
            !(context.getBasedir().resolve(spec.getTemplateDirectory().trim()).toFile().exists())) {
            errors.configuration("distribution." + distribution.getName() + ".docker." + spec.getName() + ".template does not exist. " + spec.getTemplateDirectory());
        } else {
            spec.setTemplateDirectory("src/jreleaser/distributions/" + distribution.getName() + "/" + spec.getName() + "/" + docker.getName());
        }
    }
}
