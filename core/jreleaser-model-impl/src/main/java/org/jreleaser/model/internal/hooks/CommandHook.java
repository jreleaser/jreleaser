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
package org.jreleaser.model.internal.hooks;

import org.jreleaser.model.Active;
import org.jreleaser.model.api.hooks.ExecutionEvent;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.project.Project;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.mustache.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public final class CommandHook extends AbstractHook<CommandHook> {
    private String cmd;

    private final org.jreleaser.model.api.hooks.CommandHook immutable = new org.jreleaser.model.api.hooks.CommandHook() {
        @Override
        public String getCmd() {
            return cmd;
        }

        @Override
        public Filter getFilter() {
            return filter.asImmutable();
        }

        @Override
        public boolean isContinueOnError() {
            return CommandHook.this.isContinueOnError();
        }

        @Override
        public Active getActive() {
            return active;
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
        this.active = merge(this.active, source.active);
    }

    public String getResolvedCmd(JReleaserContext context, ExecutionEvent event) {
        Map<String, Object> props = context.fullProps();
        props.put("event", event);
        return resolveTemplate(cmd, props);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void disable() {
        active = Active.NEVER;
        enabled = false;
    }

    public boolean resolveEnabled(Project project) {
        if (null == active) {
            setActive(Active.ALWAYS);
        }
        enabled = active.check(project);
        return enabled;
    }

    @Override
    public Active getActive() {
        return active;
    }

    @Override
    public void setActive(Active active) {
        this.active = active;
    }

    @Override
    public void setActive(String str) {
        setActive(Active.of(str));
    }

    @Override
    public boolean isActiveSet() {
        return active != null;
    }

    @Override
    public boolean isContinueOnError() {
        return continueOnError != null && continueOnError;
    }

    @Override
    public void setContinueOnError(Boolean continueOnError) {
        this.continueOnError = continueOnError;
    }

    @Override
    public boolean isContinueOnErrorSet() {
        return continueOnError != null;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter.merge(filter);
    }

    @Override
    public void asMap(boolean full, Map<String, Object> map) {
        map.put("cmd", cmd);
    }
}
