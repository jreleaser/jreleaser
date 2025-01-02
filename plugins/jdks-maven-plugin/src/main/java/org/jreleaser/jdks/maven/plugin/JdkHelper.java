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
package org.jreleaser.jdks.maven.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.ChecksumUtils;
import org.jreleaser.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

/**
 * Downloads, verifies, and unpacks JDKs.
 *
 * @author Andres Almiray
 * @since 0.9.0
 */
public class JdkHelper {
    private final MavenProject project;
    private final Log log;
    private final File outputDirectory;
    private final MavenSession session;
    private final BuildPluginManager pluginManager;
    private final ArchiverManager archiverManager;

    public JdkHelper(MavenProject project, Log log, File outputDirectory,
                     MavenSession session, BuildPluginManager pluginManager,
                     ArchiverManager archiverManager) {
        this.project = project;
        this.log = log;
        this.outputDirectory = outputDirectory;
        this.session = session;
        this.pluginManager = pluginManager;
        this.archiverManager = archiverManager;
    }

    public void setupJdk(Jdk jdk, boolean unpack) throws MojoExecutionException {
        File jdkExtractDirectory = new File(outputDirectory, jdk.getName());

        boolean downloaded = false;
        if (!new File(jdkExtractDirectory, getFilename(jdk)).exists()) {
            downloadJdk(jdkExtractDirectory, jdk);
            downloaded = true;
        }

        verifyJdk(jdkExtractDirectory, jdk);

        File jdkDir = new File(jdkExtractDirectory, getDirname(jdk));
        if (jdkDir.exists()) {
            if (downloaded) {
                try {
                    FileUtils.deleteFiles(jdkDir.toPath());
                } catch (IOException e) {
                    throw new MojoExecutionException("Unexpected error", e);
                }
                extractJdk(jdkExtractDirectory, jdk, unpack);
            }
        } else {
            extractJdk(jdkExtractDirectory, jdk, unpack);
        }
    }

    private void downloadJdk(File jdkExtractDirectory, Jdk jdk) throws MojoExecutionException {
        String filename = getFilename(jdk);
        log.info("Downloading " + jdk.getUrl() + " to " + jdkExtractDirectory + File.separator + filename);

        Boolean interactiveMode = session.getSettings().getInteractiveMode();
        session.getSettings().setInteractiveMode(false);

        try {
            String cacheDirectory = Paths.get(session.getSettings().getLocalRepository())
                .resolve(".cache/download-maven-plugin")
                .toAbsolutePath().toString();

            executeMojo(
                plugin("com.googlecode.maven-download-plugin",
                    "download-maven-plugin",
                    "1.8.1"),
                goal("wget"),
                configuration(
                    element("uri", jdk.getUrl()),
                    element("followRedirects", "true"),
                    element("outputDirectory", jdkExtractDirectory.getAbsolutePath()),
                    element("cacheDirectory", cacheDirectory),
                    element("outputFileName", filename)
                ),
                executionEnvironment(
                    project,
                    session,
                    pluginManager));
        } finally {
            session.getSettings().setInteractiveMode(interactiveMode);
        }
    }

    private void verifyJdk(File jdkExtractDirectory, Jdk jdk) throws MojoExecutionException {
        String checksum = jdk.getChecksum();
        String filename = getFilename(jdk);

        if (isBlank(checksum)) {
            log.info("Checksum not available. Skipping verification of " + filename);
            return;
        }

        String algo = Algorithm.SHA_256.formatted();
        if (checksum.contains("/")) {
            String[] parts = checksum.split("/");
            algo = parts[0];
            checksum = parts[1];
        }

        try {
            // calculate checksum
            Path input = new File(jdkExtractDirectory, filename).toPath();
            String calculatedChecksum = ChecksumUtils.checksum(Algorithm.of(algo), Files.readAllBytes(input));

            // verify checksum
            log.info("Verifying " + filename);
            if (!calculatedChecksum.equalsIgnoreCase(checksum)) {
                throw new MojoExecutionException("Invalid checksum for file '" +
                    filename + "'. Expected " + checksum.toLowerCase(Locale.ENGLISH) +
                    " but got " + calculatedChecksum.toLowerCase(Locale.ENGLISH) + ".");
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Unexpected error when verifying " + filename, e);
        }
    }

    private String getFilename(Jdk jdk) {
        if (isNotBlank(jdk.getFilename())) {
            return jdk.getFilename();
        }
        int p = jdk.getUrl().lastIndexOf("/");
        return jdk.getUrl().substring(p + 1);
    }

    private String getDirname(Jdk jdk) {
        String filename = getFilename(jdk);
        return filename.substring(0, filename.lastIndexOf('.'));
    }

    private void extractJdk(File jdkExtractDirectory, Jdk jdk, boolean unpack) throws MojoExecutionException {
        if (!unpack) return;
        File inputFile = new File(jdkExtractDirectory, getFilename(jdk));

        try {
            log.info("Extracting " + inputFile.getName());
            UnArchiver unarchiver = archiverManager.getUnArchiver(inputFile);
            unarchiver.setSourceFile(inputFile);
            unarchiver.setDestDirectory(jdkExtractDirectory);
            unarchiver.extract();
        } catch (NoSuchArchiverException e) {
            throw new MojoExecutionException("Unexpected error when extracting " + inputFile.getName(), e);
        }
    }
}
