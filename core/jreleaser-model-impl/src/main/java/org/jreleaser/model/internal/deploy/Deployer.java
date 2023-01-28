/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
package org.jreleaser.model.internal.deploy;

import org.jreleaser.model.internal.common.Activatable;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.ExtraProperties;
import org.jreleaser.model.internal.project.Project;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public interface Deployer<A extends org.jreleaser.model.api.deploy.Deployer> extends Domain, Activatable, ExtraProperties {
    String getType();

    String getName();

    void setName(String name);

    boolean resolveEnabledWithSnapshot(Project project);

    A asImmutable();
}
