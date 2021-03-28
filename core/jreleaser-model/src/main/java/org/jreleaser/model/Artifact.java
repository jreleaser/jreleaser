/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import org.jreleaser.util.Constants;

import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.nio.file.Files.exists;
import static org.jreleaser.util.MustacheUtils.applyTemplate;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Artifact implements Domain {
    private String path;
    private String hash;
    private String platform;
    private Path filePath;

    public Path getResolvedPath(JReleaserContext context) {
        if (null == filePath) {
            if (path.contains("{{")) {
                path = applyTemplate(new StringReader(path), context.getModel().props());
            }
            filePath = context.getBasedir().resolve(Paths.get(path)).normalize();
            if (!exists(filePath)) {
                throw new JReleaserException("Path does not exist. " + context.getBasedir().relativize(filePath));
            }
        }
        return filePath;
    }

    public Path getResolvedPath(JReleaserContext context, Distribution distribution) {
        if (null == filePath) {
            if (path.contains("{{")) {
                Map<String, Object> props = context.getModel().props();
                props.put(Constants.KEY_DISTRIBUTION_NAME, distribution.getName());
                props.put(Constants.KEY_DISTRIBUTION_EXECUTABLE, distribution.getExecutable());
                props.put(Constants.KEY_DISTRIBUTION_JAVA_GROUP_ID, distribution.getJava().getGroupId());
                props.put(Constants.KEY_DISTRIBUTION_JAVA_ARTIFACT_ID, distribution.getJava().getArtifactId());
                props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION, distribution.getJava().getVersion());
                props.putAll(distribution.getResolvedExtraProperties());
                path = applyTemplate(new StringReader(path), props);
            }
            filePath = context.getBasedir().resolve(Paths.get(path)).normalize();
            if (!exists(filePath)) {
                throw new JReleaserException("Artifact does not exist. " + context.getBasedir().relativize(filePath));
            }
        }
        return filePath;
    }

    public Path getFilePath() {
        return filePath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Map<String, Object> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("path", path);
        map.put("hash", hash);
        map.put("platform", platform);
        return map;
    }

    public static Artifact of(Path filePath) {
        Artifact artifact = new Artifact();
        artifact.path = filePath.toAbsolutePath().toString();
        artifact.filePath = filePath;
        return artifact;
    }
}
