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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.util.PlatformUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class NativeImage extends AbstractJavaAssembler<NativeImage> {
    public static final String TYPE = "native-image";

    private final List<String> args = new ArrayList<>();
    private final Artifact graal = new Artifact();
    private final Set<Artifact> graalJdks = new LinkedHashSet<>();
    private final Upx upx = new Upx();
    private final Linux linux = new Linux();
    private final Windows windows = new Windows();
    private final Osx osx = new Osx();

    private String imageName;
    private String imageNameTransform;
    private Archive.Format archiveFormat;

    public NativeImage() {
        super(TYPE);
    }

    @Override
    public Distribution.DistributionType getDistributionType() {
        return Distribution.DistributionType.NATIVE_IMAGE;
    }

    @Override
    public void freeze() {
        super.freeze();
        graal.freeze();
        graalJdks.forEach(Artifact::freeze);
        upx.freeze();
        linux.freeze();
        windows.freeze();
        osx.freeze();
    }

    @Override
    public void merge(NativeImage nativeImage) {
        freezeCheck();
        super.merge(nativeImage);
        this.imageName = merge(this.imageName, nativeImage.imageName);
        this.imageNameTransform = merge(this.imageNameTransform, nativeImage.imageNameTransform);
        this.archiveFormat = merge(this.archiveFormat, nativeImage.archiveFormat);
        setGraal(nativeImage.graal);
        setGraalJdks(merge(this.graalJdks, nativeImage.graalJdks));
        setArgs(merge(this.args, nativeImage.args));
        setUpx(nativeImage.upx);
        setLinux(nativeImage.linux);
        setWindows(nativeImage.windows);
        setOsx(nativeImage.osx);
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
        freezeCheck();
        this.imageName = imageName;
    }

    public String getImageNameTransform() {
        return imageNameTransform;
    }

    public void setImageNameTransform(String imageNameTransform) {
        freezeCheck();
        this.imageNameTransform = imageNameTransform;
    }

    public Archive.Format getArchiveFormat() {
        return archiveFormat;
    }

    public void setArchiveFormat(Archive.Format archiveFormat) {
        freezeCheck();
        this.archiveFormat = archiveFormat;
    }

    public void setArchiveFormat(String archiveFormat) {
        freezeCheck();
        this.archiveFormat = Archive.Format.of(archiveFormat);
    }

    public Artifact getGraal() {
        return graal;
    }

    public void setGraal(Artifact graal) {
        this.graal.merge(graal);
    }

    public Set<Artifact> getGraalJdks() {
        return freezeWrap(Artifact.sortArtifacts(graalJdks));
    }

    public void setGraalJdks(Set<Artifact> graalJdks) {
        freezeCheck();
        this.graalJdks.clear();
        this.graalJdks.addAll(graalJdks);
    }

    public void addGraalJdks(Set<Artifact> graalJdks) {
        freezeCheck();
        this.graalJdks.addAll(graalJdks);
    }

    public void addGraalJdk(Artifact jdk) {
        freezeCheck();
        if (null != jdk) {
            this.graalJdks.add(jdk);
        }
    }

    public List<String> getArgs() {
        return freezeWrap(args);
    }

    public void setArgs(List<String> args) {
        freezeCheck();
        this.args.clear();
        this.args.addAll(args);
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

    public static class Upx extends AbstractModelObject<Upx> implements Domain, Activatable {
        private final List<String> args = new ArrayList<>();

        @JsonIgnore
        private boolean enabled;
        private Active active;
        private String version;

        @Override
        public void merge(Upx upx) {
            freezeCheck();
            this.active = this.merge(this.active, upx.active);
            this.enabled = this.merge(this.enabled, upx.enabled);
            this.version = this.merge(this.version, upx.version);
            setArgs(merge(this.args, upx.args));
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
            freezeCheck();
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
            freezeCheck();
            this.version = version;
        }

        public List<String> getArgs() {
            return freezeWrap(args);
        }

        public void setArgs(List<String> args) {
            freezeCheck();
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
        public void merge(S customizer) {
            freezeCheck();
            setArgs(merge(this.args, customizer.args));
        }

        public List<String> getArgs() {
            return freezeWrap(args);
        }

        public void setArgs(List<String> args) {
            freezeCheck();
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

    public static class Linux extends AbstractPlatformCustomizer<Linux> {
        public Linux() {
            super("linux");
        }
    }

    public static class Windows extends AbstractPlatformCustomizer<Windows> {
        public Windows() {
            super("windows");
        }
    }

    public static class Osx extends AbstractPlatformCustomizer<Osx> {
        public Osx() {
            super("osx");
        }
    }
}
