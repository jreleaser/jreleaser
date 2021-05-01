/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.jreleaser.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
 * @since 0.3.0
 */
@Mojo(name = "setup-jdks")
public class SetupJdksMojo extends AbstractJdksMojo {
    @Parameter(property = "jdks.output.directory", defaultValue = "${project.build.directory}/jdks")
    private File outputDirectory;

    @Parameter(defaultValue = "${session}")
    private MavenSession session;

    @Component
    private BuildPluginManager pluginManager;

    /**
     * The name of the JDK to be downloaded.
     */
    @Parameter(property = "jdk.name")
    private String jdkName;

    /**
     * Skip execution.
     */
    @Parameter(property = "jdks.setup.skip")
    private boolean skip;

    @Component
    private ArchiverManager archiverManager;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Banner.display(project, getLog());
        if (skip) return;

        if (jdks == null || jdks.isEmpty()) return;
        validate();

        if (isNotBlank(jdkName)) {
            // find the given JDK
            Jdk jdk = jdks.stream()
                .filter(j -> j.getName().equals(jdkName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Jdk " + jdkName + " was not found"));
            setupJdk(jdk);
        } else {
            for (Jdk jdk : jdks) {
                setupJdk(jdk);
            }
        }
    }

    private void setupJdk(Jdk jdk) throws MojoExecutionException {
        boolean downloaded = false;
        if (!new File(outputDirectory, getFilename(jdk)).exists()) {
            downloadJdk(jdk);
            downloaded = true;
        }

        verifyJdk(jdk);

        File jdkDir = new File(outputDirectory, getDirname(jdk));
        if (jdkDir.exists()) {
            if (downloaded) {
                try {
                    FileUtils.deleteFiles(jdkDir.toPath());
                } catch (IOException e) {
                    throw new MojoExecutionException("Unexpected error", e);
                }
                extractJdk(jdk);
            }
        } else {
            extractJdk(jdk);
        }
    }

    private void downloadJdk(Jdk jdk) throws MojoExecutionException {
        getLog().info("Downloading " + jdk.getUrl());

        Boolean interactiveMode = session.getSettings().getInteractiveMode();
        session.getSettings().setInteractiveMode(false);

        try {
            executeMojo(
                plugin("com.googlecode.maven-download-plugin",
                    "download-maven-plugin",
                    "1.6.3"),
                goal("wget"),
                configuration(
                    element("uri", jdk.getUrl()),
                    element("followRedirects", "true"),
                    element("outputDirectory", outputDirectory.getAbsolutePath())
                ),
                executionEnvironment(
                    project,
                    session,
                    pluginManager));
        } finally {
            session.getSettings().setInteractiveMode(interactiveMode);
        }
    }

    private void verifyJdk(Jdk jdk) throws MojoExecutionException {
        String algorithm = "SHA-256";
        String checksum = jdk.getChecksum();
        if (checksum.contains("/")) {
            String[] parts = checksum.split("/");
            algorithm = parts[0];
            checksum = parts[1];
        }

        String filename = getFilename(jdk);

        try {
            // calculate checksum
            MessageDigest md = MessageDigest.getInstance(algorithm);
            String calculatedChecksum;
            try (FileInputStream fis = new FileInputStream(new File(outputDirectory, filename))) {
                byte[] buf = new byte[1024];
                int read;
                while ((read = fis.read(buf)) != -1) {
                    md.update(buf, 0, read);
                }
                calculatedChecksum = toHex(md.digest());
            }

            // verify checksum
            getLog().info("Verifying " + filename);
            if (!calculatedChecksum.equalsIgnoreCase(checksum)) {
                throw new MojoExecutionException("Invalid checksum for file '" +
                    filename + "'. Expected " + checksum.toLowerCase() +
                    " but got " + calculatedChecksum.toLowerCase() + ".");
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new MojoExecutionException("Unexpected error when verifying " + filename, e);
        }
    }

    private String getFilename(Jdk jdk) {
        int p = jdk.getUrl().lastIndexOf("/");
        return jdk.getUrl().substring(p + 1);
    }

    private String getDirname(Jdk jdk) {
        String filename = getFilename(jdk);
        return filename.substring(0, filename.lastIndexOf('.'));
    }

    private String toHex(byte[] barr) {
        StringBuilder result = new StringBuilder();
        for (byte b : barr) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }

    private void extractJdk(Jdk jdk) throws MojoExecutionException {
        File inputFile = new File(outputDirectory, getFilename(jdk));

        try {
            getLog().info("Extracting " + inputFile.getName());
            UnArchiver unarchiver = archiverManager.getUnArchiver(inputFile);
            unarchiver.setSourceFile(inputFile);
            unarchiver.setDestDirectory(outputDirectory);
            unarchiver.extract();
        } catch (NoSuchArchiverException e) {
            throw new MojoExecutionException("Unexpected error when extracting " + inputFile.getName(), e);
        }
    }
}
