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
package org.jreleaser.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class CommitAuthor extends AbstractModelObject<CommitAuthor> implements Domain {
    private String email;
    private String name;

    @Override
    public void merge(CommitAuthor author) {
        freezeCheck();
        this.email = merge(this.email, author.email);
        this.name = merge(this.name, author.name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        freezeCheck();
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        freezeCheck();
        this.email = email;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", getName());
        map.put("email", getEmail());
        return map;
    }
}
