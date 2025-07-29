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
package org.jreleaser.model.internal.tools;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.mustache.TemplateContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.20.0
 */
public final class Jbang extends AbstractModelObject<Jbang> implements Domain {
    //private static final long serialVersionUID = 2934083013130665535L;

    private final List<String> args = new ArrayList<>();
    private final List<String> jbangArgs = new ArrayList<>();
    private final List<String> trusts = new ArrayList<>();
    private String version;
    private String script;

    @JsonIgnore
    private final org.jreleaser.model.api.tools.Jbang immutable = new org.jreleaser.model.api.tools.Jbang() {
        private static final long serialVersionUID = -684610546142494357L;

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
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Jbang.this.asMap(full));
        }
    };

    public org.jreleaser.model.api.tools.Jbang asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Jbang source) {
        this.version = merge(this.version, source.version);
        this.script = merge(this.script, source.script);
        setArgs(merge(this.args, source.args));
        setJbangArgs(merge(this.jbangArgs, source.jbangArgs));
        setTrusts(merge(this.trusts, source.trusts));
    }

    public boolean isSet() {
        return isNotBlank(script);
    }

    public String getResolvedScript(JReleaserContext context) {
        TemplateContext props = context.fullProps();
        context.getModel().getRelease().getReleaser().fillProps(props, context.getModel());
        return resolveTemplate(script, props);
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
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("version", version);
        props.put("script", script);
        props.put("args", args);
        props.put("jbangArgs", jbangArgs);
        props.put("trusts", trusts);
        return props;
    }
}
