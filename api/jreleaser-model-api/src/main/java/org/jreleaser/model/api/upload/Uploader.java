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
package org.jreleaser.model.api.upload;

import org.jreleaser.model.api.common.Activatable;
import org.jreleaser.model.api.common.Domain;
import org.jreleaser.model.api.common.ExtraProperties;
import org.jreleaser.model.api.common.TimeoutAware;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public interface Uploader extends Domain, Activatable, TimeoutAware, ExtraProperties {
    String getType();

    String getName();

    boolean isSnapshotSupported();

    boolean isArtifacts();

    boolean isFiles();

    boolean isSignatures();

    boolean isChecksums();

    boolean isCatalogs();
}
