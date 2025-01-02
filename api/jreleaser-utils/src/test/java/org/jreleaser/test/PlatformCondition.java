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
package org.jreleaser.test;

import org.jreleaser.logging.SimpleJReleaserLoggerAdapter;
import org.jreleaser.util.PlatformUtils;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Optional;

import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public class PlatformCondition implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Optional<Platform> optional = findAnnotation(context.getElement(), Platform.class);
        if (optional.isPresent()) {
            PlatformUtils.resolveCurrentPlatform(new SimpleJReleaserLoggerAdapter());
            Platform annotation = optional.get();
            boolean match = annotation.match();
            String platform = annotation.platform();
            boolean compatible = isNotBlank(platform) && PlatformUtils.isCompatible(platform, PlatformUtils.getCurrentFull());
            boolean result = match == compatible;
            if (result) {
                return ConditionEvaluationResult.enabled("Platform " + platform + " matches");
            } else {
                return ConditionEvaluationResult.disabled("Platform " + platform + " did not match");
            }
        }
        return ConditionEvaluationResult.enabled("Nothing to match");
    }
}