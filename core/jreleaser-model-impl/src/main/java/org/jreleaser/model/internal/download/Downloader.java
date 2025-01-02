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
package org.jreleaser.model.internal.download;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Activatable;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.EnabledAware;
import org.jreleaser.model.internal.common.ExtraProperties;
import org.jreleaser.model.internal.common.TimeoutAware;
import org.jreleaser.mustache.TemplateContext;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.model.Constants.KEY_ARTIFACT_FILE;
import static org.jreleaser.model.Constants.KEY_DOWNLOADER_NAME;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public interface Downloader<A extends org.jreleaser.model.api.download.Downloader> extends Domain, Activatable, TimeoutAware, ExtraProperties {
    String getType();

    String getName();

    void setName(String name);

    List<Asset> getAssets();

    void setAssets(List<Asset> assets);

    void addAsset(Asset asset);

    A asImmutable();

    class Unpack extends AbstractModelObject<Unpack> implements Domain, EnabledAware {
        private static final long serialVersionUID = -5735907573642807009L;

        private Boolean enabled;
        private Boolean skipRootEntry;

        @JsonIgnore
        public final org.jreleaser.model.api.download.Downloader.Unpack immutable = new org.jreleaser.model.api.download.Downloader.Unpack() {
            private static final long serialVersionUID = -6271748744186223250L;

            @Override
            public boolean isSkipRootEntry() {
                return Downloader.Unpack.this.isSkipRootEntry();
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Downloader.Unpack.this.asMap(full));
            }

            @Override
            public boolean isEnabled() {
                return Downloader.Unpack.this.isEnabled();
            }
        };

        public org.jreleaser.model.api.download.Downloader.Unpack asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Unpack source) {
            this.enabled = merge(this.enabled, source.enabled);
            this.skipRootEntry = merge(this.skipRootEntry, source.skipRootEntry);
        }

        @Override
        public boolean isEnabled() {
            return null != enabled && enabled;
        }

        @Override
        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public boolean isEnabledSet() {
            return null != enabled;
        }

        public boolean isSkipRootEntry() {
            return null != skipRootEntry && skipRootEntry;
        }

        public void setSkipRootEntry(Boolean skipRootEntry) {
            this.skipRootEntry = skipRootEntry;
        }

        public boolean isSkipRootEntrySet() {
            return null != skipRootEntry;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            if (!full && !isEnabled()) return Collections.emptyMap();

            Map<String, Object> props = new LinkedHashMap<>();
            props.put("enabled", isEnabled());
            props.put("skipRootEntry", isSkipRootEntry());

            return props;
        }
    }

    class Asset extends AbstractModelObject<Asset> implements Domain {
        private static final long serialVersionUID = -2850050928704465633L;

        private final Unpack unpack = new Unpack();
        private String input;
        private String output;

        @JsonIgnore
        private final org.jreleaser.model.api.download.Downloader.Asset immutable = new org.jreleaser.model.api.download.Downloader.Asset() {
            private static final long serialVersionUID = 2845138939915499623L;

            @Override
            public String getInput() {
                return input;
            }

            @Override
            public String getOutput() {
                return output;
            }

            @Override
            public org.jreleaser.model.api.download.Downloader.Unpack getUnpack() {
                return unpack.asImmutable();
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Downloader.Asset.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.download.Downloader.Asset asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Asset source) {
            this.input = merge(this.input, source.input);
            this.output = merge(this.output, source.output);
            setUnpack(source.unpack);
        }

        public String getResolvedInput(JReleaserContext context, Downloader<?> downloader) {
            TemplateContext p = context.getModel().props();
            p.setAll(downloader.resolvedExtraProperties());
            p.set(KEY_DOWNLOADER_NAME, downloader.getName());
            return resolveTemplate(input, p);
        }

        public String getResolvedOutput(JReleaserContext context, Downloader<?> downloader, String artifactFile) {
            if (isBlank(output)) return output;
            TemplateContext p = context.getModel().props();
            p.setAll(downloader.resolvedExtraProperties());
            p.set(KEY_DOWNLOADER_NAME, downloader.getName());
            p.set(KEY_ARTIFACT_FILE, artifactFile);
            return resolveTemplate(output, p);
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
            this.unpack.merge(unpack);
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("input", input);
            props.put("output", output);
            props.put("unpack", unpack.asMap(full));
            return props;
        }
    }
}
