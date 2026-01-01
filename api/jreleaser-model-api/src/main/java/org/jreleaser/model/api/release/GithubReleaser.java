/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.model.api.release;

import org.jreleaser.model.api.common.Domain;
import org.jreleaser.model.api.common.EnabledAware;

import java.util.Locale;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface GithubReleaser extends Releaser {
    String TYPE = "github";

    boolean isDraft();

    boolean isImmutableRelease();

    String getDiscussionCategoryName();

    ReleaseNotes getReleaseNotes();

    MakeLatest getMakeLatest();

    interface ReleaseNotes extends Domain, EnabledAware {
        String getConfigurationFile();
    }

    enum MakeLatest {
        FALSE,
        TRUE,
        LEGACY;

        public String formatted() {
            return name().toLowerCase();
        }

        public static MakeLatest of(String str) {
            if (isBlank(str)) return null;
            return MakeLatest.valueOf(str.toUpperCase(Locale.ENGLISH).trim());
        }
    }
}
