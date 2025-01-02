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
import org.jreleaser.model.Http;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.upload.HttpUploader;
import org.jreleaser.model.internal.validation.common.HttpValidator;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public final class HttpUploaderValidator {
    private HttpUploaderValidator() {
        // noop
    }

    public static void validateHttpUploader(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, HttpUploader> http = context.getModel().getUpload().getHttp();
        if (!http.isEmpty()) context.getLogger().debug("upload.http");

        for (Map.Entry<String, HttpUploader> e : http.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig()) {
                validateHttp(context, e.getValue(), errors);
            }
        }
    }

    private static void validateHttp(JReleaserContext context, HttpUploader uploader, Errors errors) {
        context.getLogger().debug("upload.http.{}", uploader.getName());

        resolveActivatable(context, uploader,
            listOf("upload.http." + uploader.getName(), "upload.http"),
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

        if (isBlank(uploader.getUploadUrl())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "upload.http." + uploader.getName() + ".uploadUrl"));
        }
        if (isBlank(uploader.getDownloadUrl())) {
            uploader.setDownloadUrl(uploader.getUploadUrl());
        }

        if (null == uploader.getMethod()) {
            uploader.setMethod(Http.Method.PUT);
        }

        HttpValidator.validateHttp(context, uploader, "upload", uploader.getName(), errors);
        validateTimeout(uploader);
    }
}
