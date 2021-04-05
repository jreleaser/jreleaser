/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
package org.jreleaser.model;

import java.util.Collections;
import java.util.List;

import static org.jreleaser.model.validation.AnnouncersValidator.validateAnnouncers;
import static org.jreleaser.model.validation.DistributionsValidator.validateDistributions;
import static org.jreleaser.model.validation.FilesValidator.validateFiles;
import static org.jreleaser.model.validation.PackagersValidator.validatePackagers;
import static org.jreleaser.model.validation.ProjectValidator.validateProject;
import static org.jreleaser.model.validation.ReleaseValidator.validateRelease;
import static org.jreleaser.model.validation.SignValidator.validateSign;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class JReleaserModelValidator {
    private JReleaserModelValidator() {
        // noop
    }

    public static List<String> validate(JReleaserContext context, List<String> errors) {
        context.getLogger().increaseIndent();
        context.getLogger().setPrefix("validation");
        try {
            validateModel(context, errors);
            return Collections.unmodifiableList(errors);
        } finally {
            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
        }
    }

    private static void validateModel(JReleaserContext context, List<String> errors) {
        validateProject(context, errors);
        validateSign(context, errors);
        validateRelease(context, errors);
        validatePackagers(context, errors);
        validateAnnouncers(context, errors);
        validateFiles(context, errors);
        validateDistributions(context, errors);
    }
}
