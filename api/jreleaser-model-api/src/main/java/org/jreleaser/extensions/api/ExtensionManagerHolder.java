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
package org.jreleaser.extensions.api;

import org.jreleaser.bundle.RB;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public final class ExtensionManagerHolder {
    private static final ThreadLocal<ExtensionManager> EXTENSION_MANAGER_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        List<ExtensionManager> extensionManagers = StreamSupport
            .stream(resolveServiceLoader().spliterator(), false)
            .collect(toList());

        if (extensionManagers.isEmpty()) {
            // Should never happen!
            throw new IllegalStateException(RB.$("ERROR_extension_manager_load"));
        } else if (extensionManagers.size() > 1) {
            throw new IllegalStateException(RB.$("ERROR_extension_manager_multiple_instances", extensionManagers.size()));
        }

        return extensionManagers.get(0);
    });

    private ExtensionManagerHolder() {
        // noop
    }

    public static ExtensionManager get() {
        return EXTENSION_MANAGER_THREAD_LOCAL.get();
    }

    public static void cleanup() {
        EXTENSION_MANAGER_THREAD_LOCAL.remove();
    }

    private static ServiceLoader<ExtensionManager> resolveServiceLoader() {
        // TODO: review when moving baseline to JDK11+
        // Check if handlers must be loaded from a ModuleLayer
        // if (null != ExtensionManager.class.getModule().getLayer()) {
        //     ServiceLoader<ExtensionManager> handlers = ServiceLoader.load(ExtensionManager.class.getModule().getLayer(), ExtensionManager.class);
        //     if (handlers.stream().count() > 0) {
        //         return handlers;
        //     }
        // }

        // Check if the ExtensionManager.class.classLoader works
        ServiceLoader<ExtensionManager> handlers = ServiceLoader.load(ExtensionManager.class, ExtensionManager.class.getClassLoader());
        if (handlers.iterator().hasNext()) {
            return handlers;
        }

        // If *nothing* else works
        return ServiceLoader.load(ExtensionManager.class);
    }
}
