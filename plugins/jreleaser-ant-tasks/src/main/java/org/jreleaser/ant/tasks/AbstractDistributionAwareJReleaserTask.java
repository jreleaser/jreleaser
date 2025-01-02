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

import org.jreleaser.model.internal.JReleaserContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AbstractDistributionAwareJReleaserTask extends AbstractPlatformAwareJReleaserTask {
    private final List<String> distributions = new ArrayList<>();
    private final List<String> excludedDistributions = new ArrayList<>();

    public void setDistributions(String distributions) {
        this.distributions.addAll(expandAndCollect(distributions));
    }

    public void setExcludedDistributions(String excludedDistributions) {
        this.excludedDistributions.addAll(expandAndCollect(excludedDistributions));
    }

    public void setDistributions(List<String> distributions) {
        if (null != distributions) {
            this.distributions.addAll(distributions);
        }
    }

    public void setExcludedDistributions(List<String> excludedDistributions) {
        if (null != excludedDistributions) {
            this.excludedDistributions.addAll(excludedDistributions);
        }
    }

    protected JReleaserContext setupContext(JReleaserContext context) {
        context.setIncludedDistributions(collectEntries(distributions));
        context.setExcludedDistributions(collectEntries(excludedDistributions));
        return context;
    }
}
