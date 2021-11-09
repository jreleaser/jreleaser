/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.jreleaser.model.util.Artifacts.resolveForFileSet;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public class FileSet implements Domain, ExtraProperties {
    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    private final Set<String> includes = new LinkedHashSet<>();
    private final Set<String> excludes = new LinkedHashSet<>();

    private String input;
    private String output;

    void setAll(FileSet fileSet) {
        this.input = fileSet.input;
        this.output = fileSet.output;
        setIncludes(fileSet.includes);
        setExcludes(fileSet.excludes);
        setExtraProperties(fileSet.extraProperties);
    }

    @Override
    public String getPrefix() {
        return "artifact";
    }

    public String getResolvedInput(JReleaserContext context) {
        return resolveForFileSet(input, context, this);
    }

    public String getResolvedOutput(JReleaserContext context) {
        return resolveForFileSet(output, context, this);
    }

    public Set<String> getResolvedIncludes(JReleaserContext context) {
        return includes.stream()
            .map(s -> resolveForFileSet(s, context, this))
            .collect(toSet());
    }

    public Set<String> getResolvedExcludes(JReleaserContext context) {
        return excludes.stream()
            .map(s -> resolveForFileSet(s, context, this))
            .collect(toSet());
    }

    public Set<String> getIncludes() {
        return includes;
    }

    public void setIncludes(Set<String> includes) {
        this.includes.clear();
        this.includes.addAll(includes);
    }

    public Set<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes.clear();
        this.excludes.addAll(excludes);
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    @Override
    public Map<String, Object> getExtraProperties() {
        return extraProperties;
    }

    @Override
    public void setExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.clear();
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public void addExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("input", input);
        props.put("output", output);
        props.put("includes", includes);
        props.put("excludes", excludes);
        props.put("extraProperties", getResolvedExtraProperties());
        return props;
    }
}
