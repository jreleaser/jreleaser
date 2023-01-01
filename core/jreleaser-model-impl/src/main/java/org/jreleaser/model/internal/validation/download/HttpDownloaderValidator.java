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
package org.jreleaser.model.internal.validation.download;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.download.Downloader;
import org.jreleaser.model.internal.download.HttpDownloader;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class HttpDownloaderValidator {
    private HttpDownloaderValidator() {
        // noop
    }

    public static void validateHttpDownloader(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, HttpDownloader> http = context.getModel().getDownload().getHttp();
        if (!http.isEmpty()) context.getLogger().debug("download.http");

        for (Map.Entry<String, HttpDownloader> e : http.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig() || mode.validateDownload()) {
                validateHttp(context, e.getValue(), errors);
            }
        }
    }

    private static void validateHttp(JReleaserContext context, HttpDownloader http, Errors errors) {
        context.getLogger().debug("download.http.{}", http.getName());

        if (!http.isActiveSet()) {
            http.setActive(Active.ALWAYS);
        }
        if (!http.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        String baseKey = "download.http." + http.getName() + ".";
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
                        baseKey + ".password",
                        http.getPassword(),
                        errors,
                        context.isDryrun()));
                break;
            case NONE:
                break;
        }

        validateTimeout(http);

        if (http.getAssets().isEmpty()) {
            errors.configuration(RB.$("validation_must_not_be_empty", "http." + http.getName() + ".assets"));
        } else {
            int index = 0;
            for (Downloader.Asset asset : http.getAssets()) {
                if (isBlank(asset.getInput())) {
                    errors.configuration(RB.$("validation_must_not_be_null", "http." + http.getName() + ".asset[" + (index++) + "].input"));
                }
            }
        }
    }
}
