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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY;
import static org.jreleaser.model.Distribution.DistributionType.JLINK;
import static org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Jbang extends AbstractRepositoryPackager<Jbang> {
    public static final String TYPE = "jbang";

    private static final Map<Distribution.DistributionType, Set<String>> SUPPORTED = new LinkedHashMap<>();

    static {
        SUPPORTED.put(JAVA_BINARY, emptySet());
        SUPPORTED.put(JLINK, emptySet());
        SUPPORTED.put(SINGLE_JAR, emptySet());
    }

    private final JbangCatalog catalog = new JbangCatalog();
    private String alias;

    public Jbang() {
        super(TYPE);
    }

    @Override
    public void freeze() {
        super.freeze();
        catalog.freeze();
    }

    @Override
    public void merge(Jbang jbang) {
        freezeCheck();
        super.merge(jbang);
        this.alias = merge(this.alias, jbang.alias);
        setCatalog(jbang.catalog);
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        freezeCheck();
        this.alias = alias;
    }

    public JbangCatalog getCatalog() {
        return catalog;
    }

    public void setCatalog(JbangCatalog tap) {
        this.catalog.merge(tap);
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
        return SUPPORTED.containsKey(distribution.getType());
    }

    @Override
    public Set<String> getSupportedExtensions(Distribution distribution) {
        return Collections.unmodifiableSet(SUPPORTED.getOrDefault(distribution.getType(), emptySet()));
    }

    @Override
    protected boolean isNotSkipped(Artifact artifact) {
        return true;
    }

    @Override
    public boolean isSnapshotSupported() {
        return true;
    }

    public static class JbangCatalog extends AbstractRepositoryTap<JbangCatalog> {
        public JbangCatalog() {
            super("jbang", "jbang-catalog");
        }

        @Override
        public String getResolvedName() {
            return tapName;
        }
    }
}
