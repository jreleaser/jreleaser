/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.sdk.command.Command;
import org.jreleaser.sdk.command.CommandException;
import org.jreleaser.sdk.command.CommandExecutor;
import org.jreleaser.util.FileUtils;
import org.jreleaser.util.IoUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public class DownloadableTool {
    private static final String BASE_TEMPLATE_PREFIX = "META-INF/jreleaser/tools/";
    private static final String DOWNLOAD_URL = "download.url";
    private static final String VERSION = "version";
    private static final String EXECUTABLE = ".executable";
    private static final String FILENAME = ".filename";
    private static final String COMMAND_VERSION = "command.version";
    private static final String COMMAND_VERIFY = "command.verify";
    private static final String EXECUTABLE_PATH = ".executable.path";
    private static final String UNPACK = "unpack";

    private final JReleaserLogger logger;
    private final String name;
    private final String version;
    private final String platform;
    private final boolean enabled;
    private final Properties properties;
    private final boolean verifyErrorOutput;

    private Path executable;

    public DownloadableTool(JReleaserLogger logger, String name, String version, String platform, boolean verifyErrorOutput) throws ToolException {
        this.logger = logger;
        this.name = name;
        this.version = version;
        this.platform = platform;
        this.verifyErrorOutput = verifyErrorOutput;

        String key = name + ".properties";

        try {
            properties = new Properties();
            properties.load(DownloadableTool.class.getClassLoader()
                .getResourceAsStream(BASE_TEMPLATE_PREFIX + key));
            enabled = properties.containsKey(platformKey(EXECUTABLE));
            if (enabled) {
                executable = Paths.get(properties.getProperty(platformKey(EXECUTABLE)));
            }
        } catch (Exception e) {
            throw new ToolException(RB.$("ERROR_unexpected_reading_resource_for", key, "classpath"));
        }
    }

    private String platformKey(String key) {
        String k = platform + key;
        if (properties.containsKey(k)) {
            return k;
        }

        return key.startsWith(".") ? key.substring(1) : key;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getPlatform() {
        return platform;
    }

    public Path getExecutable() {
        return executable;
    }

    public boolean verify() {
        if (null != executable) {
            return verify(executable);
        }
        return false;
    }

    private boolean verify(Path executable) {
        Command command = new Command(executable.toString())
            .arg(properties.getProperty(COMMAND_VERSION));

        try {
            String verify = properties.getProperty(COMMAND_VERIFY).trim();
            Map<String, Object> props = props();
            verify = resolveTemplate(verify, props);

            Pattern pattern = Pattern.compile(verify);

            if (verifyErrorOutput) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ByteArrayOutputStream err = new ByteArrayOutputStream();
                executeCommandCapturing(command, out, err);
                return pattern.matcher(IoUtils.toString(out)).find() || pattern.matcher(IoUtils.toString(err)).find();
            } else {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                executeCommandCapturing(command, out, null);
                return pattern.matcher(IoUtils.toString(out)).find();
            }
        } catch (CommandException e) {
            if (null != e.getCause()) {
                logger.debug(e.getCause().getMessage());
            } else {
                logger.debug(e.getMessage());
            }
        }
        return false;
    }

    public void download() throws ToolException {
        String filename = properties.getProperty(platformKey(FILENAME));

        if (isBlank(filename)) {
            executable = null;
            return;
        }

        Path caches = resolveJReleaserCacheDir();
        Path dest = caches.resolve(name).resolve(version);

        boolean unpack = Boolean.parseBoolean(properties.getProperty(UNPACK));
        String downloadUrl = properties.getProperty(DOWNLOAD_URL);
        String executablePath = properties.getProperty(platformKey(EXECUTABLE_PATH));
        String exec = properties.getProperty(platformKey(EXECUTABLE));

        Map<String, Object> props = props();
        filename = resolveTemplate(filename, props);
        if (isNotBlank(executablePath)) executablePath = resolveTemplate(executablePath, props);

        Path test = dest;
        if (unpack) {
            test = dest.resolve(executablePath);
        }
        test = test.resolve(exec).toAbsolutePath();

        if (Files.exists(test)) {
            executable = test;
            logger.debug(RB.$("tool.cached", executable));
            return;
        }

        downloadUrl = resolveTemplate(downloadUrl, props) + filename;
        try (InputStream stream = new URL(downloadUrl).openStream()) {
            Path tmp = Files.createTempDirectory("jreleaser");
            Path destination = tmp.resolve(filename);

            logger.debug(RB.$("tool.located", filename));
            logger.debug(RB.$("tool.downloading", downloadUrl));
            Files.copy(stream, destination, REPLACE_EXISTING);
            logger.debug(RB.$("tool.downloaded", filename));

            Files.createDirectories(dest);
            if (unpack) {
                FileUtils.unpackArchive(destination, dest, false);
                logger.debug(RB.$("tool.unpacked", filename));
                executable = dest.resolve(executablePath).resolve(exec).toAbsolutePath();
            } else {
                Path executableFile = dest.resolve(exec);
                Files.move(destination, executableFile);
                FileUtils.grantExecutableAccess(executableFile);
                executable = executableFile.toAbsolutePath();
            }
            logger.debug(RB.$("tool.cached", executable));
        } catch (FileNotFoundException e) {
            logger.debug(RB.$("tool.not.found", filename));
            throw new ToolException(RB.$("tool.not.found", filename), e);
        } catch (Exception e) {
            logger.debug(RB.$("tool.download.error", filename));
            throw new ToolException(RB.$("tool.download.error", filename), e);
        }
    }

    public Command asCommand() {
        return new Command(executable.toString());
    }

    private Map<String, Object> props() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put(VERSION, version);
        return props;
    }

    private void executeCommandCapturing(Command command, OutputStream out, OutputStream err) throws CommandException {
        int exitValue = new CommandExecutor(logger)
            .executeCommandCapturing(command, out, err);
        if (exitValue != 0) {
            logger.error(out.toString().trim());
            throw new CommandException(RB.$("ERROR_command_execution_exit_value", exitValue));
        }
    }

    private Path resolveJReleaserCacheDir() {
        String home = System.getenv("JRELEASER_USER_HOME");
        if (isBlank(home)) {
            home = System.getProperty("user.home") + File.separator + ".jreleaser";
        }

        return Paths.get(home).resolve("caches");
    }
}
