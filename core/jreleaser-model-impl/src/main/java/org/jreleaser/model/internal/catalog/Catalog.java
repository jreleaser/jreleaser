/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
package org.jreleaser.model.internal.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.catalog.sbom.Sbom;
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.Domain;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public final class Catalog extends AbstractActivatable<Catalog> implements Domain {
    private static final long serialVersionUID = -1338155149564894995L;

    private final Sbom sbom = new Sbom();
    private final SlsaCataloger slsa = new SlsaCataloger();

    @JsonIgnore
    private final org.jreleaser.model.api.catalog.Catalog immutable = new org.jreleaser.model.api.catalog.Catalog() {
        private static final long serialVersionUID = 6865438805449170431L;

        @Override
        public org.jreleaser.model.api.catalog.sbom.Sbom getSbom() {
            return sbom.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.catalog.SlsaCataloger getSlsa() {
            return slsa.asImmutable();
        }

        @Override
        public Active getActive() {
            return Catalog.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return Catalog.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Catalog.this.asMap(full));
        }
    };

    public Catalog() {
        enabledSet(true);
    }

    public org.jreleaser.model.api.catalog.Catalog asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Catalog source) {
        super.merge(source);
        setSbom(source.sbom);
        setSlsa(source.slsa);
    }

    @Override
    public boolean isSet() {
        return super.isSet() ||
            sbom.isSet() ||
            slsa.isSet();
    }

    public Sbom getSbom() {
        return sbom;
    }

    public void setSbom(Sbom sbom) {
        this.sbom.merge(sbom);
    }

    public SlsaCataloger getSlsa() {
        return slsa;
    }

    public void setSlsa(SlsaCataloger slsa) {
        this.slsa.merge(slsa);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", getActive());
        map.put("sbom", sbom.asMap(full));
        map.putAll(slsa.asMap(full));
        return map;
    }
}
