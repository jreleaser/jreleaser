/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 The JReleaser authors.
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
package org.jreleaser.model.internal.validation.common;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Gitlab;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.util.CollectionUtils.setOf;

/**
 * @author Andres Almiray
 * @author Jan Wloka
 * @since 1.11.0
 */
public final class GitlabValidator {
    public static final String DOT = ".";
    public static final String GITLAB = "gitlab";

    private static final String PROJECT_IDENTIFIER = "project.identifier";

    private GitlabValidator() {
        // noop
    }

    public static void validateProjectIdentifier(JReleaserContext context, Gitlab subject, Gitlab other, String prefix, String name, Errors errors, boolean continueOnError) {
        subject.setProjectIdentifier(
            checkProperty(context,
                setOf(
                    prefix + DOT + GITLAB + DOT + name + DOT + PROJECT_IDENTIFIER,
                    prefix + DOT + GITLAB + DOT + PROJECT_IDENTIFIER,
                    GITLAB + DOT + name + DOT + PROJECT_IDENTIFIER,
                    GITLAB + DOT + PROJECT_IDENTIFIER),
                prefix + DOT + GITLAB + DOT + name + DOT + ".projectIdentifier",
                subject.getProjectIdentifier(),
                null != other ? other.getProjectIdentifier() : null,
                errors,
                continueOnError));
    }
}
