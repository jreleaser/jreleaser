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
package org.jreleaser.model.internal.validation.common;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Matrix;
import org.jreleaser.util.Errors;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 1.16.0
 */
public final class MatrixValidator {
    private MatrixValidator() {
        // noop
    }

    public static void validateMatrix(JReleaserContext context, Matrix matrix, String key, Errors errors) {
        if (matrix.isEmpty()) return;
        context.getLogger().debug(key);

        if (matrix.hasVars() && matrix.hasRows()) {
            errors.configuration(RB.$("validation_matrix_vars_and_rows", key));
            return;
        }

        if (matrix.hasVars()) {
            matrix.getVars().forEach((k, v) -> {
                if (null == v || v.isEmpty()) {
                    errors.configuration(RB.$("validation_matrix_invalid_var", key, k));
                }
            });
        }

        if (matrix.hasRows()) {
            List<Map<String, String>> rows = matrix.getRows();
            long variants = rows.stream()
                .mapToInt(Map::size)
                .distinct()
                .count();

            if (variants != 1) {
                // different column sizes
                errors.configuration(RB.$("validation_matrix_column_count", key));
            }

            Set<String> keys = rows.get(0).keySet();
            for (int i = 0; i < rows.size(); i++) {
                Set<String> target = rows.get(i).keySet();
                if (!target.containsAll(keys) && target.size() == keys.size()) {
                    errors.configuration(RB.$("validation_matrix_different_keys", key, i));
                }
            }
        }
    }
}