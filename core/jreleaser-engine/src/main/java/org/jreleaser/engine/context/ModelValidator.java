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
package org.jreleaser.engine.context;

import org.jreleaser.bundle.RB;
import org.jreleaser.engine.release.Releasers;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModelPrinter;
import org.jreleaser.util.Errors;
import org.jreleaser.util.JReleaserException;
import org.jreleaser.util.PlatformUtils;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public class ModelValidator {
    public static void validate(JReleaserContext context) {
        try {
            Errors errors = context.validateModel();

            if (context.getMode() != JReleaserContext.Mode.CHANGELOG) {
                new JReleaserModelPrinter.Plain(context.getLogger().getTracer())
                    .print(context.getModel().asMap(true));
            }

            switch (context.getMode()) {
                case DOWNLOAD:
                case ASSEMBLE:
                    if (errors.hasConfigurationErrors()) {
                        throw new JReleaserException(RB.$("ERROR_context_configurer_jreleaser_misconfigured") +
                            System.lineSeparator() + errors.asString());
                    }
                    break;
                case FULL:
                default:
                    if (errors.hasErrors()) {
                        throw new JReleaserException(RB.$("ERROR_context_configurer_jreleaser_misconfigured") +
                            System.lineSeparator() + errors.asString());
                    }
                    break;
            }
        } catch (JReleaserException e) {
            context.getLogger().trace(e);
            throw e;
        } catch (Exception e) {
            context.getLogger().trace(e);
            throw new JReleaserException(RB.$("ERROR_context_configurer_jreleaser_misconfigured"), e);
        }

        if (!context.getMode().validateStandalone()) {
            context.setReleaser(Releasers.releaserFor(context));
        }

        // context.freeze();

        report(context);
    }

    private static void report(JReleaserContext context) {
        String version = context.getModel().getProject().getVersion();
        context.getModel().getProject().parseVersion();

        context.getLogger().info(RB.$("context.creator.report.project-version"), version);
        context.getLogger().info(RB.$("context.creator.report.release"), context.getModel().getProject().isSnapshot() ? " " : " " + RB.$("not") + " ");
        context.getLogger().info(RB.$("context.creator.report.timestamp"), context.getModel().getTimestamp());
        if (context.getModel().getCommit() != null) {
            context.getLogger().info(RB.$("context.creator.report.head"), context.getModel().getCommit().getShortHash());
        }
        context.getLogger().info(RB.$("context.creator.report.platform"), PlatformUtils.getCurrentFull());
    }
}
