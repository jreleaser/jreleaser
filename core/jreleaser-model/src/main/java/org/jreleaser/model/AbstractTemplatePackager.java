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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
public abstract class AbstractTemplatePackager<S extends AbstractTemplatePackager<S>> extends AbstractPackager<S> implements TemplatePackager {
    protected final List<String> skipTemplates = new ArrayList<>();
    protected String templateDirectory;

    protected AbstractTemplatePackager(String type) {
        super(type);
    }

    @Override
    public void merge(S packager) {
        freezeCheck();
        super.merge(packager);
        this.templateDirectory = merge(this.templateDirectory, packager.templateDirectory);
        setSkipTemplates(merge(this.skipTemplates, packager.skipTemplates));
    }

    @Override
    public String getTemplateDirectory() {
        return templateDirectory;
    }

    @Override
    public void setTemplateDirectory(String templateDirectory) {
        freezeCheck();
        this.templateDirectory = templateDirectory;
    }

    @Override
    public List<String> getSkipTemplates() {
        return freezeWrap(skipTemplates);
    }

    @Override
    public void setSkipTemplates(List<String> skipTemplates) {
        freezeCheck();
        this.skipTemplates.clear();
        this.skipTemplates.addAll(skipTemplates);
    }

    @Override
    public void addSkipTemplates(List<String> templates) {
        freezeCheck();
        this.skipTemplates.addAll(templates);
    }

    @Override
    public void addSkipTemplate(String template) {
        freezeCheck();
        if (isNotBlank(template)) {
            this.skipTemplates.add(template.trim());
        }
    }

    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("templateDirectory", templateDirectory);
        props.put("skipTemplates", skipTemplates);
    }
}
