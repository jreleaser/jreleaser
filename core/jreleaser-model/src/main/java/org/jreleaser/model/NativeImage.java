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
import java.util.List;
import java.util.Map;

import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class NativeImage extends AbstractAssembler {
    public static final String NAME = "native-image";

    private final List<String> args = new ArrayList<>();
    private final Artifact graal = new Artifact();
    private final Artifact mainJar = new Artifact();
    private final List<Glob> jars = new ArrayList<>();
    private final List<Glob> files = new ArrayList<>();

    private String imageName;

    public NativeImage() {
        super(NAME);
    }

    @Override
    public Distribution.DistributionType getDistributionType() {
        return Distribution.DistributionType.JLINK;
    }

    void setAll(NativeImage nativeImage) {
        super.setAll(nativeImage);
        this.imageName = nativeImage.imageName;
        setGraal(nativeImage.graal);
        setMainJar(nativeImage.mainJar);
        setArgs(nativeImage.args);
        setJars(nativeImage.jars);
        setFiles(nativeImage.files);
    }

    public String getResolvedImageName(JReleaserContext context) {
        Map<String, Object> props = context.props();
        props.putAll(props());
        return applyTemplate(imageName, props);
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Artifact getGraal() {
        return graal;
    }

    public void setGraal(Artifact graal) {
        this.graal.setAll(graal);
    }

    public Artifact getMainJar() {
        return mainJar;
    }

    public void setMainJar(Artifact mainJar) {
        this.mainJar.setAll(mainJar);
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

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("imageName", imageName);
        props.put("graal", graal.asMap(full));
        props.put("args", args);
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
    }
}
