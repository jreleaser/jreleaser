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
package org.jreleaser.model.api;

import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.api.signing.Keyring;
import org.jreleaser.model.api.signing.SigningException;
import org.jreleaser.mustache.TemplateContext;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.jreleaser.model.Constants.KEY_CHANGELOG;
import static org.jreleaser.model.Constants.KEY_CHANGELOG_CHANGES;
import static org.jreleaser.model.Constants.KEY_CHANGELOG_CONTRIBUTORS;
import static org.jreleaser.mustache.MustacheUtils.passThrough;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface JReleaserContext extends Serializable {
    String BASEDIR = "BASEDIR";
    String DRY_RUN = "DRY_RUN";
    String GIT_ROOT_SEARCH = "GIT_ROOT_SEARCH";
    String STRICT = "STRICT";
    String SELECT_CURRENT_PLATFORM = "SELECT_CURRENT_PLATFORM";
    String SELECT_PLATFORMS = "SELECT_PLATFORMS";
    String REJECT_PLATFORMS = "REJECT_PLATFORMS";

    Path relativize(Path basedir, Path other);

    Path relativizeToBasedir(Path other);

    JReleaserLogger getLogger();

    Mode getMode();

    JReleaserModel getModel();

    Path getBasedir();

    Path getOutputDirectory();

    Path getChecksumsDirectory();

    Path getCatalogsDirectory();

    Path getSignaturesDirectory();

    Path getPrepareDirectory();

    Path getPackageDirectory();

    Path getAssembleDirectory();

    Path getDownloadDirectory();

    Path getArtifactsDirectory();

    Path getDeployDirectory();

    boolean isDryrun();

    boolean isGitRootSearch();

    boolean isStrict();

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

    TemplateContext props();

    TemplateContext fullProps();

    void nag(String version, String message);

    Keyring createKeyring() throws SigningException;

    Changelog getChangelog();

    Map<String, Object> getAdditionalProperties();

    class Changelog implements Serializable {
        private static final long serialVersionUID = -7619174395858420344L;

        private String resolvedChangelog;
        private String formattedChanges;
        private String formattedContributors;

        public String getResolvedChangelog() {
            return resolvedChangelog;
        }

        public void setResolvedChangelog(String resolvedChangelog) {
            this.resolvedChangelog = resolvedChangelog;
        }

        public String getFormattedChanges() {
            return formattedChanges;
        }

        public void setFormattedChanges(String formattedChanges) {
            this.formattedChanges = formattedChanges;
        }

        public String getFormattedContributors() {
            return formattedContributors;
        }

        public void setFormattedContributors(String formattedContributors) {
            this.formattedContributors = formattedContributors;
        }

        public void apply(TemplateContext props) {
            if (!props.contains(KEY_CHANGELOG)) props.set(KEY_CHANGELOG, passThrough(resolvedChangelog));
            if (!props.contains(KEY_CHANGELOG_CHANGES)) props.set(KEY_CHANGELOG_CHANGES, passThrough(formattedChanges));
            if (!props.contains(KEY_CHANGELOG_CONTRIBUTORS)) {
                props.set(KEY_CHANGELOG_CONTRIBUTORS, passThrough(formattedContributors));
            }
        }
    }

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

        public boolean validateDeploy() {
            return this == DEPLOY;
        }

        public boolean validateDownload() {
            return this == DOWNLOAD;
        }

        public boolean validateAssembly() {
            return this == ASSEMBLE;
        }

        public boolean validateStandalone() {
            return validateAssembly() || validateDownload() || validateDeploy();
        }

        public boolean validateConfig() {
            return this == CONFIG || this == FULL;
        }

        public boolean validatePaths() {
            return this == FULL;
        }
    }
}
