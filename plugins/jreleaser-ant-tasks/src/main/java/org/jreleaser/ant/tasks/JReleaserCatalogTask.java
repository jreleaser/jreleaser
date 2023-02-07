/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
package org.jreleaser.ant.tasks;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.workflow.Workflows;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public class JReleaserCatalogTask extends AbstractDistributionAwareJReleaserTask {
    private final List<String> catalogers = new ArrayList<>();
    private final List<String> excludedCatalogers = new ArrayList<>();

    public void setCatalogers(String catalogers) {
        this.catalogers.addAll(expandAndCollect(catalogers));
    }

    public void setExcludedCatalogers(String excludedCatalogers) {
        this.excludedCatalogers.addAll(expandAndCollect(excludedCatalogers));
    }

    public void setCatalogers(List<String> catalogers) {
        if (null != catalogers) {
            this.catalogers.addAll(catalogers);
        }
    }

    public void setExcludedCatalogers(List<String> excludedCatalogers) {
        if (null != excludedCatalogers) {
            this.excludedCatalogers.addAll(excludedCatalogers);
        }
    }

    @Override
    protected JReleaserContext setupContext(JReleaserContext context) {
        super.setupContext(context);
        context.setIncludedCatalogers(collectEntries(catalogers, true));
        context.setExcludedCatalogers(collectEntries(excludedCatalogers, true));
        return context;
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        Workflows.catalog(setupContext(context)).execute();
    }
}
