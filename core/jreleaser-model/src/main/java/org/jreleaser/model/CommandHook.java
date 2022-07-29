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
package org.jreleaser.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public class CommandHook extends AbstractModelObject<CommandHook> implements Hook {
    private final Filter filter = new Filter();
    protected Boolean continueOnError;
    private String cmd;

    private Active active;
    @JsonIgnore
    private boolean enabled;

    @Override
    public void freeze() {
        super.freeze();
        filter.freeze();
    }

    @Override
    public void merge(CommandHook source) {
        freezeCheck();
        this.active = merge(this.active, source.active);
        this.enabled = merge(this.enabled, source.enabled);
        this.cmd = merge(this.cmd, source.cmd);
        this.continueOnError = merge(this.continueOnError, source.continueOnError);
        setFilter(source.filter);
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
        freezeCheck();
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
        freezeCheck();
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
        freezeCheck();
        this.cmd = cmd;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    public void setFilter(Filter filter) {
        this.filter.merge(filter);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", active);
        map.put("continueOnError", isContinueOnError());
        Map<String, Object> filterAsMap = filter.asMap(full);
        if (full || !filterAsMap.isEmpty()) {
            map.put("filter", filterAsMap);
        }
        map.put("cmd", cmd);

        return map;
    }
}
