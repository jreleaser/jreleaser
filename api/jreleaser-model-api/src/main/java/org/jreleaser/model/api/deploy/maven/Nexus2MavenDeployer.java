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
package org.jreleaser.model.api.deploy.maven;

import org.jreleaser.bundle.RB;

import java.util.Locale;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public interface Nexus2MavenDeployer extends MavenDeployer {
    String TYPE = "nexus2";

    String getSnapshotUrl();

    String getVerifyUrl();

    boolean isCloseRepository();

    boolean isReleaseRepository();

    Integer getTransitionDelay();

    Integer getTransitionMaxRetries();

    String getStagingProfileId();

    String getStagingRepositoryId();

    Stage getStartStage();

    Stage getEndStage();

    enum Stage {
        UPLOAD,
        CLOSE,
        RELEASE;

        public String formatted() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public static Stage of(String str) {
            if (isBlank(str)) return null;
            return Stage.valueOf(str.toUpperCase(Locale.ENGLISH).trim());
        }
    }

    enum StageOperation {
        FULL_DEPLOYMENT,
        UPLOAD,
        UPLOAD_AND_CLOSE,
        CLOSE,
        CLOSE_AND_RELEASE,
        RELEASE;

        public static StageOperation of(Stage start, Stage end) {
            if (null == start) {
                if (end == Stage.UPLOAD) return UPLOAD;
                if (end == Stage.CLOSE) return UPLOAD_AND_CLOSE;
                return FULL_DEPLOYMENT;
            }

            if (Stage.UPLOAD == start) {
                if (null == end) return FULL_DEPLOYMENT;
                switch (end) {
                    case UPLOAD:
                        return UPLOAD;
                    case CLOSE:
                        return UPLOAD_AND_CLOSE;
                    case RELEASE:
                        return FULL_DEPLOYMENT;
                }
            }

            if (Stage.CLOSE == start) {
                if (null == end) return CLOSE_AND_RELEASE;
                switch (end) {
                    case UPLOAD:
                        throw new IllegalArgumentException(RB.$("ERROR_nexus_stage", start, end));
                    case CLOSE:
                        return CLOSE;
                    case RELEASE:
                        return CLOSE_AND_RELEASE;
                }
            }

            if (Stage.RELEASE == start) {
                if (null == end) return RELEASE;
                switch (end) {
                    case UPLOAD:
                    case CLOSE:
                        throw new IllegalArgumentException(RB.$("ERROR_nexus_stage", start, end));
                    case RELEASE:
                        return RELEASE;
                }
            }

            return FULL_DEPLOYMENT;
        }
    }
}
