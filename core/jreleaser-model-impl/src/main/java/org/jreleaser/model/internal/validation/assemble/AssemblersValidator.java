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
package org.jreleaser.model.internal.validation.assemble;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.ArchiveAssembler;
import org.jreleaser.model.internal.assemble.Assemble;
import org.jreleaser.model.internal.assemble.JavaArchiveAssembler;
import org.jreleaser.model.internal.assemble.JlinkAssembler;
import org.jreleaser.model.internal.assemble.JpackageAssembler;
import org.jreleaser.model.internal.assemble.NativeImageAssembler;
import org.jreleaser.model.internal.validation.common.Validator;
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

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public abstract class AssemblersValidator extends Validator {
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

    public static void postValidateAssemblers(JReleaserContext context, Mode mode, Errors errors) {
        context.getLogger().debug("assemble");

        postValidateJpackage(context);
    }
}