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
import org.jreleaser.config.JReleaserConfigLoader;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.util.PlatformUtils;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public final class ContextCreator {
    private ContextCreator() {
        // noop
    }

    public static JReleaserContext create(JReleaserLogger logger,
                                          JReleaserContext.Configurer configurer,
                                          Mode mode,
                                          JReleaserCommand command,
                                          Path configFile,
                                          Path basedir,
                                          Path outputDirectory,
                                          boolean dryrun,
                                          boolean gitRootSearch,
                                          boolean strict,
                                          List<String> selectedPlatforms,
                                          List<String> rejectedPlatforms) {
        return create(logger,
            configurer,
            mode,
            command,
            resolveModel(logger, configFile),
            basedir,
            outputDirectory,
            dryrun,
            gitRootSearch,
            strict,
            selectedPlatforms,
            rejectedPlatforms);
    }

    public static JReleaserContext create(JReleaserLogger logger,
                                          JReleaserContext.Configurer configurer,
                                          Mode mode,
                                          JReleaserCommand command,
                                          JReleaserModel model,
                                          Path basedir,
                                          Path outputDirectory,
                                          boolean dryrun,
                                          boolean gitRootSearch,
                                          boolean strict,
                                          List<String> selectedPlatforms,
                                          List<String> rejectedPlatforms) {
        JReleaserContext context = new JReleaserContext(
            logger,
            configurer,
            mode,
            command,
            model,
            basedir,
            outputDirectory,
            dryrun,
            gitRootSearch,
            strict,
            selectedPlatforms,
            rejectedPlatforms);

        PlatformUtils.resolveCurrentPlatform(logger);
        logger.info(RB.$("context.creator.git_root_search", context.isGitRootSearch()));
        ModelConfigurer.configure(context);

        return context;
    }

    public static JReleaserModel resolveModel(JReleaserLogger logger, Path configFile) {
        try {
            logger.info(RB.$("context.creator.reading_configuration"));
            return JReleaserConfigLoader.loadConfig(configFile);
        } catch (JReleaserException e) {
            logger.trace(e);
            throw e;
        } catch (Exception e) {
            logger.trace(e);
            throw new JReleaserException(RB.$("ERROR_context_creator_parse_configuration", configFile.toAbsolutePath()), e);
        }
    }
}
