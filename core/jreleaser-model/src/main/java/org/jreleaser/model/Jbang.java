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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Jbang extends AbstractRepositoryTool {
    public static final String NAME = "jbang";
    private final JbangCatalog catalog = new JbangCatalog();
    private String alias;

    public Jbang() {
        super(NAME);
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Collections.emptySet();
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

    public JbangCatalog getCatalog() {
        return catalog;
    }

    public void setCatalog(JbangCatalog tap) {
        this.catalog.setAll(tap);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        super.asMap(full, props);
        props.put("alias", alias);
        props.put("catalog", catalog.asMap(full));
    }

    @Override
    public RepositoryTap getRepositoryTap() {
        return catalog;
    }

    @Override
    public boolean supportsPlatform(String platform) {
        return true;
    }

    @Override
    public boolean supportsDistribution(Distribution distribution) {
        return distribution.getType() != Distribution.DistributionType.NATIVE_IMAGE &&
            distribution.getType() != Distribution.DistributionType.NATIVE_PACKAGE &&
            distribution.getType() != Distribution.DistributionType.BINARY;
    }

    @Override
    public boolean isSnapshotSupported() {
        return true;
    }

    public static class JbangCatalog extends AbstractRepositoryTap {
        public JbangCatalog() {
            super("jbang", "jbang-catalog");
        }

        @Override
        public String getResolvedName() {
            return tapName;
        }
    }
}
