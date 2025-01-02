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

import java.util.Locale;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.12.0
 */
public interface MavenCentralMavenDeployer extends MavenDeployer {
    String TYPE = "mavenCentral";
    String DEPLOYMENT_ID = "deployment.id";

    Stage getStage();

    String getNamespace();

    String getDeploymentId();

    Integer getRetryDelay();

    Integer getMaxRetries();

    String getVerifyUrl();

    enum Stage {
        UPLOAD,
        PUBLISH,
        FULL;

        public String formatted() {
            return name().toLowerCase(Locale.ENGLISH);
        }

        public static Stage of(String str) {
            if (isBlank(str)) return null;
            return Stage.valueOf(str.toUpperCase(Locale.ENGLISH).trim());
        }
    }
}
