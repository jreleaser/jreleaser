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
package org.jreleaser.model.internal.assemble;

import org.jreleaser.model.Active;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.EnabledAware;
import org.jreleaser.model.internal.common.FileSet;
import org.jreleaser.model.internal.common.Glob;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.jreleaser.model.api.assemble.JlinkAssembler.TYPE;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public final class JlinkAssembler extends AbstractJavaAssembler<JlinkAssembler, org.jreleaser.model.api.assemble.JlinkAssembler> {
    private static final long serialVersionUID = -4001565972495119198L;

    private final Set<Artifact> targetJdks = new LinkedHashSet<>();
    private final Set<String> moduleNames = new LinkedHashSet<>();
    private final Set<String> additionalModuleNames = new LinkedHashSet<>();
    private final List<String> args = new ArrayList<>();
    private final Artifact jdk = new Artifact();
    private final Jdeps jdeps = new Jdeps();

    private String imageName;
    private String imageNameTransform;
    private Boolean copyJars;

    private final org.jreleaser.model.api.assemble.JlinkAssembler immutable = new org.jreleaser.model.api.assemble.JlinkAssembler() {
        private static final long serialVersionUID = -1953894547371342764L;

        private List<? extends org.jreleaser.model.api.common.FileSet> fileSets;
        private Set<? extends org.jreleaser.model.api.common.Artifact> outputs;
        private List<? extends org.jreleaser.model.api.common.Glob> jars;
        private List<? extends org.jreleaser.model.api.common.Glob> files;
        private Set<? extends org.jreleaser.model.api.common.Artifact> targetJdks;

        @Override
        public Jdeps getJdeps() {
            return jdeps.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.common.Artifact getJdk() {
            return jdk.asImmutable();
        }

        @Override
        public String getImageName() {
            return imageName;
        }

        @Override
        public String getImageNameTransform() {
            return imageNameTransform;
        }

        @Override
        public Set<? extends org.jreleaser.model.api.common.Artifact> getTargetJdks() {
            if (null == targetJdks) {
                targetJdks = JlinkAssembler.this.targetJdks.stream()
                    .map(Artifact::asImmutable)
                    .collect(toSet());
            }
            return targetJdks;
        }

        @Override
        public Set<String> getModuleNames() {
            return unmodifiableSet(moduleNames);
        }

        @Override
        public List<String> getArgs() {
            return unmodifiableList(args);
        }

        @Override
        public boolean isCopyJars() {
            return JlinkAssembler.this.isCopyJars();
        }

        @Override
        public String getExecutable() {
            return executable;
        }

        @Override
        public String getTemplateDirectory() {
            return templateDirectory;
        }

        @Override
        public org.jreleaser.model.api.common.Java getJava() {
            return java.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.common.Artifact getMainJar() {
            return mainJar.asImmutable();
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.Glob> getJars() {
            if (null == jars) {
                jars = JlinkAssembler.this.jars.stream()
                    .map(Glob::asImmutable)
                    .collect(toList());
            }
            return jars;
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.Glob> getFiles() {
            if (null == files) {
                files = JlinkAssembler.this.files.stream()
                    .map(Glob::asImmutable)
                    .collect(toList());
            }
            return files;
        }

        @Override
        public org.jreleaser.model.api.platform.Platform getPlatform() {
            return platform.asImmutable();
        }

        @Override
        public Distribution.DistributionType getDistributionType() {
            return JlinkAssembler.this.getDistributionType();
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public Stereotype getStereotype() {
            return JlinkAssembler.this.getStereotype();
        }

        @Override
        public boolean isExported() {
            return JlinkAssembler.this.isExported();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.FileSet> getFileSets() {
            if (null == fileSets) {
                fileSets = JlinkAssembler.this.fileSets.stream()
                    .map(FileSet::asImmutable)
                    .collect(toList());
            }
            return fileSets;
        }

        @Override
        public Set<? extends org.jreleaser.model.api.common.Artifact> getOutputs() {
            if (null == outputs) {
                outputs = JlinkAssembler.this.outputs.stream()
                    .map(Artifact::asImmutable)
                    .collect(toSet());
            }
            return outputs;
        }

        @Override
        public Active getActive() {
            return active;
        }

        @Override
        public boolean isEnabled() {
            return JlinkAssembler.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(JlinkAssembler.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return JlinkAssembler.this.getPrefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(extraProperties);
        }
    };

    public JlinkAssembler() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.assemble.JlinkAssembler asImmutable() {
        return immutable;
    }

    @Override
    public Distribution.DistributionType getDistributionType() {
        return Distribution.DistributionType.JLINK;
    }

    @Override
    public void merge(JlinkAssembler source) {
        super.merge(source);
        this.imageName = merge(this.imageName, source.imageName);
        this.imageNameTransform = merge(this.imageNameTransform, source.imageNameTransform);
        this.copyJars = merge(this.copyJars, source.copyJars);
        setJdeps(source.jdeps);
        setJdk(source.jdk);
        setTargetJdks(merge(this.targetJdks, source.targetJdks));
        setModuleNames(merge(this.moduleNames, source.moduleNames));
        setAdditionalModuleNames(merge(this.additionalModuleNames, source.additionalModuleNames));
        setArgs(merge(this.args, source.args));
    }

    public String getResolvedImageName(JReleaserContext context) {
        Map<String, Object> props = context.getModel().props();
        props.putAll(props());
        return resolveTemplate(imageName, props);
    }

    public String getResolvedImageNameTransform(JReleaserContext context) {
        if (isBlank(imageNameTransform)) return null;
        Map<String, Object> props = context.getModel().props();
        props.putAll(props());
        return resolveTemplate(imageNameTransform, props);
    }

    public Jdeps getJdeps() {
        return jdeps;
    }

    public void setJdeps(Jdeps jdeps) {
        this.jdeps.merge(jdeps);
    }

    public Artifact getJdk() {
        return jdk;
    }

    public void setJdk(Artifact jdk) {
        this.jdk.merge(jdk);
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
        return Artifact.sortArtifacts(targetJdks);
    }

    public void setTargetJdks(Set<Artifact> targetJdks) {
        this.targetJdks.clear();
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

    public boolean isCopyJars() {
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
        props.put("moduleNames", moduleNames);
        props.put("additionalModuleNames", additionalModuleNames);
        props.put("args", args);
        props.put("jdeps", jdeps.asMap(full));
        Map<String, Map<String, Object>> mappedJdks = new LinkedHashMap<>();
        int i = 0;
        for (Artifact targetJdk : getTargetJdks()) {
            mappedJdks.put("jdk " + (i++), targetJdk.asMap(full));
        }
        props.put("jdk", jdk.asMap(full));
        props.put("targetJdks", mappedJdks);
        props.put("copyJars", isCopyJars());
    }

    public static final class Jdeps extends AbstractModelObject<Jdeps> implements Domain, EnabledAware {
        private static final long serialVersionUID = 2752412877591717403L;

        private final Set<String> targets = new LinkedHashSet<>();
        private String multiRelease;
        private Boolean ignoreMissingDeps;
        private Boolean useWildcardInPath;
        private Boolean enabled;

        private final org.jreleaser.model.api.assemble.JlinkAssembler.Jdeps immutable = new org.jreleaser.model.api.assemble.JlinkAssembler.Jdeps() {
            private static final long serialVersionUID = -6727485936574423582L;

            @Override
            public String getMultiRelease() {
                return multiRelease;
            }

            @Override
            public boolean isIgnoreMissingDeps() {
                return Jdeps.this.isIgnoreMissingDeps();
            }

            @Override
            public boolean isUseWildcardInPath() {
                return Jdeps.this.isUseWildcardInPath();
            }

            @Override
            public Set<String> getTargets() {
                return unmodifiableSet(targets);
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Jdeps.this.asMap(full));
            }

            @Override
            public boolean isEnabled() {
                return Jdeps.this.isEnabled();
            }
        };

        public org.jreleaser.model.api.assemble.JlinkAssembler.Jdeps asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Jdeps source) {
            this.multiRelease = this.merge(this.multiRelease, source.multiRelease);
            this.ignoreMissingDeps = this.merge(this.ignoreMissingDeps, source.ignoreMissingDeps);
            this.useWildcardInPath = this.merge(this.useWildcardInPath, source.useWildcardInPath);
            this.enabled = merge(this.enabled, source.enabled);
            setTargets(merge(this.targets, source.targets));
        }

        public String getMultiRelease() {
            return multiRelease;
        }

        public void setMultiRelease(String multiRelease) {
            this.multiRelease = multiRelease;
        }

        public boolean isIgnoreMissingDeps() {
            return ignoreMissingDeps != null && ignoreMissingDeps;
        }

        public void setIgnoreMissingDeps(Boolean ignoreMissingDeps) {
            this.ignoreMissingDeps = ignoreMissingDeps;
        }

        public boolean isIgnoreMissingDepsSet() {
            return ignoreMissingDeps != null;
        }

        public boolean isUseWildcardInPath() {
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

        @Override
        public boolean isEnabled() {
            return enabled != null && enabled;
        }

        @Override
        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public boolean isEnabledSet() {
            return enabled != null;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("enabled", isEnabled());
            props.put("multiRelease", multiRelease);
            props.put("ignoreMissingDeps", isIgnoreMissingDeps());
            props.put("useWildcardInPath", isUseWildcardInPath());
            props.put("targets", targets);
            return props;
        }
    }
}
