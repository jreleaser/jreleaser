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

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Writer;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * @since 1.18.0
 */
public class MavenMetadataTransformationUtils {

    private static final Pattern VERSION_PATTERN = Pattern.compile("\\d++\\.\\d++\\.\\d++.*+");
    private static final DateTimeFormatter LAST_UPDATED_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private MavenMetadataTransformationUtils() {
        // prevent instantiation
    }

    public static void recreateMetadataXml(byte[] in, String version, String groupId, String artifactId, Writer out, Comparator<String> versionComparator) throws javax.xml.stream.XMLStreamException {
        recreateMetadataXml(new ByteArrayInputStream(in), version, groupId, artifactId, out, versionComparator);
    }

    public static void recreateMetadataXml(InputStream in, String version, String groupId, String artifactId, Writer out, Comparator<String> versionComparator) throws javax.xml.stream.XMLStreamException {
        String latest = version;

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();

        XMLEventReader reader = xmlInputFactory.createXMLEventReader(in);
        XMLEventWriter writer = outFactory.createXMLEventWriter(out);

        writer.add(eventFactory.createStartDocument());
        writer.add(eventFactory.createCharacters("\n"));
        writer.add(eventFactory.createStartElement("", "", "metadata"));
        writer.add(eventFactory.createCharacters("\n  "));
        writer.add(eventFactory.createStartElement("", "", "groupId"));
        writer.add(eventFactory.createCharacters(groupId));
        writer.add(eventFactory.createEndElement("", "", "groupId"));
        writer.add(eventFactory.createCharacters("\n  "));
        writer.add(eventFactory.createStartElement("", "", "artifactId"));
        writer.add(eventFactory.createCharacters(artifactId));
        writer.add(eventFactory.createEndElement("", "", "artifactId"));
        writer.add(eventFactory.createCharacters("\n  "));
        writer.add(eventFactory.createStartElement("", "", "versioning"));
        writer.add(eventFactory.createCharacters("\n    "));
        writer.add(eventFactory.createStartElement("", "", "versions"));
        while (reader.hasNext()) {
            XMLEvent xmlEvent = reader.nextEvent();
            if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("text")) {
                XMLEvent text = reader.nextEvent();
                if (!text.isCharacters()) {
                    throw new IllegalStateException("Unexpected evet instead of characters: " + text);
                }
                String data = text.asCharacters().getData();
                if (VERSION_PATTERN.matcher(data).matches()) {
                    if (versionComparator.compare(latest, data) <= 0) {
                        latest = data;
                    }
                    writer.add(eventFactory.createCharacters("\n      "));
                    writer.add(eventFactory.createStartElement("", "", "version"));
                    writer.add(eventFactory.createCharacters(data));
                    writer.add(eventFactory.createEndElement("", "", "version"));
                    reader.nextEvent(); // just get the value out of the stream (discard)
                }
            }
        }
        writer.add(eventFactory.createCharacters("\n    "));
        writer.add(eventFactory.createEndElement("", "", "versions"));
        writer.add(eventFactory.createCharacters("\n    "));
        writer.add(eventFactory.createStartElement("", "", "latest"));
        writer.add(eventFactory.createCharacters(latest));
        writer.add(eventFactory.createEndElement("", "", "latest"));
        writer.add(eventFactory.createCharacters("\n    "));
        writer.add(eventFactory.createStartElement("", "", "lastUpdated"));
        writer.add(eventFactory.createCharacters(LAST_UPDATED_FORMAT.format(LocalDateTime.now(Clock.systemUTC()))));
        writer.add(eventFactory.createEndElement("", "", "lastUpdated"));
        writer.add(eventFactory.createCharacters("\n  "));
        writer.add(eventFactory.createEndElement("", "", "versioning"));
        writer.add(eventFactory.createCharacters("\n"));
        writer.add(eventFactory.createEndElement("", "", "metadata"));
        writer.add(eventFactory.createEndDocument());
        writer.flush();
        writer.close();
    }

    public static void mergeMetadataXml(byte[] in, String version, Writer out, Comparator<String> versionComparator) throws javax.xml.stream.XMLStreamException {
        mergeMetadataXml(new ByteArrayInputStream(in), version, out, versionComparator);
    }

    public static void mergeMetadataXml(InputStream in, String version, Writer out, Comparator<String> versionComparator) throws javax.xml.stream.XMLStreamException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();

        XMLEventReader reader = xmlInputFactory.createXMLEventReader(in);
        XMLEventWriter writer = outFactory.createXMLEventWriter(out);

        boolean hasCurrentVersion = false;

        while (reader.hasNext()) {
            XMLEvent xmlEvent = reader.nextEvent();
            if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("lastUpdated")) {
                writer.add(xmlEvent);
                reader.nextEvent(); // just get the value out of the stream (discard), we'll set one ourselves:
                writer.add(eventFactory.createCharacters(LAST_UPDATED_FORMAT.format(LocalDateTime.now(Clock.systemUTC()))));
                writer.add(reader.nextEvent());
                continue;
            }
            if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("version")) {
                writer.add(xmlEvent);
                XMLEvent ver = reader.nextEvent();
                if (!ver.isCharacters()) {
                    throw new IllegalStateException("Unexpected event when reading version value: " + ver);
                }
                hasCurrentVersion |= ver.asCharacters().getData().equals(version);
                writer.add(ver);
                writer.add(reader.nextEvent());

                continue;
            }
            if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("latest")) {
                writer.add(xmlEvent);
                XMLEvent ver = reader.nextEvent();
                if (!ver.isCharacters()) {
                    throw new IllegalStateException("Unexpected event when reading version value: " + ver);
                }
                String data = ver.asCharacters().getData();
                String latest = versionComparator.compare(version, data) <= 0 ? data : version;
                writer.add(eventFactory.createCharacters(latest));
                writer.add(reader.nextEvent());

                continue;
            }
            if (xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals("versions") && !hasCurrentVersion) {
                writer.add(eventFactory.createStartElement("", "", "version"));
                writer.add(eventFactory.createCharacters(version));
                writer.add(eventFactory.createEndElement("", "", "version"));
            }
            writer.add(xmlEvent);
        }
        writer.flush();
        writer.close();
        reader.close();
    }
}
