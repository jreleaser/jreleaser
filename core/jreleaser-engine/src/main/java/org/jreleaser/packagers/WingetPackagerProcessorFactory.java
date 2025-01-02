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
package org.jreleaser.packagers;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.packagers.WingetPackager;
import org.jreleaser.model.spi.packagers.PackagerProcessorFactory;
import org.kordamp.jipsy.annotations.ServiceProviderFor;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
@ServiceProviderFor(PackagerProcessorFactory.class)
public class WingetPackagerProcessorFactory implements PackagerProcessorFactory<WingetPackager, WingetPackagerProcessor> {
    @Override
    public String getName() {
        return org.jreleaser.model.api.packagers.WingetPackager.TYPE;
    }

    @Override
    public WingetPackagerProcessor getPackagerNameProcessor(JReleaserContext context) {
        return new WingetPackagerProcessor(context);
    }
}