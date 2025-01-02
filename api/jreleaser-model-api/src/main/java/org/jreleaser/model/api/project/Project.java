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
package org.jreleaser.model.api.project;

import org.jreleaser.model.Active;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.api.common.Domain;
import org.jreleaser.model.api.common.ExtraProperties;
import org.jreleaser.model.api.common.Icon;
import org.jreleaser.model.api.common.Java;
import org.jreleaser.model.api.common.Screenshot;
import org.jreleaser.version.Version;

import java.io.Serializable;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface Project extends Domain, ExtraProperties, Active.Releaseable {
    String PROJECT_NAME = "PROJECT_NAME";
    String PROJECT_VERSION = "PROJECT_VERSION";
    String PROJECT_VERSION_PATTERN = "PROJECT_VERSION_PATTERN";
    String PROJECT_SNAPSHOT_PATTERN = "PROJECT_SNAPSHOT_PATTERN";
    String PROJECT_SNAPSHOT_LABEL = "PROJECT_SNAPSHOT_LABEL";
    String PROJECT_SNAPSHOT_FULL_CHANGELOG = "PROJECT_SNAPSHOT_FULL_CHANGELOG";
    String DEFAULT_SNAPSHOT_PATTERN = ".*-SNAPSHOT";
    String DEFAULT_SNAPSHOT_LABEL = "early-access";

    boolean isSnapshot();

    @Override
    boolean isRelease();

    String getName();

    String getVersion();

    String getVersionPattern();

    Snapshot getSnapshot();

    String getDescription();

    String getLongDescription();

    @Deprecated
    String getWebsite();

    String getLicense();

    @Deprecated
    String getLicenseUrl();

    String getInceptionYear();

    String getCopyright();

    String getVendor();

    @Deprecated
    String getDocsUrl();

    Stereotype getStereotype();

    List<? extends Screenshot> getScreenshots();

    List<? extends Icon> getIcons();

    @Deprecated
    Java getJava();

    Languages getLanguages();

    List<String> getAuthors();

    List<String> getTags();

    List<String> getMaintainers();

    Links getLinks();

    Version<?> version();

    interface Snapshot extends Domain {
        String getPattern();

        String getLabel();

        boolean isFullChangelog();
    }

    interface Links extends Domain {
        String getHomepage();

        String getDocumentation();

        String getLicense();

        String getBugTracker();

        String getFaq();

        String getHelp();

        String getDonation();

        String getTranslate();

        String getContact();

        String getVcsBrowser();

        String getContribute();
    }

    /**
     * @author Andres Almiray
     * @since 0.5.0
     */
    interface VersionPattern extends Serializable {
        org.jreleaser.model.VersionPattern.Type getType();

        String getFormat();
    }
}
