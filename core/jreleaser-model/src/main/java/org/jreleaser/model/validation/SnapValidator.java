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

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Plug;
import org.jreleaser.model.Slot;
import org.jreleaser.model.Snap;
import org.jreleaser.model.SnapTap;
import org.jreleaser.util.Errors;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jreleaser.model.validation.DistributionsValidator.validateArtifactPlatforms;
import static org.jreleaser.model.validation.ExtraPropertiesValidator.mergeExtraProperties;
import static org.jreleaser.model.validation.TemplateValidator.validateTemplate;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class SnapValidator extends Validator {
    public static void validateSnap(JReleaserContext context, Distribution distribution, Snap tool, Errors errors) {
        JReleaserModel model = context.getModel();
        Snap parentTool = model.getPackagers().getSnap();

        if (!tool.isActiveSet() && parentTool.isActiveSet()) {
            tool.setActive(parentTool.getActive());
        }
        if (!tool.resolveEnabled(context.getModel().getProject(), distribution)) return;
        GitService service = model.getRelease().getGitService();
        if (!service.isReleaseSupported()) {
            tool.disable();
            return;
        }

        context.getLogger().debug("distribution.{}.snap", distribution.getName());

        validateCommitAuthor(tool, parentTool);
        SnapTap snap = tool.getSnap();
        snap.resolveEnabled(model.getProject());
        validateOwner(snap, parentTool.getSnap());
        if (isBlank(snap.getBranch())) {
            snap.setBranch(parentTool.getSnap().getBranch());
        }
        validateTemplate(context, distribution, tool, parentTool, errors);
        mergeExtraProperties(tool, parentTool);
        validateContinueOnError(tool, parentTool);
        mergeSnapPlugs(tool, parentTool);
        mergeSnapSlots(tool, parentTool);

        if (isBlank(tool.getBase())) {
            tool.setBase(parentTool.getBase());
            if (isBlank(tool.getBase())) {
                errors.configuration(RB.$("validation_must_not_be_blank", "distribution." + distribution.getName() + ".snap.base"));
            }
        }
        if (isBlank(tool.getGrade())) {
            tool.setGrade(parentTool.getGrade());
            if (isBlank(tool.getGrade())) {
                errors.configuration(RB.$("validation_must_not_be_blank", "distribution." + distribution.getName() + ".snap.grade"));
            }
        }
        if (isBlank(tool.getConfinement())) {
            tool.setConfinement(parentTool.getConfinement());
            if (isBlank(tool.getConfinement())) {
                errors.configuration(RB.$("validation_must_not_be_blank", "distribution." + distribution.getName() + ".snap.confinement"));
            }
        }
        if (!tool.isRemoteBuildSet() && parentTool.isRemoteBuildSet()) {
            tool.setRemoteBuild(parentTool.isRemoteBuild());
        }
        if (!tool.isRemoteBuild() && isBlank(tool.getExportedLogin())) {
            tool.setExportedLogin(parentTool.getExportedLogin());
            if (isBlank(tool.getExportedLogin())) {
                errors.configuration(RB.$("validation_must_not_be_empty", "distribution." + distribution.getName() + ".snap.exportedLogin"));
            } else if (!context.getBasedir().resolve(tool.getExportedLogin()).toFile().exists()) {
                errors.configuration(RB.$("validation_directory_not_exist", "distribution." + distribution.getName() + ".snap.exportedLogin",
                    context.getBasedir().resolve(tool.getExportedLogin())));
            }
        }

        if (isBlank(snap.getName())) {
            snap.setName(distribution.getName() + "-snap");
        }
        if (isBlank(snap.getUsername())) {
            snap.setUsername(parentTool.getSnap().getUsername());
        }
        if (isBlank(snap.getToken())) {
            snap.setToken(parentTool.getSnap().getToken());
        }

        validateTap(context, distribution, snap, "snap.snap");

        validateArtifactPlatforms(context, distribution, tool, errors);
    }

    private static void mergeSnapPlugs(Snap tool, Snap common) {
        Set<String> localPlugs = new LinkedHashSet<>();
        localPlugs.addAll(tool.getLocalPlugs());
        localPlugs.addAll(common.getLocalPlugs());
        tool.setLocalPlugs(localPlugs);

        Map<String, Plug> commonPlugs = common.getPlugs().stream()
            .collect(Collectors.toMap(Plug::getName, Plug::copyOf));
        Map<String, Plug> toolPlugs = tool.getPlugs().stream()
            .collect(Collectors.toMap(Plug::getName, Plug::copyOf));
        commonPlugs.forEach((name, cp) -> {
            Plug tp = toolPlugs.remove(name);
            if (null != tp) {
                cp.getAttributes().putAll(tp.getAttributes());
            }
        });
        commonPlugs.putAll(toolPlugs);
        tool.setPlugs(new ArrayList<>(commonPlugs.values()));
    }

    private static void mergeSnapSlots(Snap tool, Snap common) {
        Set<String> localSlots = new LinkedHashSet<>();
        localSlots.addAll(tool.getLocalSlots());
        localSlots.addAll(common.getLocalSlots());
        tool.setLocalSlots(localSlots);

        Map<String, Slot> commonSlots = common.getSlots().stream()
            .collect(Collectors.toMap(Slot::getName, Slot::copyOf));
        Map<String, Slot> toolSlots = tool.getSlots().stream()
            .collect(Collectors.toMap(Slot::getName, Slot::copyOf));
        commonSlots.forEach((name, cp) -> {
            Slot tp = toolSlots.remove(name);
            if (null != tp) {
                cp.getAttributes().putAll(tp.getAttributes());
            }
        });
        commonSlots.putAll(toolSlots);
        tool.setSlots(new ArrayList<>(commonSlots.values()));
    }
}
