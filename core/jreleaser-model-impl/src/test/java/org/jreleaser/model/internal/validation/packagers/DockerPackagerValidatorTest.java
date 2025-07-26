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
package org.jreleaser.model.internal.validation.packagers;

import org.jreleaser.model.internal.packagers.DockerPackager;
import org.junit.jupiter.api.Test;

import static org.jreleaser.model.Constants.LABEL_OCI_IMAGE_SOURCE;
import static org.junit.jupiter.api.Assertions.*;

class DockerPackagerValidatorTest {
    @Test
    void shouldSetSourceLabelIfMissing() throws Exception {
        DockerPackager config = new DockerPackager();
        // Do not set LABEL_OCI_IMAGE_SOURCE
        assertFalse(config.getLabels().containsKey(LABEL_OCI_IMAGE_SOURCE));

        // Use reflection to call the private validateLabels method
        java.lang.reflect.Method method = DockerPackagerValidator.class.getDeclaredMethod("validateLabels", org.jreleaser.model.internal.packagers.DockerConfiguration.class);
        method.setAccessible(true);
        method.invoke(null, config);

        assertTrue(config.getLabels().containsKey(LABEL_OCI_IMAGE_SOURCE));
        assertEquals("{{projectSource}}", config.getLabels().get(LABEL_OCI_IMAGE_SOURCE));
    }
}
