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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Activatable;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.util.Env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public final class CommandHooks extends AbstractModelObject<CommandHooks> implements Domain, Activatable {
    private static final long serialVersionUID = 2902577556347608164L;

    private final List<CommandHook> before = new ArrayList<>();
    private final List<CommandHook> success = new ArrayList<>();
    private final List<CommandHook> failure = new ArrayList<>();

    private Active active;
    @JsonIgnore
    private boolean enabled = true;

    private final org.jreleaser.model.api.hooks.CommandHooks immutable = new org.jreleaser.model.api.hooks.CommandHooks() {
        private static final long serialVersionUID = 5109938718153117453L;

        private List<? extends org.jreleaser.model.api.hooks.CommandHook> before;
        private List<? extends org.jreleaser.model.api.hooks.CommandHook> success;
        private List<? extends org.jreleaser.model.api.hooks.CommandHook> failure;

        @Override
        public List<? extends org.jreleaser.model.api.hooks.CommandHook> getBefore() {
            if (null == before) {
                before = CommandHooks.this.before.stream()
                    .map(CommandHook::asImmutable)
                    .collect(toList());
            }
            return before;
        }

        @Override
        public List<? extends org.jreleaser.model.api.hooks.CommandHook> getSuccess() {
            if (null == success) {
                success = CommandHooks.this.success.stream()
                    .map(CommandHook::asImmutable)
                    .collect(toList());
            }
            return success;
        }

        @Override
        public List<? extends org.jreleaser.model.api.hooks.CommandHook> getFailure() {
            if (null == failure) {
                failure = CommandHooks.this.failure.stream()
                    .map(CommandHook::asImmutable)
                    .collect(toList());
            }
            return failure;
        }

        @Override
        public Active getActive() {
            return active;
        }

        @Override
        public boolean isEnabled() {
            return CommandHooks.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return Collections.unmodifiableMap(CommandHooks.this.asMap(full));
        }
    };

    public org.jreleaser.model.api.hooks.CommandHooks asImmutable() {
        return immutable;
    }

    @Override
    public void merge(CommandHooks source) {
        this.active = merge(this.active, source.active);
        this.enabled = merge(this.enabled, source.enabled);
        setBefore(merge(this.before, source.before));
        setSuccess(merge(this.success, source.success));
        setFailure(merge(this.failure, source.failure));
    }

    public boolean isSet() {
        return !before.isEmpty() ||
            !success.isEmpty() ||
            !failure.isEmpty();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void disable() {
        active = Active.NEVER;
        enabled = false;
    }

    public boolean resolveEnabled(Project project) {
        if (null == active) {
            setActive(Env.resolveOrDefault("hooks.command.active", "", "ALWAYS"));
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

    public List<CommandHook> getBefore() {
        return before;
    }

    public void setBefore(List<CommandHook> before) {
        this.before.clear();
        this.before.addAll(before);
    }

    public List<CommandHook> getSuccess() {
        return success;
    }

    public void setSuccess(List<CommandHook> success) {
        this.success.clear();
        this.success.addAll(success);
    }

    public List<CommandHook> getFailure() {
        return failure;
    }

    public void setFailure(List<CommandHook> failure) {
        this.failure.clear();
        this.failure.addAll(failure);
    }

    public void addBefore(CommandHook hook) {
        if (null != hook) {
            this.before.add(hook);
        }
    }

    public void addSuccess(CommandHook hook) {
        if (null != hook) {
            this.success.add(hook);
        }
    }

    public void addFailure(CommandHook hook) {
        if (null != hook) {
            this.failure.add(hook);
        }
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", active);

        Map<String, Map<String, Object>> m = new LinkedHashMap<>();
        int i = 0;
        for (CommandHook hook : getBefore()) {
            m.put("hook " + (i++), hook.asMap(full));
        }
        map.put("before", m);

        m = new LinkedHashMap<>();
        i = 0;
        for (CommandHook hook : getSuccess()) {
            m.put("hook " + (i++), hook.asMap(full));
        }
        map.put("success", m);

        m = new LinkedHashMap<>();
        i = 0;
        for (CommandHook hook : getFailure()) {
            m.put("hook " + (i++), hook.asMap(full));
        }
        map.put("failure", m);

        return map;
    }
}
