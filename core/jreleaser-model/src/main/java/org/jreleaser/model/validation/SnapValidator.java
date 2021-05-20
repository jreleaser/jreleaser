/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Plug;
import org.jreleaser.model.Slot;
import org.jreleaser.model.Snap;
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
        if (!model.getRelease().getGitService().isReleaseSupported()) {
            tool.disable();
            return;
        }

        context.getLogger().debug("distribution.{}.snap", distribution.getName());

        validateCommitAuthor(tool, parentTool);
        validateOwner(tool.getSnap(), parentTool.getSnap());
        validateTemplate(context, distribution, tool, parentTool, errors);
        Snap commonSnap = parentTool;
        mergeExtraProperties(tool, parentTool);
        mergeSnapPlugs(tool, parentTool);
        mergeSnapSlots(tool, parentTool);

        if (isBlank(tool.getBase())) {
            tool.setBase(commonSnap.getBase());
            if (isBlank(tool.getBase())) {
                errors.configuration("distribution." + distribution.getName() + ".snap.base must not be blank");
            }
        }
        if (isBlank(tool.getGrade())) {
            tool.setGrade(commonSnap.getGrade());
            if (isBlank(tool.getGrade())) {
                errors.configuration("distribution." + distribution.getName() + ".snap.grade must not be blank");
            }
        }
        if (isBlank(tool.getConfinement())) {
            tool.setConfinement(commonSnap.getConfinement());
            if (isBlank(tool.getConfinement())) {
                errors.configuration("distribution." + distribution.getName() + ".snap.confinement must not be blank");
            }
        }
        if (!tool.isRemoteBuildSet() && parentTool.isRemoteBuildSet()) {
            tool.setRemoteBuild(parentTool.isRemoteBuild());
        }
        if (!tool.isRemoteBuild() && isBlank(tool.getExportedLogin())) {
            tool.setExportedLogin(commonSnap.getExportedLogin());
            if (isBlank(tool.getExportedLogin())) {
                errors.configuration("distribution." + distribution.getName() + ".snap.exportedLogin must not be empty");
            } else if (!context.getBasedir().resolve(tool.getExportedLogin()).toFile().exists()) {
                errors.configuration("distribution." + distribution.getName() + ".snap.exportedLogin does not exist. " +
                    context.getBasedir().resolve(tool.getExportedLogin()));
            }
        }

        if (isBlank(tool.getSnap().getName())) {
            tool.getSnap().setName(distribution.getName() + "-snap");
        }
        tool.getSnap().setBasename(distribution.getName() + "-snap");
        if (isBlank(tool.getSnap().getUsername())) {
            tool.getSnap().setUsername(parentTool.getSnap().getUsername());
        }
        if (isBlank(tool.getSnap().getToken())) {
            tool.getSnap().setToken(parentTool.getSnap().getToken());
        }

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
