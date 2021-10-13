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

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.Http;
import org.jreleaser.model.HttpUploader;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public abstract class HttpValidator extends Validator {
    public static void validateHttp(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        context.getLogger().debug("http");
        Map<String, Http> http = context.getModel().getUpload().getHttp();

        for (Map.Entry<String, Http> e : http.entrySet()) {
            Http h = e.getValue();
            if (isBlank(h.getName())) {
                h.setName(e.getKey());
            }
            validateHttp(context, mode, h, errors);
        }
    }

    private static void validateHttp(JReleaserContext context, JReleaserContext.Mode mode, Http http, Errors errors) {
        context.getLogger().debug("http.{}", http.getName());

        if (!http.isActiveSet()) {
            http.setActive(Active.NEVER);
        }
        if (!http.resolveEnabled(context.getModel().getProject())) return;

        if (!http.isArtifacts() && !http.isFiles() && !http.isSignatures()) {
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
            http.setMethod(HttpUploader.Method.PUT);
        }

        switch (http.resolveAuthorization()) {
            case BEARER:
                http.setPassword(
                    checkProperty(context,
                        "HTTP_" + Env.toVar(http.getName()) + "_PASSWORD",
                        "http.password",
                        http.getPassword(),
                        errors,
                        context.isDryrun()));
                break;
            case BASIC:
                http.setUsername(
                    checkProperty(context,
                        "HTTP_" + Env.toVar(http.getName()) + "_USERNAME",
                        "http.username",
                        http.getUsername(),
                        errors,
                        context.isDryrun()));

                http.setPassword(
                    checkProperty(context,
                        "HTTP_" + Env.toVar(http.getName()) + "_PASSWORD",
                        "http.password",
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
