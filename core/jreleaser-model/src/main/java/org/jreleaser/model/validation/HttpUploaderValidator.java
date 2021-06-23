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
import org.jreleaser.model.HttpUploader;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Uploader;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public abstract class HttpUploaderValidator extends Validator {
    public static void validateHttp(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        context.getLogger().debug("http");
        Map<String, HttpUploader> http = context.getModel().getUpload().getHttp();

        for (Map.Entry<String, HttpUploader> e : http.entrySet()) {
            HttpUploader h = e.getValue();
            if (isBlank(h.getName())) {
                h.setName(e.getKey());
            }
            validateHttpUploader(context, mode, h, errors);
        }
    }

    private static void validateHttpUploader(JReleaserContext context, JReleaserContext.Mode mode, HttpUploader http, Errors errors) {
        context.getLogger().debug("http.{}", http.getName());

        if (!http.isActiveSet()) {
            http.setActive(Active.NEVER);
        }
        if (!http.resolveEnabled(context.getModel().getProject())) return;

        if (!http.isArtifacts() && !http.isFiles() && !http.isSignatures()) {
            http.disable();
            return;
        }

        if (isBlank(http.getTarget())) {
            errors.configuration("http." + http.getName() + ".target must not be blank.");
        }

        if (null == http.getMethod()) {
            http.setMethod(Uploader.Method.PUT);
        }

        switch (http.resolveAuthorization()) {
            case BEARER:
                http.setPassword(
                    checkProperty(context.getModel().getEnvironment(),
                        "HTTP_" + Env.toVar(http.getName()) + "_PASSWORD",
                        "http.password",
                        http.getPassword(),
                        errors,
                        context.isDryrun()));
                break;
            case BASIC:
                http.setUsername(
                    checkProperty(context.getModel().getEnvironment(),
                        "HTTP_" + Env.toVar(http.getName()) + "_USERNAME",
                        "http.username",
                        http.getUsername(),
                        errors,
                        context.isDryrun()));

                http.setPassword(
                    checkProperty(context.getModel().getEnvironment(),
                        "HTTP_" + Env.toVar(http.getName()) + "_PASSWORD",
                        "http.password",
                        http.getPassword(),
                        errors,
                        context.isDryrun()));
                break;
            case NONE:
                break;
        }

        if (http.getConnectTimeout() <= 0 || http.getConnectTimeout() > 300) {
            http.setConnectTimeout(20);
        }
        if (http.getReadTimeout() <= 0 || http.getReadTimeout() > 300) {
            http.setReadTimeout(60);
        }
    }
}
