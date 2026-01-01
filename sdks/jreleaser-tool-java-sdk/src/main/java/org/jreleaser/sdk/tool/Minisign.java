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
package org.jreleaser.sdk.tool;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext;
import org.jreleaser.model.api.signing.SigningException;
import org.jreleaser.sdk.command.Command;
import org.jreleaser.sdk.command.CommandException;
import org.jreleaser.sdk.command.CommandExecutor;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Andres Almiray
 * @since 1.22.0
 */
public class Minisign extends AbstractTool {
    private static final String MINISIGN_PASSWORD = "MINISIGN_PASSWORD";

    public Minisign(JReleaserContext context, String version) {
        super(context, "minisign", version, true);
    }

    public void sign(Path keyFile, String password, Path input, Path destinationDir) throws SigningException {
        context.getLogger().info("{}", context.relativizeToBasedir(input));

        try {
            Path signature = destinationDir.resolve(input.getFileName() + ".minisig");

            Command command = tool.asCommand()
                .arg("-S")
                .arg("-x")
                .arg(signature.toAbsolutePath().toString())
                .arg("-s")
                .arg(keyFile.toAbsolutePath().toString())
                .arg("-m")
                .arg(input.toAbsolutePath().toString());

            ByteArrayInputStream stdin = new ByteArrayInputStream(password.getBytes(UTF_8));
            Command.Result result = executeCommand(() -> new CommandExecutor(context.getLogger(), CommandExecutor.Output.QUIET)
                .executeCommand(command, stdin));
            if (result.getExitValue() != 0) {
                throw new CommandException(RB.$("ERROR_command_execution_exit_value", result.getExitValue()));
            }
        } catch (CommandException e) {
            throw new SigningException(RB.$("ERROR_unexpected_error_signing", input.toAbsolutePath()), e);
        }
    }

    public void verify(Path keyFile, Path signature, Path input) throws SigningException {
        context.getLogger().debug("{}", context.relativizeToBasedir(signature));

        try {
            Command command = tool.asCommand()
                .arg("-V")
                .arg("-x")
                .arg(signature.toAbsolutePath().toString())
                .arg("-p")
                .arg(keyFile.toAbsolutePath().toString())
                .arg("-q")
                .arg("-m")
                .arg(input.toAbsolutePath().toString());

            Command.Result result = executeCommand(() -> new CommandExecutor(context.getLogger(), CommandExecutor.Output.QUIET)
                .executeCommand(command));
            if (result.getExitValue() != 0) {
                throw new CommandException(RB.$("ERROR_command_execution_exit_value", result.getExitValue()));
            }
        } catch (CommandException e) {
            throw new SigningException(RB.$("ERROR_signing_verify_signature",
                context.relativizeToBasedir(signature)), e);
        }
    }
}
