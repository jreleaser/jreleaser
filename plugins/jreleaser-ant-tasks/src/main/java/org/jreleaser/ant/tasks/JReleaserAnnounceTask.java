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
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.workflow.Workflows;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserAnnounceTask extends AbstractJReleaserTask {
    private final List<String> announcers = new ArrayList<>();
    private final List<String> excludedAnnouncers = new ArrayList<>();

    public void setAnnouncers(String announcers) {
        this.announcers.addAll(expandAndCollect(announcers));
    }

    public void setExcludedAnnouncers(String excludedAnnouncers) {
        this.excludedAnnouncers.addAll(expandAndCollect(excludedAnnouncers));
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
        context.setIncludedAnnouncers(collectEntries(announcers, true));
        context.setExcludedAnnouncers(collectEntries(excludedAnnouncers, true));
        Workflows.announce(context).execute();
    }

    @Override
    protected Mode getMode() {
        return Mode.ANNOUNCE;
    }

    @Override
    protected JReleaserCommand getCommand() {
        return JReleaserCommand.ANNOUNCE;
    }
}
