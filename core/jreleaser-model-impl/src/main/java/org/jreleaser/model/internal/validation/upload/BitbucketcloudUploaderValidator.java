/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.upload.BitbucketcloudUploader;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Hasnae Rehioui
 * @since 1.7.0
 */
public class BitbucketcloudUploaderValidator {
    private BitbucketcloudUploaderValidator() {
        // noop
    }

    public static void validateBitbucketcloud(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, BitbucketcloudUploader> bitbucketcloud = context.getModel().getUpload().getBitbucketcloud();
        if (!bitbucketcloud.isEmpty()) context.getLogger().debug("upload.bitbucketcloud");

        for (Map.Entry<String, BitbucketcloudUploader> e: bitbucketcloud.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig()) {
                validateBitbucketcloud(context, e.getValue(), errors);
            }
        }
    }

    private static void validateBitbucketcloud(JReleaserContext context, BitbucketcloudUploader uploader, Errors errors) {
        context.getLogger().debug("upload.bitbucketcloud.{}", uploader.getName());

        resolveActivatable(context, uploader,
            listOf("upload.bitbucketcloud." + uploader.getName(), "upload.bitbucketcloud"),
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

        String baseKey1 = "upload.bitbucketcloud." + uploader.getName();
        String baseKey2 = "upload.bitbucketcloud";
        String baseKey3 = "bitbucketcloud." + uploader.getName();
        String baseKey4 = "bitbucketcloud";

        uploader.setToken(
            checkProperty(context,
                listOf(
                    baseKey1 + ".token",
                    baseKey2 + ".token",
                    baseKey3 + ".token",
                    baseKey4 + ".token"),
                baseKey1 + ".token",
                uploader.getToken(),
                errors,
                context.isDryrun()));

        if (isBlank(uploader.getPackageName())) {
            uploader.setPackageName(uploader.getName());
        }
        if (isBlank(uploader.getPackageVersion())) {
            uploader.setPackageVersion("{{projectVersion}}");
        }

        if (isBlank(uploader.getProjectIdentifier())) {
            errors.configuration(RB.$("validation_must_not_be_blank", baseKey1 + ".projectIdentifier"));
        }

        validateTimeout(uploader);
    }
}
