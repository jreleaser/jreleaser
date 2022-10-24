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
package org.jreleaser.sdk.discourse.api;


import java.util.ArrayList;

/**
 * @author shblue21
 * @since 1.3.0
 */
public class CategoryList{
    public boolean can_create_category;
    public boolean can_create_topic;
    public ArrayList<Category> categories;

    public boolean isCan_create_category() {
        return can_create_category;
    }

    public void setCan_create_category(boolean can_create_category) {
        this.can_create_category = can_create_category;
    }

    public boolean isCan_create_topic() {
        return can_create_topic;
    }

    public void setCan_create_topic(boolean can_create_topic) {
        this.can_create_topic = can_create_topic;
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }
}