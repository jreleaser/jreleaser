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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
abstract class AbstractAssembler implements Assembler {
    protected final Set<Artifact> output = new LinkedHashSet<>();
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    private final Java java = new Java();
    protected String name;
    protected boolean enabled;
    protected Active active;
    protected String executable;
    protected String templateDirectory;
    protected Boolean exported;

    void setAll(AbstractAssembler assembler) {
        this.active = assembler.active;
        this.enabled = assembler.enabled;
        this.exported = assembler.exported;
        this.name = assembler.name;
        this.executable = assembler.executable;
        this.templateDirectory = assembler.templateDirectory;
        setOutputs(assembler.output);
        setJava(assembler.java);
        setExtraProperties(assembler.extraProperties);
    }

    @Override
    public boolean isExported() {
        return exported == null || exported;
    }

    @Override
    public void setExported(boolean exported) {
        this.exported = exported;
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
    public String getExecutable() {
        return executable;
    }

    @Override
    public void setExecutable(String executable) {
        this.executable = executable;
    }

    @Override
    public String getTemplateDirectory() {
        return templateDirectory;
    }

    @Override
    public void setTemplateDirectory(String templateDirectory) {
        this.templateDirectory = templateDirectory;
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
    public Set<Artifact> getOutputs() {
        return output;
    }

    @Override
    public void setOutputs(Set<Artifact> output) {
        this.output.clear();
        this.output.addAll(output);
    }

    @Override
    public void addOutput(Artifact artifact) {
        if (null != artifact) {
            this.output.add(artifact);
        }
    }

    @Override
    public Java getJava() {
        return java;
    }

    @Override
    public void setJava(Java java) {
        this.java.setAll(java);
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
}
