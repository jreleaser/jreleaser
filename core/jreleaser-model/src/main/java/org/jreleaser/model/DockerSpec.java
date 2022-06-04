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

import org.jreleaser.util.PlatformUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public class DockerSpec extends AbstractDockerConfiguration<DockerSpec> {
    private final Map<String, Object> matchers = new LinkedHashMap<>();
    private Artifact artifact;

    private String name;

    @Override
    public void freeze() {
        super.freeze();
        if (null != artifact) artifact.freeze();
    }

    @Override
    public void merge(DockerSpec docker) {
        freezeCheck();
        super.merge(docker);
        this.name = merge(this.name, docker.name);
        this.artifact = docker.artifact;
        setMatchers(merge(this.matchers, docker.matchers));
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public void setArtifact(Artifact artifact) {
        freezeCheck();
        this.artifact = artifact;
        this.artifact.activate();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        freezeCheck();
        this.name = name;
    }

    public Map<String, Object> getMatchers() {
        return freezeWrap(matchers);
    }

    public void setMatchers(Map<String, Object> matchers) {
        freezeCheck();
        this.matchers.putAll(matchers);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> props = super.asMap(full);

        if (!props.isEmpty()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(name, props);
            return map;
        }

        return props;
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("matchers", matchers);
        if (artifact != null) {
            props.put("artifact", artifact.asMap(full));
        }
    }

    public boolean matches(Artifact artifact) {
        boolean matched = true;

        for (Map.Entry<String, Object> e : matchers.entrySet()) {
            String key = e.getKey();
            if ("platform".equals(key)) {
                matched &= PlatformUtils.isCompatible(String.valueOf(e.getValue()), artifact.getPlatform());
            } else if (artifact.getExtraProperties().containsKey(key)) {
                matched &= e.getValue().equals(artifact.getExtraProperties().get(key));
            }
        }

        return matched;
    }
}
