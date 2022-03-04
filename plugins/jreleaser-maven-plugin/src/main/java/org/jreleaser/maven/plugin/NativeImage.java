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

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class NativeImage extends AbstractJavaAssembler {
    private final List<String> args = new ArrayList<>();
    private final Artifact graal = new Artifact();
    private final Upx upx = new Upx();
    private final Linux linux = new Linux();
    private final Windows windows = new Windows();
    private final Osx osx = new Osx();
    private final Set<Artifact> graalJdks = new LinkedHashSet<>();

    private String imageName;
    private String imageNameTransform;
    private Archive.Format archiveFormat;

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
        this.graal.setAll(graal);
    }

    public Set<Artifact> getGraalJdks() {
        return graalJdks;
    }

    public void setGraalJdks(Set<Artifact> graalJdks) {
        this.graalJdks.clear();
        this.graalJdks.addAll(graalJdks);
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args.clear();
        this.args.addAll(args);
    }

    public Upx getUpx() {
        return upx;
    }

    public void setUpx(Upx upx) {
        this.upx.setAll(upx);
    }

    public Linux getLinux() {
        return linux;
    }

    public void setLinux(Linux linux) {
        this.linux.setAll(linux);
    }

    public Windows getWindows() {
        return windows;
    }

    public void setWindows(Windows windows) {
        this.windows.setAll(windows);
    }

    public Osx getOsx() {
        return osx;
    }

    public void setOsx(Osx osx) {
        this.osx.setAll(osx);
    }

    public interface PlatformCustomizer {
        String getPlatform();

        List<String> getArgs();

        void setArgs(List<String> args);

        void addArgs(List<String> args);
    }

    public static class Upx implements Activatable {
        private final List<String> args = new ArrayList<>();

        private Active active;
        private String version;

        void setAll(NativeImage.Upx upx) {
            this.active = upx.active;
            this.version = upx.version;
            setArgs(upx.args);
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
        public String resolveActive() {
            return active != null ? active.name() : null;
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
    }

    private static abstract class AbstractPlatformCustomizer implements PlatformCustomizer {
        private final List<String> args = new ArrayList<>();
        private final String platform;

        protected AbstractPlatformCustomizer(String platform) {
            this.platform = platform;
        }

        void setAll(AbstractPlatformCustomizer customizer) {
            setArgs(customizer.args);
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

        @Override
        public String getPlatform() {
            return platform;
        }
    }

    public static class Linux extends AbstractPlatformCustomizer {
        public Linux() {
            super("linux");
        }
    }

    public static class Windows extends AbstractPlatformCustomizer {
        public Windows() {
            super("windows");
        }
    }

    public static class Osx extends AbstractPlatformCustomizer {
        public Osx() {
            super("osx");
        }
    }
}
