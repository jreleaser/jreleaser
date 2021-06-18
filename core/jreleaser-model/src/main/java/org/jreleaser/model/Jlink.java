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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class Jlink extends AbstractAssembler {
    public static final String NAME = "jlink";

    private final Set<Artifact> targetJdks = new TreeSet<>();
    private final Set<String> moduleNames = new LinkedHashSet<>();
    private final List<String> args = new ArrayList<>();
    private final Artifact jdk = new Artifact();
    private final Artifact mainJar = new Artifact();
    private final List<Glob> jars = new ArrayList<>();

    private String imageName;
    private String moduleName;

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
        this.moduleName = jlink.moduleName;
        setJdk(jlink.jdk);
        setMainJar(jlink.mainJar);
        setTargetJdks(jlink.targetJdks);
        setModuleNames(jlink.moduleNames);
        setArgs(jlink.args);
        setJars(jlink.jars);
    }

    public String getResolvedImageName(JReleaserContext context) {
        Map<String, Object> props = context.props();
        props.putAll(props());
        return applyTemplate(imageName, props);
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

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("imageName", imageName);
        props.put("moduleName", moduleName);
        props.put("moduleNames", moduleNames);
        props.put("args", args);
        Map<String, Map<String, Object>> mappedJdks = new LinkedHashMap<>();
        int i = 0;
        for (Artifact targetJdk : targetJdks) {
            mappedJdks.put("jdk " + (i++), targetJdk.asMap(full));
        }
        props.put("jdk", jdk.asMap(full));
        props.put("targetJdks", mappedJdks);
        Map<String, Map<String, Object>> mappedJars = new LinkedHashMap<>();
        for (i = 0; i < jars.size(); i++) {
            mappedJars.put("glob " + i, jars.get(i).asMap(full));
        }
        props.put("jars", mappedJars);
    }
}
