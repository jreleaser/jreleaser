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
package org.jreleaser.model.validation;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.Downloader;
import org.jreleaser.model.HttpDownloader;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public abstract class HttpDownloaderValidator extends Validator {
    public static void validateHttpDownloader(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        context.getLogger().debug("http");
        Map<String, HttpDownloader> http = context.getModel().getDownload().getHttp();

        for (Map.Entry<String, HttpDownloader> e : http.entrySet()) {
            e.getValue().setName(e.getKey());
            validateHttp(context, mode, e.getValue(), errors);
        }
    }

    private static void validateHttp(JReleaserContext context, JReleaserContext.Mode mode, HttpDownloader http, Errors errors) {
        context.getLogger().debug("http.{}", http.getName());

        if (!http.isActiveSet()) {
            http.setActive(Active.ALWAYS);
        }
        if (!http.resolveEnabled(context.getModel().getProject())) {
            return;
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
