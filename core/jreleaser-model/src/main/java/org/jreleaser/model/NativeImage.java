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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class NativeImage extends AbstractJavaAssembler {
    public static final String TYPE = "native-image";

    private final List<String> args = new ArrayList<>();
    private final Artifact graal = new Artifact();
    private final Set<Artifact> graalJdks = new LinkedHashSet<>();

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

    void setAll(NativeImage nativeImage) {
        super.setAll(nativeImage);
        this.imageName = nativeImage.imageName;
        this.imageNameTransform = nativeImage.imageNameTransform;
        this.archiveFormat = nativeImage.archiveFormat;
        setGraal(nativeImage.graal);
        setGraalJdks(nativeImage.graalJdks);
        setArgs(nativeImage.args);
    }

    public String getResolvedImageName(JReleaserContext context) {
        Map<String, Object> props = context.props();
        props.putAll(props());
        return applyTemplate(imageName, props);
    }

    public String getResolvedImageNameTransform(JReleaserContext context) {
        if (isBlank(imageNameTransform)) return null;
        Map<String, Object> props = context.props();
        props.putAll(props());
        return applyTemplate(imageNameTransform, props);
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
        this.graal.setAll(graal);
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
    }
}
