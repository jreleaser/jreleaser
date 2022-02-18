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
 * @since 1.1.0
 */
abstract class AbstractDownloader implements Downloader {
    protected final String type;
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    private final Unpack unpack = new Unpack();

    protected String name;

    protected boolean enabled;
    protected Active active;
    private int connectTimeout;
    private int readTimeout;

    protected AbstractDownloader(String type) {
        this.type = type;
    }

    void setAll(AbstractDownloader downloader) {
        this.name = downloader.name;
        this.active = downloader.active;
        this.enabled = downloader.enabled;
        this.connectTimeout = downloader.connectTimeout;
        this.readTimeout = downloader.readTimeout;
        setExtraProperties(downloader.extraProperties);
        setUnpack(downloader.unpack);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
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

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public int getReadTimeout() {
        return readTimeout;
    }

    @Override
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    @Override
    public Map<String, Object> getExtraProperties() {
        return extraProperties;
    }

    @Override
    public void setExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.clear();
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public Unpack getUnpack() {
        return unpack;
    }

    @Override
    public void setUnpack(Unpack unpack) {
        this.unpack.setAll(unpack);
    }
}
