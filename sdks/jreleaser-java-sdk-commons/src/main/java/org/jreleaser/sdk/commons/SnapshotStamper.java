/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.sdk.commons;

import org.jreleaser.model.spi.deploy.maven.Deployable;
import org.jreleaser.util.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.jreleaser.model.spi.deploy.maven.Deployable.MAVEN_METADATA_XML;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * Gives snapshot files staged the way 'mvn install' does, eg {@code lib-1.0.0-SNAPSHOT.jar}, the names
 * 'mvn deploy' gives them, eg {@code lib-1.0.0-20261017.120000-1.jar}, and writes the version-level
 * maven-metadata.xml that lists them. The staged files are copied, never renamed in place.
 * <p>
 * Every file of a deployment shares one timestamp and build number 1. Like the maven-metadata.xml that
 * 'mvn deploy' writes to a staging repository, the metadata describes this deployment only.
 */
final class SnapshotStamper {
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss");
    private static final DateTimeFormatter UPDATED = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String BUILD_NUMBER = "1";
    private static final String SNAPSHOT = "SNAPSHOT";

    private SnapshotStamper() {
        // noop
    }

    /**
     * @param deployables the staged files
     * @param target      an empty directory for the stamped copies, cleared first if it exists
     * @param now         when the deployment happens
     * @return the deployables, with every unstamped one replaced by its stamped copy and a maven-metadata.xml
     * for each version they belong to; the deployables themselves when none is unstamped
     */
    static Set<Deployable> stamp(Set<Deployable> deployables, Path target, ZonedDateTime now) throws IOException {
        Set<String> versionPaths = new TreeSet<>();
        for (Deployable deployable : deployables) {
            if (deployable.isUnstamped()) versionPaths.add(deployable.getPath());
        }
        if (versionPaths.isEmpty()) return deployables;

        if (Files.exists(target)) FileUtils.deleteFiles(target);

        ZonedDateTime utc = now.withZoneSameInstant(ZoneOffset.UTC);
        String timestamp = TIMESTAMP.format(utc);
        String stagingRepository = target.toAbsolutePath().toString();

        Set<Deployable> result = new TreeSet<>();
        Map<String, List<Deployable>> stampedByVersion = new TreeMap<>();
        for (Deployable deployable : deployables) {
            if (!deployable.isUnstamped()) {
                // staged metadata of a stamped version describes the plain names, so it is written again
                boolean stagedMetadata = deployable.getFilename().startsWith(MAVEN_METADATA_XML);
                if (!stagedMetadata || !versionPaths.contains(deployable.getPath())) result.add(deployable);
                continue;
            }

            String version = deployable.getVersion();
            String timestampedVersion = version.substring(0, version.length() - SNAPSHOT.length()) +
                timestamp + "-" + BUILD_NUMBER;
            Deployable stamped = deployable.stamp(stagingRepository, timestampedVersion);
            Files.createDirectories(stamped.getLocalPath().getParent());
            Files.copy(deployable.getLocalPath(), stamped.getLocalPath(), REPLACE_EXISTING, COPY_ATTRIBUTES);

            result.add(stamped);
            stampedByVersion.computeIfAbsent(deployable.getPath(), k -> new ArrayList<>()).add(stamped);
        }

        for (Map.Entry<String, List<Deployable>> e : stampedByVersion.entrySet()) {
            Deployable metadata = new Deployable(true, stagingRepository, e.getKey(), "", MAVEN_METADATA_XML, false);
            Files.write(metadata.getLocalPath(), metadata(e.getValue(), timestamp, UPDATED.format(utc)).getBytes(UTF_8));
            result.add(metadata);
        }

        return result;
    }

    private static String metadata(List<Deployable> stamped, String timestamp, String updated) {
        Deployable first = stamped.get(0);
        String value = first.getVersion().substring(0, first.getVersion().length() - SNAPSHOT.length()) +
            timestamp + "-" + BUILD_NUMBER;

        StringBuilder xml = new StringBuilder()
            .append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            .append("<metadata modelVersion=\"1.1.0\">\n")
            .append("  <groupId>").append(escape(first.getGroupId())).append("</groupId>\n")
            .append("  <artifactId>").append(escape(first.getArtifactId())).append("</artifactId>\n")
            .append("  <version>").append(escape(first.getVersion())).append("</version>\n")
            .append("  <versioning>\n")
            .append("    <snapshot>\n")
            .append("      <timestamp>").append(timestamp).append("</timestamp>\n")
            .append("      <buildNumber>").append(BUILD_NUMBER).append("</buildNumber>\n")
            .append("    </snapshot>\n")
            .append("    <lastUpdated>").append(updated).append("</lastUpdated>\n")
            .append("    <snapshotVersions>\n");

        for (Deployable deployable : new TreeSet<>(stamped)) {
            if (deployable.isSignature() || deployable.isChecksum()) continue;

            xml.append("      <snapshotVersion>\n");
            if (isNotBlank(deployable.getClassifier())) {
                xml.append("        <classifier>").append(escape(deployable.getClassifier())).append("</classifier>\n");
            }
            xml.append("        <extension>").append(escape(deployable.getExtension())).append("</extension>\n")
                .append("        <value>").append(escape(value)).append("</value>\n")
                .append("        <updated>").append(updated).append("</updated>\n")
                .append("      </snapshotVersion>\n");
        }

        return xml.append("    </snapshotVersions>\n")
            .append("  </versioning>\n")
            .append("</metadata>\n")
            .toString();
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
