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
package org.jreleaser.extensions.internal.mustache;

import org.jreleaser.extensions.api.Extension;
import org.jreleaser.extensions.api.ExtensionPoint;
import org.kordamp.jipsy.annotations.ServiceProviderFor;

import java.util.Collections;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
@ServiceProviderFor(Extension.class)
public final class DefaultMustacheExtension implements Extension {
    @Override
    public String getName() {
        return "default-jreleaser-mustache";
    }

    @Override
    public Set<ExtensionPoint> provides() {
        return Collections.singleton(new DefaultMustacheExtensionPoint());
    }
}
