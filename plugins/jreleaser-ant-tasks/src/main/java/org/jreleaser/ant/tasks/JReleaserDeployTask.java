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

import static org.jreleaser.model.api.JReleaserContext.Mode.DEPLOY;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public class JReleaserDeployTask extends AbstractJReleaserTask {
    private final List<String> deployerTypes = new ArrayList<>();
    private final List<String> excludedDeployerTypes = new ArrayList<>();
    private final List<String> deployerNames = new ArrayList<>();
    private final List<String> excludedDeployerNames = new ArrayList<>();

    public void setDeployerTypes(String deployerTypes) {
        this.deployerTypes.addAll(expandAndCollect(deployerTypes));
    }

    public void setExcludedDeployerTypes(String excludedDeployerTypes) {
        this.excludedDeployerTypes.addAll(expandAndCollect(excludedDeployerTypes));
    }

    public void setDeployerNames(String deployerNames) {
        this.deployerNames.addAll(expandAndCollect(deployerNames));
    }

    public void setExcludedDeployerNames(String excludedDeployerNames) {
        this.excludedDeployerNames.addAll(expandAndCollect(excludedDeployerNames));
    }

    public void setDeployerTypes(List<String> deployerTypes) {
        if (null != deployerTypes) {
            this.deployerTypes.addAll(deployerTypes);
        }
    }

    public void setExcludedDeployerTypes(List<String> excludedDeployerTypes) {
        if (null != excludedDeployerTypes) {
            this.excludedDeployerTypes.addAll(excludedDeployerTypes);
        }
    }

    public void setDeployerNames(List<String> deployerNames) {
        if (null != deployerNames) {
            this.deployerNames.addAll(deployerNames);
        }
    }

    public void setExcludedDeployerNames(List<String> excludedDeployerNames) {
        if (null != excludedDeployerNames) {
            this.excludedDeployerNames.addAll(excludedDeployerNames);
        }
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        context.setIncludedDeployerTypes(collectEntries(deployerTypes, true));
        context.setExcludedDeployerTypes(collectEntries(excludedDeployerTypes, true));
        context.setIncludedDeployerNames(deployerNames);
        context.setExcludedDeployerNames(excludedDeployerNames);
        Workflows.deploy(context).execute();
    }

    @Override
    protected Mode getMode() {
        return DEPLOY;
    }

    @Override
    protected JReleaserCommand getCommand() {
        return JReleaserCommand.DEPLOY;
    }
}
