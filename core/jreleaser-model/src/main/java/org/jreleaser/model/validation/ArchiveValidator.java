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
import org.jreleaser.model.Active;
import org.jreleaser.model.Archive;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.FileSet;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public abstract class ArchiveValidator extends Validator {
    public static void validateArchive(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        context.getLogger().debug("archive");
        Map<String, Archive> archive = context.getModel().getAssemble().getArchive();

        for (Map.Entry<String, Archive> e : archive.entrySet()) {
            e.getValue().setName(e.getKey());
            validateArchive(context, mode, e.getValue(), errors);
        }
    }

    private static void validateArchive(JReleaserContext context, JReleaserContext.Mode mode, Archive archive, Errors errors) {
        context.getLogger().debug("archive.{}", archive.getName());

        if (!archive.isActiveSet()) {
            archive.setActive(Active.NEVER);
        }
        if (!archive.resolveEnabled(context.getModel().getProject())) return;

        if (isBlank(archive.getName())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "archive.name"));
            return;
        }

        archive.setPlatform(archive.getPlatform().mergeValues(context.getModel().getPlatform()));

        if (null == archive.getDistributionType()) {
            archive.setDistributionType(Distribution.DistributionType.BINARY);
        }
        if (null == archive.getStereotype()) {
            archive.setStereotype(context.getModel().getProject().getStereotype());
        }

        if (isBlank(archive.getArchiveName())) {
            archive.setArchiveName("{{distributionName}}-{{projectVersion}}");
        }

        if (archive.getFormats().isEmpty()) {
            archive.addFormat(Archive.Format.ZIP);
        }

        if (archive.getFileSets().isEmpty()) {
            errors.configuration(RB.$("validation_archive_empty_fileset", archive.getName()));
        } else {
            int i = 0;
            for (FileSet fileSet : archive.getFileSets()) {
                validateFileSet(context, mode, archive, fileSet, i++, errors);
            }
        }
    }
}
