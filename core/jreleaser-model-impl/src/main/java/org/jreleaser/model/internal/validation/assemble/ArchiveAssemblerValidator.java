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
import org.jreleaser.model.Distribution;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.ArchiveAssembler;
import org.jreleaser.util.Errors;

import java.util.List;
import java.util.Map;

import static org.jreleaser.model.Constants.KEY_PLATFORM;
import static org.jreleaser.model.internal.validation.assemble.AssemblersValidator.validateAssembler;
import static org.jreleaser.model.internal.validation.common.MatrixValidator.validateMatrix;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public final class ArchiveAssemblerValidator {
    private ArchiveAssemblerValidator() {
        // noop
    }

    public static void validateArchive(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, ArchiveAssembler> archive = context.getModel().getAssemble().getArchive();
        if (!archive.isEmpty()) context.getLogger().debug("assemble.archive");

        for (Map.Entry<String, ArchiveAssembler> e : archive.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig() || mode.validateAssembly()) {
                validateArchive(context, mode, e.getValue(), errors);
            }
        }
    }

    private static void validateArchive(JReleaserContext context, Mode mode, ArchiveAssembler assembler, Errors errors) {
        context.getLogger().debug("assemble.archive.{}", assembler.getName());

        resolveActivatable(context, assembler,
            listOf("assemble.archive." + assembler.getName(), "assemble.archive"),
            "NEVER");
        if (!assembler.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (isBlank(assembler.getName())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "archive.name"));
            context.getLogger().debug(RB.$("validation.disabled.error"));
            assembler.disable();
            return;
        }

        if (assembler.isApplyDefaultMatrix()) {
            assembler.setMatrix(context.getModel().getMatrix());
        }

        validateMatrix(context, assembler.getMatrix(), "assemble.archive." + assembler.getName() + ".matrix", errors);
        List<Map<String, String>> matrix = assembler.getMatrix().resolve();
        if (!matrix.isEmpty() && matrix.get(0).containsKey(KEY_PLATFORM)) {
            assembler.setAttachPlatform(false);
        }

        assembler.setPlatform(assembler.getPlatform().mergeValues(context.getModel().getPlatform()));

        if (null == assembler.getDistributionType()) {
            assembler.setDistributionType(Distribution.DistributionType.BINARY);
        }

        if (isBlank(assembler.getArchiveName())) {
            assembler.setArchiveName("{{distributionName}}-{{projectVersion}}");
        }

        if (assembler.getFormats().isEmpty()) {
            assembler.addFormat(Archive.Format.ZIP);
        }

        if (null == assembler.getOptions().getTimestamp()) {
            assembler.getOptions().setTimestamp(context.getModel().resolveArchiveTimestamp());
        }

        if (assembler.getFileSets().isEmpty()) {
            errors.configuration(RB.$("validation_archive_empty_fileset", assembler.getName()));
        }

        validateAssembler(context, mode, assembler, errors);
    }
}
