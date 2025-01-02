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
package org.jreleaser.model.internal.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.project.Project;

import java.io.Serializable;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public abstract class AbstractActivatable<S extends AbstractActivatable<S>> extends AbstractModelObject<S> implements Activatable, Serializable {
    private static final long serialVersionUID = -4546036141213581709L;

    @JsonIgnore
    private boolean enabled;
    private Active active;

    @Override
    public void merge(S source) {
        this.active = merge(this.active, source.getActive());
        this.enabled = merge(this.enabled, source.isEnabled());
    }

    protected boolean isSet() {
        return null != active;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void disable() {
        active = Active.NEVER;
        enabled = false;
    }

    @Override
    public boolean resolveEnabledWithSnapshot(Project project) {
        enabled = null != active && active.check(project);
        if (project.isSnapshot() && !isSnapshotSupported()) {
            enabled = false;
        }
        return enabled;
    }

    @Override
    public boolean resolveEnabled(Project project) {
        enabled = null != active && active.check(project);
        return enabled;
    }

    @Override
    public Active getActive() {
        return active;
    }

    @Override
    public void setActive(Active active) {
        this.active = active;
    }

    @Override
    public void setActive(String str) {
        setActive(Active.of(str));
    }

    @Override
    public boolean isActiveSet() {
        return null != active;
    }

    @Override
    public boolean isSnapshotSupported() {
        return true;
    }

    protected void enabledSet(boolean enabled) {
        this.enabled = enabled;
    }
}
