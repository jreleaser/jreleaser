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
package org.jreleaser.model.internal.validation.download;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.download.Downloader;
import org.jreleaser.model.internal.download.HttpDownloader;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
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

        resolveActivatable(context, http,
            listOf("download.http." + http.getName(), "download.http"),
            "ALWAYS");
        if (!http.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        String baseKey1 = "download.http." + http.getName();
        String baseKey2 = "download.http";
        String baseKey3 = "http." + http.getName();
        String baseKey4 = "http";
        switch (http.resolveAuthorization()) {
            case BEARER:
                http.setPassword(
                    checkProperty(context,
                        listOf(
                            baseKey1 + ".password",
                            baseKey2 + ".password",
                            baseKey3 + ".password",
                            baseKey4 + ".password"),
                        baseKey1 + ".password",
                        http.getPassword(),
                        errors,
                        context.isDryrun()));
                break;
            case BASIC:
                http.setUsername(
                    checkProperty(context,
                        listOf(
                            baseKey1 + ".username",
                            baseKey2 + ".username",
                            baseKey3 + ".username",
                            baseKey4 + ".username"),
                        baseKey1 + ".username",
                        http.getUsername(),
                        errors,
                        context.isDryrun()));

                http.setPassword(
                    checkProperty(context,
                        listOf(
                            baseKey1 + ".password",
                            baseKey2 + ".password",
                            baseKey3 + ".password",
                            baseKey4 + ".password"),
                        baseKey1 + ".password",
                        http.getPassword(),
                        errors,
                        context.isDryrun()));
                break;
            case NONE:
                break;
        }

        validateTimeout(http);

        if (http.getAssets().isEmpty()) {
            errors.configuration(RB.$("validation_must_not_be_empty", baseKey1 + ".assets"));
        } else {
            int index = 0;
            for (Downloader.Asset asset : http.getAssets()) {
                if (isBlank(asset.getInput())) {
                    errors.configuration(RB.$("validation_must_not_be_null", baseKey1 + ".asset[" + (index++) + "].input"));
                }
            }
        }
    }
}
