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
package org.jreleaser.model.spi.deploy.maven;

import org.jreleaser.mustache.TemplateContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;

import static org.jreleaser.util.CollectionUtils.setOf;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.4.0
 */
public class Deployable implements Comparable<Deployable> {
    public static final String PACKAGING_AAR = "aar";
    public static final String PACKAGING_JAR = "jar";
    public static final String PACKAGING_WAR = "war";
    public static final String PACKAGING_POM = "pom";
    public static final String PACKAGING_NBM = "nbm";
    public static final String PACKAGING_ZIP = "zip";
    public static final String PACKAGING_MAVEN_ARCHETYPE = "maven-archetype";
    public static final String MAVEN_METADATA_XML = "maven-metadata.xml";
    public static final String EXT_JAR = ".jar";
    public static final String EXT_WAR = ".war";
    public static final String EXT_POM = ".pom";
    public static final String EXT_ASC = ".asc";
    public static final String EXT_MODULE = ".module";
    public static final String EXT_XML = ".xml";
    public static final String EXT_JSON = ".json";

    private static final String[] EXT_CHECKSUMS = {".md5", ".sha1", ".sha256", ".sha512"};

    private static final Set<String> JAR_EXCLUSIONS = setOf(
        PACKAGING_POM,
        PACKAGING_AAR,
        PACKAGING_WAR,
        PACKAGING_ZIP
    );

    private static final Set<String> SOURCE_EXCLUSIONS = setOf(
        PACKAGING_POM,
        PACKAGING_NBM,
        PACKAGING_AAR
    );

    private static final Set<String> JAVADOC_EXCLUSIONS = setOf(
        PACKAGING_POM,
        PACKAGING_NBM,
        PACKAGING_MAVEN_ARCHETYPE,
        PACKAGING_AAR
    );

    private final String stagingRepository;
    private final String path;
    private final String filename;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String packaging;
    private final boolean relocated;

    public Deployable(String stagingRepository, String path, String packaging, String filename, boolean relocated) {
        this.stagingRepository = stagingRepository;
        this.path = path;
        this.filename = filename;
        this.packaging = packaging;
        this.relocated = relocated;

        if (!MAVEN_METADATA_XML.equals(filename)) {
            Path p = Paths.get(path);
            this.version = p.getFileName().toString();
            p = p.getParent();
            this.artifactId = p.getFileName().toString();
            p = p.getParent();
            String gid = p.toString()
                .replace("/", ".")
                .replace("\\", ".");
            if (gid.startsWith(".")) {
                gid = gid.substring(1);
            }
            this.groupId = gid;
        } else {
            this.version = "";
            this.artifactId = "";
            this.groupId = "";
        }
    }


    public TemplateContext props() {
        TemplateContext props = new TemplateContext();
        props.set("groupId", groupId);
        props.set("artifactId", artifactId);
        props.set("version", version);
        props.set("filename", filename);
        props.set("path", getDeployPath());
        return props;
    }

    public boolean requiresJar() {
        return isNotBlank(packaging) && !JAR_EXCLUSIONS.contains(packaging);
    }

    public boolean requiresWar() {
        return isNotBlank(packaging) && PACKAGING_WAR.equalsIgnoreCase(packaging);
    }

    public boolean requiresSourcesJar() {
        return isNotBlank(packaging) && !SOURCE_EXCLUSIONS.contains(packaging);
    }

    public boolean requiresJavadocJar() {
        return isNotBlank(packaging) && !JAVADOC_EXCLUSIONS.contains(packaging);
    }

    public String getGav() {
        return groupId + ":" + artifactId + ":" + version;
    }

    public String getStagingRepository() {
        return stagingRepository;
    }

    public String getPath() {
        return path;
    }

    public String getFullDeployPath() {
        return getDeployPath() + "/" + getFilename();
    }

    public String getDeployPath() {
        return path.replace("\\", "/").substring(1);
    }

    public String getFilename() {
        return filename;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public Path getLocalPath() {
        return Paths.get(stagingRepository, path, filename);
    }

    public boolean isRelocated() {
        return relocated;
    }

    public Deployable deriveByFilename(String filename) {
        return new Deployable(stagingRepository, path, packaging, filename, false);
    }

    public Deployable deriveByFilename(String packaging, String filename) {
        return new Deployable(stagingRepository, path, packaging, filename, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;
        Deployable that = (Deployable) o;
        return stagingRepository.equals(that.stagingRepository) &&
            path.equals(that.path) &&
            filename.equals(that.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stagingRepository, path, filename);
    }

    @Override
    public int compareTo(Deployable o) {
        if (null == o) return -1;
        return getFullDeployPath().compareTo(o.getFullDeployPath());
    }

    public boolean isArtifact() {
        return !isPom() &&
            !isSignature() &&
            !isChecksum() &&
            !isJson() &&
            !isXml() &&
            !isGradleMetadata() &&
            !isMavenMetadata();
    }

    public boolean isPom() {
        return filename.endsWith(EXT_POM);
    }

    public boolean isSignature() {
        return filename.endsWith(EXT_ASC);
    }

    public boolean isChecksum() {
        for (String ext : EXT_CHECKSUMS) {
            if (filename.endsWith(ext)) return true;
        }
        return false;
    }

    public boolean isJson() {
        return filename.endsWith(EXT_JSON);
    }

    public boolean isXml() {
        return filename.endsWith(EXT_XML);
    }

    public boolean isGradleMetadata() {
        return filename.endsWith(EXT_MODULE);
    }

    public boolean isMavenMetadata() {
        return filename.endsWith(MAVEN_METADATA_XML);
    }
}
