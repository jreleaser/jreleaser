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
package org.jreleaser.sdk.signing;

import org.jreleaser.bundle.RB;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.sdk.command.Command;
import org.jreleaser.sdk.command.CommandException;
import org.jreleaser.sdk.command.CommandExecutor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public final class GpgCommandSigner {
    private final JReleaserLogger logger;
    private final List<String> args = new ArrayList<>();

    private String executable;
    private String passphrase;
    private String keyName;
    private String homeDir;
    private String publicKeyring;
    private boolean defaultKeyring;

    public GpgCommandSigner(JReleaserLogger logger) {
        this.logger = logger;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public void setHomeDir(String homeDir) {
        this.homeDir = homeDir;
    }

    public void setPublicKeyring(String publicKeyring) {
        this.publicKeyring = publicKeyring;
    }

    public void setDefaultKeyring(boolean defaultKeyring) {
        this.defaultKeyring = defaultKeyring;
    }

    public void setArgs(List<String> args) {
        this.args.clear();
        this.args.addAll(args);
    }

    public void sign(Path input, Path output) throws CommandException {
        Command cmd = createSignCommand();

        cmd.arg("--output")
            .arg(output.toAbsolutePath().toString())
            .arg(input.toAbsolutePath().toString());

        Command.Result result = new CommandExecutor(logger)
            .executeCommand(cmd,
                new ByteArrayInputStream(passphrase.getBytes(UTF_8)));
        if (result.getExitValue() != 0) {
            throw new CommandException(RB.$("ERROR_command_execution_exit_value", result.getExitValue()));
        }
    }

    public boolean verify(Path signature, Path target) throws CommandException {
        Command cmd = createVerifyCommand()
            .arg(signature.toAbsolutePath().toString())
            .arg(target.toAbsolutePath().toString());
        return new CommandExecutor(logger, CommandExecutor.Output.QUIET)
            .executeCommand(cmd).getExitValue() == 0;
    }

    public byte[] sign(byte[] in) throws CommandException {
        try {
            Path input = Files.createTempFile("jreleaser", "sign-input");
            Files.write(input, in, WRITE);
            Path output = Files.createTempDirectory("jreleaser-" + UUID.randomUUID())
                .resolve(UUID.randomUUID().toString());

            sign(input, output);

            return Files.readAllBytes(output);
        } catch (IOException e) {
            throw new CommandException(RB.$("ERROR_unexpected_error"), e);
        }
    }

    private Command createSignCommand() {
        Command cmd = new Command(executable)
            .args(args);

        if (isNotBlank(homeDir)) {
            cmd.arg("--homedir")
                .arg(homeDir);
        }
        if (isNotBlank(keyName)) {
            cmd.arg("--local-user")
                .arg(keyName);
        }

        cmd.arg("--armor")
            .arg("--detach-sign")
            .arg("--batch")
            .arg("--no-tty")
            .arg("--pinentry-mode")
            .arg("loopback")
            .arg("--passphrase-fd")
            .arg("0");

        if (!defaultKeyring) {
            cmd.arg("--no-default-keyring");
        }

        if (isNotBlank(publicKeyring)) {
            cmd.arg("--keyring")
                .arg(publicKeyring);
        }

        return cmd;
    }

    private Command createVerifyCommand() {
        Command cmd = new Command(executable)
            .args(args);

        if (isNotBlank(homeDir)) {
            cmd.arg("--homedir")
                .arg(homeDir);
        }
        if (isNotBlank(keyName)) {
            cmd.arg("--local-user")
                .arg(keyName);
        }

        if (!defaultKeyring) {
            cmd.arg("--no-default-keyring");
        }

        if (isNotBlank(publicKeyring)) {
            cmd.arg("--keyring")
                .arg(publicKeyring);
        }

        cmd.arg("--verify");

        return cmd;
    }
}
