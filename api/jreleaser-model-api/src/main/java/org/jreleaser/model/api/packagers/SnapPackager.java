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
import java.util.Map;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface SnapPackager extends RepositoryPackager {
    String TYPE = "snap";
    String SKIP_SNAP = "skipSnap";

    String getPackageName();

    String getBase();

    String getGrade();

    String getConfinement();

    Set<String> getLocalPlugs();

    Set<String> getLocalSlots();

    List<? extends Plug> getPlugs();

    List<? extends Slot> getSlots();

    List<? extends Architecture> getArchitectures();

    String getExportedLogin();

    boolean isRemoteBuild();

    PackagerRepository getRepository();

    @Deprecated
    PackagerRepository getSnap();

    interface Plug extends Domain {
        String getName();

        Map<String, String> getAttributes();

        List<String> getReads();

        List<String> getWrites();
    }

    interface Slot extends Domain {
        String getName();

        Map<String, String> getAttributes();

        List<String> getReads();

        List<String> getWrites();
    }

    interface Architecture extends Domain {
        List<String> getBuildOn();

        List<String> getRunOn();

        boolean isIgnoreError();
    }
}
