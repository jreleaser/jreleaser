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

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.workflow.Workflows;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public class JReleaserDownloadTask extends AbstractJReleaserTask {
    private final List<String> downloaderTypes = new ArrayList<>();
    private final List<String> excludedDownloaderTypes = new ArrayList<>();
    private final List<String> downloaderNames = new ArrayList<>();
    private final List<String> excludedDownloaderNames = new ArrayList<>();

    public void setDownloaderTypes(String downloaderTypes) {
        this.downloaderTypes.addAll(expandAndCollect(downloaderTypes));
    }

    public void setExcludedDownloaderTypes(String excludedDownloaderTypes) {
        this.excludedDownloaderTypes.addAll(expandAndCollect(excludedDownloaderTypes));
    }

    public void setDownloaderNames(String downloaderNames) {
        this.downloaderNames.addAll(expandAndCollect(downloaderNames));
    }

    public void setExcludedDownloaderNames(String excludedDownloaderNames) {
        this.excludedDownloaderNames.addAll(expandAndCollect(excludedDownloaderNames));
    }

    public void setDownloaderTypes(List<String> downloaderTypes) {
        if (null != downloaderTypes) {
            this.downloaderTypes.addAll(downloaderTypes);
        }
    }

    public void setExcludedDownloaderTypes(List<String> excludedDownloaderTypes) {
        if (null != excludedDownloaderTypes) {
            this.excludedDownloaderTypes.addAll(excludedDownloaderTypes);
        }
    }

    public void setDownloaderNames(List<String> downloaderNames) {
        if (null != downloaderNames) {
            this.downloaderNames.addAll(downloaderNames);
        }
    }

    public void setExcludedDownloaderNames(List<String> excludedDownloaderNames) {
        if (null != excludedDownloaderNames) {
            this.excludedDownloaderNames.addAll(excludedDownloaderNames);
        }
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        context.setIncludedDownloaderTypes(downloaderTypes);
        context.setExcludedDownloaderTypes(excludedDownloaderTypes);
        context.setIncludedDownloaderNames(downloaderNames);
        context.setExcludedDownloaderNames(excludedDownloaderNames);
        Workflows.download(context).execute();
    }

    @Override
    protected JReleaserContext.Mode getMode() {
        return JReleaserContext.Mode.DOWNLOAD;
    }
}
