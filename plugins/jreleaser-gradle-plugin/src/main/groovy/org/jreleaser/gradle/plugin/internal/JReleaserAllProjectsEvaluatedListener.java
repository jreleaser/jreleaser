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
package org.jreleaser.gradle.plugin.internal;

import org.gradle.api.Project;
import org.kordamp.gradle.annotations.DependsOn;
import org.kordamp.gradle.listener.AllProjectsEvaluatedListener;

import javax.inject.Named;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@DependsOn({"base", "*"})
@Named("jreleaser")
public class JReleaserAllProjectsEvaluatedListener implements AllProjectsEvaluatedListener {
    private Runnable runnable;

    @Override
    public void allProjectsEvaluated(Project project) {
        if (null != runnable) runnable.run();
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }
}
