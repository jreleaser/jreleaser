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
package org.jreleaser.model.internal.validation.upload;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.upload.ScpUploader;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.SshValidator.validateSsh;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class ScpUploaderValidator {
    private ScpUploaderValidator() {
        // noop
    }

    public static void validateScpUploader(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, ScpUploader> scp = context.getModel().getUpload().getScp();
        if (!scp.isEmpty()) context.getLogger().debug("upload.scp");

        for (Map.Entry<String, ScpUploader> e : scp.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig()) {
                validateScpUploader(context, e.getValue(), errors);
            }
        }
    }

    private static void validateScpUploader(JReleaserContext context, ScpUploader uploader, Errors errors) {
        context.getLogger().debug("upload.scp.{}", uploader.getName());

        resolveActivatable(context, uploader,
            listOf("upload.scp." + uploader.getName(), "upload.scp"),
            "NEVER");
        if (!uploader.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (!uploader.isArtifacts() && !uploader.isFiles() && !uploader.isSignatures()) {
            errors.warning(RB.$("WARNING.validation.uploader.no.artifacts", uploader.getType(), uploader.getName()));
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            uploader.disable();
            return;
        }

        validateSsh(context, uploader, uploader.getType(), uploader.getName(), "upload.", errors);
        if (isBlank(uploader.getPath())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "upload.scp." + uploader.getName() + ".path"));
        }
        validateTimeout(uploader);
    }
}
