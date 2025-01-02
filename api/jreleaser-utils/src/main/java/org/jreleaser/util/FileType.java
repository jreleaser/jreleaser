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

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
public enum FileType {
    ASC("asc"),
    BAT("bat"),
    CMD("cmd"),
    DEB("deb"),
    DMG("dmg"),
    EXE("exe"),
    JAR("jar"),
    MSI("msi"),
    NUGET("nuget"),
    PKG("pkg"),
    PS1("ps1"),
    RPM("rpm"),
    SH("sh"),
    SIG("sig"),
    TAR("tar", true),
    TAR_BZ2("tar.bz2", true),
    TAR_GZ("tar.gz", true),
    TAR_XZ("tar.xz", true),
    TAR_ZST("tar.zst", true),
    TBZ2("tbz2", true),
    TGZ("tgz", true),
    TXZ("txz", true),
    ZIP("zip", true),
    ZST("zst", true);

    private final String type;
    private final boolean archive;

    FileType(String type) {
        this(type, false);
    }

    FileType(String type, boolean archive) {
        this.type = type;
        this.archive = archive;
    }

    public boolean archive() {
        return this.archive;
    }

    public String type() {
        return this.type;
    }

    public String extension() {
        return "." + this.type;
    }

    public String formatted() {
        return type();
    }

    public static FileType of(String str) {
        if (isBlank(str)) return null;
        return FileType.valueOf(str.toUpperCase(Locale.ENGLISH).trim()
            .replace(".", "_"));
    }

    public static Set<String> getSupportedTypes() {
        Set<String> set = new LinkedHashSet<>();
        for (FileType value : values()) {
            set.add(value.type());
        }
        return set;
    }

    public static Set<String> getSupportedExtensions() {
        Set<String> set = new LinkedHashSet<>();
        for (FileType value : values()) {
            set.add(value.extension());
        }
        return set;
    }

    public static Optional<FileType> getFileType(Path path) {
        if (null != path) {
            return getFileType(path.getFileName().toString());
        }
        return Optional.empty();
    }

    public static Optional<FileType> getFileType(String path) {
        if (isBlank(path)) return Optional.empty();

        for (FileType value : values()) {
            if (path.endsWith(value.extension())) {
                return Optional.of(value);
            }
        }

        return Optional.empty();
    }

    public static String getType(Path path) {
        if (null != path) {
            return getType(path.getFileName().toString());
        }
        return "";
    }

    public static String getType(String path) {
        if (isBlank(path)) return "";

        for (FileType value : values()) {
            if (path.endsWith(value.extension())) {
                return value.type();
            }
        }

        return "";
    }

    public static String getExtension(Path path) {
        if (null != path) {
            return getExtension(path.getFileName().toString());
        }
        return "";
    }

    public static String getExtension(String path) {
        if (isBlank(path)) return "";

        for (FileType value : values()) {
            if (path.endsWith(value.extension())) {
                return value.extension();
            }
        }

        return "";
    }
}