/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
package org.jreleaser.model.validation;

import org.jreleaser.model.Checksum;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.Errors;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public abstract class ChecksumValidator extends Validator {
    public static void validateChecksum(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        if (mode != JReleaserContext.Mode.FULL) {
            return;
        }

        context.getLogger().debug("checksum");
        Checksum checksum = context.getModel().getChecksum();

        if (!checksum.isIndividualSet()) {
            checksum.setIndividual(false);
        }

        if (isBlank(checksum.getName())) {
            checksum.setName("checksums.txt");
        }

        checksum.getAlgorithms().add(Algorithm.SHA_256);
    }
}
