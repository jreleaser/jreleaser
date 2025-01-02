/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 The JReleaser authors.
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
import org.jreleaser.model.internal.catalog.swid.SwidTagAware;
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.ArchiveOptions;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.FileSet;
import org.jreleaser.model.internal.common.Glob;
import org.jreleaser.mustache.TemplateContext;
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
public final class NativeImageAssembler extends AbstractJavaAssembler<NativeImageAssembler, org.jreleaser.model.api.assemble.NativeImageAssembler> implements SwidTagAware {
    private static final long serialVersionUID = 3475271658640868162L;

    private final List<String> args = new ArrayList<>();
    private final Set<String> components = new LinkedHashSet<>();
    private final Artifact graal = new Artifact();
    private final Set<Artifact> graalJdks = new LinkedHashSet<>();
    private final Upx upx = new Upx();
    private final Linux linux = new Linux();
    private final Windows windows = new Windows();
    private final Osx osx = new Osx();
    private final ArchiveOptions options = new ArchiveOptions();

    private String imageName;
    private String imageNameTransform;
    private Archive.Format archiveFormat;

    @JsonIgnore
    private final org.jreleaser.model.api.assemble.NativeImageAssembler immutable = new org.jreleaser.model.api.assemble.NativeImageAssembler() {
        private static final long serialVersionUID = -3773732095608192037L;

        private Set<? extends org.jreleaser.model.api.common.Artifact> artifacts;
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
        public org.jreleaser.model.api.common.ArchiveOptions getOptions() {
            return options.asImmutable();
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
            return NativeImageAssembler.this.getExecutable();
        }

        @Override
        public String getTemplateDirectory() {
            return NativeImageAssembler.this.getTemplateDirectory();
        }

        @Override
        public Set<String> getSkipTemplates() {
            return unmodifiableSet(NativeImageAssembler.this.getSkipTemplates());
        }

        @Override
        public org.jreleaser.model.api.common.Java getJava() {
            return NativeImageAssembler.this.getJava().asImmutable();
        }

        @Override
        public org.jreleaser.model.api.common.Artifact getMainJar() {
            return NativeImageAssembler.this.getMainJar().asImmutable();
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.Glob> getJars() {
            if (null == jars) {
                jars = NativeImageAssembler.this.getJars().stream()
                    .map(Glob::asImmutable)
                    .collect(toList());
            }
            return jars;
        }

        @Override
        public Set<? extends org.jreleaser.model.api.common.Artifact> getArtifacts() {
            if (null == artifacts) {
                artifacts = NativeImageAssembler.this.getArtifacts().stream()
                    .map(Artifact::asImmutable)
                    .collect(toSet());
            }
            return artifacts;
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.Glob> getFiles() {
            if (null == files) {
                files = NativeImageAssembler.this.getFiles().stream()
                    .map(Glob::asImmutable)
                    .collect(toList());
            }
            return files;
        }

        @Override
        public org.jreleaser.model.api.platform.Platform getPlatform() {
            return NativeImageAssembler.this.getPlatform().asImmutable();
        }

        @Override
        public org.jreleaser.model.api.catalog.swid.SwidTag getSwid() {
            return NativeImageAssembler.this.getSwid().asImmutable();
        }

        @Override
        public Distribution.DistributionType getDistributionType() {
            return NativeImageAssembler.this.getDistributionType();
        }

        @Override
        public String getType() {
            return NativeImageAssembler.this.getType();
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
            return NativeImageAssembler.this.getName();
        }

        @Override
        public List<? extends org.jreleaser.model.api.common.FileSet> getFileSets() {
            if (null == fileSets) {
                fileSets = NativeImageAssembler.this.getFileSets().stream()
                    .map(FileSet::asImmutable)
                    .collect(toList());
            }
            return fileSets;
        }

        @Override
        public Set<? extends org.jreleaser.model.api.common.Artifact> getOutputs() {
            if (null == outputs) {
                outputs = NativeImageAssembler.this.getOutputs().stream()
                    .map(Artifact::asImmutable)
                    .collect(toSet());
            }
            return outputs;
        }

        @Override
        public Active getActive() {
            return NativeImageAssembler.this.getActive();
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
            return NativeImageAssembler.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(NativeImageAssembler.this.getExtraProperties());
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
        return Distribution.DistributionType.BINARY;
    }

    @Override
    public void merge(NativeImageAssembler source) {
        super.merge(source);
        this.imageName = merge(this.imageName, source.imageName);
        this.imageNameTransform = merge(this.imageNameTransform, source.imageNameTransform);
        this.archiveFormat = merge(this.archiveFormat, source.archiveFormat);
        setOptions(source.options);
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
        TemplateContext props = context.getModel().props();
        props.setAll(props());
        return resolveTemplate(imageName, props);
    }

    public String getResolvedImageNameTransform(JReleaserContext context) {
        if (isBlank(imageNameTransform)) return null;
        TemplateContext props = context.getModel().props();
        props.setAll(props());
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

    public ArchiveOptions getOptions() {
        return options;
    }

    public void setOptions(ArchiveOptions options) {
        this.options.merge(options);
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
        props.put("options", options.asMap(full));
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

    public static final class Upx extends AbstractActivatable<Upx> implements Domain {
        private static final long serialVersionUID = -4962541080085819348L;

        private final List<String> args = new ArrayList<>();
        private String version;

        @JsonIgnore
        private final org.jreleaser.model.api.assemble.NativeImageAssembler.Upx immutable = new org.jreleaser.model.api.assemble.NativeImageAssembler.Upx() {
            private static final long serialVersionUID = 3190807504460186043L;

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
                return Upx.this.getActive();
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
            super.merge(source);
            this.version = this.merge(this.version, source.version);
            setArgs(merge(this.args, source.args));
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
            props.put("active", getActive());
            props.put("version", version);

            return props;
        }
    }

    private abstract static class AbstractPlatformCustomizer<S extends AbstractPlatformCustomizer<S>> extends AbstractModelObject<S> implements PlatformCustomizer {
        private static final long serialVersionUID = 8640931163688760790L;

        private final List<String> args = new ArrayList<>();
        private final String platform;

        protected AbstractPlatformCustomizer(String platform) {
            this.platform = platform;
        }

        @Override
        public void merge(S source) {
            setArgs(merge(this.args, source.getArgs()));
        }

        @Override
        public List<String> getArgs() {
            return args;
        }

        @Override
        public void setArgs(List<String> args) {
            this.args.clear();
            this.args.addAll(args);
        }

        @Override
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
        private static final long serialVersionUID = -7751015791770722168L;

        @JsonIgnore
        private final org.jreleaser.model.api.assemble.NativeImageAssembler.Linux immutable = new org.jreleaser.model.api.assemble.NativeImageAssembler.Linux() {
            private static final long serialVersionUID = -4020602674846221641L;

            @Override
            public String getPlatform() {
                return Linux.this.getPlatform();
            }

            @Override
            public List<String> getArgs() {
                return unmodifiableList(Linux.this.getArgs());
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
        private static final long serialVersionUID = -2310019463850744244L;

        @JsonIgnore
        private final org.jreleaser.model.api.assemble.NativeImageAssembler.Windows immutable = new org.jreleaser.model.api.assemble.NativeImageAssembler.Windows() {
            private static final long serialVersionUID = 1216711581026682524L;

            @Override
            public String getPlatform() {
                return Windows.this.getPlatform();
            }

            @Override
            public List<String> getArgs() {
                return unmodifiableList(Windows.this.getArgs());
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
        private static final long serialVersionUID = 1619426199448547975L;

        @JsonIgnore
        private final org.jreleaser.model.api.assemble.NativeImageAssembler.Osx immutable = new org.jreleaser.model.api.assemble.NativeImageAssembler.Osx() {
            private static final long serialVersionUID = -4484609486153782109L;

            @Override
            public String getPlatform() {
                return Osx.this.getPlatform();
            }

            @Override
            public List<String> getArgs() {
                return unmodifiableList(Osx.this.getArgs());
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
