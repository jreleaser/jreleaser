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
package org.jreleaser.assemblers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.jreleaser.bundle.RB;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.catalog.swid.SwidTag;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.model.internal.util.VersionUtils;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.ChecksumUtils;
import org.jreleaser.version.SemanticVersion;
import org.jreleaser.version.Version;

import java.io.IOException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * @author Andres Almiray
 * @since 1.11.0
 */
public final class SwidTagGenerator {
    private SwidTagGenerator() {
        // noop
    }

    public static void generateTag(JReleaserContext context, Path archiveDirectory, SwidTag tag) throws IOException {
        SoftwareIdentity softwareIdentity = createSoftwareIdentity(context, tag);
        addEntities(softwareIdentity, tag);
        addPayload(context, softwareIdentity, tag, archiveDirectory);

        ObjectMapper xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            lineSeparator() +
            xmlMapper.writeValueAsString(softwareIdentity);

        Path tagFile = archiveDirectory.resolve(tag.getPath())
            .resolve(tag.getName() + ".xml");
        Files.createDirectories(tagFile.getParent());
        Files.write(tagFile, xml.getBytes(UTF_8), CREATE, WRITE, TRUNCATE_EXISTING);
    }

    private static SoftwareIdentity createSoftwareIdentity(JReleaserContext context, SwidTag tag) {
        SoftwareIdentity o = new SoftwareIdentity();

        Project project = context.getModel().getProject();
        o.setName(project.getName());
        o.setTagId(tag.getTagId());
        o.setTagVersion(tag.getTagVersion().toString());
        o.setVersion(project.getResolvedVersion());
        o.setCorpus(tag.getCorpus());
        o.setPatch(tag.getPatch());
        o.setLang(tag.getLang());

        Pattern versionPattern = VersionUtils.resolveVersionPattern(context);
        Version<?> version = VersionUtils.version(context, project.getResolvedVersion(), versionPattern);
        if (version instanceof SemanticVersion) {
            o.setVersionScheme("semver");
        } else {
            o.setVersionScheme("alphanumeric");
        }

        return o;
    }

    private static void addEntities(SoftwareIdentity softwareIdentity, SwidTag tag) {
        tag.getEntities().stream()
            .map(e -> {
                Entity o = new Entity();
                o.setName(e.getName());
                o.setRegid(e.getRegid());
                o.setRole(String.join(" ", e.getRoles()));
                return o;
            }).forEach(softwareIdentity.getEntities()::add);
    }

    private static void addPayload(JReleaserContext context, SoftwareIdentity softwareIdentity, SwidTag tag, Path inputPath) throws IOException {
        FileTagger tagger = new FileTagger(context.getLogger());
        Files.walkFileTree(inputPath, tagger);
        if (tagger.isSuccessful()) {
            softwareIdentity.getPayload().getDirectories().add(tagger.getRoot());
        } else {
            throw new IOException(RB.$("ERROR_swid_generator", inputPath));
        }
    }

    private static class FileTagger implements FileVisitor<Path> {
        private final JReleaserLogger logger;
        private final Deque<Directory> directories = new ArrayDeque<>();
        private Directory root = new Directory();
        private boolean success = true;

        private FileTagger(JReleaserLogger logger) {
            this.logger = logger;
        }

        public Directory getRoot() {
            return root;
        }

        public boolean isSuccessful() {
            return success;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Directory d = new Directory();
            d.setName(dir.getFileName().toString());
            if (directories.isEmpty()) {
                root = d;
            } else {
                directories.peek().getDirectories().add(d);
            }
            directories.addFirst(d);
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            directories.removeFirst();
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            File f = new File();
            f.setName(file.getFileName().toString());
            f.setSize(Files.size(file));

            byte[] data = Files.readAllBytes(file);
            f.setSha256(ChecksumUtils.checksum(Algorithm.SHA_256, data));
            f.setSha512(ChecksumUtils.checksum(Algorithm.SHA_512, data));

            directories.peek().getFiles().add(f);
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
            if (e instanceof FileSystemLoopException) {
                logger.error(RB.$("ERROR_files_cycle"), file);
            } else {
                logger.error(RB.$("ERROR_files_read"), file, e);
            }
            success = false;
            return CONTINUE;
        }
    }

    public static class SoftwareIdentity {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "Entity")
        private Set<Entity> entities = new TreeSet<>();
        @JacksonXmlProperty(localName = "Payload")
        private Payload payload = new Payload();
        @JacksonXmlProperty(isAttribute = true)
        private Boolean corpus;
        @JacksonXmlProperty(isAttribute = true)
        private Boolean patch;
        @JacksonXmlProperty(isAttribute = true)
        private String name;
        @JacksonXmlProperty(isAttribute = true)
        private String tagId;
        @JacksonXmlProperty(isAttribute = true)
        private String tagVersion;
        @JacksonXmlProperty(isAttribute = true)
        private String version;
        @JacksonXmlProperty(isAttribute = true)
        private String versionScheme;
        @JacksonXmlProperty(isAttribute = true, localName = "xml:lang")
        private String lang = "en-US";
        @JacksonXmlProperty(isAttribute = true)
        private String xmlns = "http://standards.iso.org/iso/19770/-2/2015/schema.xsd";

        public String getXmlns() {
            return xmlns;
        }

        public void setXmlns(String xmlns) {
            this.xmlns = xmlns;
        }

        public Set<Entity> getEntities() {
            return entities;
        }

        public void setEntities(Set<Entity> entities) {
            this.entities = entities;
        }

        public Payload getPayload() {
            return payload;
        }

        public void setPayload(Payload payload) {
            this.payload = payload;
        }

        public Boolean isCorpus() {
            return corpus;
        }

        public void setCorpus(Boolean corpus) {
            this.corpus = corpus;
        }

        public Boolean isPatch() {
            return patch;
        }

        public void setPatch(Boolean patch) {
            this.patch = patch;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTagId() {
            return tagId;
        }

        public void setTagId(String tagId) {
            this.tagId = tagId;
        }

        public String getTagVersion() {
            return tagVersion;
        }

        public void setTagVersion(String tagVersion) {
            this.tagVersion = tagVersion;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getVersionScheme() {
            return versionScheme;
        }

        public void setVersionScheme(String versionScheme) {
            this.versionScheme = versionScheme;
        }

        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }
    }

    public static class Entity implements Comparable<Entity> {
        @JacksonXmlProperty(isAttribute = true)
        private String name;
        @JacksonXmlProperty(isAttribute = true)
        private String regid;
        @JacksonXmlProperty(isAttribute = true)
        private String role;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRegid() {
            return regid;
        }

        public void setRegid(String regid) {
            this.regid = regid;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        @Override
        public int compareTo(Entity o) {
            return Comparator.comparing(Entity::getName).compare(this, o);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entity entity = (Entity) o;
            return name.equals(entity.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    public abstract static class ResourceCollection {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "Directory")
        private Set<Directory> directories = new TreeSet<>();
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "File")
        private Set<File> files = new TreeSet<>();

        public Set<Directory> getDirectories() {
            return directories;
        }

        public void setDirectories(Set<Directory> directories) {
            this.directories = directories;
        }

        public Set<File> getFiles() {
            return files;
        }

        public void setFiles(Set<File> files) {
            this.files = files;
        }
    }

    public static class Payload extends ResourceCollection {
        @JacksonXmlProperty(isAttribute = true, localName = "xmlns:SHA-256")
        private String sha256 = "http://www.w3.org/2001/04/xmlenc#sha256";
        @JacksonXmlProperty(isAttribute = true, localName = "xmlns:SHA-512")
        private String sha512 = "http://www.w3.org/2001/04/xmlenc#sha512";

        public String getSha256() {
            return sha256;
        }

        public void setSha256(String sha256) {
            this.sha256 = sha256;
        }

        public String getSha512() {
            return sha512;
        }

        public void setSha512(String sha512) {
            this.sha512 = sha512;
        }
    }

    public static class FileSystemItem implements Comparable<FileSystemItem> {
        @JacksonXmlProperty(isAttribute = true)
        private Boolean key;
        @JacksonXmlProperty(isAttribute = true)
        private String location;
        @JacksonXmlProperty(isAttribute = true)
        private String name;
        @JacksonXmlProperty(isAttribute = true)
        private String root;

        public Boolean isKey() {
            return key;
        }

        public void setKey(Boolean key) {
            this.key = key;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRoot() {
            return root;
        }

        public void setRoot(String root) {
            this.root = root;
        }

        @Override
        public int compareTo(FileSystemItem o) {
            return Comparator.comparing(FileSystemItem::getName).compare(this, o);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FileSystemItem entity = (FileSystemItem) o;
            return name.equals(entity.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    @SuppressWarnings("checkstyle:equalsHashCode")
    public static class Directory extends FileSystemItem {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "Directory")
        private Set<Directory> directories = new TreeSet<>();
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "File")
        private Set<File> files = new TreeSet<>();

        public Set<Directory> getDirectories() {
            return directories;
        }

        public void setDirectories(Set<Directory> directories) {
            this.directories = directories;
        }

        public Set<File> getFiles() {
            return files;
        }

        public void setFiles(Set<File> files) {
            this.files = files;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            Directory directory = (Directory) o;
            return directories.equals(directory.directories) &&
                files.equals(directory.files);
        }
    }

    @SuppressWarnings("checkstyle:EqualsHashCode")
    public static class File extends FileSystemItem {
        @JacksonXmlProperty(isAttribute = true)
        private long size;
        @JacksonXmlProperty(isAttribute = true)
        private String version;
        @JacksonXmlProperty(isAttribute = true, localName = "SHA-256:hash")
        private String sha256;
        @JacksonXmlProperty(isAttribute = true, localName = "SHA-512:hash")
        private String sha512;

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getSha256() {
            return sha256;
        }

        public void setSha256(String sha256) {
            this.sha256 = sha256;
        }

        public String getSha512() {
            return sha512;
        }

        public void setSha512(String sha512) {
            this.sha512 = sha512;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            File file = (File) o;
            return size == file.size &&
                version.equals(file.version) &&
                sha256.equals(file.sha256) &&
                sha512.equals(file.sha512);
        }
    }
}
