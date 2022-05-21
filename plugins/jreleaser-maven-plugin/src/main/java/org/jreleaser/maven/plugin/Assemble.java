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
package org.jreleaser.maven.plugin;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class Assemble implements Activatable {
    private final Map<String, Archive> archive = new LinkedHashMap<>();
    private final Map<String, Jlink> jlink = new LinkedHashMap<>();
    private final Map<String, Jpackage> jpackage = new LinkedHashMap<>();
    private final Map<String, NativeImage> nativeImage = new LinkedHashMap<>();
    private Active active;

    void setAll(Assemble assemble) {
        this.active = assemble.active;
        setArchive(assemble.archive);
        setJlink(assemble.jlink);
        setJpackage(assemble.jpackage);
        setNativeImage(assemble.nativeImage);
    }

    @Override
    public Active getActive() {
        return active;
    }

    @Override
    public void setActive(Active active) {
        this.active = active;
    }

    @Override
    public String resolveActive() {
        return active != null ? active.name() : null;
    }

    public Map<String, Archive> getArchive() {
        return archive;
    }

    public void setArchive(Map<String, Archive> archive) {
        this.archive.clear();
        this.archive.putAll(archive);
    }

    public Map<String, Jlink> getJlink() {
        return jlink;
    }

    public void setJlink(Map<String, Jlink> jlink) {
        this.jlink.clear();
        this.jlink.putAll(jlink);
    }

    public Map<String, Jpackage> getJpackage() {
        return jpackage;
    }

    public void setJpackage(Map<String, Jpackage> jpackage) {
        this.jpackage.clear();
        this.jpackage.putAll(jpackage);
    }

    public Map<String, NativeImage> getNativeImage() {
        return nativeImage;
    }

    public void setNativeImage(Map<String, NativeImage> nativeImage) {
        this.nativeImage.clear();
        this.nativeImage.putAll(nativeImage);
    }
}
