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
package org.jreleaser.ant.tasks;

import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.workflow.Workflows;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class JReleaserUploadTask extends AbstractDistributionAwareJReleaserTask {
    private final List<String> uploaderTypes = new ArrayList<>();
    private final List<String> excludedUploaderTypes = new ArrayList<>();
    private final List<String> uploaderNames = new ArrayList<>();
    private final List<String> excludedUploaderNames = new ArrayList<>();
    private final List<String> catalogers = new ArrayList<>();
    private final List<String> excludedCatalogers = new ArrayList<>();

    public void setUploaderTypes(String uploaderTypes) {
        this.uploaderTypes.addAll(expandAndCollect(uploaderTypes));
    }

    public void setExcludedUploaderTypes(String excludedUploaderTypes) {
        this.excludedUploaderTypes.addAll(expandAndCollect(excludedUploaderTypes));
    }

    public void setUploaderNames(String uploaderNames) {
        this.uploaderNames.addAll(expandAndCollect(uploaderNames));
    }

    public void setExcludedUploaderNames(String excludedUploaderNames) {
        this.excludedUploaderNames.addAll(expandAndCollect(excludedUploaderNames));
    }

    public void setUploaderTypes(List<String> uploaderTypes) {
        if (null != uploaderTypes) {
            this.uploaderTypes.addAll(uploaderTypes);
        }
    }

    public void setExcludedUploaderTypes(List<String> excludedUploaderTypes) {
        if (null != excludedUploaderTypes) {
            this.excludedUploaderTypes.addAll(excludedUploaderTypes);
        }
    }

    public void setUploaderNames(List<String> uploaderNames) {
        if (null != uploaderNames) {
            this.uploaderNames.addAll(uploaderNames);
        }
    }

    public void setExcludedUploaderNames(List<String> excludedUploaderNames) {
        if (null != excludedUploaderNames) {
            this.excludedUploaderNames.addAll(excludedUploaderNames);
        }
    }

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
    protected void doExecute(JReleaserContext context) {
        context.setIncludedUploaderTypes(collectEntries(uploaderTypes, true));
        context.setExcludedUploaderTypes(collectEntries(excludedUploaderTypes, true));
        context.setIncludedUploaderNames(uploaderNames);
        context.setExcludedUploaderNames(excludedUploaderNames);
        context.setIncludedCatalogers(collectEntries(catalogers, true));
        context.setExcludedCatalogers(collectEntries(excludedCatalogers, true));
        Workflows.upload(setupContext(context)).execute();
    }

    @Override
    protected JReleaserCommand getCommand() {
        return JReleaserCommand.UPLOAD;
    }
}
