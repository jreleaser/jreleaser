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

import org.apache.maven.plugins.annotations.Parameter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Brew extends AbstractTool {
    public static final String NAME = "brew";
    @Parameter(property = "dependencies")
    private final Properties dependencies = new Properties();
    private Tap tap = new Tap();

    public Brew() {
        super(NAME);
    }

    void setAll(Brew brew) {
        super.setAll(brew);
        this.tap.setAll(brew.tap);
        setDependencies(brew.dependencies);
    }

    public Tap getTap() {
        return tap;
    }

    public void setTap(Tap tap) {
        this.tap = tap;
    }

    public Properties getDependencies() {
        return dependencies;
    }

    public void setDependencies(Properties dependencies) {
        this.dependencies.clear();
        this.dependencies.putAll(dependencies);
    }

    @Override
    public boolean isSet() {
        return super.isSet() ||
            !dependencies.isEmpty() ||
            tap.isSet();
    }
}
