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
public class Assemble implements Domain, EnabledAware {
    private final Map<String, Jlink> jlink = new LinkedHashMap<>();
    private final Map<String, NativeImage> nativeImage = new LinkedHashMap<>();
    private Boolean enabled;

    void setAll(Assemble assemble) {
        this.enabled = assemble.enabled;
        setJlink(assemble.jlink);
        setNativeImage(assemble.nativeImage);
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
        return jlink.values().stream()
            .filter(Jlink::isEnabled)
            .collect(Collectors.toList());
    }

    public Map<String, Jlink> getJlink() {
        return jlink;
    }

    public void setJlink(Map<String, Jlink> jlink) {
        this.jlink.clear();
        this.jlink.putAll(jlink);
    }

    public void addJlink(Jlink jlink) {
        this.jlink.put(jlink.getName(), jlink);
    }

    public Jlink findJlink(String name) {
        if (isBlank(name)) {
            throw new JReleaserException("Jlink name must not be blank");
        }

        if (jlink.containsKey(name)) {
            return jlink.get(name);
        }

        throw new JReleaserException("Jlink '" + name + "' not found");
    }

    public List<NativeImage> getActiveNativeImages() {
        return nativeImage.values().stream()
            .filter(NativeImage::isEnabled)
            .collect(Collectors.toList());
    }

    public Map<String, NativeImage> getNativeImage() {
        return nativeImage;
    }

    public void setNativeImage(Map<String, NativeImage> nativeImage) {
        this.nativeImage.clear();
        this.nativeImage.putAll(nativeImage);
    }

    public void addNativeImage(NativeImage nativeImage) {
        this.nativeImage.put(nativeImage.getName(), nativeImage);
    }

    public NativeImage findNativeImage(String name) {
        if (isBlank(name)) {
            throw new JReleaserException("NativeImage name must not be blank");
        }

        if (nativeImage.containsKey(name)) {
            return nativeImage.get(name);
        }

        throw new JReleaserException("NativeImage '" + name + "' not found");
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());

        List<Map<String, Object>> jlink = this.jlink.values()
            .stream()
            .filter(d -> full || d.isEnabled())
            .map(d -> d.asMap(full))
            .collect(Collectors.toList());
        if (!jlink.isEmpty()) map.put("jlink", jlink);

        List<Map<String, Object>> nativeImage = this.nativeImage.values()
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
                return (Map<String, A>) jlink;
            case NativeImage.NAME:
                return (Map<String, A>) nativeImage;
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
