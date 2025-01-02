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
package org.jreleaser.model.internal.catalog.sbom;

import org.jreleaser.model.internal.catalog.AbstractCataloger;

import java.util.Map;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public abstract class AbstractSbomCataloger<S extends AbstractSbomCataloger<S, A>, A extends org.jreleaser.model.api.catalog.sbom.SbomCataloger> extends AbstractCataloger<S, A> implements SbomCataloger<A> {
    private static final long serialVersionUID = 2297157203661110390L;

    private final Pack pack = new Pack();
    protected Boolean distributions;
    protected Boolean files;

    protected AbstractSbomCataloger(String type) {
        super(type);
    }

    @Override
    public void merge(S source) {
        super.merge(source);
        this.distributions = merge(this.distributions, source.distributions);
        this.files = merge(this.files, source.files);
        setPack(source.getPack());
    }

    @Override
    protected boolean isSet() {
        return super.isSet() ||
            null != distributions ||
            null != files ||
            pack.isSet();
    }

    @Override
    public boolean isDistributions() {
        return null != distributions && distributions;
    }

    @Override
    public void setDistributions(Boolean distributions) {
        this.distributions = distributions;
    }

    @Override
    public boolean isDistributionsSet() {
        return null != distributions;
    }

    @Override
    public boolean isFiles() {
        return null != files && files;
    }

    @Override
    public void setFiles(Boolean files) {
        this.files = files;
    }

    @Override
    public boolean isFilesSet() {
        return null != files;
    }

    @Override
    public Pack getPack() {
        return pack;
    }

    @Override
    public void setPack(Pack pack) {
        this.pack.merge(pack);
    }

    @Override
    protected void asMap(boolean full, Map<String, Object> props) {
        props.put("distributions", isDistributions());
        props.put("files", isFiles());
        props.put("pack", pack.asMap(full));
    }
}
