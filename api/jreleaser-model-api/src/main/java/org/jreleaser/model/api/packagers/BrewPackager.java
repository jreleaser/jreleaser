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

import org.jreleaser.model.api.common.Domain;

import java.util.List;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface BrewPackager extends RepositoryPackager {
    String TYPE = "brew";
    String SKIP_BREW = "skipBrew";

    String getFormulaName();

    String getDownloadStrategy();

    Set<String> getRequireRelative();

    boolean isMultiPlatform();

    PackagerRepository getRepository();

    @Deprecated
    PackagerRepository getTap();

    Cask getCask();

    List<String> getLivecheck();

    interface Cask extends Domain {
        boolean isEnabled();

        String getName();

        String getDisplayName();

        String getPkgName();

        String getAppName();

        String getAppcast();

        List<? extends CaskItem> getUninstallItems();

        List<? extends CaskItem> getZapItems();
    }

    interface CaskItem extends Domain {
        String getName();

        List<String> getItems();
    }
}
