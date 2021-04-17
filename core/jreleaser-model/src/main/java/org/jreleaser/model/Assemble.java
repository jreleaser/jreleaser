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
package org.jreleaser.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class Assemble implements Domain, EnabledProvider {
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

    public List<Jlink> getActiveJlinks() {
        return jlinks.values().stream()
            .filter(Jlink::isEnabled)
            .collect(Collectors.toList());
    }

    public Map<String, Jlink> getJlinks() {
        return jlinks;
    }

    public void setJlinks(Map<String, Jlink> jlinks) {
        this.jlinks.clear();
        this.jlinks.putAll(jlinks);
    }

    public void addJlink(Jlink jlink) {
        this.jlinks.put(jlink.getName(), jlink);
    }

    public Jlink findJlink(String name) {
        if (isBlank(name)) {
            throw new JReleaserException("Jlink name must not be blank");
        }

        if (jlinks.containsKey(name)) {
            return jlinks.get(name);
        }

        throw new JReleaserException("Jlink '" + name + "' not found");
    }

    public List<NativeImage> getActiveNativeImages() {
        return nativeImages.values().stream()
            .filter(NativeImage::isEnabled)
            .collect(Collectors.toList());
    }

    public Map<String, NativeImage> getNativeImages() {
        return nativeImages;
    }

    public void setNativeImages(Map<String, NativeImage> nativeImages) {
        this.nativeImages.clear();
        this.nativeImages.putAll(nativeImages);
    }

    public void addNativeImage(NativeImage nativeImage) {
        this.nativeImages.put(nativeImage.getName(), nativeImage);
    }

    public NativeImage findNativeImage(String name) {
        if (isBlank(name)) {
            throw new JReleaserException("NativeImage name must not be blank");
        }

        if (nativeImages.containsKey(name)) {
            return nativeImages.get(name);
        }

        throw new JReleaserException("NativeImage '" + name + "' not found");
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());

        List<Map<String, Object>> jlink = this.jlinks.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(Collectors.toList());
        if (!jlink.isEmpty()) map.put("jlink", jlink);

        List<Map<String, Object>> nativeImage = this.nativeImages.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(Collectors.toList());
        if (!nativeImage.isEmpty()) map.put("nativeImage", nativeImage);

        return map;
    }

    public <A extends Assembler> Map<String, A> findAssemblersByType(String assemblerName) {
        switch (assemblerName) {
            case Jlink.NAME:
                return (Map<String, A>) jlinks;
            case NativeImage.NAME:
                return (Map<String, A>) nativeImages;
        }

        return Collections.emptyMap();
    }

    public <A extends Assembler> Collection<A> findAllAssemblers() {
        List<A> assemblers = new ArrayList<>();
        assemblers.addAll((List<A>) getActiveJlinks());
        assemblers.addAll((List<A>) getActiveNativeImages());
        return assemblers;
    }
}
