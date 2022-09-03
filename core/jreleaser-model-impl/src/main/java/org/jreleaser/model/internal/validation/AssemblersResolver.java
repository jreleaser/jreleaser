/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
package org.jreleaser.model.internal.validation;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.environment.Environment;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.ArchiveAssemblerResolver.resolveArchiveOutputs;
import static org.jreleaser.model.internal.validation.JlinkAssemblerResolver.resolveJlinkOutputs;
import static org.jreleaser.model.internal.validation.JpackageAssemblerResolver.resolveJpackageOutputs;
import static org.jreleaser.model.internal.validation.NativeImageAssemblerResolver.resolveNativeImageOutputs;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public abstract class AssemblersResolver extends Validator {
    public static void resolveAssemblers(JReleaserContext context, Errors errors) {
        Environment environment = context.getModel().getEnvironment();
        if (environment.getBooleanProperty("skipAssembleResolvers")) return;

        context.getLogger().debug("assemble");
        if (!environment.getBooleanProperty("skipArchiveResolver")) {
            resolveArchiveOutputs(context, errors);
        }
        if (!environment.getBooleanProperty("skipJlinkResolver")) {
            resolveJlinkOutputs(context, errors);
        }
        if (!environment.getBooleanProperty("skipJpackageResolver")) {
            resolveJpackageOutputs(context, errors);
        }
        if (!environment.getBooleanProperty("skipNativeImageResolver")) {
            resolveNativeImageOutputs(context, errors);
        }
    }
}