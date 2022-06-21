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
import org.jreleaser.model.Archive;
import org.jreleaser.model.Assemble;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Jlink;
import org.jreleaser.model.Jpackage;
import org.jreleaser.model.NativeImage;
import org.jreleaser.util.Errors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.jreleaser.model.validation.ArchiveValidator.validateArchive;
import static org.jreleaser.model.validation.JlinkValidator.validateJlink;
import static org.jreleaser.model.validation.JpackageValidator.postValidateJpackage;
import static org.jreleaser.model.validation.JpackageValidator.validateJpackage;
import static org.jreleaser.model.validation.NativeImageValidator.validateNativeImage;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public abstract class AssemblersValidator extends Validator {
    public static void validateAssemblers(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        if (mode == JReleaserContext.Mode.CHANGELOG) {
            return;
        }

        context.getLogger().debug("assemble");

        Assemble assemble = context.getModel().getAssemble();
        validateArchive(context, mode, errors);
        validateJlink(context, mode, errors);
        validateJpackage(context, mode, errors);
        validateNativeImage(context, mode, errors);

        // validate unique distribution names between exported assemblers
        Map<String, List<String>> byDistributionName = new LinkedHashMap<>();
        for (Archive archive : assemble.getActiveArchives()) {
            List<String> types = byDistributionName.computeIfAbsent(archive.getName(), k -> new ArrayList<>());
            types.add(archive.getType());
        }
        for (Jlink jlink : assemble.getActiveJlinks()) {
            List<String> types = byDistributionName.computeIfAbsent(jlink.getName(), k -> new ArrayList<>());
            if (jlink.isExported()) types.add(jlink.getType());
        }
        for (Jpackage jpackage : assemble.getActiveJpackages()) {
            List<String> types = byDistributionName.computeIfAbsent(jpackage.getName(), k -> new ArrayList<>());
            if (jpackage.isExported()) types.add(jpackage.getType());
        }
        for (NativeImage nativeImage : assemble.getActiveNativeImages()) {
            List<String> types = byDistributionName.computeIfAbsent(nativeImage.getName(), k -> new ArrayList<>());
            if (nativeImage.isExported()) types.add(nativeImage.getType());
        }
        byDistributionName.forEach((name, types) -> {
            if (types.size() > 1) {
                errors.configuration(RB.$("validation_multiple_assemblers", "distribution." + name, types));
                assemble.disable();
            }
        });

        boolean activeSet = assemble.isActiveSet();
        if (mode.validateConfig() || mode.validateAssembly()) {
            assemble.resolveEnabled(context.getModel().getProject());
        }

        if (assemble.isEnabled()) {
            boolean enabled = !assemble.getActiveArchives().isEmpty() ||
                !assemble.getActiveJlinks().isEmpty() ||
                !assemble.getActiveJpackages().isEmpty() ||
                !assemble.getActiveNativeImages().isEmpty();

            if (!activeSet && !enabled) assemble.disable();
        }
    }

    public static void postValidateAssemblers(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        if (mode == JReleaserContext.Mode.CHANGELOG) {
            return;
        }

        context.getLogger().debug("assemble");

        postValidateJpackage(context, mode, errors);
    }
}