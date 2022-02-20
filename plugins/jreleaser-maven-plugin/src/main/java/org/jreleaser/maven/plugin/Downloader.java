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
package org.jreleaser.maven.plugin;

import java.util.List;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public interface Downloader extends Activatable, TimeoutAware, ExtraProperties {
    String getName();

    void setName(String name);

    List<Asset> getAssets();

    void setAssets(List<Asset> assets);

    class Unpack implements EnabledAware {
        private Boolean enabled;
        private Boolean skipRootEntry;

        void setAll(Unpack unpack) {
            this.enabled = unpack.enabled;
            this.skipRootEntry = unpack.skipRootEntry;
        }

        @Override
        public boolean isEnabled() {
            return enabled != null && enabled;
        }

        @Override
        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public boolean isEnabledSet() {
            return enabled != null;
        }

        public boolean isSkipRootEntry() {
            return skipRootEntry != null && skipRootEntry;
        }

        public void setSkipRootEntry(Boolean skipRootEntry) {
            this.skipRootEntry = skipRootEntry;
        }

        public boolean isSkipRootEntrySet() {
            return skipRootEntry != null;
        }
    }

    class Asset {
        private final Unpack unpack = new Unpack();
        private String input;
        private String output;

        void setAll(Asset asset) {
            this.input = asset.input;
            this.output = asset.output;
            setUnpack(asset.unpack);
        }

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

        public String getOutput() {
            return output;
        }

        public void setOutput(String output) {
            this.output = output;
        }

        public Unpack getUnpack() {
            return unpack;
        }

        public void setUnpack(Unpack unpack) {
            this.unpack.setAll(unpack);
        }
    }
}
