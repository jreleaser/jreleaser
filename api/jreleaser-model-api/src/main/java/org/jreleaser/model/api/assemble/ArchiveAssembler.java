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
package org.jreleaser.model.api.assemble;

import org.jreleaser.model.Archive;
import org.jreleaser.model.api.catalog.swid.SwidTagAware;
import org.jreleaser.model.api.common.ArchiveOptions;
import org.jreleaser.model.api.common.Matrix;

import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public interface ArchiveAssembler extends Assembler, SwidTagAware {
    String TYPE = "archive";

    String getArchiveName();

    boolean isApplyDefaultMatrix();

    boolean isAttachPlatform();

    Set<Archive.Format> getFormats();

    ArchiveOptions getOptions();

    Matrix getMatrix();
}
