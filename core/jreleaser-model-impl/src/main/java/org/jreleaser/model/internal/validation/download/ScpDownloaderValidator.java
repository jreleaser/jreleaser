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
import org.jreleaser.model.internal.download.ScpDownloader;
import org.jreleaser.model.internal.validation.common.Validator;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.SshValidator.validateSsh;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public abstract class ScpDownloaderValidator extends Validator {
    public static void validateScpDownloader(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, ScpDownloader> scp = context.getModel().getDownload().getScp();
        if (!scp.isEmpty()) context.getLogger().debug("download.scp");

        for (Map.Entry<String, ScpDownloader> e : scp.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig() || mode.validateDownload()) {
                validateScpDownloader(context, mode, e.getValue(), errors);
            }
        }
    }

    private static void validateScpDownloader(JReleaserContext context, Mode mode, ScpDownloader scp, Errors errors) {
        context.getLogger().debug("download.scp.{}", scp.getName());

        if (!scp.isActiveSet()) {
            scp.setActive(Active.ALWAYS);
        }
        if (!scp.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        validateSsh(context, scp, scp.getName(), "SCP", scp.getType(), errors);
        validateTimeout(scp);

        if (scp.getAssets().isEmpty()) {
            errors.configuration(RB.$("validation_must_not_be_empty", "scp." + scp.getName() + ".assets"));
        } else {
            int index = 0;
            for (Downloader.Asset asset : scp.getAssets()) {
                if (isBlank(asset.getInput())) {
                    errors.configuration(RB.$("validation_must_not_be_null", "scp." + scp.getName() + ".asset[" + (index++) + "].input"));
                }
            }
        }
    }
}
