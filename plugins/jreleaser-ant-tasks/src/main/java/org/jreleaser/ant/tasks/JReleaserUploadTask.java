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

import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class JReleaserUploadTask extends AbstractPlatformAwareJReleaserTask {
    private List<String> uploaderTypes;
    private List<String> excludedUploaderTypes;
    private List<String> uploaderNames;
    private List<String> excludedUploaderNames;

    public void setUploaderTypes(List<String> uploaderTypes) {
        this.uploaderTypes = uploaderTypes;
    }

    public void setExcludedUploaderTypes(List<String> excludedUploaderTypes) {
        this.excludedUploaderTypes = excludedUploaderTypes;
    }

    public void setUploaderNames(List<String> uploaderNames) {
        this.uploaderNames = uploaderNames;
    }

    public void setExcludedUploaderNames(List<String> excludedUploaderNames) {
        this.excludedUploaderNames = excludedUploaderNames;
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        context.setIncludedUploaderTypes(uploaderTypes);
        context.setExcludedUploaderTypes(excludedUploaderTypes);
        context.setIncludedUploaderNames(uploaderNames);
        context.setExcludedUploaderNames(excludedUploaderNames);
        Workflows.upload(context).execute();
    }
}
