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

import org.jreleaser.model.api.common.Activatable;
import org.jreleaser.model.api.common.Domain;
import org.jreleaser.model.api.common.ExtraProperties;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.6.0
 */
public interface JibConfiguration extends Domain, ExtraProperties, Activatable {
    String TYPE = "jib";

    String getTemplateDirectory();

    List<String> getSkipTemplates();

    String getBaseImage();

    String getCreationTime();

    Format getFormat();

    Map<String, String> getEnvironment();

    Map<String, String> getLabels();

    Set<String> getImageNames();

    Set<String> getVolumes();

    Set<String> getExposedPorts();

    String getUser();

    String getWorkingDirectory();

    Set<? extends Registry> getRegistries();

    interface Registry extends Domain, Comparable<Registry> {
        String getName();

        String getServer();

        String getUsername();

        String getToUsername();

        String getFromUsername();

        String getPassword();

        String getToPassword();

        String getFromPassword();
    }

    enum Format {
        DOCKER("Docker"),
        OCI("OCI");

        private final String alias;

        Format(String alias) {
            this.alias = alias;
        }

        public String formatted() {
            return alias;
        }

        public static Format of(String str) {
            if (isBlank(str)) return null;
            return valueOf(str.toUpperCase(Locale.ENGLISH).trim()
                .replace(".", "_"));
        }
    }
}
