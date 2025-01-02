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
package org.jreleaser.model.internal.validation.packagers;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.SnapPackager;
import org.jreleaser.model.internal.release.Releaser;
import org.jreleaser.model.internal.validation.common.Validator;
import org.jreleaser.util.Errors;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.internal.validation.common.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.internal.validation.common.TemplateValidator.validateTemplate;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateCommitAuthor;
import static org.jreleaser.model.internal.validation.common.Validator.validateContinueOnError;
import static org.jreleaser.model.internal.validation.distributions.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class SnapPackagerValidator {
    private SnapPackagerValidator() {
        // noop
    }

    public static void validateSnap(JReleaserContext context, Distribution distribution, SnapPackager packager, Errors errors) {
        context.getLogger().debug("distribution.{}." + packager.getType(), distribution.getName());
        JReleaserModel model = context.getModel();
        SnapPackager parentPackager = model.getPackagers().getSnap();

        resolveActivatable(context, packager, "distributions." + distribution.getName() + "." + packager.getType(), parentPackager);
        if (!packager.resolveEnabled(context.getModel().getProject(), distribution)) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }
        Releaser<?> service = model.getRelease().getReleaser();
        if (!service.isReleaseSupported()) {
            context.getLogger().debug(RB.$("validation.disabled.release"));
            packager.disable();
            return;
        }

        List<Artifact> candidateArtifacts = packager.resolveCandidateArtifacts(context, distribution);
        if (candidateArtifacts.isEmpty()) {
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            errors.warning(RB.$("WARNING.validation.packager.no.artifacts", distribution.getName(),
                packager.getType(), packager.getSupportedFileExtensions(distribution.getType())));
            packager.disable();
            return;
        } else if (candidateArtifacts.size() > 1) {
            errors.configuration(RB.$("validation_packager_multiple_artifacts", "distribution." + distribution.getName() + ".snap"));
            context.getLogger().debug(RB.$("validation.disabled.multiple.artifacts"));
            errors.warning(RB.$("WARNING.validation.packager.multiple.artifacts", distribution.getName(),
                packager.getType(), candidateArtifacts.stream()
                    .map(Artifact::getPath)
                    .collect(toList())));
            packager.disable();
            return;
        }

        validateCommitAuthor(packager, parentPackager);
        SnapPackager.SnapRepository snap = packager.getRepository();
        Validator.validateRepository(context, distribution, snap, parentPackager.getRepository(), "snap.repository");
        if (isBlank(snap.getName())) {
            snap.setName(distribution.getName() + "-snap");
        }
        validateTemplate(context, distribution, packager, parentPackager, errors);
        mergeExtraProperties(packager, parentPackager);
        validateContinueOnError(packager, parentPackager);
        if (isBlank(packager.getDownloadUrl())) {
            packager.setDownloadUrl(parentPackager.getDownloadUrl());
        }
        mergeSnapPlugs(packager, parentPackager);
        mergeSnapSlots(packager, parentPackager);

        if (isBlank(packager.getPackageName())) {
            packager.setPackageName(parentPackager.getPackageName());
            if (isBlank(packager.getPackageName())) {
                packager.setPackageName(distribution.getName());
            }
        }
        if (isBlank(packager.getBase())) {
            packager.setBase(parentPackager.getBase());
            if (isBlank(packager.getBase())) {
                packager.setBase("core20");
            }
        }
        if (isBlank(packager.getGrade())) {
            packager.setGrade(parentPackager.getGrade());
            if (isBlank(packager.getGrade())) {
                packager.setGrade("stable");
            }
        }
        if (isBlank(packager.getConfinement())) {
            packager.setConfinement(parentPackager.getConfinement());
            if (isBlank(packager.getConfinement())) {
                packager.setConfinement("strict");
            }
        }
        if (!packager.isRemoteBuildSet() && parentPackager.isRemoteBuildSet()) {
            packager.setRemoteBuild(parentPackager.isRemoteBuild());
        }
        if (!packager.isRemoteBuild() && isBlank(packager.getExportedLogin())) {
            packager.setExportedLogin(parentPackager.getExportedLogin());
            if (isBlank(packager.getExportedLogin())) {
                errors.configuration(RB.$("validation_must_not_be_empty", "distribution." + distribution.getName() + ".snap.exportedLogin"));
            } else if (!Files.exists(context.getBasedir().resolve(packager.getExportedLogin()))) {
                errors.configuration(RB.$("validation_directory_not_exist", "distribution." + distribution.getName() + ".snap.exportedLogin",
                    context.getBasedir().resolve(packager.getExportedLogin())));
            }
        }

        validateArtifactPlatforms(distribution, packager, candidateArtifacts, errors);

        packager.addArchitecture(parentPackager.getArchitectures());
        for (int i = 0; i < packager.getArchitectures().size(); i++) {
            SnapPackager.Architecture arch = packager.getArchitectures().get(i);
            if (!arch.hasBuildOn()) {
                errors.configuration(RB.$("validation_snap_missing_buildon", "distribution." + distribution.getName() + ".snap.architectures", i));
            }
        }
    }

    private static void mergeSnapPlugs(SnapPackager packager, SnapPackager common) {
        Set<String> localPlugs = new LinkedHashSet<>();
        localPlugs.addAll(packager.getLocalPlugs());
        localPlugs.addAll(common.getLocalPlugs());
        packager.setLocalPlugs(localPlugs);

        Map<String, SnapPackager.Plug> commonPlugs = common.getPlugs().stream()
            .collect(Collectors.toMap(SnapPackager.Plug::getName, SnapPackager.Plug::copyOf));
        Map<String, SnapPackager.Plug> packagerPlugs = packager.getPlugs().stream()
            .collect(Collectors.toMap(SnapPackager.Plug::getName, SnapPackager.Plug::copyOf));
        commonPlugs.forEach((name, cp) -> {
            SnapPackager.Plug tp = packagerPlugs.remove(name);
            if (null != tp) {
                cp.getAttributes().putAll(tp.getAttributes());
                Set<String> tmp = new LinkedHashSet<>(cp.getReads());
                tmp.addAll(tp.getReads());
                cp.setReads(new ArrayList<>(tmp));
                tmp = new LinkedHashSet<>(cp.getWrites());
                tmp.addAll(tp.getWrites());
                cp.setWrites(new ArrayList<>(tmp));
            }
        });
        commonPlugs.putAll(packagerPlugs);
        packager.setPlugs(new ArrayList<>(commonPlugs.values()));
    }

    private static void mergeSnapSlots(SnapPackager packager, SnapPackager common) {
        Set<String> localSlots = new LinkedHashSet<>();
        localSlots.addAll(packager.getLocalSlots());
        localSlots.addAll(common.getLocalSlots());
        packager.setLocalSlots(localSlots);

        Map<String, SnapPackager.Slot> commonSlots = common.getSlots().stream()
            .collect(Collectors.toMap(SnapPackager.Slot::getName, SnapPackager.Slot::copyOf));
        Map<String, SnapPackager.Slot> packagerSlots = packager.getSlots().stream()
            .collect(Collectors.toMap(SnapPackager.Slot::getName, SnapPackager.Slot::copyOf));
        commonSlots.forEach((name, cp) -> {
            SnapPackager.Slot tp = packagerSlots.remove(name);
            if (null != tp) {
                cp.getAttributes().putAll(tp.getAttributes());
                Set<String> tmp = new LinkedHashSet<>(cp.getReads());
                tmp.addAll(tp.getReads());
                cp.setReads(new ArrayList<>(tmp));
                tmp = new LinkedHashSet<>(cp.getWrites());
                tmp.addAll(tp.getWrites());
                cp.setWrites(new ArrayList<>(tmp));
            }
        });
        commonSlots.putAll(packagerSlots);
        packager.setSlots(new ArrayList<>(commonSlots.values()));
    }
}
