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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.Archive;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.Stereotype;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Activatable;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.FileSet;
import org.jreleaser.model.internal.common.Glob;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.util.PlatformUtils;

import java.util.ArrayList;
import java.util.Collections;
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
import static org.jreleaser.model.api.assemble.NativeImageAssembler.TYPE;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public final class NativeImageAssembler extends AbstractJavaAssembler<NativeImageAssembler, org.jreleaser.model.api.assemble.NativeImageAssembler> {
    private final List<String> args = new ArrayList<>();
    private final Set<String> components = new LinkedHashSet<>();
    private final Artifact graal = new Artifact();
    private final Set<Artifact> graalJdks = new LinkedHashSet<>();
    private final Upx upx = new Upx();
    private final Linux linux = new Linux();
    private final Windows windows = new Windows();
    private final Osx osx = new Osx();

    private String imageName;
    private String imageNameTransform;
    private Archive.Format archiveFormat;

    private final org.jreleaser.model.api.assemble.NativeImageAssembler immutable = new org.jreleaser.model.api.assemble.NativeImageAssembler() {
        private Set<? extends org.jreleaser.model.api.common.Artifact> graalJdks;
        private List<? extends org.jreleaser.model.api.common.FileSet> fileSets;
        private Set<? extends org.jreleaser.model.api.common.Artifact> outputs;
        private List<? extends org.jreleaser.model.api.common.Glob> jars;
        private List<? extends org.jreleaser.model.api.common.Glob> files;

        @Override
        public String getImageName() {
            return imageName;
        }

        @Override
        public String getImageNameTransform() {
            return imageNameTransform;
        }

        @Override
        public Archive.Format getArchiveFormat() {
            return archiveFormat;
        }

        @Override
        public org.jreleaser.model.api.common.Artifact getGraal() {
            return graal.asImmutable();
        }

        @Override
        public Set<? extends org.jreleaser.model.api.common.Artifact> getGraalJdks() {
            if (null == graalJdks) {
                graalJdks = NativeImageAssembler.this.graalJdks.stream()
                    .map(Artifact::asImmutable)
                    .collect(toSet());
            }
            return graalJdks;
        }

        @Override
        public List<String> getArgs() {
            return unmodifiableList(args);
        }

        @Override
        public Set<String> getComponents() {
            return unmodifiableSet(components);
        }

        @Override
        public Upx getUpx() {
            return upx.asImmutable();
        }

        @Override
        public Linux getLinux() {
            return linux.asImmutable();
        }

        @Override
        public Windows getWindows() {
            return windows.asImmutable();
        }

        @Override
        public Osx getOsx() {
            return osx.asImmutable();
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
                jars = NativeImageAssembler.this.jars.stream()
                    .map(Glob::asImmutable)
                    .collect(toList());
            }
            return jars;
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.Glob> getFiles() {
            if (null == files) {
                files = NativeImageAssembler.this.files.stream()
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
            return NativeImageAssembler.this.getDistributionType();
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public Stereotype getStereotype() {
            return NativeImageAssembler.this.getStereotype();
        }

        @Override
        public boolean isExported() {
            return NativeImageAssembler.this.isExported();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.FileSet> getFileSets() {
            if (null == fileSets) {
                fileSets = NativeImageAssembler.this.fileSets.stream()
                    .map(FileSet::asImmutable)
                    .collect(toList());
            }
            return fileSets;
        }

        @Override
        public Set<? extends org.jreleaser.model.api.common.Artifact> getOutputs() {
            if (null == outputs) {
                outputs = NativeImageAssembler.this.outputs.stream()
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
            return NativeImageAssembler.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(NativeImageAssembler.this.asMap(full));
        }

        @Override
        public String getPrefix() {
            return NativeImageAssembler.this.getPrefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(extraProperties);
        }
    };

    public NativeImageAssembler() {
        super(TYPE);
    }

    @Override
    public org.jreleaser.model.api.assemble.NativeImageAssembler asImmutable() {
        return immutable;
    }

    @Override
    public Distribution.DistributionType getDistributionType() {
        return Distribution.DistributionType.NATIVE_IMAGE;
    }

    @Override
    public void merge(NativeImageAssembler source) {
        super.merge(source);
        this.imageName = merge(this.imageName, source.imageName);
        this.imageNameTransform = merge(this.imageNameTransform, source.imageNameTransform);
        this.archiveFormat = merge(this.archiveFormat, source.archiveFormat);
        setGraal(source.graal);
        setGraalJdks(merge(this.graalJdks, source.graalJdks));
        setArgs(merge(this.args, source.args));
        setComponents(merge(this.components, source.components));
        setUpx(source.upx);
        setLinux(source.linux);
        setWindows(source.windows);
        setOsx(source.osx);
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

    public PlatformCustomizer getResolvedPlatformCustomizer() {
        String currentPlatform = PlatformUtils.getCurrentFull();
        if (PlatformUtils.isMac(currentPlatform)) {
            return getOsx();
        } else if (PlatformUtils.isWindows(currentPlatform)) {
            return getWindows();
        }
        return getLinux();
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

    public Archive.Format getArchiveFormat() {
        return archiveFormat;
    }

    public void setArchiveFormat(Archive.Format archiveFormat) {
        this.archiveFormat = archiveFormat;
    }

    public void setArchiveFormat(String archiveFormat) {
        this.archiveFormat = Archive.Format.of(archiveFormat);
    }

    public Artifact getGraal() {
        return graal;
    }

    public void setGraal(Artifact graal) {
        this.graal.merge(graal);
    }

    public Set<Artifact> getGraalJdks() {
        return Artifact.sortArtifacts(graalJdks);
    }

    public void setGraalJdks(Set<Artifact> graalJdks) {
        this.graalJdks.clear();
        this.graalJdks.addAll(graalJdks);
    }

    public void addGraalJdks(Set<Artifact> graalJdks) {
        this.graalJdks.addAll(graalJdks);
    }

    public void addGraalJdk(Artifact jdk) {
        if (null != jdk) {
            this.graalJdks.add(jdk);
        }
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args.clear();
        this.args.addAll(args);
    }

    public Set<String> getComponents() {
        return components;
    }

    public void setComponents(Set<String> components) {
        this.components.clear();
        this.components.addAll(components);
    }

    public Upx getUpx() {
        return upx;
    }

    public void setUpx(Upx upx) {
        this.upx.merge(upx);
    }

    public Linux getLinux() {
        return linux;
    }

    public void setLinux(Linux linux) {
        this.linux.merge(linux);
    }

    public Windows getWindows() {
        return windows;
    }

    public void setWindows(Windows windows) {
        this.windows.merge(windows);
    }

    public Osx getOsx() {
        return osx;
    }

    public void setOsx(Osx osx) {
        this.osx.merge(osx);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("imageName", imageName);
        props.put("imageNameTransform", imageNameTransform);
        props.put("archiveFormat", archiveFormat);
        Map<String, Map<String, Object>> mappedJdks = new LinkedHashMap<>();
        int i = 0;
        for (Artifact graalJdk : getGraalJdks()) {
            mappedJdks.put("jdk " + (i++), graalJdk.asMap(full));
        }
        props.put("graal", graal.asMap(full));
        props.put("graalJdks", mappedJdks);
        props.put("args", args);
        props.put("components", components);
        props.put("upx", upx.asMap(full));
        props.putAll(linux.asMap(full));
        props.putAll(osx.asMap(full));
        props.putAll(windows.asMap(full));
    }

    public interface PlatformCustomizer extends Domain {
        String getPlatform();

        List<String> getArgs();

        void setArgs(List<String> args);
    }

    public static final class Upx extends AbstractModelObject<Upx> implements Domain, Activatable {
        private final List<String> args = new ArrayList<>();

        @JsonIgnore
        private boolean enabled;
        private Active active;
        private String version;

        private final org.jreleaser.model.api.assemble.NativeImageAssembler.Upx immutable = new org.jreleaser.model.api.assemble.NativeImageAssembler.Upx() {
            @Override
            public String getVersion() {
                return version;
            }

            @Override
            public List<String> getArgs() {
                return unmodifiableList(args);
            }

            @Override
            public Active getActive() {
                return active;
            }

            @Override
            public boolean isEnabled() {
                return Upx.this.isEnabled();
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Upx.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.assemble.NativeImageAssembler.Upx asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Upx source) {
            this.active = this.merge(this.active, source.active);
            this.enabled = this.merge(this.enabled, source.enabled);
            this.version = this.merge(this.version, source.version);
            setArgs(merge(this.args, source.args));
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        public void disable() {
            active = Active.NEVER;
            enabled = false;
        }

        public boolean resolveEnabled(Project project) {
            if (null == active) {
                active = Active.NEVER;
            }
            enabled = active.check(project);
            return enabled;
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
        public void setActive(String str) {
            setActive(Active.of(str));
        }

        @Override
        public boolean isActiveSet() {
            return active != null;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public List<String> getArgs() {
            return args;
        }

        public void setArgs(List<String> args) {
            this.args.clear();
            this.args.addAll(args);
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            if (!full && !isEnabled()) return Collections.emptyMap();

            Map<String, Object> props = new LinkedHashMap<>();
            props.put("enabled", isEnabled());
            props.put("active", active);
            props.put("version", version);

            return props;
        }
    }

    private static abstract class AbstractPlatformCustomizer<S extends AbstractPlatformCustomizer<S>> extends AbstractModelObject<S> implements PlatformCustomizer {
        protected final List<String> args = new ArrayList<>();
        protected final String platform;

        protected AbstractPlatformCustomizer(String platform) {
            this.platform = platform;
        }

        @Override
        public void merge(S source) {
            setArgs(merge(this.args, source.args));
        }

        public List<String> getArgs() {
            return args;
        }

        @Override
        public void setArgs(List<String> args) {
            this.args.clear();
            this.args.addAll(args);
        }

        public String getPlatform() {
            return platform;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("args", args);

            Map<String, Object> map = new LinkedHashMap<>();
            map.put(platform, props);
            return map;
        }
    }

    public static final class Linux extends NativeImageAssembler.AbstractPlatformCustomizer<Linux> {
        private final org.jreleaser.model.api.assemble.NativeImageAssembler.Linux immutable = new org.jreleaser.model.api.assemble.NativeImageAssembler.Linux() {
            @Override
            public String getPlatform() {
                return platform;
            }

            @Override
            public List<String> getArgs() {
                return unmodifiableList(args);
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Linux.this.asMap(full));
            }
        };

        public Linux() {
            super("linux");
        }

        public org.jreleaser.model.api.assemble.NativeImageAssembler.Linux asImmutable() {
            return immutable;
        }
    }

    public static final class Windows extends NativeImageAssembler.AbstractPlatformCustomizer<Windows> {
        private final org.jreleaser.model.api.assemble.NativeImageAssembler.Windows immutable = new org.jreleaser.model.api.assemble.NativeImageAssembler.Windows() {
            @Override
            public String getPlatform() {
                return platform;
            }

            @Override
            public List<String> getArgs() {
                return unmodifiableList(args);
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Windows.this.asMap(full));
            }
        };

        public Windows() {
            super("windows");
        }

        public org.jreleaser.model.api.assemble.NativeImageAssembler.Windows asImmutable() {
            return immutable;
        }
    }

    public static final class Osx extends NativeImageAssembler.AbstractPlatformCustomizer<Osx> {
        private final org.jreleaser.model.api.assemble.NativeImageAssembler.Osx immutable = new org.jreleaser.model.api.assemble.NativeImageAssembler.Osx() {
            @Override
            public String getPlatform() {
                return platform;
            }

            @Override
            public List<String> getArgs() {
                return unmodifiableList(args);
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Osx.this.asMap(full));
            }
        };

        public Osx() {
            super("osx");
        }

        public org.jreleaser.model.api.assemble.NativeImageAssembler.Osx asImmutable() {
            return immutable;
        }
    }
}
