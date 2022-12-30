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
package org.jreleaser.model.internal.packagers;

import org.jreleaser.model.Active;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class PackagerRepository extends AbstractRepositoryTap<PackagerRepository> {
    private static final long serialVersionUID = -3486962328445966652L;

    private final org.jreleaser.model.api.packagers.PackagerRepository immutable = new org.jreleaser.model.api.packagers.PackagerRepository() {
        private static final long serialVersionUID = -3703423912099731255L;

        @Override
        public String getBasename() {
            return basename;
        }

        @Override
        public String getCanonicalRepoName() {
            return PackagerRepository.this.getCanonicalRepoName();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getTagName() {
            return tagName;
        }

        @Override
        public String getBranch() {
            return branch;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getToken() {
            return token;
        }

        @Override
        public String getCommitMessage() {
            return commitMessage;
        }

        @Override
        public Active getActive() {
            return active;
        }

        @Override
        public boolean isEnabled() {
            return PackagerRepository.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(PackagerRepository.this.asMap(full));
        }

        @Override
        public String getOwner() {
            return owner;
        }
    };

    public PackagerRepository(String basename, String tapName) {
        super(basename, tapName);
    }

    public org.jreleaser.model.api.packagers.PackagerRepository asImmutable() {
        return immutable;
    }
}