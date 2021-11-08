/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
package org.jreleaser.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.9.0
 */
public class Jdeps implements Domain {
    private String multiRelease;
    private Boolean ignoreMissingDeps;

    void setAll(Jdeps jdeps) {
        this.multiRelease = jdeps.multiRelease;
        this.ignoreMissingDeps = jdeps.ignoreMissingDeps;
    }

    public String getMultiRelease() {
        return multiRelease;
    }

    public void setMultiRelease(String multiRelease) {
        this.multiRelease = multiRelease;
    }

    public Boolean isIgnoreMissingDeps() {
        return ignoreMissingDeps != null && ignoreMissingDeps;
    }

    public void setIgnoreMissingDeps(Boolean ignoreMissingDeps) {
        this.ignoreMissingDeps = ignoreMissingDeps;
    }

    public boolean isIgnoreMissingDepsSet() {
        return ignoreMissingDeps != null;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("multiRelease", multiRelease);
        props.put("ignoreMissingDeps", isIgnoreMissingDeps());
        return props;
    }
}
