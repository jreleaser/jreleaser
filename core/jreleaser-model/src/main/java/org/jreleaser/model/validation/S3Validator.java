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

import org.jreleaser.model.Active;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.S3;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public abstract class S3Validator extends Validator {
    public static void validateS3(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        context.getLogger().debug("s3");
        Map<String, S3> s3 = context.getModel().getUpload().getS3();

        for (Map.Entry<String, S3> e : s3.entrySet()) {
            e.getValue().setName(e.getKey());
            validateS3(context, mode, e.getValue(), errors);
        }
    }

    private static void validateS3(JReleaserContext context, JReleaserContext.Mode mode, S3 s3, Errors errors) {
        context.getLogger().debug("s3.{}", s3.getName());

        if (!s3.isActiveSet()) {
            s3.setActive(Active.NEVER);
        }
        if (!s3.resolveEnabled(context.getModel().getProject())) return;

        if (!s3.isArtifacts() && !s3.isFiles() && !s3.isSignatures()) {
            s3.disable();
            return;
        }

        s3.setRegion(
            checkProperty(context,
                Env.toVar("S3_" + s3.getName()) + "_REGION",
                "s3." + s3.getName() + ".region",
                s3.getRegion(),
                errors));

        s3.setBucket(
            checkProperty(context,
                Env.toVar("S3_" + s3.getName()) + "_BUCKET",
                "s3." + s3.getName() + ".bucket",
                s3.getBucket(),
                errors));

        s3.setAccessKeyId(
            checkProperty(context,
                Env.toVar("S3_" + s3.getName()) + "_ACCESS_KEY_ID",
                "s3." + s3.getName() + ".accessKeyId",
                s3.getAccessKeyId(),
                s3.getResolvedAccessKeyId()));

        s3.setSecretKey(
            checkProperty(context,
                Env.toVar("S3_" + s3.getName()) + "_SECRET_KEY",
                "s3." + s3.getName() + ".secretKey",
                s3.getSecretKey(),
                s3.getResolvedSecretKey()));

        s3.setSessionToken(
            checkProperty(context,
                Env.toVar("S3_" + s3.getName()) + "_SESSION_TOKEN",
                "s3." + s3.getName() + ".sessionToken",
                s3.getSessionToken(),
                s3.getResolvedSessionToken()));

        s3.setPath(
            checkProperty(context,
                Env.toVar("S3_" + s3.getName()) + "_PATH",
                "s3." + s3.getName() + ".path",
                s3.getPath(),
                "{{projectName}}/{{tagName}}/{{artifactFileName}}"));

        s3.setDownloadUrl(
            checkProperty(context,
                Env.toVar("S3_" + s3.getName()) + "_DOWNLOAD_URL",
                "s3." + s3.getName() + ".downloadUrl",
                s3.getDownloadUrl(),
                s3.getResolvedDownloadUrl()));

        s3.setEndpoint(
            checkProperty(context,
                Env.toVar("S3_" + s3.getName()) + "_ENDPOINT",
                "s3." + s3.getName() + ".endpoint",
                s3.getEndpoint(),
                ""));

        validateTimeout(s3);
    }
}
