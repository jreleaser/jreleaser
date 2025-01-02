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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.Activatable;
import org.jreleaser.model.internal.common.Domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public final class Sbom extends AbstractActivatable<Sbom> implements Domain, Activatable {
    private static final long serialVersionUID = 8210488305115310708L;

    private final CyclonedxSbomCataloger cyclonedx = new CyclonedxSbomCataloger();
    private final SyftSbomCataloger syft = new SyftSbomCataloger();

    @JsonIgnore
    private final org.jreleaser.model.api.catalog.sbom.Sbom immutable = new org.jreleaser.model.api.catalog.sbom.Sbom() {
        private static final long serialVersionUID = 5388790360937381329L;

        @Override
        public org.jreleaser.model.api.catalog.sbom.CyclonedxSbomCataloger getCyclonedx() {
            return cyclonedx.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.catalog.sbom.SyftSbomCataloger getSyft() {
            return syft.asImmutable();
        }

        @Override
        public Active getActive() {
            return Sbom.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return Sbom.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Sbom.this.asMap(full));
        }
    };

    public Sbom() {
        enabledSet(true);
    }

    public org.jreleaser.model.api.catalog.sbom.Sbom asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Sbom source) {
        super.merge(source);
        setSyft(source.syft);
        setCyclonedx(source.cyclonedx);
    }

    @Override
    public boolean isSet() {
        return super.isSet() ||
            cyclonedx.isSet() ||
            syft.isSet();
    }

    public CyclonedxSbomCataloger getCyclonedx() {
        return cyclonedx;
    }

    public void setCyclonedx(CyclonedxSbomCataloger cyclonedx) {
        this.cyclonedx.merge(cyclonedx);
    }

    public SyftSbomCataloger getSyft() {
        return syft;
    }

    public void setSyft(SyftSbomCataloger syft) {
        this.syft.merge(syft);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", getActive());
        map.putAll(cyclonedx.asMap(full));
        map.putAll(syft.asMap(full));
        return map;
    }

    public <A extends SbomCataloger<?>> A findSbomCataloger(String type) {
        if (isBlank(type)) {
            throw new JReleaserException("SbomCataloger type must not be blank");
        }

        return resolveSbomCataloger(type);
    }

    public List<? extends SbomCataloger<?>> findAllActiveSbomCatalogers() {
        List list = new ArrayList<>();
        if (cyclonedx.isEnabled()) list.add(getCyclonedx());
        if (syft.isEnabled()) list.add(getSyft());
        return list;
    }

    private <A extends SbomCataloger<?>> A resolveSbomCataloger(String name) {
        switch (name.toLowerCase(Locale.ENGLISH).trim()) {
            case org.jreleaser.model.api.catalog.sbom.CyclonedxSbomCataloger.TYPE:
                return (A) getCyclonedx();
            case org.jreleaser.model.api.catalog.sbom.SyftSbomCataloger.TYPE:
                return (A) getSyft();
            default:
                throw new JReleaserException(RB.$("ERROR_unsupported_cataloger", name));
        }
    }
}
