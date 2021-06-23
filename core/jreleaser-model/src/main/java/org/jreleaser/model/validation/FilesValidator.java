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

import org.jreleaser.model.Glob;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Errors;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class FilesValidator extends Validator {
    public static void validateFiles(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        if (mode != JReleaserContext.Mode.FULL) {
            return;
        }

        context.getLogger().debug("files");

        int i = 0;
        for (Glob glob : context.getModel().getFiles().getGlobs()) {
            boolean isBaseDir = false;

            if (isBlank(glob.getDirectory())) {
                glob.setDirectory(".");
                isBaseDir = true;
            }

            boolean includeAll = false;
            if (isBlank(glob.getInclude())) {
                glob.setInclude("*");
                includeAll = true;
            }

            if (isBlank(glob.getExclude()) &&
                includeAll && isBaseDir) {
                // too broad!
                errors.configuration("files.glob[" + i + "] must define either a directory or an include/exclude pattern");
            }
        }
    }
}