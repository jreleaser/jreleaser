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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class Jlink extends AbstractJavaAssembler {
    private final Set<Artifact> targetJdks = new LinkedHashSet<>();
    private final Set<String> moduleNames = new LinkedHashSet<>();
    private final Set<String> additionalModuleNames = new LinkedHashSet<>();
    private final List<String> args = new ArrayList<>();
    private final Java java = new Java();
    private final Jdeps jdeps = new Jdeps();
    private final Artifact jdk = new Artifact();
    private final Artifact mainJar = new Artifact();
    private final List<Glob> jars = new ArrayList<>();
    private final List<Glob> files = new ArrayList<>();

    private String imageName;
    private String imageNameTransform;
    private String moduleName;
    private Boolean copyJars;

    void setAll(Jlink jlink) {
        super.setAll(jlink);
        this.imageName = jlink.imageName;
        this.imageNameTransform = jlink.imageNameTransform;
        this.moduleName = jlink.moduleName;
        this.copyJars = jlink.copyJars;
        setJava(jlink.java);
        setJdeps(jlink.jdeps);
        setJdk(jlink.jdk);
        setMainJar(jlink.mainJar);
        setTargetJdks(jlink.targetJdks);
        setModuleNames(jlink.moduleNames);
        setAdditionalModuleNames(jlink.additionalModuleNames);
        setArgs(jlink.args);
        setJars(jlink.jars);
        setFiles(jlink.files);
    }

    public Jdeps getJdeps() {
        return jdeps;
    }

    public void setJdeps(Jdeps jdeps) {
        this.jdeps.setAll(jdeps);
    }

    public Artifact getJdk() {
        return jdk;
    }

    public void setJdk(Artifact jdk) {
        this.jdk.setAll(jdk);
    }

    public Artifact getMainJar() {
        return mainJar;
    }

    public void setMainJar(Artifact mainJar) {
        this.mainJar.setAll(mainJar);
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageNameTransform() {
        return imageNameTransform;
    }

    public void setImageNameTransform(String imageNameTransform) {
        this.imageNameTransform = imageNameTransform;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public Set<Artifact> getTargetJdks() {
        return targetJdks;
    }

    public void setTargetJdks(Set<Artifact> targetJdks) {
        this.targetJdks.clear();
        this.targetJdks.addAll(targetJdks);
    }

    public void addTargetJdks(Set<Artifact> targetJdks) {
        this.targetJdks.addAll(targetJdks);
    }

    public void addTargetJdk(Artifact jdk) {
        if (null != jdk) {
            this.targetJdks.add(jdk);
        }
    }

    public Set<String> getModuleNames() {
        return moduleNames;
    }

    public void setModuleNames(Set<String> moduleNames) {
        this.moduleNames.clear();
        this.moduleNames.addAll(moduleNames);
    }

    public void addModuleNames(List<String> moduleNames) {
        this.moduleNames.addAll(moduleNames);
    }

    public void addModuleName(String moduleName) {
        if (isNotBlank(moduleName)) {
            this.moduleNames.add(moduleName.trim());
        }
    }

    public void removeModuleName(String moduleName) {
        if (isNotBlank(moduleName)) {
            this.moduleNames.remove(moduleName.trim());
        }
    }

    public Set<String> getAdditionalModuleNames() {
        return additionalModuleNames;
    }

    public void setAdditionalModuleNames(Set<String> additionalModuleNames) {
        this.additionalModuleNames.clear();
        this.additionalModuleNames.addAll(additionalModuleNames);
    }

    public void addAdditionalModuleNames(List<String> additionalModuleNames) {
        this.additionalModuleNames.addAll(additionalModuleNames);
    }

    public void addAdditionalModuleName(String additionalModuleName) {
        if (isNotBlank(additionalModuleName)) {
            this.additionalModuleNames.add(additionalModuleName.trim());
        }
    }

    public void removeAdditionalModuleName(String additionalModuleName) {
        if (isNotBlank(additionalModuleName)) {
            this.additionalModuleNames.remove(additionalModuleName.trim());
        }
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args.clear();
        this.args.addAll(args);
    }

    public void addArgs(List<String> args) {
        this.args.addAll(args);
    }

    public void addArg(String arg) {
        if (isNotBlank(arg)) {
            this.args.add(arg.trim());
        }
    }

    public void removeArg(String arg) {
        if (isNotBlank(arg)) {
            this.args.remove(arg.trim());
        }
    }

    public Java getJava() {
        return java;
    }

    public void setJava(Java java) {
        this.java.setAll(java);
    }

    public List<Glob> getJars() {
        return jars;
    }

    public void setJars(List<Glob> jars) {
        this.jars.clear();
        this.jars.addAll(jars);
    }

    public void addJars(List<Glob> jars) {
        this.jars.addAll(jars);
    }

    public void addJar(Glob jar) {
        if (null != jar) {
            this.jars.add(jar);
        }
    }

    public List<Glob> getFiles() {
        return files;
    }

    public void setFiles(List<Glob> files) {
        this.files.clear();
        this.files.addAll(files);
    }

    public Boolean isCopyJars() {
        return copyJars == null || copyJars;
    }

    public void setCopyJars(Boolean copyJars) {
        this.copyJars = copyJars;
    }

    public boolean isCopyJarsSet() {
        return copyJars != null;
    }
}
