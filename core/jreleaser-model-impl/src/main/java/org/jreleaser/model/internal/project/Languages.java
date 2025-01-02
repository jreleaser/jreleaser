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
package org.jreleaser.model.internal.project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Constants;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.Java;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.version.SemanticVersion;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.16.0
 */
public final class Languages extends AbstractModelObject<Languages> implements Domain {
    private static final long serialVersionUID = -3588782616807329125L;

    private final Java java = new Java();

    @JsonIgnore
    private final org.jreleaser.model.api.project.Languages immutable = new org.jreleaser.model.api.project.Languages() {
        private static final long serialVersionUID = -5183387318385144903L;

        @Override
        public org.jreleaser.model.api.common.Java getJava() {
            return java.asImmutable();
        }


        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Languages.this.asMap(full));
        }
    };

    public org.jreleaser.model.api.project.Languages asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Languages source) {
        setJava(source.java);
    }

    public Java getJava() {
        return java;
    }

    public void setJava(Java java) {
        this.java.merge(java);
    }

    public boolean isEnabled() {
        return java.isEnabled();
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (java.isEnabled() || full) {
            map.put("java", java.asMap(full));
        }
        return map;
    }

    public void fillProperties(TemplateContext props) {
        if (java.isEnabled()) {
            props.setAll(java.resolvedExtraProperties());
            props.set(Constants.KEY_PROJECT_JAVA_GROUP_ID, java.getGroupId());
            props.set(Constants.KEY_PROJECT_JAVA_ARTIFACT_ID, java.getArtifactId());
            String javaVersion = java.getVersion();
            props.set(Constants.KEY_PROJECT_JAVA_VERSION, javaVersion);
            props.set(Constants.KEY_PROJECT_JAVA_MAIN_CLASS, java.getMainClass());
            if (isNotBlank(javaVersion)) {
                SemanticVersion jv = SemanticVersion.of(javaVersion);
                props.set(Constants.KEY_PROJECT_JAVA_VERSION_MAJOR, jv.getMajor());
                if (jv.hasMinor()) props.set(Constants.KEY_PROJECT_JAVA_VERSION_MINOR, jv.getMinor());
                if (jv.hasPatch()) props.set(Constants.KEY_PROJECT_JAVA_VERSION_PATCH, jv.getPatch());
                if (jv.hasTag()) props.set(Constants.KEY_PROJECT_JAVA_VERSION_TAG, jv.getTag());
                if (jv.hasBuild()) props.set(Constants.KEY_PROJECT_JAVA_VERSION_BUILD, jv.getBuild());
            }
        }
    }
}
