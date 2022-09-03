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
package org.jreleaser.ant.tasks;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.workflow.Workflows;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserFullReleaseTask extends AbstractPackagerAwareJReleaserTask {
    private final List<String> uploaderTypes = new ArrayList<>();
    private final List<String> excludedUploaderTypes = new ArrayList<>();
    private final List<String> uploaderNames = new ArrayList<>();
    private final List<String> excludedUploaderNames = new ArrayList<>();
    private final List<String> announcers = new ArrayList<>();
    private final List<String> excludedAnnouncers = new ArrayList<>();

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

    public void setAnnouncers(String announcers) {
        this.announcers.addAll(expandAndCollect(announcers));
    }

    public void setExcludedAnnouncers(String excludedAnnouncers) {
        this.excludedAnnouncers.addAll(expandAndCollect(excludedAnnouncers));
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

    public void setAnnouncers(List<String> announcers) {
        if (null != announcers) {
            this.announcers.addAll(announcers);
        }
    }

    public void setExcludedAnnouncers(List<String> excludedAnnouncers) {
        if (null != excludedAnnouncers) {
            this.excludedAnnouncers.addAll(excludedAnnouncers);
        }
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        context.setIncludedUploaderTypes(uploaderTypes);
        context.setExcludedUploaderTypes(excludedUploaderTypes);
        context.setIncludedUploaderNames(uploaderNames);
        context.setExcludedUploaderNames(excludedUploaderNames);
        context.setIncludedAnnouncers(announcers);
        context.setExcludedAnnouncers(excludedAnnouncers);
        Workflows.fullRelease(setupContext(context)).execute();
    }
}
