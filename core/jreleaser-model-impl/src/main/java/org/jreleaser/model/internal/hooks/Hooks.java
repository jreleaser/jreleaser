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
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.Domain;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public final class Hooks extends AbstractActivatable<Hooks> implements Domain {
    private static final long serialVersionUID = 6972671895393136081L;

    private final CommandHooks command = new CommandHooks();
    private final ScriptHooks script = new ScriptHooks();

    @JsonIgnore
    private final org.jreleaser.model.api.hooks.Hooks immutable = new org.jreleaser.model.api.hooks.Hooks() {
        private static final long serialVersionUID = 6110061902155343412L;

        @Override
        public org.jreleaser.model.api.hooks.CommandHooks getCommand() {
            return command.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.hooks.ScriptHooks getScript() {
            return script.asImmutable();
        }

        @Override
        public Active getActive() {
            return Hooks.this.getActive();
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

    public Hooks() {
        enabledSet(true);
    }

    public org.jreleaser.model.api.hooks.Hooks asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Hooks source) {
        super.merge(source);
        setCommand(source.command);
        setScript(source.script);
    }

    @Override
    public boolean isSet() {
        return super.isSet() ||
            command.isSet() ||
            script.isSet();
    }

    public CommandHooks getCommand() {
        return command;
    }

    public void setCommand(CommandHooks command) {
        this.command.merge(command);
    }

    public ScriptHooks getScript() {
        return script;
    }

    public void setScript(ScriptHooks script) {
        this.script.merge(script);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", getActive());
        map.put("command", command.asMap(full));
        map.put("script", script.asMap(full));
        return map;
    }
}
