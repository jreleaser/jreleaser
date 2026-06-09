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
package org.jreleaser.model.internal.validation.release;

import org.jreleaser.logging.SimpleJReleaserLoggerAdapter;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.model.internal.release.GithubReleaser;
import org.jreleaser.util.Errors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that the release branch is resolved from the current Git context
 * instead of always assuming "main".
 *
 * @see <a href="https://github.com/jreleaser/jreleaser/issues/2118">#2118</a>
 */
class BaseReleaserValidatorTest {

    @Test
    void resolvesCurrentBranchWhenOnMaster(@TempDir Path tempDir) {
        // Given: a repository checked out on "master" with no branch configured
        JReleaserContext context = createContext(tempDir, "master");
        GithubReleaser service = createService(context);

        // When: the git service is validated
        validateBranch(context, service);

        // Then: the resolved branch is "master", not the hardcoded "main"
        assertEquals("master", service.getBranch());
    }

    @Test
    void resolvesCurrentBranchWhenOnMain(@TempDir Path tempDir) {
        // Given: a repository checked out on "main" with no branch configured
        JReleaserContext context = createContext(tempDir, "main");
        GithubReleaser service = createService(context);

        // When: the git service is validated
        validateBranch(context, service);

        // Then: the resolved branch is "main" (unchanged behavior)
        assertEquals("main", service.getBranch());
    }

    @Test
    void explicitlyConfiguredBranchWinsOverDetection(@TempDir Path tempDir) {
        // Given: a repository on "master" but the branch is explicitly configured
        JReleaserContext context = createContext(tempDir, "master");
        GithubReleaser service = createService(context);
        service.setBranch("release/1.x");

        // When: the git service is validated
        validateBranch(context, service);

        // Then: the explicit value wins over detection
        assertEquals("release/1.x", service.getBranch());
    }

    @Test
    void fallsBackToMainWhenNoGitContext(@TempDir Path tempDir) {
        // Given: no Git context (commit/refName unavailable, e.g. detached HEAD)
        JReleaserContext context = createContext(tempDir, null);
        GithubReleaser service = createService(context);

        // When: the git service is validated
        validateBranch(context, service);

        // Then: it falls back to "main"
        assertEquals("main", service.getBranch());
    }

    @Test
    void fallsBackToMainWhenRefNameBlank(@TempDir Path tempDir) {
        // Given: a Git context whose refName is blank (e.g. detached HEAD)
        JReleaserContext context = createContext(tempDir, "");
        GithubReleaser service = createService(context);

        // When: the git service is validated
        validateBranch(context, service);

        // Then: it falls back to "main"
        assertEquals("main", service.getBranch());
    }

    // Drives the branch-resolution portion of validateGitService. Later validation
    // steps (e.g. tag-name template resolution) require the ExtensionManager service
    // which is provided by jreleaser-engine and is not on this module's unit-test
    // classpath; the branch is resolved before that point, so we tolerate a downstream
    // failure and assert only on the resolved branch.
    private static void validateBranch(JReleaserContext context, GithubReleaser service) {
        try {
            BaseReleaserValidator.validateGitService(context, Mode.FULL, service, new Errors());
        } catch (RuntimeException ignored) {
            // branch is already resolved before any downstream template resolution
        }
    }

    private static GithubReleaser createService(JReleaserContext context) {
        GithubReleaser service = new GithubReleaser();
        service.setOwner("acme");
        service.setName("widget");
        service.setToken("token");
        context.getModel().getRelease().setGithub(service);
        return service;
    }

    private static JReleaserContext createContext(Path basedir, String refName) {
        JReleaserContext context = new JReleaserContext(
            new SimpleJReleaserLoggerAdapter(SimpleJReleaserLoggerAdapter.Level.WARN),
            JReleaserContext.Configurer.CLI_YAML,
            Mode.FULL,
            JReleaserCommand.FULL_RELEASE,
            new JReleaserModel(),
            basedir,
            basedir.resolve("settings.properties"),
            basedir.resolve("out/jreleaser"),
            true,
            true,
            true,
            false,
            false,
            Collections.emptyList(),
            Collections.emptyList());
        context.getModel().getEnvironment().initProps(context);
        context.getModel().getProject().setName("widget");
        if (null != refName) {
            context.getModel().setCommit(new org.jreleaser.model.api.JReleaserModel.Commit(
                "abc1234", "abc1234567890", refName, 0, ZonedDateTime.now()));
        }
        return context;
    }
}
