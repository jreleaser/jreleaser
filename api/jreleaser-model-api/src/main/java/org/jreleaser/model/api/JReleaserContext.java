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
package org.jreleaser.model.api;

import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.api.signing.Keyring;
import org.jreleaser.model.api.signing.SigningException;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface JReleaserContext {
    Path relativize(Path basedir, Path other);

    Path relativizeToBasedir(Path other);

    JReleaserLogger getLogger();

    Mode getMode();

    JReleaserModel getModel();

    Path getBasedir();

    Path getOutputDirectory();

    Path getChecksumsDirectory();

    Path getSignaturesDirectory();

    Path getPrepareDirectory();

    Path getPackageDirectory();

    Path getAssembleDirectory();

    Path getDownloadDirectory();

    Path getArtifactsDirectory();

    boolean isDryrun();

    boolean isGitRootSearch();

    List<String> getIncludedAnnouncers();

    List<String> getIncludedAssemblers();

    List<String> getIncludedDistributions();

    List<String> getIncludedPackagers();

    List<String> getIncludedDownloaderTypes();

    List<String> getIncludedDownloaderNames();

    List<String> getIncludedDeployerTypes();

    List<String> getIncludedDeployerNames();

    List<String> getIncludedUploaderTypes();

    List<String> getIncludedUploaderNames();

    List<String> getExcludedAnnouncers();

    List<String> getExcludedAssemblers();

    List<String> getExcludedDistributions();

    List<String> getExcludedPackagers();

    List<String> getExcludedDownloaderTypes();

    List<String> getExcludedDownloaderNames();

    List<String> getExcludedDeployerTypes();

    List<String> getExcludedDeployerNames();

    List<String> getExcludedUploaderTypes();

    List<String> getExcludedUploaderNames();

    JReleaserCommand getCommand();

    Map<String, Object> props();

    Map<String, Object> fullProps();

    void nag(String version, String message);

    Keyring createKeyring() throws SigningException;

    enum Mode {
        CONFIG,
        DOWNLOAD,
        ASSEMBLE,
        DEPLOY,
        FULL,
        CHANGELOG,
        ANNOUNCE;

        public boolean validateChangelog() {
            return this == CHANGELOG;
        }

        public boolean validateAnnounce() {
            return this == ANNOUNCE;
        }

        public boolean validateDownload() {
            return this == DOWNLOAD;
        }

        public boolean validateAssembly() {
            return this == ASSEMBLE;
        }

        public boolean validateStandalone() {
            return validateAssembly() || validateDownload();
        }

        public boolean validateConfig() {
            return this == CONFIG || this == FULL;
        }

        public boolean validatePaths() {
            return this == FULL;
        }
    }
}
