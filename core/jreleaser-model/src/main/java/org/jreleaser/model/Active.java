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

import java.util.function.Predicate;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public enum Active {
    ALWAYS(project -> true),
    NEVER(project -> false),
    RELEASE(Project::isRelease),
    SNAPSHOT(Project::isSnapshot);

    private final Predicate<Project> test;

    Active(Predicate<Project> test) {
        this.test = test;
    }

    boolean check(Project project) {
        return test.test(project);
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static Active of(String str) {
        if (isBlank(str)) return null;
        return Active.valueOf(str.toUpperCase().trim());
    }
}
