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
package org.jreleaser.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.fail;

class MavenMetadataTransformationUtilsTest {
    @Test
    void merge() throws Exception {
        try (var sw = new StringWriter()) {
            var projectVersion = "9.0.0-SNAPSHOT";
            // Because of the used versions in the examples, we can rely on simple string comparator without parsing the versions:
            MavenMetadataTransformationUtils.mergeMetadataXml(MERGE_XML.getBytes(StandardCharsets.UTF_8), projectVersion, sw, String::compareTo);
            String xml = sw.toString();
            Assertions.assertTrue(
                Pattern.compile("<\\?xml version=\\\"1\\.0\\\" encoding=\\\"UTF-8\\\"\\?>\\s*<metadata>\\s*" +
                    "<groupId>org\\.hibernate\\.orm</groupId>\\s*" +
                    "<artifactId>hibernate-core</artifactId>\\s*" +
                    "<versioning>\\s*" +
                    "<latest>9\\.0\\.0-SNAPSHOT</latest>\\s*" +
                    "<versions>\\s*" +
                    "<version>7\\.0\\.7-SNAPSHOT</version>\\s*" +
                    "<version>9\\.0\\.0-SNAPSHOT</version>\\s*" +
                    "</versions>\\s*" +
                    "<lastUpdated>\\d{14}</lastUpdated>\\s*" + // the date will change all the time, and no point in dragging a fixed clock into this, just to test things.
                    "</versioning>\\s*</metadata>\\s*").matcher(
                    xml).matches());
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    void mergedXmlCanBeReprocessed() throws Exception {
        String xml;
        try (var sw = new StringWriter()) {
            var projectVersion = "8.0.0-SNAPSHOT";
            // Because of the used versions in the examples, we can rely on simple string comparator without parsing the versions:
            MavenMetadataTransformationUtils.mergeMetadataXml(MERGE_XML.getBytes(StandardCharsets.UTF_8), projectVersion, sw, String::compareTo);
             xml = sw.toString();
        }
        try (var sw = new StringWriter()) {
            // now pass the processed xml as if we've uploaded it already, and are processing it on the next release
            var projectVersion = "9.0.0-SNAPSHOT";
            MavenMetadataTransformationUtils.mergeMetadataXml(xml.getBytes(StandardCharsets.UTF_8), projectVersion, sw, String::compareTo);
            xml = sw.toString();

            Assertions.assertTrue(
                Pattern.compile("<\\?xml version=\\\"1\\.0\\\" encoding=\\\"UTF-8\\\"\\?>\\s*<metadata>\\s*" +
                    "<groupId>org\\.hibernate\\.orm</groupId>\\s*" +
                    "<artifactId>hibernate-core</artifactId>\\s*" +
                    "<versioning>\\s*" +
                    "<latest>9\\.0\\.0-SNAPSHOT</latest>\\s*" +
                    "<versions>\\s*" +
                    "<version>7\\.0\\.7-SNAPSHOT</version>\\s*" +
                    "<version>8\\.0\\.0-SNAPSHOT</version>\\s*" +
                    "<version>9\\.0\\.0-SNAPSHOT</version>\\s*" +
                    "</versions>\\s*" +
                    "<lastUpdated>\\d{14}</lastUpdated>\\s*" + // the date will change all the time, and no point in dragging a fixed clock into this, just to test things.
                    "</versioning>\\s*</metadata>\\s*").matcher(
                    xml).matches());
        } catch (IOException e) {
            fail(e);
        }
    }

    @Test
    void recreate() throws Exception {
        var projectVersion = "9.0.0-SNAPSHOT";
        var groupId = "org.hibernate.orm";
        var artifactId = "hibernate-core";
        try (var sw = new StringWriter()) {
            // Because of the used versions in the examples, we can rely on simple string comparator without parsing the versions:
            MavenMetadataTransformationUtils.recreateMetadataXml(RECREATE_XML.getBytes(StandardCharsets.UTF_8), projectVersion, groupId, artifactId, sw, String::compareTo);
            String xml = sw.toString();
            Assertions.assertTrue(
                Pattern.compile("<\\?xml version=\\\"1\\.0\\\" encoding=\\\"UTF-8\\\"\\?>\\s*<metadata>\\s*" +
                    "<groupId>org\\.hibernate\\.orm</groupId>\\s*" +
                    "<artifactId>hibernate-core</artifactId>\\s*" +
                    "<versioning>\\s*<versions>\\s*" +
                    "<version>6\\.4\\.0-SNAPSHOT</version>\\s*" +
                    "<version>6\\.4\\.9-SNAPSHOT</version>\\s*" +
                    "<version>6\\.2\\.13-SNAPSHOT</version>\\s*" +
                    "<version>6\\.5\\.0-SNAPSHOT</version>\\s*" +
                    "<version>6\\.4\\.7-SNAPSHOT</version>\\s*" +
                    "<version>6\\.3\\.3-SNAPSHOT</version>\\s*" +
                    "</versions>\\s*" +
                    "<latest>9\\.0\\.0-SNAPSHOT</latest>\\s*" +
                    "<lastUpdated>\\d{14}</lastUpdated>\\s*" + // the date will change all the time, and no point in dragging a fixed clock into this, just to test things.
                    "</versioning>\\s*</metadata>\\s*").matcher(
                    xml).matches());
        } catch (IOException e) {
            fail(e);
        }
    }

    private static final String MERGE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<metadata>\n" +
        "  <groupId>org.hibernate.orm</groupId>\n" +
        "  <artifactId>hibernate-core</artifactId>\n" +
        "  <versioning>\n" +
        "    <latest>7.0.7-SNAPSHOT</latest>\n" +
        "    <versions>\n" +
        "      <version>7.0.7-SNAPSHOT</version>\n" +
        "    </versions>\n" +
        "    <lastUpdated>20250429074943</lastUpdated>\n" +
        "  </versioning>\n" +
        "</metadata>\n";

    private static final String RECREATE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<content>\n" +
        "  <data>\n" +
        "    <content-item>\n" +
        "      <resourceURI>https://oss.sonatype.org/service/local/repositories/snapshots/content/org/hibernate/orm/hibernate-core/6.4.0-SNAPSHOT/</resourceURI>\n" +
        "      <relativePath>/org/hibernate/orm/hibernate-core/6.4.0-SNAPSHOT/</relativePath>\n" +
        "      <text>6.4.0-SNAPSHOT</text>\n" +
        "      <leaf>false</leaf>\n" +
        "      <lastModified>2024-09-30 07:38:24.321 UTC</lastModified>\n" +
        "      <sizeOnDisk>-1</sizeOnDisk>\n" +
        "    </content-item>\n" +
        "    <content-item>\n" +
        "      <resourceURI>https://oss.sonatype.org/service/local/repositories/snapshots/content/org/hibernate/orm/hibernate-core/maven-metadata.xml.sha512</resourceURI>\n" +
        "      <relativePath>/org/hibernate/orm/hibernate-core/maven-metadata.xml.sha512</relativePath>\n" +
        "      <text>maven-metadata.xml.sha512</text>\n" +
        "      <leaf>true</leaf>\n" +
        "      <lastModified>2025-04-26 18:38:56.885 UTC</lastModified>\n" +
        "      <sizeOnDisk>128</sizeOnDisk>\n" +
        "    </content-item>\n" +
        "    <content-item>\n" +
        "      <resourceURI>https://oss.sonatype.org/service/local/repositories/snapshots/content/org/hibernate/orm/hibernate-core/6.4.9-SNAPSHOT/</resourceURI>\n" +
        "      <relativePath>/org/hibernate/orm/hibernate-core/6.4.9-SNAPSHOT/</relativePath>\n" +
        "      <text>6.4.9-SNAPSHOT</text>\n" +
        "      <leaf>false</leaf>\n" +
        "      <lastModified>2024-09-30 07:38:24.609 UTC</lastModified>\n" +
        "      <sizeOnDisk>-1</sizeOnDisk>\n" +
        "    </content-item>\n" +
        "    <content-item>\n" +
        "      <resourceURI>https://oss.sonatype.org/service/local/repositories/snapshots/content/org/hibernate/orm/hibernate-core/6.2.13-SNAPSHOT/</resourceURI>\n" +
        "      <relativePath>/org/hibernate/orm/hibernate-core/6.2.13-SNAPSHOT/</relativePath>\n" +
        "      <text>6.2.13-SNAPSHOT</text>\n" +
        "      <leaf>false</leaf>\n" +
        "      <lastModified>2024-09-30 07:38:24.849 UTC</lastModified>\n" +
        "      <sizeOnDisk>-1</sizeOnDisk>\n" +
        "    </content-item>\n" +
        "    <content-item>\n" +
        "      <resourceURI>https://oss.sonatype.org/service/local/repositories/snapshots/content/org/hibernate/orm/hibernate-core/6.5.0-SNAPSHOT/</resourceURI>\n" +
        "      <relativePath>/org/hibernate/orm/hibernate-core/6.5.0-SNAPSHOT/</relativePath>\n" +
        "      <text>6.5.0-SNAPSHOT</text>\n" +
        "      <leaf>false</leaf>\n" +
        "      <lastModified>2024-09-30 07:38:25.413 UTC</lastModified>\n" +
        "      <sizeOnDisk>-1</sizeOnDisk>\n" +
        "    </content-item>\n" +
        "    <content-item>\n" +
        "      <resourceURI>https://oss.sonatype.org/service/local/repositories/snapshots/content/org/hibernate/orm/hibernate-core/6.4.7-SNAPSHOT/</resourceURI>\n" +
        "      <relativePath>/org/hibernate/orm/hibernate-core/6.4.7-SNAPSHOT/</relativePath>\n" +
        "      <text>6.4.7-SNAPSHOT</text>\n" +
        "      <leaf>false</leaf>\n" +
        "      <lastModified>2024-09-30 07:38:25.717 UTC</lastModified>\n" +
        "      <sizeOnDisk>-1</sizeOnDisk>\n" +
        "    </content-item>\n" +
        "    <content-item>\n" +
        "      <resourceURI>https://oss.sonatype.org/service/local/repositories/snapshots/content/org/hibernate/orm/hibernate-core/6.3.3-SNAPSHOT/</resourceURI>\n" +
        "      <relativePath>/org/hibernate/orm/hibernate-core/6.3.3-SNAPSHOT/</relativePath>\n" +
        "      <text>6.3.3-SNAPSHOT</text>\n" +
        "      <leaf>false</leaf>\n" +
        "      <lastModified>2024-09-30 07:38:43.942 UTC</lastModified>\n" +
        "      <sizeOnDisk>-1</sizeOnDisk>\n" +
        "    </content-item>\n" +
        "  </data>\n" +
        "</content>\n";
}