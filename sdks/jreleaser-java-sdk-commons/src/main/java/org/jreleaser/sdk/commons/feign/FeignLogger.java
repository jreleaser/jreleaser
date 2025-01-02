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
package org.jreleaser.sdk.commons.feign;

import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.api.JReleaserContext;
import org.jreleaser.util.Env;

import java.util.Locale;
import java.util.Properties;

import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Brian Demers
 * @author Andres Almiray
 * @since 1.12.0
 */
public class FeignLogger extends feign.Logger {
    private final JReleaserLogger logger;

    public FeignLogger(JReleaserLogger logger) {
        this.logger = logger;
    }

    @Override
    protected void log(String configKey, String format, Object... args) {
        logger.trace(String.format(methodTag(configKey) + format, args));
    }

    public static Level resolveLevel(JReleaserContext context) {
        Properties vars = context.getModel().getEnvironment().getVars();
        if (null == vars) {
            vars = new Properties();
        }
        String value = Env.resolve(listOf("feign.logger.level"), vars);
        if (isBlank(value)) return Level.NONE;
        return Level.valueOf(value.trim().toUpperCase(Locale.ENGLISH));
    }
}