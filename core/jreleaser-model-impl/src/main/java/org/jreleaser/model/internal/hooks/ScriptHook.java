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
 * @since 1.6.0
 */
public final class ScriptHook extends AbstractHook<ScriptHook> {
    private static final long serialVersionUID = -8731244470036406905L;

    private String run;
    private org.jreleaser.model.api.hooks.ScriptHook.Shell shell = org.jreleaser.model.api.hooks.ScriptHook.Shell.BASH;

    @JsonIgnore
    private final org.jreleaser.model.api.hooks.ScriptHook immutable = new org.jreleaser.model.api.hooks.ScriptHook() {
        private static final long serialVersionUID = -338908878151893273L;

        @Override
        public String getRun() {
            return run;
        }

        @Override
        public Shell getShell() {
            return shell;
        }

        @Override
        public Map<String, String> getEnvironment() {
            return unmodifiableMap(ScriptHook.this.getEnvironment());
        }

        @Override
        public boolean isApplyDefaultMatrix() {
            return ScriptHook.this.isApplyDefaultMatrix();
        }

        @Override
        public org.jreleaser.model.api.common.Matrix getMatrix() {
            return matrix.asImmutable();
        }

        @Override
        public Set<String> getPlatforms() {
            return unmodifiableSet(ScriptHook.this.getPlatforms());
        }

        @Override
        public Filter getFilter() {
            return ScriptHook.this.getFilter().asImmutable();
        }

        @Override
        public boolean isContinueOnError() {
            return ScriptHook.this.isContinueOnError();
        }

        @Override
        public boolean isVerbose() {
            return ScriptHook.this.isVerbose();
        }

        @Override
        public String getCondition() {
            return ScriptHook.this.getCondition();
        }

        @Override
        public Active getActive() {
            return ScriptHook.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return ScriptHook.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(ScriptHook.this.asMap(full));
        }
    };

    public org.jreleaser.model.api.hooks.ScriptHook asImmutable() {
        return immutable;
    }

    @Override
    public void merge(ScriptHook source) {
        super.merge(source);
        this.run = merge(this.run, source.run);
        this.shell = merge(this.shell, source.shell);
    }

    public String getResolvedRun(JReleaserContext context, TemplateContext additionalContext, ExecutionEvent event) {
        TemplateContext props = context.fullProps().setAll(additionalContext);
        props.set("event", event);
        return resolveTemplate(run, props);
    }

    public String getRun() {
        return run;
    }

    public void setRun(String run) {
        this.run = run;
    }

    public org.jreleaser.model.api.hooks.ScriptHook.Shell getShell() {
        return shell;
    }

    public void setShell(org.jreleaser.model.api.hooks.ScriptHook.Shell shell) {
        this.shell = shell;
    }

    public void setShell(String shell) {
        setShell(org.jreleaser.model.api.hooks.ScriptHook.Shell.of(shell));
    }

    @Override
    public void asMap(boolean full, Map<String, Object> map) {
        map.put("shell", shell);
        map.put("run", run);
    }
}
