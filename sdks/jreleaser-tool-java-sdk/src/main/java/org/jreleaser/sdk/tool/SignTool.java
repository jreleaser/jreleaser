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

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext;
import org.jreleaser.model.api.signing.SigningException;
import org.jreleaser.sdk.command.Command;
import org.jreleaser.sdk.command.CommandException;
import org.jreleaser.sdk.command.CommandExecutor;
import org.jreleaser.util.PlatformUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

public class SignTool {
    private final JReleaserContext context;
    private final String executable;

    public SignTool(JReleaserContext context) {
        this(context, "signtool");
    }

    public SignTool(JReleaserContext context, String executable) {
        this.context = context;
        this.executable = executable;
    }

    public boolean isAvailable() {
        if (!PlatformUtils.isWindows()) {
            context.getLogger().debug("Windows SignTool is only available on Windows platforms");
            return false;
        }

        try {
            Path signToolPath = findSignTool();
            if (signToolPath != null && Files.exists(signToolPath)) {
                Command command = new Command(signToolPath.toString()).arg("/?");
                CommandExecutor executor = new CommandExecutor(context.getLogger(), CommandExecutor.Output.QUIET);
                Command.Result result = executor.executeCommand(command);
                return result.getExitValue() == 0;
            }
        } catch (Exception e) {
            context.getLogger().debug("Error checking SignTool availability: " + e.getMessage());
        }
        
        return false;
    }

    public void signFile(Path file, String certificateFile, String password, 
                        String timestampUrl, String algorithm, String description) throws SigningException {
        if (!Files.exists(file)) {
            throw new SigningException(RB.$("ERROR_path_does_not_exist", file.toAbsolutePath()));
        }

        Path signToolPath = findSignTool();
        if (signToolPath == null || !Files.exists(signToolPath)) {
            throw new SigningException("SignTool executable not found");
        }

        List<String> args = new ArrayList<>();
        args.add(signToolPath.toString());
        args.add("sign");

        if (isNotBlank(certificateFile)) {
            args.add("/f");
            args.add(certificateFile);
        }

        if (isNotBlank(password)) {
            args.add("/p");
            args.add(password);
        }

        if (isNotBlank(algorithm)) {
            args.add("/fd");
            args.add(algorithm);
        } else {
            args.add("/fd");
            args.add("SHA256");
        }

        if (isNotBlank(timestampUrl)) {
            args.add("/tr");
            args.add(timestampUrl);
            args.add("/td");
            args.add("SHA256");
        }

        if (isNotBlank(description)) {
            args.add("/d");
            args.add(description);
        }

        args.add("/v");
        args.add(file.toString());

        try {
            Command command = new Command(args);
            CommandExecutor executor = new CommandExecutor(context.getLogger());
            Command.Result result = executor.executeCommand(command);

            if (result.getExitValue() != 0) {
                throw new SigningException("SignTool failed with exit code: " + result.getExitValue() + 
                    ". Error: " + result.getErr());
            }

            context.getLogger().info("Successfully signed: " + context.relativizeToBasedir(file));
        } catch (CommandException e) {
            throw new SigningException("Failed to execute SignTool", e);
        }
    }

    public boolean verifyFile(Path file) throws SigningException {
        if (!Files.exists(file)) {
            throw new SigningException(RB.$("ERROR_path_does_not_exist", file.toAbsolutePath()));
        }

        Path signToolPath = findSignTool();
        if (signToolPath == null || !Files.exists(signToolPath)) {
            throw new SigningException("SignTool executable not found");
        }

        try {
            Command command = new Command(signToolPath.toString())
                .arg("verify")
                .arg("/pa")
                .arg("/v")
                .arg(file.toString());

            CommandExecutor executor = new CommandExecutor(context.getLogger());
            Command.Result result = executor.executeCommand(command);

            return result.getExitValue() == 0;
        } catch (CommandException e) {
            throw new SigningException("Failed to verify signature", e);
        }
    }

    private Path findSignTool() {
        List<String> searchPaths = new ArrayList<>();
        
        String customPath = System.getProperty("jreleaser.signtool.path");
        if (isNotBlank(customPath)) {
            searchPaths.add(customPath);
        }

        customPath = System.getenv("JRELEASER_SIGNTOOL_PATH");
        if (isNotBlank(customPath)) {
            searchPaths.add(customPath);
        }

        searchPaths.add("signtool");
        searchPaths.add("signtool.exe");

        String programFiles = System.getenv("ProgramFiles");
        String programFilesX86 = System.getenv("ProgramFiles(x86)");

        if (isNotBlank(programFiles)) {
            searchPaths.add(programFiles + "\\Windows Kits\\10\\bin\\x64\\signtool.exe");
            searchPaths.add(programFiles + "\\Windows Kits\\10\\bin\\x86\\signtool.exe");
            searchPaths.add(programFiles + "\\Microsoft SDKs\\Windows\\v7.1A\\Bin\\signtool.exe");
            searchPaths.add(programFiles + "\\Microsoft SDKs\\Windows\\v7.0A\\Bin\\signtool.exe");
        }

        if (isNotBlank(programFilesX86)) {
            searchPaths.add(programFilesX86 + "\\Windows Kits\\10\\bin\\x64\\signtool.exe");
            searchPaths.add(programFilesX86 + "\\Windows Kits\\10\\bin\\x86\\signtool.exe");
            searchPaths.add(programFilesX86 + "\\Microsoft SDKs\\Windows\\v7.1A\\Bin\\signtool.exe");
            searchPaths.add(programFilesX86 + "\\Microsoft SDKs\\Windows\\v7.0A\\Bin\\signtool.exe");
        }

        for (String pathStr : searchPaths) {
            try {
                Path path = Paths.get(pathStr);
                if (Files.exists(path) && Files.isExecutable(path)) {
                    context.getLogger().debug("Found SignTool at: " + path);
                    return path;
                }
            } catch (Exception e) {
                context.getLogger().debug("Error checking path " + pathStr + ": " + e.getMessage());
            }
        }

        try {
            Command command = new Command("where").arg("signtool");
            CommandExecutor executor = new CommandExecutor(context.getLogger(), CommandExecutor.Output.QUIET);
            Command.Result result = executor.executeCommand(command);
            
            if (result.getExitValue() == 0 && isNotBlank(result.getOut())) {
                String foundPath = result.getOut().split("\\r?\\n")[0].trim();
                Path path = Paths.get(foundPath);
                if (Files.exists(path)) {
                    context.getLogger().debug("Found SignTool via 'where' command at: " + path);
                    return path;
                }
            }
        } catch (Exception e) {
            context.getLogger().debug("Error using 'where' command to find signtool: " + e.getMessage());
        }

        return null;
    }
}