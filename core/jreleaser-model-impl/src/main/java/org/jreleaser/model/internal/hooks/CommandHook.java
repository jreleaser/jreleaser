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
package org.jreleaser.model.internal.hooks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.api.hooks.ExecutionEvent;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.mustache.TemplateContext;

import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.jreleaser.mustache.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public final class CommandHook extends AbstractHook<CommandHook> {
    private static final long serialVersionUID = 6669599201326933824L;

    private String cmd;

    @JsonIgnore
    private final org.jreleaser.model.api.hooks.CommandHook immutable = new org.jreleaser.model.api.hooks.CommandHook() {
        private static final long serialVersionUID = 3604503981521691484L;

        @Override
        public String getCmd() {
            return cmd;
        }

        @Override
        public Map<String, String> getEnvironment() {
            return unmodifiableMap(CommandHook.this.getEnvironment());
        }

        @Override
        public boolean isApplyDefaultMatrix() {
            return CommandHook.this.isApplyDefaultMatrix();
        }

        @Override
        public org.jreleaser.model.api.common.Matrix getMatrix() {
            return matrix.asImmutable();
        }

        @Override
        public Set<String> getPlatforms() {
            return unmodifiableSet(CommandHook.this.getPlatforms());
        }

        @Override
        public Filter getFilter() {
            return CommandHook.this.getFilter().asImmutable();
        }

        @Override
        public boolean isContinueOnError() {
            return CommandHook.this.isContinueOnError();
        }

        @Override
        public boolean isVerbose() {
            return CommandHook.this.isVerbose();
        }

        @Override
        public String getCondition() {
            return CommandHook.this.getCondition();
        }

        @Override
        public Active getActive() {
            return CommandHook.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return CommandHook.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(CommandHook.this.asMap(full));
        }
    };

    public org.jreleaser.model.api.hooks.CommandHook asImmutable() {
        return immutable;
    }

    @Override
    public void merge(CommandHook source) {
        super.merge(source);
        this.cmd = merge(this.cmd, source.cmd);
    }

    public String getResolvedCmd(JReleaserContext context, TemplateContext additionalContext, ExecutionEvent event) {
        TemplateContext props = context.fullProps().setAll(additionalContext);
        props.set("event", event);
        return resolveTemplate(cmd, props);
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    @Override
    public void asMap(boolean full, Map<String, Object> map) {
        map.put("cmd", cmd);
    }
}
