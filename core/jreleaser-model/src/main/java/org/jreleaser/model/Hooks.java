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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public class Hooks extends AbstractModelObject<Hooks> implements Domain, Activatable {
    private final CommandHooks command = new CommandHooks();

    private Active active;
    @JsonIgnore
    private boolean enabled = true;

    @Override
    public void freeze() {
        super.freeze();
        command.freeze();
    }

    @Override
    public void merge(Hooks source) {
        freezeCheck();
        this.active = merge(this.active, source.active);
        this.enabled = merge(this.enabled, source.enabled);
        setCommand(source.command);
    }

    public boolean isSet() {
        return command.isSet();
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
            setActive(Env.resolveOrDefault("hooks.active", "", "ALWAYS"));
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

    public CommandHooks getCommand() {
        return command;
    }

    public void setCommand(CommandHooks command) {
        this.command.merge(command);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", active);
        map.put("command", command.asMap(full));
        return map;
    }
}
