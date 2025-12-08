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
import org.jreleaser.model.internal.common.Matrix;
import org.jreleaser.model.internal.common.MatrixAware;
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
import static org.jreleaser.model.JReleaserOutput.nag;
import static org.jreleaser.model.api.assemble.NativeImageAssembler.TYPE;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public final class NativeImageAssembler extends AbstractJavaAssembler<NativeImageAssembler, org.jreleaser.model.api.assemble.NativeImageAssembler> implements SwidTagAware, MatrixAware {
    private static final long serialVersionUID = 629659372890762847L;

    private final List<String> args = new ArrayList<>();
    private final Set<String> components = new LinkedHashSet<>();
    private final Artifact graal = new Artifact();
    private final Set<Artifact> graalJdks = new LinkedHashSet<>();
    private final Archiving archiving = new Archiving();
    private final Matrix matrix = new Matrix();
    private final Artifact graalJdkPattern = new Artifact();
    private final Upx upx = new Upx();
    private final LinuxX86 linuxX86 = new LinuxX86();
    private final WindowsX86 windowsX86 = new WindowsX86();
    private final MacosX86 macosX86 = new MacosX86();
    private final LinuxArm linuxArm = new LinuxArm();
    private final MacosArm macosArm = new MacosArm();
    private final ArchiveOptions options = new ArchiveOptions();

    private String imageName;
    private String imageNameTransform;
    private Boolean applyDefaultMatrix;

    @JsonIgnore
    private final org.jreleaser.model.api.assemble.NativeImageAssembler immutable = new org.jreleaser.model.api.assemble.NativeImageAssembler() {
        private static final long serialVersionUID = 2532557224813258883L;

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
        @Deprecated
        public Archive.Format getArchiveFormat() {
            return NativeImageAssembler.this.getArchiveFormat();
        }

        @Override
        public Archiving getArchiving() {
            return archiving.asImmutable();
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
        public boolean isApplyDefaultMatrix() {
            return NativeImageAssembler.this.isApplyDefaultMatrix();
        }

        @Override
        public org.jreleaser.model.api.common.Matrix getMatrix() {
            return matrix.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.common.Artifact getGraalJdkPattern() {
            return graalJdkPattern.asImmutable();
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
            return getLinuxX86();
        }

        @Override
        public Windows getWindows() {
            return getWindowsX86();
        }

        @Override
        public Osx getOsx() {
            return getMacosX86();
        }

        @Override
        public LinuxX86 getLinuxX86() {
            return linuxX86.asImmutable();
        }

        @Override
        public WindowsX86 getWindowsX86() {
            return windowsX86.asImmutable();
        }

        @Override
        public MacosX86 getMacosX86() {
            return macosX86.asImmutable();
        }

        @Override
        public LinuxArm getLinuxArm() {
            return linuxArm.asImmutable();
        }

        @Override
        public MacosArm getMacosArm() {
            return macosArm.asImmutable();
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
        this.applyDefaultMatrix = merge(this.applyDefaultMatrix, source.applyDefaultMatrix);
        setArchiving(source.archiving);
        setOptions(source.options);
        setGraal(source.graal);
        setGraalJdks(merge(this.graalJdks, source.graalJdks));
        setArgs(merge(this.args, source.args));
        setComponents(merge(this.components, source.components));
        setUpx(source.upx);
        setLinuxX86(source.linuxX86);
        setWindowsX86(source.windowsX86);
        setMacosX86(source.macosX86);
        setLinuxArm(source.linuxArm);
        setMacosArm(source.macosArm);
        setMatrix(source.matrix);
        setGraalJdkPattern(source.graalJdkPattern);
    }

    public String getResolvedImageName(JReleaserContext context) {
        TemplateContext props = context.getModel().props(context);
        props.setAll(props(context));
        return resolveTemplate(context.getLogger(), imageName, props);
    }

    public String getResolvedImageNameTransform(JReleaserContext context) {
        if (isBlank(imageNameTransform)) return null;
        TemplateContext props = context.getModel().props(context);
        props.setAll(props(context));
        return resolveTemplate(context.getLogger(), imageNameTransform, props);
    }

    public PlatformCustomizer getResolvedPlatformCustomizer() {
        String currentPlatform = PlatformUtils.getCurrentFull();
        if (PlatformUtils.isMac(currentPlatform)) {
            return PlatformUtils.isArm(currentPlatform) ? getMacosArm() : getMacosX86();
        } else if (PlatformUtils.isWindows(currentPlatform)) {
            return getWindowsX86();
        }
        return PlatformUtils.isArm(currentPlatform) ? getLinuxArm() : getLinuxX86();
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

    @Deprecated
    public Archive.Format getArchiveFormat() {
        return archiving.getFormat();
    }

    @Deprecated
    public void setArchiveFormat(Archive.Format archiveFormat) {
        nag("nativeImage.archiveFormat is deprecated since 1.18.0 and will be removed in 2.0.0. Use nativeImage.archiving.format instead");
        archiving.setFormat(archiveFormat);
    }

    @Deprecated
    public void setArchiveFormat(String archiveFormat) {
        nag("nativeImage.archiveFormat is deprecated since 1.18.0 and will be removed in 2.0.0. Use nativeImage.archiving.format instead");
        setArchiveFormat(Archive.Format.of(archiveFormat));
    }

    public Archiving getArchiving() {
        return archiving;
    }

    public void setArchiving(Archiving archiving) {
        this.archiving.merge(archiving);
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

    @Override
    public boolean isApplyDefaultMatrixSet() {
        return null != applyDefaultMatrix;
    }

    @Override
    public boolean isApplyDefaultMatrix() {
        return null != applyDefaultMatrix && applyDefaultMatrix;
    }

    @Override
    public void setApplyDefaultMatrix(Boolean applyDefaultMatrix) {
        this.applyDefaultMatrix = applyDefaultMatrix;
    }

    @Override
    public Matrix getMatrix() {
        return matrix;
    }

    @Override
    public void setMatrix(Matrix matrix) {
        this.matrix.merge(matrix);
    }

    public Artifact getGraalJdkPattern() {
        return graalJdkPattern;
    }

    public void setGraalJdkPattern(Artifact artifactPattern) {
        this.graalJdkPattern.merge(artifactPattern);
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

    @Deprecated
    public LinuxX86 getLinux() {
        return getLinuxX86();
    }

    @Deprecated
    public void setLinux(LinuxX86 linux) {
        nag("native-image.linux is deprecated since 1.17.0 and will be removed in 2.0.0. Use native-image.linuxX86 instead");
        setLinuxX86(linux);
    }

    @Deprecated
    public WindowsX86 getWindows() {
        return getWindowsX86();
    }

    @Deprecated
    public void setWindows(WindowsX86 windows) {
        nag("native-image.windows is deprecated since 1.17.0 and will be removed in 2.0.0. Use native-image.windowsX86 instead");
        setWindowsX86(windows);
    }

    @Deprecated
    public MacosX86 getOsx() {
        return getMacosX86();
    }

    @Deprecated
    public void setOsx(MacosX86 osx) {
        nag("native-image.osx is deprecated since 1.17.0 and will be removed in 2.0.0. Use native-image.macosX86 instead");
        setMacosX86(osx);
    }

    public LinuxX86 getLinuxX86() {
        return linuxX86;
    }

    public void setLinuxX86(LinuxX86 linuxX86) {
        this.linuxX86.merge(linuxX86);
    }

    public WindowsX86 getWindowsX86() {
        return windowsX86;
    }

    public void setWindowsX86(WindowsX86 windowsX86) {
        this.windowsX86.merge(windowsX86);
    }

    public MacosX86 getMacosX86() {
        return macosX86;
    }

    public void setMacosX86(MacosX86 macosX86) {
        this.macosX86.merge(macosX86);
    }

    public LinuxArm getLinuxArm() {
        return linuxArm;
    }

    public void setLinuxArm(LinuxArm linuxArm) {
        this.linuxArm.merge(linuxArm);
    }

    public MacosArm getMacosArm() {
        return macosArm;
    }

    public void setMacosArm(MacosArm macosArm) {
        this.macosArm.merge(macosArm);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("imageName", imageName);
        props.put("imageNameTransform", imageNameTransform);
        props.put("archiving", archiving.asMap(full));
        props.put("options", options.asMap(full));
        props.put("applyDefaultMatrix", isApplyDefaultMatrix());
        matrix.asMap(props);
        props.put("graalJdkPattern", graalJdkPattern.asMap(full));

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
        if (full || linuxX86.isSet()) props.putAll(linuxX86.asMap(full));
        if (full || macosX86.isSet()) props.putAll(macosX86.asMap(full));
        if (full || windowsX86.isSet()) props.putAll(windowsX86.asMap(full));
        if (full || linuxArm.isSet()) props.putAll(linuxArm.asMap(full));
        if (full || macosArm.isSet()) props.putAll(macosArm.asMap(full));
    }

    public static final class Archiving extends AbstractActivatable<Archiving> implements Domain {
        private static final long serialVersionUID = -6730452833078647208L;

        private Boolean enabled;
        private Archive.Format format;

        @JsonIgnore
        private final org.jreleaser.model.api.assemble.NativeImageAssembler.Archiving immutable = new org.jreleaser.model.api.assemble.NativeImageAssembler.Archiving() {
            private static final long serialVersionUID = -7026942817421830530L;

            @Override
            public boolean isEnabled() {
                return Archiving.this.isEnabled();
            }

            @Override
            public Archive.Format getFormat() {
                return format;
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Archiving.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.assemble.NativeImageAssembler.Archiving asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Archiving source) {
            super.merge(source);
            this.enabled = merge(this.enabled, source.enabled);
            this.format = merge(this.format, source.format);
        }

        public boolean isEnabled() {
            return null != enabled && enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabledSet() {
            return null != enabled;
        }

        public Archive.Format getFormat() {
            return format;
        }

        public void setFormat(Archive.Format format) {
            this.format = format;
        }

        public void setFormat(String format) {
            this.format = Archive.Format.of(format);
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("enabled", isEnabled());
            props.put("format", format);

            return props;
        }
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
        private static final long serialVersionUID = -6257221685457294205L;

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

        public boolean isSet() {
            return !args.isEmpty();
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

    public static final class LinuxX86 extends NativeImageAssembler.AbstractPlatformCustomizer<LinuxX86> {
        private static final long serialVersionUID = -8831893038081740645L;

        @JsonIgnore
        private final org.jreleaser.model.api.assemble.NativeImageAssembler.LinuxX86 immutable = new org.jreleaser.model.api.assemble.NativeImageAssembler.LinuxX86() {
            private static final long serialVersionUID = -3352619976211811469L;

            @Override
            public String getPlatform() {
                return LinuxX86.this.getPlatform();
            }

            @Override
            public List<String> getArgs() {
                return unmodifiableList(LinuxX86.this.getArgs());
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(LinuxX86.this.asMap(full));
            }
        };

        public LinuxX86() {
            super("linuxX86");
        }

        public org.jreleaser.model.api.assemble.NativeImageAssembler.LinuxX86 asImmutable() {
            return immutable;
        }
    }

    public static final class MacosX86 extends NativeImageAssembler.AbstractPlatformCustomizer<MacosX86> {
        private static final long serialVersionUID = 1765626220071702695L;

        @JsonIgnore
        private final org.jreleaser.model.api.assemble.NativeImageAssembler.MacosX86 immutable = new org.jreleaser.model.api.assemble.NativeImageAssembler.MacosX86() {
            private static final long serialVersionUID = 5024962242878918936L;

            @Override
            public String getPlatform() {
                return MacosX86.this.getPlatform();
            }

            @Override
            public List<String> getArgs() {
                return unmodifiableList(MacosX86.this.getArgs());
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(MacosX86.this.asMap(full));
            }
        };

        public MacosX86() {
            super("macosX86");
        }

        public org.jreleaser.model.api.assemble.NativeImageAssembler.MacosX86 asImmutable() {
            return immutable;
        }
    }

    public static final class WindowsX86 extends NativeImageAssembler.AbstractPlatformCustomizer<WindowsX86> {
        private static final long serialVersionUID = -2428122936085887109L;

        @JsonIgnore
        private final org.jreleaser.model.api.assemble.NativeImageAssembler.WindowsX86 immutable = new org.jreleaser.model.api.assemble.NativeImageAssembler.WindowsX86() {
            private static final long serialVersionUID = 5225971842238088904L;

            @Override
            public String getPlatform() {
                return WindowsX86.this.getPlatform();
            }

            @Override
            public List<String> getArgs() {
                return unmodifiableList(WindowsX86.this.getArgs());
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(WindowsX86.this.asMap(full));
            }
        };

        public WindowsX86() {
            super("windowsX86");
        }

        public org.jreleaser.model.api.assemble.NativeImageAssembler.WindowsX86 asImmutable() {
            return immutable;
        }
    }

    public static final class LinuxArm extends NativeImageAssembler.AbstractPlatformCustomizer<LinuxArm> {
        private static final long serialVersionUID = 7581399244465819003L;

        @JsonIgnore
        private final org.jreleaser.model.api.assemble.NativeImageAssembler.LinuxArm immutable = new org.jreleaser.model.api.assemble.NativeImageAssembler.LinuxArm() {
            private static final long serialVersionUID = -6003576591744585492L;

            @Override
            public String getPlatform() {
                return LinuxArm.this.getPlatform();
            }

            @Override
            public List<String> getArgs() {
                return unmodifiableList(LinuxArm.this.getArgs());
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(LinuxArm.this.asMap(full));
            }
        };

        public LinuxArm() {
            super("linuxArm");
        }

        public org.jreleaser.model.api.assemble.NativeImageAssembler.LinuxArm asImmutable() {
            return immutable;
        }
    }

    public static final class MacosArm extends NativeImageAssembler.AbstractPlatformCustomizer<MacosArm> {
        private static final long serialVersionUID = 4870042060382221246L;

        @JsonIgnore
        private final org.jreleaser.model.api.assemble.NativeImageAssembler.MacosArm immutable = new org.jreleaser.model.api.assemble.NativeImageAssembler.MacosArm() {
            private static final long serialVersionUID = -432154406205528371L;

            @Override
            public String getPlatform() {
                return MacosArm.this.getPlatform();
            }

            @Override
            public List<String> getArgs() {
                return unmodifiableList(MacosArm.this.getArgs());
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(MacosArm.this.asMap(full));
            }
        };

        public MacosArm() {
            super("macosArm");
        }

        public org.jreleaser.model.api.assemble.NativeImageAssembler.MacosArm asImmutable() {
            return immutable;
        }
    }
}
