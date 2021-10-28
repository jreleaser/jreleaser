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
package org.jreleaser.util;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public final class ResourceUtils {
    private ResourceUtils() {
        // noop
    }

    public static URL resolveLocation(Class<?> klass) {
        if (klass == null) return null;

        try {
            URL codeSourceLocation = klass.getProtectionDomain()
                .getCodeSource()
                .getLocation();
            if (codeSourceLocation != null) return codeSourceLocation;
        } catch (SecurityException | NullPointerException ignored) {
            // noop
        }

        URL classResource = klass.getResource(klass.getSimpleName() + ".class");
        if (classResource == null) return null;

        String url = classResource.toString();
        String suffix = klass.getCanonicalName().replace('.', '/') + ".class";
        if (!url.endsWith(suffix)) return null;
        String path = url.substring(0, url.length() - suffix.length());

        if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);

        try {
            return new URL(path);
        } catch (MalformedURLException ignored) {
            return null;
        }
    }
}
