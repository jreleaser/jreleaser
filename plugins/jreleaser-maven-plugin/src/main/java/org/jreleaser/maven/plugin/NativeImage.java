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
package org.jreleaser.maven.plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class NativeImage extends AbstractJavaAssembler {
    private final List<String> args = new ArrayList<>();
    private final Artifact graal = new Artifact();

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

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args.clear();
        this.args.addAll(args);
    }
}
