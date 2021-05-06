/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
package org.jreleaser.maven.plugin;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class Assemble implements EnabledAware {
    private final Map<String, Jlink> jlinks = new LinkedHashMap<>();
    private final Map<String, NativeImage> nativeImages = new LinkedHashMap<>();
    private Boolean enabled;

    void setAll(Assemble assemble) {
        this.enabled = assemble.enabled;
        setJlinks(assemble.jlinks);
        setNativeImages(assemble.nativeImages);
    }

    @Override
    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabledSet() {
        return enabled != null;
    }

    public Map<String, Jlink> getJlinks() {
        return jlinks;
    }

    public void setJlinks(Map<String, Jlink> jlinks) {
        this.jlinks.clear();
        this.jlinks.putAll(jlinks);
    }

    public Map<String, NativeImage> getNativeImages() {
        return nativeImages;
    }

    public void setNativeImages(Map<String, NativeImage> nativeImages) {
        this.nativeImages.clear();
        this.nativeImages.putAll(nativeImages);
    }
}
