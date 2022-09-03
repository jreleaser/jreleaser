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
package org.jreleaser.model.spi.release;

import java.util.Objects;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Commit {
    private final String shortHash;
    private final String fullHash;
    private final String refName;

    public Commit(String shortHash, String fullHash, String refName) {
        this.shortHash = shortHash;
        this.fullHash = fullHash;
        this.refName = refName;
    }

    public String getShortHash() {
        return shortHash;
    }

    public String getFullHash() {
        return fullHash;
    }

    public String getRefName() {
        return refName;
    }

    @Override
    public String toString() {
        return "Commit[" +
            "shortHash='" + shortHash + '\'' +
            ", fullHash='" + fullHash + '\'' +
            ", refName='" + refName + '\'' +
            "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Commit that = (Commit) o;
        return shortHash.equals(that.shortHash) &&
            fullHash.equals(that.fullHash) &&
            refName.equals(that.refName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortHash, fullHash, refName);
    }
}
