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
package org.jreleaser.model.internal.validation.upload;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.upload.S3Uploader;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public abstract class S3UploaderValidator {
    public static void validateS3(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, S3Uploader> s3 = context.getModel().getUpload().getS3();
        if (!s3.isEmpty()) context.getLogger().debug("upload.s3");

        for (Map.Entry<String, S3Uploader> e : s3.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig()) {
                validateS3(context, e.getValue(), errors);
            }
        }
    }

    private static void validateS3(JReleaserContext context, S3Uploader s3, Errors errors) {
        context.getLogger().debug("upload.s3.{}", s3.getName());

        if (!s3.isActiveSet()) {
            s3.setActive(Active.NEVER);
        }
        if (!s3.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (!s3.isArtifacts() && !s3.isFiles() && !s3.isSignatures()) {
            errors.warning(RB.$("WARNING.validation.uploader.no.artifacts", s3.getType(), s3.getName()));
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            s3.disable();
            return;
        }

        String baseKey = "upload.s3." + s3.getName() + ".";
        s3.setRegion(
            checkProperty(context,
                Env.toVar("S3_" + s3.getName()) + "_REGION",
                baseKey + "region",
                s3.getRegion(),
                errors));

        s3.setBucket(
            checkProperty(context,
                Env.toVar("S3_" + s3.getName()) + "_BUCKET",
                baseKey + "bucket",
                s3.getBucket(),
                errors));

        s3.setAccessKeyId(
            checkProperty(context,
                Env.toVar("S3_" + s3.getName()) + "_ACCESS_KEY_ID",
                baseKey + "accessKeyId",
                s3.getAccessKeyId(),
                s3.getAccessKeyId()));

        s3.setSecretKey(
            checkProperty(context,
                Env.toVar("S3_" + s3.getName()) + "_SECRET_KEY",
                baseKey + "secretKey",
                s3.getSecretKey(),
                s3.getSecretKey()));

        s3.setSessionToken(
            checkProperty(context,
                Env.toVar("S3_" + s3.getName()) + "_SESSION_TOKEN",
                baseKey + "sessionToken",
                s3.getSessionToken(),
                s3.getSessionToken()));

        s3.setPath(
            checkProperty(context,
                Env.toVar("S3_" + s3.getName()) + "_PATH",
                baseKey + "path",
                s3.getPath(),
                "{{projectName}}/{{tagName}}/{{artifactFile}}"));

        s3.setDownloadUrl(
            checkProperty(context,
                Env.toVar("S3_" + s3.getName()) + "_DOWNLOAD_URL",
                "s3." + s3.getName() + ".downloadUrl",
                s3.getDownloadUrl(),
                s3.getDownloadUrl()));

        s3.setEndpoint(
            checkProperty(context,
                Env.toVar("S3_" + s3.getName()) + "_ENDPOINT",
                baseKey + "endpoint",
                s3.getEndpoint(),
                ""));

        if (isNotBlank(s3.getEndpoint()) && isBlank(s3.getDownloadUrl())) {
            errors.configuration(RB.$("validation_s3_missing_download_url", "s3." + s3.getName()));
        }

        validateTimeout(s3);
    }
}
