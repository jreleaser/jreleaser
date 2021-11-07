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
package org.jreleaser.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class Jlink extends AbstractJavaAssembler {
    public static final String NAME = "jlink";

    private final Set<Artifact> targetJdks = new LinkedHashSet<>();
    private final Set<String> moduleNames = new LinkedHashSet<>();
    private final List<String> args = new ArrayList<>();
    private final Artifact jdk = new Artifact();
    private final Artifact mainJar = new Artifact();
    private final List<Glob> jars = new ArrayList<>();
    private final List<Glob> files = new ArrayList<>();

    private String imageName;
    private String imageNameTransform;
    private String moduleName;
    private Boolean copyJars;

    public Jlink() {
        super(NAME);
    }

    @Override
    public Distribution.DistributionType getDistributionType() {
        return Distribution.DistributionType.JLINK;
    }

    void setAll(Jlink jlink) {
        super.setAll(jlink);
        this.imageName = jlink.imageName;
        this.imageNameTransform = jlink.imageNameTransform;
        this.moduleName = jlink.moduleName;
        this.copyJars = jlink.copyJars;
        setJdk(jlink.jdk);
        setMainJar(jlink.mainJar);
        setTargetJdks(jlink.targetJdks);
        setModuleNames(jlink.moduleNames);
        setArgs(jlink.args);
        setJars(jlink.jars);
        setFiles(jlink.files);
    }

    public String getResolvedImageName(JReleaserContext context) {
        Map<String, Object> props = context.props();
        props.putAll(props());
        return applyTemplate(imageName, props);
    }

    public String getResolvedImageNameTransform(JReleaserContext context) {
        if (isBlank(imageNameTransform)) return null;
        Map<String, Object> props = context.props();
        props.putAll(props());
        return applyTemplate(imageNameTransform, props);
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
        return Artifact.sortArtifacts(targetJdks);
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

    public void addFiles(List<Glob> files) {
        this.files.addAll(files);
    }

    public void addFile(Glob file) {
        if (null != file) {
            this.files.add(file);
        }
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

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("imageName", imageName);
        props.put("imageNameTransform", imageNameTransform);
        props.put("moduleName", moduleName);
        props.put("moduleNames", moduleNames);
        props.put("args", args);
        Map<String, Map<String, Object>> mappedJdks = new LinkedHashMap<>();
        int i = 0;
        for (Artifact targetJdk : getTargetJdks()) {
            mappedJdks.put("jdk " + (i++), targetJdk.asMap(full));
        }
        props.put("jdk", jdk.asMap(full));
        props.put("targetJdks", mappedJdks);
        Map<String, Map<String, Object>> mappedJars = new LinkedHashMap<>();
        for (i = 0; i < jars.size(); i++) {
            mappedJars.put("glob " + i, jars.get(i).asMap(full));
        }
        props.put("copyJars", isCopyJars());
        props.put("mainJar", mainJar.asMap(full));
        props.put("jars", mappedJars);
        Map<String, Map<String, Object>> mappedFiles = new LinkedHashMap<>();
        for (i = 0; i < files.size(); i++) {
            mappedFiles.put("glob " + i, files.get(i).asMap(full));
        }
        props.put("files", mappedFiles);
    }
}
