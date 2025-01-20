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
package org.jreleaser.model.api.assemble;

import org.jreleaser.model.Archive;
import org.jreleaser.model.api.catalog.swid.SwidTagAware;
import org.jreleaser.model.api.common.Activatable;
import org.jreleaser.model.api.common.ArchiveOptions;
import org.jreleaser.model.api.common.Artifact;
import org.jreleaser.model.api.common.Domain;
import org.jreleaser.model.api.common.Matrix;

import java.util.List;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public interface NativeImageAssembler extends Assembler, JavaAssembler, SwidTagAware {
    String TYPE = "native-image";

    String getImageName();

    String getImageNameTransform();

    Archive.Format getArchiveFormat();

    ArchiveOptions getOptions();

    Artifact getGraal();

    Set<? extends Artifact> getGraalJdks();

    boolean isApplyDefaultMatrix();

    Matrix getMatrix();

    Artifact getGraalJdkPattern();

    List<String> getArgs();

    Set<String> getComponents();

    Upx getUpx();

    @Deprecated
    Linux getLinux();

    @Deprecated
    Windows getWindows();

    @Deprecated
    Osx getOsx();

    LinuxX86 getLinuxX86();

    WindowsX86 getWindowsX86();

    MacosX86 getMacosX86();

    LinuxArm getLinuxArm();

    MacosArm getMacosArm();

    interface PlatformCustomizer extends Domain {
        String getPlatform();

        List<String> getArgs();
    }

    interface Upx extends Domain, Activatable {
        String getVersion();

        List<String> getArgs();
    }

    @Deprecated
    interface Linux extends PlatformCustomizer {
    }

    @Deprecated
    interface Osx extends PlatformCustomizer {
    }

    @Deprecated
    interface Windows extends PlatformCustomizer {
    }

    interface LinuxX86 extends Linux {
    }

    interface MacosX86 extends Osx {
    }

    interface WindowsX86 extends Windows {
    }

    interface LinuxArm extends PlatformCustomizer {
    }

    interface MacosArm extends PlatformCustomizer {
    }
}
