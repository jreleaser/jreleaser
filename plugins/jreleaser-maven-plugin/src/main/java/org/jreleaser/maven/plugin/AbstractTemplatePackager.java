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
package org.jreleaser.maven.plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
abstract class AbstractTemplatePackager extends AbstractPackager implements TemplatePackager {
    protected final List<String> skipTemplates = new ArrayList<>();
    protected String templateDirectory;

    void setAll(AbstractTemplatePackager packager) {
        super.setAll(packager);
        this.templateDirectory = packager.templateDirectory;
        setSkipTemplates(packager.skipTemplates);
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
    public List<String> getSkipTemplates() {
        return skipTemplates;
    }

    @Override
    public void setSkipTemplates(List<String> skipTemplates) {
        this.skipTemplates.clear();
        this.skipTemplates.addAll(skipTemplates);
    }

    @Override
    public boolean isSet() {
        return super.isSet() ||
            !skipTemplates.isEmpty() ||
            null != templateDirectory;
    }
}
