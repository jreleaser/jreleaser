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
package org.jreleaser.model.internal.environment;

import org.jreleaser.logging.SimpleJReleaserLoggerAdapter;
import org.jreleaser.model.Active;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.model.internal.signing.Signing;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Stream;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @since 1.26.0
 */
class EnvironmentOverrideTest {
    private static final String KEY = "ENVIRONMENT_OVERRIDE_TEST";
    private static final String ALT_KEY = "ENVIRONMENT_OVERRIDE_TEST_ALT";
    private static final String SYS_KEY = Env.sysKey(KEY);
    private static final String ALT_SYS_KEY = Env.sysKey(ALT_KEY);
    private static final String SIGNING_SYS_KEY = Env.sysKey("signing.active");
    private static final String OVERRIDE_SYS_KEY = Env.sysKey(org.jreleaser.model.api.environment.Environment.ENVIRONMENT_OVERRIDE);

    @BeforeEach
    @AfterEach
    void reset() {
        System.clearProperty(SYS_KEY);
        System.clearProperty(ALT_SYS_KEY);
        System.clearProperty(SIGNING_SYS_KEY);
        System.clearProperty(OVERRIDE_SYS_KEY);
        Env.setOverride(false);
    }

    @ParameterizedTest
    @MethodSource("property_factory")
    void testPropertyResolution(String expected, Boolean override, String overrideSysProp, String sysProp, @TempDir Path tempDir) {
        // given:
        if (isNotBlank(overrideSysProp)) System.setProperty(OVERRIDE_SYS_KEY, overrideSysProp);
        if (isNotBlank(sysProp)) System.setProperty(SYS_KEY, sysProp);
        JReleaserContext context = createContext(tempDir, override);
        Errors errors = new Errors();

        // expect:
        assertEquals(null != override ? override : "true".equals(overrideSysProp),
            context.getModel().getEnvironment().isOverride());
        assertEquals(expected, checkProperty(context, KEY, "test", "from-dsl", errors));
    }

    private static Stream<Arguments> property_factory() {
        return Stream.of(
            Arguments.of("from-dsl", false, null, "from-sysprop"),
            Arguments.of("from-sysprop", true, null, "from-sysprop"),
            Arguments.of("from-dsl", true, null, null),
            Arguments.of("from-sysprop", null, "true", "from-sysprop"),
            Arguments.of("from-dsl", false, "true", "from-sysprop")
        );
    }

    @Test
    void sysPropWinsWhenOverrideIsEnabledForMultipleKeys(@TempDir Path tempDir) {
        // given:
        System.setProperty(ALT_SYS_KEY, "from-sysprop");
        JReleaserContext context = createContext(tempDir, true);
        Errors errors = new Errors();

        // expect:
        assertEquals("from-sysprop", checkProperty(context, listOf(KEY, ALT_KEY), "test", "from-dsl", errors));
        assertTrue(Env.isOverride());
    }

    @Test
    void explicitValueSurvivesWhenNothingIsSetExternallyForMultipleKeys(@TempDir Path tempDir) {
        // given:
        JReleaserContext context = createContext(tempDir, true);
        Errors errors = new Errors();

        // expect:
        assertEquals("from-dsl", checkProperty(context, listOf(KEY, ALT_KEY), "test", "from-dsl", errors));
    }

    @ParameterizedTest
    @MethodSource("active_factory")
    void testActiveResolution(Active expected, Boolean override, @TempDir Path tempDir) {
        // given:
        System.setProperty(SIGNING_SYS_KEY, "NEVER");
        JReleaserContext context = createContext(tempDir, override);
        Signing signing = context.getModel().getSigning();
        signing.setActive(Active.ALWAYS);

        // when:
        resolveActivatable(context, signing, "signing", "ALWAYS");

        // then:
        assertEquals(expected, signing.getActive());
    }

    private static Stream<Arguments> active_factory() {
        return Stream.of(
            Arguments.of(Active.NEVER, true),
            Arguments.of(Active.ALWAYS, false)
        );
    }

    private static JReleaserContext createContext(Path basedir, Boolean override) {
        JReleaserModel model = new JReleaserModel();
        model.getEnvironment().setOverride(override);

        JReleaserContext context = new JReleaserContext(
            new SimpleJReleaserLoggerAdapter(SimpleJReleaserLoggerAdapter.Level.WARN),
            JReleaserContext.Configurer.CLI_YAML,
            Mode.FULL,
            JReleaserCommand.FULL_RELEASE,
            model,
            basedir,
            basedir.resolve("settings.properties"),
            basedir.resolve("out/jreleaser"),
            false,
            true,
            true,
            false,
            false,
            Collections.emptyList(),
            Collections.emptyList());
        context.getModel().getEnvironment().initProps(context);
        return context;
    }
}
