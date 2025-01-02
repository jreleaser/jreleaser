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
package org.jreleaser.util;

import java.util.ResourceBundle;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class DefaultVersions {
    private static final DefaultVersions INSTANCE = new DefaultVersions();

    private final ResourceBundle bundle = ResourceBundle.getBundle("org.jreleaser.default_versions");
    private final String cosignVersion = bundle.getString("cosign.version");
    private final String jibVersion = bundle.getString("jib.version");
    private final String mvnVersion = bundle.getString("mvn.version");
    private final String pomcheckerVersion = bundle.getString("pomchecker.version");
    private final String cyclonedxVersion = bundle.getString("cyclonedx.version");
    private final String syftVersion = bundle.getString("syft.version");

    public String getCosignVersion() {
        return cosignVersion;
    }

    public String getJibVersion() {
        return jibVersion;
    }

    public String getMvnVersion() {
        return mvnVersion;
    }

    public String getPomcheckerVersion() {
        return pomcheckerVersion;
    }

    public String getCyclonedxVersion() {
        return cyclonedxVersion;
    }

    public String getSyftVersion() {
        return syftVersion;
    }

    public static DefaultVersions getInstance() {
        return INSTANCE;
    }
}
