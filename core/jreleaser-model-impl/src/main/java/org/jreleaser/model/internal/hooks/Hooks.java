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
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.Matrix;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public final class Hooks extends AbstractActivatable<Hooks> implements Domain {
    private static final long serialVersionUID = 1623942143835992825L;

    private final Map<String, String> environment = new LinkedHashMap<>();
    private final CommandHooks command = new CommandHooks();
    private final ScriptHooks script = new ScriptHooks();
    private final Matrix matrix = new Matrix();

    private String condition;
    private Boolean applyDefaultMatrix;

    @JsonIgnore
    private final org.jreleaser.model.api.hooks.Hooks immutable = new org.jreleaser.model.api.hooks.Hooks() {
        private static final long serialVersionUID = -4757423955700050484L;

        @Override
        public org.jreleaser.model.api.hooks.CommandHooks getCommand() {
            return command.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.hooks.ScriptHooks getScript() {
            return script.asImmutable();
        }

        @Override
        public String getCondition() {
            return Hooks.this.getCondition();
        }

        @Override
        public Map<String, String> getEnvironment() {
            return unmodifiableMap(Hooks.this.getEnvironment());
        }

        @Override
        public boolean isApplyDefaultMatrix() {
            return Hooks.this.isApplyDefaultMatrix();
        }

        @Override
        public org.jreleaser.model.api.common.Matrix getMatrix() {
            return matrix.asImmutable();
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
        this.condition = merge(this.condition, source.condition);
        setCommand(source.command);
        setScript(source.script);
        setEnvironment(merge(this.environment, source.getEnvironment()));
        setMatrix(source.matrix);
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

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment.clear();
        this.environment.putAll(environment);
    }

    public boolean isApplyDefaultMatrixSet() {
        return null != applyDefaultMatrix;
    }

    public boolean isApplyDefaultMatrix() {
        return null != applyDefaultMatrix && applyDefaultMatrix;
    }

    public void setApplyDefaultMatrix(Boolean applyDefaultMatrix) {
        this.applyDefaultMatrix = applyDefaultMatrix;
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public void setMatrix(Matrix matrix) {
        this.matrix.merge(matrix);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", getActive());
        map.put("condition", condition);
        map.put("environment", environment);
        matrix.asMap(map);
        map.put("command", command.asMap(full));
        map.put("script", script.asMap(full));
        return map;
    }
}
