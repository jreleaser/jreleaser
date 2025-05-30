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
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.JlinkAssembler;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.util.Errors;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.jreleaser.model.Constants.KEY_ARCHIVE_FORMAT;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public final class JlinkAssemblerResolver {
    private JlinkAssemblerResolver() {
        // noop
    }

    public static void resolveJlinkOutputs(JReleaserContext context, Errors errors) {
        List<JlinkAssembler> activeJlinks = context.getModel().getAssemble().getActiveJlinks();
        if (!activeJlinks.isEmpty()) context.getLogger().debug("assemble.jlink");

        for (JlinkAssembler jlink : activeJlinks) {
            if (jlink.isExported()) resolveJlinkOutputs(context, jlink, errors);
        }
    }

    public static void resolveJlinkOutputs(JReleaserContext context, JlinkAssembler assembler, Errors errors) {
        if (!assembler.isEnabled() || !context.isDistributionSelected(assembler.getName())) {
            return;
        }

        Path baseOutputDirectory = context.getAssembleDirectory()
            .resolve(assembler.getName())
            .resolve(assembler.getType());

        String imageName = assembler.getResolvedImageName(context);
        if (isNotBlank(assembler.getImageNameTransform())) {
            imageName = assembler.getResolvedImageNameTransform(context);
        }

        for (Artifact targetJdk : assembler.getTargetJdks()) {
            if (!targetJdk.isActiveAndSelected()) continue;

            String platform = targetJdk.getPlatform();
            String platformReplaced = assembler.getPlatform().applyReplacements(platform);
            String str = targetJdk.getExtraProperties()
                .getOrDefault(KEY_ARCHIVE_FORMAT, assembler.getArchiveFormat())
                .toString();
            Archive.Format archiveFormat = Archive.Format.of(str);

            Path image = baseOutputDirectory
                .resolve(imageName + "-" + platformReplaced + "." + archiveFormat.extension())
                .toAbsolutePath();

            if (!Files.exists(image)) {
                errors.assembly(RB.$("validation_missing_assembly",
                    assembler.getType(), assembler.getName(), assembler.getName()));
                errors.assembly(context.relativizeToBasedir(image.toAbsolutePath()).toString());
            } else {
                Artifact artifact = Artifact.of(image, platform);
                artifact.resolveActiveAndSelected(context);
                artifact.setExtraProperties(assembler.getExtraProperties());
                assembler.addOutput(artifact);
            }
        }
    }
}
