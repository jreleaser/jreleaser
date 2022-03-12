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

    private String imageName;
    private String imageNameTransform;
    private Boolean copyJars;

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

    public Set<Artifact> getTargetJdks() {
        return targetJdks;
    }

    public void setTargetJdks(Set<Artifact> targetJdks) {
        this.targetJdks.clear();
        this.targetJdks.addAll(targetJdks);
    }

    public Set<String> getModuleNames() {
        return moduleNames;
    }

    public void setModuleNames(Set<String> moduleNames) {
        this.moduleNames.clear();
        this.moduleNames.addAll(moduleNames);
    }

    public Set<String> getAdditionalModuleNames() {
        return additionalModuleNames;
    }

    public void setAdditionalModuleNames(Set<String> additionalModuleNames) {
        this.additionalModuleNames.clear();
        this.additionalModuleNames.addAll(additionalModuleNames);
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args.clear();
        this.args.addAll(args);
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

    public Boolean isCopyJars() {
        return copyJars == null || copyJars;
    }

    public void setCopyJars(Boolean copyJars) {
        this.copyJars = copyJars;
    }

    public boolean isCopyJarsSet() {
        return copyJars != null;
    }

    public static class Jdeps {
        private final Set<String> targets = new LinkedHashSet<>();
        private String multiRelease;
        private Boolean ignoreMissingDeps;
        private Boolean useWildcardInPath;

        void setAll(Jdeps jdeps) {
            this.multiRelease = jdeps.multiRelease;
            this.ignoreMissingDeps = jdeps.ignoreMissingDeps;
            this.useWildcardInPath = jdeps.useWildcardInPath;
            setTargets(jdeps.targets);
        }

        public String getMultiRelease() {
            return multiRelease;
        }

        public void setMultiRelease(String multiRelease) {
            this.multiRelease = multiRelease;
        }

        public Boolean isIgnoreMissingDeps() {
            return ignoreMissingDeps != null && ignoreMissingDeps;
        }

        public void setIgnoreMissingDeps(Boolean ignoreMissingDeps) {
            this.ignoreMissingDeps = ignoreMissingDeps;
        }

        public boolean isIgnoreMissingDepsSet() {
            return ignoreMissingDeps != null;
        }

        public Boolean isUseWildcardInPath() {
            return useWildcardInPath == null || useWildcardInPath;
        }

        public void setUseWildcardInPath(Boolean useWildcardInPath) {
            this.useWildcardInPath = useWildcardInPath;
        }

        public boolean isUseWildcardInPathSet() {
            return useWildcardInPath != null;
        }

        public Set<String> getTargets() {
            return targets;
        }

        public void setTargets(Set<String> targets) {
            this.targets.clear();
            this.targets.addAll(targets);
        }

        public void addTargets(List<String> targets) {
            this.targets.addAll(targets);
        }

        public void addTarget(String target) {
            if (isNotBlank(target)) {
                this.targets.add(target.trim());
            }
        }

        public void removeTarget(String target) {
            if (isNotBlank(target)) {
                this.targets.remove(target.trim());
            }
        }
    }
}
