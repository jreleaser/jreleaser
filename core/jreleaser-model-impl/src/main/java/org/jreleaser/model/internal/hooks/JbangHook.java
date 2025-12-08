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
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.mustache.TemplateContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.mustache.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 1.20.0
 */
public final class JbangHook extends AbstractHook<JbangHook> {
    private static final long serialVersionUID = 1424105110415449550L;

    private final List<String> args = new ArrayList<>();
    private final List<String> jbangArgs = new ArrayList<>();
    private final List<String> trusts = new ArrayList<>();
    private String version;
    private String script;

    @JsonIgnore
    private final org.jreleaser.model.api.hooks.JbangHook immutable = new org.jreleaser.model.api.hooks.JbangHook() {
        private static final long serialVersionUID = 3117507984015002700L;

        @Override
        public String getVersion() {
            return version;
        }

        @Override
        public String getScript() {
            return script;
        }

        @Override
        public List<String> getArgs() {
            return unmodifiableList(args);
        }

        @Override
        public List<String> getJbangArgs() {
            return unmodifiableList(jbangArgs);
        }

        @Override
        public List<String> getTrusts() {
            return unmodifiableList(trusts);
        }

        @Override
        public Map<String, String> getEnvironment() {
            return unmodifiableMap(JbangHook.this.getEnvironment());
        }

        @Override
        public boolean isApplyDefaultMatrix() {
            return JbangHook.this.isApplyDefaultMatrix();
        }

        @Override
        public org.jreleaser.model.api.common.Matrix getMatrix() {
            return matrix.asImmutable();
        }

        @Override
        public Set<String> getPlatforms() {
            return unmodifiableSet(JbangHook.this.getPlatforms());
        }

        @Override
        public Filter getFilter() {
            return JbangHook.this.getFilter().asImmutable();
        }

        @Override
        public boolean isContinueOnError() {
            return JbangHook.this.isContinueOnError();
        }

        @Override
        public boolean isVerbose() {
            return JbangHook.this.isVerbose();
        }

        @Override
        public String getCondition() {
            return JbangHook.this.getCondition();
        }

        @Override
        public Active getActive() {
            return JbangHook.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return JbangHook.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(JbangHook.this.asMap(full));
        }
    };

    public org.jreleaser.model.api.hooks.JbangHook asImmutable() {
        return immutable;
    }

    @Override
    public void merge(JbangHook source) {
        super.merge(source);
        this.version = merge(this.version, source.version);
        this.script = merge(this.script, source.script);
        setArgs(merge(this.args, source.args));
        setJbangArgs(merge(this.jbangArgs, source.jbangArgs));
        setTrusts(merge(this.trusts, source.trusts));
    }

    public String getResolvedScript(JReleaserContext context) {
        TemplateContext props = context.fullProps();
        context.getModel().getRelease().getReleaser().fillProps(props, context);
        return resolveTemplate(context.getLogger(), script, props);
    }

    public List<String> getResolvedArgs(JReleaserContext context) {
        TemplateContext props = context.fullProps();
        context.getModel().getRelease().getReleaser().fillProps(props, context);
        return args.stream()
            .map(s -> resolveTemplate(context.getLogger(), s, props))
            .collect(toList());
    }

    public List<String> getResolvedJbangArgs(JReleaserContext context) {
        TemplateContext props = context.fullProps();
        context.getModel().getRelease().getReleaser().fillProps(props, context);
        return jbangArgs.stream()
            .map(s -> resolveTemplate(context.getLogger(), s, props))
            .collect(toList());
    }

    public List<String> getResolvedTrusts(JReleaserContext context) {
        TemplateContext props = context.fullProps();
        context.getModel().getRelease().getReleaser().fillProps(props, context);
        return trusts.stream()
            .map(s -> resolveTemplate(context.getLogger(), s, props))
            .collect(toList());
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args.clear();
        this.args.addAll(args);
    }

    public List<String> getJbangArgs() {
        return jbangArgs;
    }

    public void setJbangArgs(List<String> jbangArgs) {
        this.jbangArgs.clear();
        this.jbangArgs.addAll(jbangArgs);
    }

    public List<String> getTrusts() {
        return trusts;
    }

    public void setTrusts(List<String> trusts) {
        this.trusts.clear();
        this.trusts.addAll(trusts);
    }

    @Override
    public void asMap(boolean full, Map<String, Object> map) {
        map.put("version", version);
        map.put("script", script);
        map.put("args", args);
        map.put("jbangArgs", jbangArgs);
        map.put("trusts", trusts);
    }
}
