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
package org.jreleaser.model.api.packagers;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface ChocolateyPackager extends RepositoryPackager {
    String CHOCOLATEY_API_KEY = "CHOCOLATEY_API_KEY";
    String TYPE = "chocolatey";
    String SKIP_CHOCOLATEY = "skipChocolatey";
    String DEFAULT_CHOCOLATEY_PUSH_URL = "https://push.chocolatey.org/";

    String getPackageName();

    String getPackageVersion();

    String getUsername();

    String getApiKey();

    String getTitle();

    String getIconUrl();

    String getSource();

    boolean isRemoteBuild();

    PackagerRepository getRepository();

    @Deprecated
    PackagerRepository getBucket();
}
