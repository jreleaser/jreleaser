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
package org.jreleaser.sdk.tool;

import org.jreleaser.model.api.JReleaserContext;
import org.jreleaser.sdk.command.Command;
import org.jreleaser.sdk.command.CommandException;
import org.jreleaser.sdk.command.CommandExecutor;
import org.jreleaser.util.ComparatorUtils;
import org.jreleaser.version.SemanticVersion;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public class PomChecker extends AbstractTool {
    public PomChecker(JReleaserContext context, String version) {
        super(context, "pomchecker", version);
    }

    public Command.Result check(Path parent, List<String> args) throws CommandException {
        SemanticVersion semver = SemanticVersion.of(version);
        SemanticVersion ofz = SemanticVersion.of("1.5.0");
        if (ComparatorUtils.greaterThanOrEqualTo(semver, ofz)) {
            args.add("-Dorg.kordamp.banner=false");
        }

        Command command = tool.asCommand().args(args);
        return executeCommand(() -> new CommandExecutor(context.getLogger())
            .executeCommand(parent, command));
    }

    public boolean isVersionCompatibleWith(String otherVersion) {
        return SemanticVersion.of(version).compareTo(SemanticVersion.of(otherVersion)) >= 0;
    }
}
