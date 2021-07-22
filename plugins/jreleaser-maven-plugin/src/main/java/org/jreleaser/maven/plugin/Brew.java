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
package org.jreleaser.maven.plugin;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Brew extends AbstractRepositoryTool {
    @Parameter(property = "dependencies")
    private final List<Dependency> dependencies = new ArrayList<>();
    private final Tap tap = new Tap();
    private final List<String> livecheck = new ArrayList<>();
    private final Cask cask = new Cask();

    private String formulaName;
    private Boolean multiPlatform;

    void setAll(Brew brew) {
        super.setAll(brew);
        this.formulaName = brew.formulaName;
        this.multiPlatform = brew.multiPlatform;
        setTap(brew.tap);
        setDependencies(brew.dependencies);
        setLivecheck(brew.livecheck);
        setCask(brew.cask);
    }

    public String getFormulaName() {
        return formulaName;
    }

    public void setFormulaName(String formulaName) {
        this.formulaName = formulaName;
    }

    public boolean isMultiPlatform() {
        return multiPlatform != null && multiPlatform;
    }

    public void setMultiPlatform(Boolean multiPlatform) {
        this.multiPlatform = multiPlatform;
    }

    public boolean isMultiPlatformSet() {
        return multiPlatform != null;
    }

    public Tap getTap() {
        return tap;
    }

    public void setTap(Tap tap) {
        this.tap.setAll(tap);
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies.clear();
        this.dependencies.addAll(dependencies);
    }

    public Cask getCask() {
        return cask;
    }

    public void setCask(Cask cask) {
        this.cask.setAll(cask);
    }

    public List<String> getLivecheck() {
        return livecheck;
    }

    public void setLivecheck(List<String> livecheck) {
        this.livecheck.clear();
        this.livecheck.addAll(livecheck);
    }

    @Override
    public boolean isSet() {
        return super.isSet() ||
            !dependencies.isEmpty() ||
            tap.isSet() ||
            !livecheck.isEmpty() ||
            multiPlatform != null ||
            cask.isSet();
    }
}
