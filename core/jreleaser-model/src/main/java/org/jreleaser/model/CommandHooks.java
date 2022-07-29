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
import org.jreleaser.util.Env;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public class CommandHooks extends AbstractModelObject<CommandHooks> implements Domain, Activatable {
    private final List<CommandHook> before = new ArrayList<>();
    private final List<CommandHook> success = new ArrayList<>();
    private final List<CommandHook> failure = new ArrayList<>();

    private Active active;
    @JsonIgnore
    private boolean enabled = true;

    @Override
    public void freeze() {
        super.freeze();
        before.forEach(CommandHook::freeze);
        success.forEach(CommandHook::freeze);
        failure.forEach(CommandHook::freeze);
    }

    @Override
    public void merge(CommandHooks source) {
        freezeCheck();
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

    public List<CommandHook> getBefore() {
        return freezeWrap(before);
    }

    public void setBefore(List<CommandHook> before) {
        freezeCheck();
        this.before.clear();
        this.before.addAll(before);
    }

    public List<CommandHook> getSuccess() {
        return freezeWrap(success);
    }

    public void setSuccess(List<CommandHook> success) {
        freezeCheck();
        this.success.clear();
        this.success.addAll(success);
    }

    public List<CommandHook> getFailure() {
        return freezeWrap(failure);
    }

    public void setFailure(List<CommandHook> failure) {
        freezeCheck();
        this.failure.clear();
        this.failure.addAll(failure);
    }

    public void addBefore(CommandHook hook) {
        freezeCheck();
        if (null != hook) {
            this.before.add(hook);
        }
    }

    public void addSuccess(CommandHook hook) {
        freezeCheck();
        if (null != hook) {
            this.success.add(hook);
        }
    }

    public void addFailure(CommandHook hook) {
        freezeCheck();
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
