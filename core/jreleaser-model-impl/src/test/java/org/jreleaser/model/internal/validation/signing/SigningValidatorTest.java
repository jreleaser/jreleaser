/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.model.internal.validation.signing;

import org.jreleaser.logging.SimpleJReleaserLoggerAdapter;
import org.jreleaser.model.Active;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.util.Errors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @since 1.25.0
 */
class SigningValidatorTest {

    @Test
    void signingPgpValidationFailsWithoutYolo(@TempDir Path tempDir) {
        // Given: PGP signing active but no secret key configured
        JReleaserContext context = createContext(tempDir, false);
        context.getModel().getSigning().setActive(Active.ALWAYS);
        context.getModel().getSigning().getPgp().setActive(Active.ALWAYS);

        Errors errors = new Errors();

        // When: validate signing
        SigningValidator.validateSigning(context, Mode.FULL, errors);

        // Then: errors should be reported
        assertTrue(errors.hasConfigurationErrors(),
            "Expected configuration errors when PGP secret key is missing without yolo");
    }

    @Test
    void signingPgpValidationSkippedWithYolo(@TempDir Path tempDir) {
        // Given: PGP signing active but no secret key configured, yolo=true
        JReleaserContext context = createContext(tempDir, true);
        context.getModel().getSigning().setActive(Active.ALWAYS);
        context.getModel().getSigning().getPgp().setActive(Active.ALWAYS);

        Errors errors = new Errors();

        // When: validate signing with yolo
        SigningValidator.validateSigning(context, Mode.FULL, errors);

        // Then: no errors (converted to warnings), and signing should be disabled
        assertFalse(errors.hasConfigurationErrors(),
            "Expected no configuration errors when yolo is set");
        assertTrue(errors.hasWarnings(),
            "Expected warnings when yolo is set and PGP secret key is missing");
        assertFalse(context.getModel().getSigning().getPgp().isEnabled(),
            "Expected PGP signing to be disabled when yolo is set and secret key is missing");
    }

    private static JReleaserContext createContext(Path basedir, boolean yolo) {
        // Use a temp dir as settings path to avoid loading ~/.jreleaser/config.properties
        // which may contain actual credentials
        JReleaserContext context = new JReleaserContext(
            new SimpleJReleaserLoggerAdapter(SimpleJReleaserLoggerAdapter.Level.WARN),
            JReleaserContext.Configurer.CLI_YAML,
            Mode.FULL,
            JReleaserCommand.FULL_RELEASE,
            new JReleaserModel(),
            basedir,
            basedir.resolve("settings.properties"),
            basedir.resolve("out/jreleaser"),
            yolo,
            true,
            true,
            false,
            false,
            Collections.emptyList(),
            Collections.emptyList());
        // Initialize environment properties using the temp settings path
        // (no config.properties exists there, so no external credentials leak in)
        context.getModel().getEnvironment().initProps(context);
        return context;
    }
}
