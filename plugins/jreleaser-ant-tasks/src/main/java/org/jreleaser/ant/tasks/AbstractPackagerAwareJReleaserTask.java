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
public abstract class AbstractPackagerAwareJReleaserTask extends AbstractDistributionAwareJReleaserTask {
    private final List<String> packagers = new ArrayList<>();
    private final List<String> excludedPackagers = new ArrayList<>();

    public void setPackagers(String packagers) {
        this.packagers.addAll(expandAndCollect(packagers));
    }

    public void setExcludedPackagers(String excludedPackagers) {
        this.excludedPackagers.addAll(expandAndCollect(excludedPackagers));
    }

    public void setPackagers(List<String> packagers) {
        if (null != packagers) {
            this.packagers.addAll(packagers);
        }
    }

    public void setExcludedPackagers(List<String> excludedPackagers) {
        if (null != excludedPackagers) {
            this.excludedPackagers.addAll(excludedPackagers);
        }
    }

    @Override
    protected JReleaserContext setupContext(JReleaserContext context) {
        super.setupContext(context);
        context.setIncludedPackagers(collectEntries(packagers, true));
        context.setExcludedPackagers(collectEntries(excludedPackagers, true));
        return context;
    }
}
