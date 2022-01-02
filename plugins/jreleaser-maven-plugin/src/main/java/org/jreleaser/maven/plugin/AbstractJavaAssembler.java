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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
abstract class AbstractJavaAssembler extends AbstractAssembler implements JavaAssembler {
    protected final Artifact mainJar = new Artifact();
    protected final List<Glob> jars = new ArrayList<>();
    protected final List<Glob> files = new ArrayList<>();
    protected final Java java = new Java();

    protected String executable;
    protected String templateDirectory;

    void setAll(AbstractJavaAssembler assembler) {
        super.setAll(assembler);
        this.executable = assembler.executable;
        this.templateDirectory = assembler.templateDirectory;
        setJava(assembler.java);
        setMainJar(assembler.mainJar);
        setJars(assembler.jars);
        setFiles(assembler.files);
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
    public Java getJava() {
        return java;
    }

    @Override
    public void setJava(Java java) {
        this.java.setAll(java);
    }

    @Override
    public Artifact getMainJar() {
        return mainJar;
    }

    @Override
    public void setMainJar(Artifact mainJar) {
        this.mainJar.setAll(mainJar);
    }

    @Override
    public List<Glob> getJars() {
        return jars;
    }

    @Override
    public void setJars(List<Glob> jars) {
        this.jars.clear();
        this.jars.addAll(jars);
    }

    @Override
    public List<Glob> getFiles() {
        return files;
    }

    @Override
    public void setFiles(List<Glob> files) {
        this.files.clear();
        this.files.addAll(files);
    }
}
