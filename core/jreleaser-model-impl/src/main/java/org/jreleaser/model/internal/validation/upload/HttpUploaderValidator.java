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
import org.jreleaser.model.Http;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.upload.HttpUploader;
import org.jreleaser.model.internal.validation.common.Validator;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public abstract class HttpUploaderValidator extends Validator {
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

    private static void validateHttp(JReleaserContext context, HttpUploader http, Errors errors) {
        context.getLogger().debug("upload.http.{}", http.getName());

        if (!http.isActiveSet()) {
            http.setActive(Active.NEVER);
        }
        if (!http.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (!http.isArtifacts() && !http.isFiles() && !http.isSignatures()) {
            errors.warning(RB.$("WARNING.validation.uploader.no.artifacts", http.getType(), http.getName()));
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            http.disable();
            return;
        }

        if (isBlank(http.getUploadUrl())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "http." + http.getName() + ".uploadUrl"));
        }
        if (isBlank(http.getDownloadUrl())) {
            http.setDownloadUrl(http.getUploadUrl());
        }

        if (null == http.getMethod()) {
            http.setMethod(Http.Method.PUT);
        }

        String baseKey = "upload.http." + http.getName() + ".";
        switch (http.resolveAuthorization()) {
            case BEARER:
                http.setPassword(
                    checkProperty(context,
                        "HTTP_" + Env.toVar(http.getName()) + "_PASSWORD",
                        baseKey + "password",
                        http.getPassword(),
                        errors,
                        context.isDryrun()));
                break;
            case BASIC:
                http.setUsername(
                    checkProperty(context,
                        "HTTP_" + Env.toVar(http.getName()) + "_USERNAME",
                        baseKey + "username",
                        http.getUsername(),
                        errors,
                        context.isDryrun()));

                http.setPassword(
                    checkProperty(context,
                        "HTTP_" + Env.toVar(http.getName()) + "_PASSWORD",
                        baseKey + "password",
                        http.getPassword(),
                        errors,
                        context.isDryrun()));
                break;
            case NONE:
                break;
        }

        validateTimeout(http);
    }
}
