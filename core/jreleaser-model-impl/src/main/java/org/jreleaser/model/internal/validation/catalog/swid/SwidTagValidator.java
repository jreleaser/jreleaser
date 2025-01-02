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
package org.jreleaser.model.internal.validation.catalog.swid;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.catalog.swid.Entity;
import org.jreleaser.model.internal.catalog.swid.SwidTag;
import org.jreleaser.model.internal.common.Java;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.util.Errors;

import java.util.Map;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.11.0
 */
public final class SwidTagValidator {
    private SwidTagValidator() {
        // noop
    }

    public static void validateSwid(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, SwidTag> swid = context.getModel().getCatalog().getSwid();
        if (swid.isEmpty()) {
            swid.put("swid-tag", new SwidTag());
        }
        context.getLogger().debug("catalog.swid");

        for (Map.Entry<String, SwidTag> e : swid.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig() || mode.validateAssembly()) {
                validateSwid(context, e.getValue(), errors);
            }
        }
    }

    private static void validateSwid(JReleaserContext context, SwidTag tag, Errors errors) {
        context.getLogger().debug("catalog.swid.{}", tag.getName());

        resolveActivatable(context, tag,
            listOf("catalog.swid." + tag.getName(), "catalog.swid"),
            "NEVER");
        tag.resolveEnabled(context.getModel().getProject());

        if (isBlank(tag.getName())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "swid.name"));
            context.getLogger().debug(RB.$("validation.disabled.error"));
            tag.disable();
            return;
        }

        validateSwidTag(context, tag, errors);
    }

    public static void validateSwid(JReleaserContext context, SwidTag tag, String activatablePath, Errors errors) {
        String tagRef = tag.getTagRef();
        SwidTag other = context.getModel().getCatalog().getSwid().get(tagRef);
        validateSwid(context, tag, other, activatablePath, errors);
    }

    private static void validateSwid(JReleaserContext context, SwidTag tag, SwidTag other, String activatablePath, Errors errors) {
        resolveActivatable(context, tag,
            listOf(activatablePath + ".swid." + tag.getName(), activatablePath + ".swid"),
            null);

        tag.copyFrom(other);

        if (!tag.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        validateSwidTag(context, tag, errors);
    }

    private static void validateSwidTag(JReleaserContext context, SwidTag tag, Errors errors) {
        if (isBlank(tag.getLang())) {
            tag.setLang("en-US");
        }

        if (isBlank(tag.getPath())) {
            tag.setPath("SWIDTAG");
        }

        if (null == tag.getTagVersion()) {
            tag.setTagVersion(1);
        }

        if (tag.getTagVersion() < 1) {
            errors.configuration(RB.$("validation_must_be greater_than", "swid.tagVersion", "0"));
        }

        Project project = context.getModel().getProject();

        if (isBlank(tag.getTagId())) {
            Java java = project.getLanguages().getJava();
            if (java.isEnabled()) {
                tag.setTagId(UUID.nameUUIDFromBytes(
                    (java.getGroupId() + "-" +
                        java.getArtifactId() + "-" +
                        project.getVersion()).getBytes(UTF_8)
                ).toString());
            } else {
                tag.setTagId(UUID.nameUUIDFromBytes(
                    (project.getVendor() + "-" +
                        project.getName() + "_" +
                        project.getVersion()).getBytes(UTF_8)
                ).toString());
            }
        }

        if (tag.getEntities().isEmpty()) {
            Entity entity = new Entity();

            entity.setName(project.getVendor());
            if (isBlank(entity.getName())) {
                entity.setName(project.getName());
            }

            entity.setRegid(project.getLinks().getHomepage());
            if (isBlank(entity.getRegid())) {
                entity.setRegid("http://invalid.unavailable");
            }

            entity.setRoles(setOf("tagCreator", "softwareCreator"));

            tag.getEntities().add(entity);
        }

        // TODO: at least 1 entity must have role = tagCreator softwareCreator
    }
}
