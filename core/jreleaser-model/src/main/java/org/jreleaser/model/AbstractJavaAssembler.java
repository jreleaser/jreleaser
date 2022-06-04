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
package org.jreleaser.model;

import org.jreleaser.util.Constants;
import org.jreleaser.util.SemVer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.jreleaser.util.CollectionUtils.safePut;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
abstract class AbstractJavaAssembler<S extends AbstractJavaAssembler<S>> extends AbstractAssembler<S> implements JavaAssembler {
    protected final Artifact mainJar = new Artifact();
    protected final List<Glob> jars = new ArrayList<>();
    protected final List<Glob> files = new ArrayList<>();
    protected final Java java = new Java();

    protected String executable;
    protected String templateDirectory;

    protected AbstractJavaAssembler(String type) {
        super(type);
    }

    @Override
    public void freeze() {
        super.freeze();
        mainJar.freeze();
        jars.forEach(Glob::freeze);
        files.forEach(Glob::freeze);
        java.freeze();
    }

    @Override
    public void merge(S assembler) {
        freezeCheck();
        super.merge(assembler);
        this.executable = merge(this.executable, assembler.executable);
        this.templateDirectory = merge(this.templateDirectory, assembler.templateDirectory);
        setJava(assembler.java);
        setMainJar(assembler.mainJar);
        setJars(merge(this.jars, assembler.jars));
        setFiles(merge(this.files, assembler.files));
    }

    @Override
    public Map<String, Object> props() {
        Map<String, Object> props = super.props();
        props.put(Constants.KEY_DISTRIBUTION_EXECUTABLE, executable);
        props.putAll(java.getResolvedExtraProperties());
        safePut(Constants.KEY_DISTRIBUTION_JAVA_GROUP_ID, java.getGroupId(), props, true);
        safePut(Constants.KEY_DISTRIBUTION_JAVA_ARTIFACT_ID, java.getArtifactId(), props, true);
        safePut(Constants.KEY_DISTRIBUTION_JAVA_MAIN_CLASS, java.getMainClass(), props, true);
        if (isNotBlank(java.getVersion())) {
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION, java.getVersion());
            SemVer jv = SemVer.of(java.getVersion());
            safePut(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MAJOR, jv.getMajor(), props, true);
            safePut(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MINOR, jv.getMinor(), props, true);
            safePut(Constants.KEY_DISTRIBUTION_JAVA_VERSION_PATCH, jv.getPatch(), props, true);
            safePut(Constants.KEY_DISTRIBUTION_JAVA_VERSION_TAG, jv.getTag(), props, true);
            safePut(Constants.KEY_DISTRIBUTION_JAVA_VERSION_BUILD, jv.getBuild(), props, true);
        } else {
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MAJOR, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MINOR, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_PATCH, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_TAG, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_BUILD, "");
        }
        return props;
    }

    @Override
    public String getExecutable() {
        return executable;
    }

    @Override
    public void setExecutable(String executable) {
        freezeCheck();
        this.executable = executable;
    }

    @Override
    public String getTemplateDirectory() {
        return templateDirectory;
    }

    @Override
    public void setTemplateDirectory(String templateDirectory) {
        freezeCheck();
        this.templateDirectory = templateDirectory;
    }

    @Override
    public Java getJava() {
        return java;
    }

    @Override
    public void setJava(Java java) {
        this.java.merge(java);
    }

    @Override
    public Artifact getMainJar() {
        return mainJar;
    }

    @Override
    public void setMainJar(Artifact mainJar) {
        this.mainJar.merge(mainJar);
    }

    @Override
    public List<Glob> getJars() {
        return freezeWrap(jars);
    }

    @Override
    public void setJars(List<Glob> jars) {
        freezeCheck();
        this.jars.clear();
        this.jars.addAll(jars);
    }

    @Override
    public void addJars(List<Glob> jars) {
        freezeCheck();
        this.jars.addAll(jars);
    }

    @Override
    public void addJar(Glob jar) {
        freezeCheck();
        if (null != jar) {
            this.jars.add(jar);
        }
    }

    @Override
    public List<Glob> getFiles() {
        return freezeWrap(files);
    }

    @Override
    public void setFiles(List<Glob> files) {
        freezeCheck();
        this.files.clear();
        this.files.addAll(files);
    }

    @Override
    public void addFiles(List<Glob> files) {
        freezeCheck();
        this.files.addAll(files);
    }

    @Override
    public void addFile(Glob file) {
        freezeCheck();
        if (null != file) {
            this.files.add(file);
        }
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("executable", executable);
        props.put("templateDirectory", templateDirectory);
        props.put("mainJar", mainJar.asMap(full));
        Map<String, Map<String, Object>> mappedJars = new LinkedHashMap<>();
        for (int i = 0; i < jars.size(); i++) {
            mappedJars.put("glob " + i, jars.get(i).asMap(full));
        }
        props.put("jars", mappedJars);
        Map<String, Map<String, Object>> mappedFiles = new LinkedHashMap<>();
        for (int i = 0; i < files.size(); i++) {
            mappedFiles.put("glob " + i, files.get(i).asMap(full));
        }
        props.put("files", mappedFiles);
        props.put("extraProperties", getResolvedExtraProperties());
        if (java.isEnabled()) {
            props.put("java", java.asMap(full));
        }
    }
}
