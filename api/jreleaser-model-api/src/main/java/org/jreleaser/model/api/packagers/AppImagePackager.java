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

import org.jreleaser.model.api.common.Icon;
import org.jreleaser.model.api.common.Screenshot;

import java.util.List;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public interface AppImagePackager extends RepositoryPackager {
    String TYPE = "appimage";
    String SKIP_APPIMAGE = "skipAppImage";

    String getComponentId();

    List<String> getCategories();

    String getDeveloperId();

    String getDeveloperName();

    boolean isRequiresTerminal();

    List<? extends Screenshot> getScreenshots();

    List<? extends Icon> getIcons();

    Set<String> getSkipReleases();

    PackagerRepository getRepository();
}
