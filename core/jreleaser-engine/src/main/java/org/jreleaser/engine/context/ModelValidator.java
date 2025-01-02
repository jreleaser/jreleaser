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
package org.jreleaser.engine.context;

import org.jreleaser.bundle.RB;
import org.jreleaser.engine.release.Releasers;
import org.jreleaser.extensions.api.ExtensionManager;
import org.jreleaser.extensions.api.ExtensionManagerHolder;
import org.jreleaser.extensions.internal.DefaultExtensionManager;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.JReleaserModelPrinter;
import org.jreleaser.model.internal.extensions.Extension;
import org.jreleaser.util.Errors;
import org.jreleaser.util.PlatformUtils;

import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public final class ModelValidator {
    private ModelValidator() {
        // noop
    }

    public static void validate(JReleaserContext context) {
        try {
            Errors errors = context.validateModel();

            new JReleaserModelPrinter.Plain(context.getLogger().getTracer())
                .print(context.getModel().asMap(true));

            if (context.isStrict() && errors.hasWarnings()) {
                throw new JReleaserException(RB.$("ERROR_context_configurer_jreleaser_misconfigured") +
                    System.lineSeparator() + errors.warningsAsString());
            }

            switch (context.getMode()) {
                case ANNOUNCE:
                case CHANGELOG:
                case DOWNLOAD:
                case ASSEMBLE:
                case DEPLOY:
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

        report(context);
        loadExtensions(context);
    }

    private static void report(JReleaserContext context) {
        String version = context.getModel().getProject().getVersion();
        context.getModel().getProject().parseVersion();

        context.getLogger().info(RB.$("context.creator.report.project-version"), version);
        context.getLogger().info(RB.$("context.creator.report.release"), context.getModel().getProject().isSnapshot() ? " " : " " + RB.$("not") + " ");
        context.getLogger().info(RB.$("context.creator.report.timestamp"), context.getModel().getTimestamp());
        if (null != context.getModel().getCommit()) {
            context.getLogger().info(RB.$("context.creator.report.head"), context.getModel().getCommit().getShortHash());
        }
        context.getLogger().info(RB.$("context.creator.report.platform"), PlatformUtils.getCurrentFull());
    }

    private static void loadExtensions(JReleaserContext context) {
        ExtensionManager em = ExtensionManagerHolder.get();

        if (!(em instanceof DefaultExtensionManager)) {
            context.getLogger().warn(RB.$("context.creator.extension.manager.error"));
            return;
        }

        DefaultExtensionManager extensionManager = (DefaultExtensionManager) em;
        for (Map.Entry<String, Extension> e : context.getModel().getExtensions().entrySet()) {
            Extension extension = e.getValue();
            DefaultExtensionManager.ExtensionBuilder builder = extensionManager.configureExtension(e.getKey())
                .withEnabled(extension.isEnabled());
            if (isNotBlank(extension.getGav())) {
                builder = builder.withGav(extension.getGav());
            }
            if (isNotBlank(extension.getDirectory())) {
                builder = builder.withDirectory(extension.getDirectory());
            }

            for (Extension.Provider provider : extension.getProviders()) {
                builder = builder.withExtensionPoint(provider.getType(), provider.getProperties());
            }

            builder.build();
        }

        extensionManager.load(context);
    }
}
