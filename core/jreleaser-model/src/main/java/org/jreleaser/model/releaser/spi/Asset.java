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
package org.jreleaser.model.releaser.spi;

import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;

import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public class Asset {
    private final Type type;
    private final Artifact artifact;
    private final Distribution distribution;
    private final Path path;
    private final String filename;

    private Asset(Type type, Artifact artifact) {
        this(type, artifact, null);
    }

    private Asset(Type type, Artifact artifact, Distribution distribution) {
        this.type = type;
        this.artifact = artifact;
        this.distribution = distribution;
        this.path = artifact.getEffectivePath();
        this.filename = this.path.getFileName().toString();
    }

    public Type getType() {
        return type;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public Distribution getDistribution() {
        return distribution;
    }

    public Path getPath() {
        return path;
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Asset asset = (Asset) o;
        return path.equals(asset.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    public static Asset file(Path path) {
        return new Asset(Type.FILE, Artifact.of(path));
    }

    public static Asset checksum(Path path) {
        return new Asset(Type.CHECKSUM, Artifact.of(path));
    }

    public static Asset signature(Path path) {
        return new Asset(Type.SIGNATURE, Artifact.of(path));
    }

    public static Asset file(Artifact artifact) {
        return new Asset(Type.FILE, artifact);
    }

    public static Asset checksum(Artifact artifact) {
        return new Asset(Type.CHECKSUM, artifact);
    }

    public static Asset signature(Artifact artifact) {
        return new Asset(Type.SIGNATURE, artifact);
    }

    public static Asset file(Artifact artifact, Distribution distribution) {
        return new Asset(Type.FILE, artifact, distribution);
    }

    enum Type {
        CHECKSUM,
        FILE,
        SIGNATURE
    }
}
