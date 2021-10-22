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

import org.jreleaser.util.Constants;
import org.jreleaser.util.Version;

import java.util.Map;

import static org.jreleaser.util.CollectionUtils.safePut;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
abstract class AbstractJavaAssembler extends AbstractAssembler implements JavaAssembler {
    private final Java java = new Java();
    protected String executable;
    protected String templateDirectory;

    protected AbstractJavaAssembler(String type) {
        super(type);
    }

    void setAll(AbstractJavaAssembler assembler) {
        super.setAll(assembler);
        this.executable = assembler.executable;
        this.templateDirectory = assembler.templateDirectory;
        setJava(assembler.java);
    }

    @Override
    public Map<String, Object> props() {
        Map<String, Object> props = super.props();
        props.put(Constants.KEY_DISTRIBUTION_EXECUTABLE, executable);
        props.putAll(java.getResolvedExtraProperties());
        safePut(Constants.KEY_DISTRIBUTION_JAVA_GROUP_ID, java.getGroupId(), props, true);
        safePut(Constants.KEY_DISTRIBUTION_JAVA_ARTIFACT_ID, java.getArtifactId(), props, true);
        safePut(Constants.KEY_DISTRIBUTION_JAVA_MAIN_CLASS, java.getMainClass(), props, true);
        if (isNotBlank(java.getVersion())) {
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION, java.getVersion());
            Version jv = Version.of(java.getVersion());
            safePut(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MAJOR, jv.getMajor(), props, true);
            safePut(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MINOR, jv.getMinor(), props, true);
            safePut(Constants.KEY_DISTRIBUTION_JAVA_VERSION_PATCH, jv.getPatch(), props, true);
            safePut(Constants.KEY_DISTRIBUTION_JAVA_VERSION_TAG, jv.getTag(), props, true);
            safePut(Constants.KEY_DISTRIBUTION_JAVA_VERSION_BUILD, jv.getBuild(), props, true);
        } else {
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MAJOR, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_MINOR, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_PATCH, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_TAG, "");
            props.put(Constants.KEY_DISTRIBUTION_JAVA_VERSION_BUILD, "");
        }
        return props;
    }

    @Override
    public String getExecutable() {
        return executable;
    }

    @Override
    public void setExecutable(String executable) {
        this.executable = executable;
    }

    @Override
    public String getTemplateDirectory() {
        return templateDirectory;
    }

    @Override
    public void setTemplateDirectory(String templateDirectory) {
        this.templateDirectory = templateDirectory;
    }

    @Override
    public Java getJava() {
        return java;
    }

    @Override
    public void setJava(Java java) {
        this.java.setAll(java);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("executable", executable);
        props.put("templateDirectory", templateDirectory);
        props.put("extraProperties", getResolvedExtraProperties());
        if (java.isEnabled()) {
            props.put("java", java.asMap(full));
        }
    }
}
