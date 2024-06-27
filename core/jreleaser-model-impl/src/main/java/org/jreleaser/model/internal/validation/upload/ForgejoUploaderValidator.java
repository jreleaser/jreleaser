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
import org.jreleaser.model.internal.servers.ForgejoServer;
import org.jreleaser.model.internal.upload.ForgejoUploader;
import org.jreleaser.model.internal.validation.common.ServerValidator;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.AuthenticatableValidator.validatePassword;
import static org.jreleaser.model.internal.validation.common.ServerValidator.validateHost;
import static org.jreleaser.model.internal.validation.common.Validator.mergeErrors;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.18.0
 */
public final class ForgejoUploaderValidator {
    private ForgejoUploaderValidator() {
        // noop
    }

    public static void validateForgejoUploader(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, ForgejoUploader> forgejo = context.getModel().getUpload().getForgejo();
        if (!forgejo.isEmpty()) context.getLogger().debug("upload.forgejo");

        for (Map.Entry<String, ForgejoUploader> e : forgejo.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig()) {
                Errors incoming = new Errors();
                validateForgejoUploader(context, e.getValue(), incoming);
                mergeErrors(context, errors, incoming, e.getValue());
            }
        }
    }

    private static void validateForgejoUploader(JReleaserContext context, ForgejoUploader uploader, Errors errors) {
        context.getLogger().debug("upload.forgejo.{}", uploader.getName());

        resolveActivatable(context, uploader,
            listOf("upload.forgejo." + uploader.getName(), "upload.forgejo"),
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

        String serverName = uploader.getServerRef();
        ForgejoServer server = context.getModel().getServers().forgejoFor(serverName);
        validatePassword(context, uploader, server, "upload", "forgejo", uploader.getName(), errors, context.isDryrun());
        validateHost(context, uploader, server, "upload", "forgejo", uploader.getName(), errors, false);
        ServerValidator.validateTimeout(context, uploader, server, "upload", "forgejo", uploader.getName(), errors, true);

        if (isBlank(uploader.getPackageName())) {
            uploader.setPackageName(uploader.getName());
        }
        if (isBlank(uploader.getPackageVersion())) {
            uploader.setPackageVersion("{{projectVersion}}");
        }

        if (isBlank(uploader.getOwner())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "upload.fprgejo." + uploader.getName() + ".owner"));
        }

        validateTimeout(uploader);
    }
}
