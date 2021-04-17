/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
package org.jreleaser.maven.plugin;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Jbang extends AbstractRepositoryTool {
    public static final String NAME = "catalog";
    private final Catalog catalog = new Catalog();
    private String alias;

    public Jbang() {
        super(NAME);
    }

    void setAll(Jbang jbang) {
        super.setAll(jbang);
        this.alias = jbang.alias;
        setCatalog(jbang.catalog);
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public void setCatalog(Catalog catalog) {
        this.catalog.setAll(catalog);
    }

    @Override
    public boolean isSet() {
        return super.isSet() ||
            isNotBlank(alias) ||
            catalog.isSet();
    }
}
