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
package org.jreleaser.model.internal.validation;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.upload.ScpUploader;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.SshValidator.validateSsh;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public abstract class ScpUploaderValidator extends Validator {
    public static void validateScpUploader(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, ScpUploader> scp = context.getModel().getUpload().getScp();
        if (!scp.isEmpty()) context.getLogger().debug("upload.scp");

        for (Map.Entry<String, ScpUploader> e : scp.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig()) {
                validateScpUploader(context, mode, e.getValue(), errors);
            }
        }
    }

    private static void validateScpUploader(JReleaserContext context, Mode mode, ScpUploader scp, Errors errors) {
        context.getLogger().debug("upload.scp.{}", scp.getName());

        if (!scp.isActiveSet()) {
            scp.setActive(Active.NEVER);
        }
        if (!scp.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (!scp.isArtifacts() && !scp.isFiles() && !scp.isSignatures()) {
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            scp.disable();
            return;
        }

        validateSsh(context, scp, scp.getName(), "SCP", scp.getType(), errors);
        if (isBlank(scp.getPath())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "scp." + scp.getName() + ".path"));
        }
        validateTimeout(scp);
    }
}
