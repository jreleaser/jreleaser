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
import org.jreleaser.model.internal.packagers.BrewPackager;
import org.jreleaser.model.internal.release.Releaser;
import org.jreleaser.model.internal.validation.common.Validator;
import org.jreleaser.util.Errors;
import org.jreleaser.util.PlatformUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.jreleaser.model.Constants.SKIP_CASK_DISPLAY_NAME_TRANSFORM;
import static org.jreleaser.model.api.packagers.BrewPackager.SKIP_BREW;
import static org.jreleaser.model.internal.validation.common.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.internal.validation.common.TemplateValidator.validateTemplate;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateCommitAuthor;
import static org.jreleaser.model.internal.validation.common.Validator.validateContinueOnError;
import static org.jreleaser.model.internal.validation.distributions.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.isTrue;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class BrewPackagerValidator {
    private BrewPackagerValidator() {
        // noop
    }

    public static void validateBrew(JReleaserContext context, Distribution distribution, BrewPackager packager, Errors errors) {
        context.getLogger().debug("distribution.{}." + packager.getType(), distribution.getName());
        JReleaserModel model = context.getModel();
        BrewPackager parentPackager = model.getPackagers().getBrew();

        resolveActivatable(context, packager, "distributions." + distribution.getName() + "." + packager.getType(), parentPackager);
        if (!packager.resolveEnabled(context.getModel().getProject(), distribution)) {
            context.getLogger().debug(RB.$("validation.disabled"));
            packager.getCask().disable();
            return;
        }
        Releaser<?> service = model.getRelease().getReleaser();
        if (!service.isReleaseSupported()) {
            context.getLogger().debug(RB.$("validation.disabled.release"));
            packager.disable();
            packager.getCask().disable();
            return;
        }

        BrewPackager.Cask cask = preValidateCask(distribution, packager, parentPackager);

        if (!packager.isMultiPlatformSet() && parentPackager.isMultiPlatformSet()) {
            packager.setMultiPlatform(parentPackager.isMultiPlatform());
        }
        if (packager.isMultiPlatform() &&
            (distribution.getType() == org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR ||
                distribution.getType() == org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY ||
                distribution.getType() == org.jreleaser.model.Distribution.DistributionType.NATIVE_PACKAGE)) {
            packager.setMultiPlatform(false);
        }
        if (packager.isMultiPlatform()) {
            packager.getCask().disable();
        }
        if (isBlank(packager.getFormulaName())) {
            packager.setFormulaName(distribution.getName());
        }
        if (isBlank(packager.getDownloadStrategy())) {
            packager.setDownloadStrategy(parentPackager.getDownloadStrategy());
        }

        if (distribution.getType() == org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR ||
            distribution.getType() == org.jreleaser.model.Distribution.DistributionType.FLAT_BINARY &&
                isBlank(packager.getDownloadStrategy())) {
            packager.setDownloadStrategy(":nounzip");
        }

        Set<String> tmpSet = new LinkedHashSet<>();
        tmpSet.addAll(parentPackager.getRequireRelative());
        tmpSet.addAll(packager.getRequireRelative());
        packager.setRequireRelative(tmpSet);

        List<String> tmpList = new ArrayList<>();
        tmpList.addAll(parentPackager.getLivecheck());
        tmpList.addAll(packager.getLivecheck());
        packager.setLivecheck(tmpList);

        mergeExtraProperties(packager, parentPackager);
        validateCask(context, distribution, packager, cask, parentPackager.getCask(), errors);
        List<Artifact> candidateArtifacts = packager.resolveCandidateArtifacts(context, distribution);
        if (candidateArtifacts.isEmpty()) {
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            errors.warning(RB.$("WARNING.validation.packager.no.artifacts", distribution.getName(),
                packager.getType(), packager.getSupportedFileExtensions(distribution.getType())));
            packager.disable();
            return;
        }

        validateCommitAuthor(packager, parentPackager);
        BrewPackager.HomebrewRepository tap = packager.getRepository();
        Validator.validateRepository(context, distribution, tap, parentPackager.getRepository(), "brew.repository");
        validateTemplate(context, distribution, packager, parentPackager, errors);
        validateContinueOnError(packager, parentPackager);
        if (isBlank(packager.getDownloadUrl())) {
            packager.setDownloadUrl(parentPackager.getDownloadUrl());
        }

        List<BrewPackager.Dependency> dependencies = new ArrayList<>(parentPackager.getDependenciesAsList());
        dependencies.addAll(packager.getDependenciesAsList());
        packager.setDependenciesAsList(dependencies);

        if (!cask.isEnabled()) {
            validateArtifactPlatforms(distribution, packager, candidateArtifacts, errors);
        }
    }

    private static BrewPackager.Cask preValidateCask(Distribution distribution, BrewPackager packager, BrewPackager parentPackager) {
        BrewPackager.Cask cask = packager.getCask();
        if (distribution.getType() == org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR) {
            packager.getCask().disable();
            return cask;
        }

        BrewPackager.Cask parentCask = parentPackager.getCask();

        if (!cask.isEnabledSet() && parentCask.isEnabledSet()) {
            cask.setEnabled(parentCask.isEnabled());
        }

        return cask;
    }

    private static void validateCask(JReleaserContext context, Distribution distribution, BrewPackager packager,
                                     BrewPackager.Cask cask, BrewPackager.Cask parentCask, Errors errors) {
        if (null == cask || cask.isEnabledSet() && !cask.isEnabled()) {
            return;
        }

        context.getLogger().debug("distribution.{}.brew.cask", distribution.getName());

        // look for a .dmg, .pkg. or .zip
        int dmgFound = 0;
        int pkgFound = 0;
        int zipFound = 0;
        String pkgName = "";
        for (Artifact artifact : distribution.getArtifacts()) {
            if (!artifact.isActiveAndSelected()) continue;
            if (artifact.getPath().endsWith(".dmg") && !isTrue(artifact.getExtraProperties().get(SKIP_BREW)) &&
                PlatformUtils.isMac(artifact.getPlatform())) {
                dmgFound++;
            } else if (artifact.getPath().endsWith(".pkg") && !isTrue(artifact.getExtraProperties().get(SKIP_BREW)) &&
                PlatformUtils.isMac(artifact.getPlatform())) {
                pkgFound++;
                pkgName = artifact.getEffectivePath(context).getFileName().toString();
            } else if (artifact.getPath().endsWith(".zip") && !isTrue(artifact.getExtraProperties().get(SKIP_BREW)) &&
                (PlatformUtils.isMac(artifact.getPlatform()) || isBlank(artifact.getPlatform()))) {
                zipFound++;
            }
        }

        if (dmgFound == 0 && pkgFound == 0 && zipFound == 0) {
            // no artifacts found, disable cask
            cask.disable();
            return;
        } else if (dmgFound > 1) {
            errors.configuration(RB.$("validation_brew_multiple_artifact", "distribution." + distribution.getName() + ".brew", ".dmg"));
            context.getLogger().debug(RB.$("validation.disabled.error"));
            cask.disable();
            return;
        } else if (pkgFound > 1) {
            errors.configuration(RB.$("validation_brew_multiple_artifact", "distribution." + distribution.getName() + ".brew", ".pkg"));
            context.getLogger().debug(RB.$("validation.disabled.error"));
            cask.disable();
            return;
        } else if (zipFound > 1) {
            errors.configuration(RB.$("validation_brew_multiple_artifact", "distribution." + distribution.getName() + ".brew", ".zip"));
            context.getLogger().debug(RB.$("validation.disabled.error"));
            cask.disable();
            return;
        } else if (dmgFound + pkgFound + zipFound > 1) {
            errors.configuration(RB.$("validation_brew_single_artifact", "distribution." + distribution.getName() + ".brew"));
            context.getLogger().debug(RB.$("validation.disabled.error"));
            cask.disable();
            return;
        }

        if (zipFound == 1 && !cask.isEnabled()) {
            // zips should only be packaged into Casks when explicitly stated
            // https://github.com/jreleaser/jreleaser/issues/337
            return;
        }

        cask.enable();

        if (isBlank(cask.getName())) {
            cask.setName(parentCask.getName());
        }
        if (isBlank(cask.getPkgName())) {
            cask.setPkgName(parentCask.getPkgName());
        }
        if (isBlank(cask.getAppName())) {
            cask.setPkgName(parentCask.getAppName());
        }
        if (isBlank(cask.getDisplayName())) {
            cask.setDisplayName(parentCask.getDisplayName());
        }
        if (isBlank(cask.getAppcast())) {
            cask.setAppcast(parentCask.getAppcast());
        }
        if (cask.getZapItems().isEmpty()) {
            cask.getZapItems().addAll(parentCask.getZapItems());
        }
        if (cask.getUninstallItems().isEmpty()) {
            cask.getUninstallItems().addAll(parentCask.getUninstallItems());
        }

        if (isBlank(cask.getPkgName()) && isNotBlank(pkgName)) {
            cask.setPkgName(pkgName);
        }

        if (isNotBlank(cask.getPkgName())) {
            if (!cask.getPkgName().endsWith(".pkg")) {
                cask.setPkgName(cask.getPkgName() + ".pkg");
            }
        } else if (isBlank(cask.getAppName())) {
            cask.setAppName(packager.getResolvedFormulaName(context) + ".app");
        } else if (!cask.getAppName().endsWith(".app")) {
            cask.setAppName(cask.getAppName() + ".app");
        }
        if (zipFound > 0) {
            cask.setAppName("");
            cask.setPkgName("");
        }

        if (isBlank(cask.getName())) {
            cask.setName(packager.getResolvedFormulaName(context).toLowerCase(Locale.ENGLISH));
        }
        if (isNotBlank(cask.getDisplayName()) && !packager.getExtraProperties().containsKey(SKIP_CASK_DISPLAY_NAME_TRANSFORM)) {
            packager.getExtraProperties().put(SKIP_CASK_DISPLAY_NAME_TRANSFORM, "true");
        }
        if (isBlank(cask.getDisplayName())) {
            cask.setDisplayName(packager.getResolvedFormulaName(context));
        }
    }

    public static void postValidateBrew(JReleaserContext context, Errors errors) {
        Map<String, List<Distribution>> map = context.getModel().getActiveDistributions().stream()
            .filter(d -> d.getBrew().isEnabled())
            .collect(groupingBy(d -> d.getBrew().getResolvedFormulaName(context)));

        map.forEach((formulaName, distributions) -> {
            if (distributions.size() > 1) {
                errors.configuration(RB.$("validation_brew_duplicate_definition", "brew.formulaName '" + formulaName + "'",
                    distributions.stream().map(Distribution::getName).collect(Collectors.joining(", "))));
            }
        });

        map = context.getModel().getActiveDistributions().stream()
            .filter(d -> d.getBrew().getCask().isEnabled())
            .collect(groupingBy(d -> d.getBrew().getCask().getResolvedCaskName(context.props())));

        map.forEach((caskName, distributions) -> {
            if (distributions.size() > 1) {
                errors.configuration(RB.$("validation_brew_duplicate_definition", "brew.cask.name '" + caskName + "'",
                    distributions.stream().map(Distribution::getName).collect(Collectors.joining(", "))));
            }
        });
    }
}
