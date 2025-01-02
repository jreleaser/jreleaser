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
import org.jreleaser.model.internal.upload.S3Uploader;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public final class S3UploaderValidator {
    private S3UploaderValidator() {
        // noop
    }

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

    private static void validateS3(JReleaserContext context, S3Uploader uploader, Errors errors) {
        context.getLogger().debug("upload.s3.{}", uploader.getName());

        resolveActivatable(context, uploader,
            listOf("upload.s3." + uploader.getName(), "upload.s3"),
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

        String baseKey1 = "upload.s3." + uploader.getName();
        String baseKey2 = "upload.s3";
        String baseKey3 = "s3." + uploader.getName();
        String baseKey4 = "s3";

        uploader.setRegion(
            checkProperty(context,
                listOf(
                    baseKey1 + ".region",
                    baseKey2 + ".region",
                    baseKey3 + ".region",
                    baseKey4 + ".region"),
                baseKey1 + ".region",
                uploader.getRegion(),
                errors));

        uploader.setBucket(
            checkProperty(context,
                listOf(
                    baseKey1 + ".bucket",
                    baseKey2 + ".bucket",
                    baseKey3 + ".bucket",
                    baseKey4 + ".bucket"),
                baseKey1 + ".bucket",
                uploader.getBucket(),
                errors));

        uploader.setAccessKeyId(
            checkProperty(context,
                listOf(
                    baseKey1 + ".access.key.id",
                    baseKey2 + ".access.key.id",
                    baseKey3 + ".access.key.id",
                    baseKey4 + ".access.key.id"),
                baseKey1 + ".accessKeyId",
                uploader.getAccessKeyId(),
                uploader.getAccessKeyId()));

        uploader.setSecretKey(
            checkProperty(context,
                listOf(
                    baseKey1 + ".secret.key",
                    baseKey2 + ".secret.key",
                    baseKey3 + ".secret.key",
                    baseKey4 + ".secret.key"),
                baseKey1 + ".secretKey",
                uploader.getSecretKey(),
                uploader.getSecretKey()));

        uploader.setSessionToken(
            checkProperty(context,
                listOf(
                    baseKey1 + ".session.token",
                    baseKey2 + ".session.token",
                    baseKey3 + ".session.token",
                    baseKey4 + ".session.token"),
                baseKey1 + ".sessionToken",
                uploader.getSessionToken(),
                uploader.getSessionToken()));

        uploader.setPath(
            checkProperty(context,
                listOf(
                    baseKey1 + ".path",
                    baseKey2 + ".path",
                    baseKey3 + ".path",
                    baseKey4 + ".path"),
                baseKey1 + ".path",
                uploader.getPath(),
                "{{projectName}}/{{tagName}}/{{artifactFile}}"));

        uploader.setDownloadUrl(
            checkProperty(context,
                listOf(
                    baseKey1 + ".download.url",
                    baseKey2 + ".download.url",
                    baseKey3 + ".download.url",
                    baseKey4 + ".download.url"),
                baseKey1 + ".downloadUrl",
                uploader.getDownloadUrl(),
                uploader.getDownloadUrl()));

        uploader.setEndpoint(
            checkProperty(context,
                listOf(
                    baseKey1 + ".endpoint",
                    baseKey2 + ".endpoint",
                    baseKey3 + ".endpoint",
                    baseKey4 + ".endpoint"),
                baseKey1 + "endpoint",
                uploader.getEndpoint(),
                ""));

        if (isNotBlank(uploader.getEndpoint()) && isBlank(uploader.getDownloadUrl())) {
            errors.configuration(RB.$("validation_s3_missing_download_url", baseKey1));
        }

        validateTimeout(uploader);
    }
}
