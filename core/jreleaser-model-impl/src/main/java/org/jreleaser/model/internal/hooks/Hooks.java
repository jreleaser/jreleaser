/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public final class Hooks extends AbstractModelObject<Hooks> implements Domain, Activatable {
    private static final long serialVersionUID = -882153513127002918L;

    private final CommandHooks command = new CommandHooks();

    private Active active;
    @JsonIgnore
    private boolean enabled = true;

    private final org.jreleaser.model.api.hooks.Hooks immutable = new org.jreleaser.model.api.hooks.Hooks() {
        private static final long serialVersionUID = -960078052893791966L;

        @Override
        public org.jreleaser.model.api.hooks.CommandHooks getCommand() {
            return command.asImmutable();
        }

        @Override
        public Active getActive() {
            return active;
        }

        @Override
        public boolean isEnabled() {
            return Hooks.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Hooks.this.asMap(full));
        }
    };

    public org.jreleaser.model.api.hooks.Hooks asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Hooks source) {
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

    @Override
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
