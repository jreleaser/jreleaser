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
package org.jreleaser.model.api.checksum;

import org.jreleaser.model.api.common.Domain;
import org.jreleaser.util.Algorithm;

import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public interface Checksum extends Domain {
    String KEY_SKIP_CHECKSUM = "skipChecksum";
    String INDIVIDUAL_CHECKSUM = "individualChecksum";

    String getName();

    boolean isIndividual();

    Set<Algorithm> getAlgorithms();

    boolean isArtifacts();

    boolean isFiles();
}
