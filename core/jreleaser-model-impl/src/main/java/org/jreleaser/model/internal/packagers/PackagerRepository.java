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
package org.jreleaser.model.internal.packagers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class PackagerRepository extends AbstractRepositoryTap<PackagerRepository> {
    private static final long serialVersionUID = 2883013964120856725L;

    @JsonIgnore
    private final org.jreleaser.model.api.packagers.PackagerRepository immutable = new org.jreleaser.model.api.packagers.PackagerRepository() {
        private static final long serialVersionUID = -7091986811979877948L;

        @Override
        public String getBasename() {
            return PackagerRepository.this.getBasename();
        }

        @Override
        public String getCanonicalRepoName() {
            return PackagerRepository.this.getCanonicalRepoName();
        }

        @Override
        public String getName() {
            return PackagerRepository.this.getName();
        }

        @Override
        public String getTagName() {
            return PackagerRepository.this.getTagName();
        }

        @Override
        public String getBranch() {
            return PackagerRepository.this.getBranch();
        }

        @Override
        public String getBranchPush() {
            return PackagerRepository.this.getBranchPush();
        }

        @Override
        public String getUsername() {
            return PackagerRepository.this.getUsername();
        }

        @Override
        public String getToken() {
            return PackagerRepository.this.getToken();
        }

        @Override
        public String getCommitMessage() {
            return PackagerRepository.this.getCommitMessage();
        }

        @Override
        public Active getActive() {
            return PackagerRepository.this.getActive();
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
            return PackagerRepository.this.getOwner();
        }

        @Override
        public String getPrefix() {
            return PackagerRepository.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(PackagerRepository.this.getExtraProperties());
        }
    };

    public PackagerRepository(String basename, String tapName) {
        super(basename, tapName);
    }

    public org.jreleaser.model.api.packagers.PackagerRepository asImmutable() {
        return immutable;
    }

    @Override
    public String prefix() {
        return "repository";
    }
}