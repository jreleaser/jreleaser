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
package org.jreleaser.model.internal.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.catalog.sbom.Sbom;
import org.jreleaser.model.internal.catalog.swid.SwidTag;
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.Domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public final class Catalog extends AbstractActivatable<Catalog> implements Domain {
    private static final long serialVersionUID = 4460775681741986722L;

    private final Sbom sbom = new Sbom();
    private final GithubCataloger github = new GithubCataloger();
    private final SlsaCataloger slsa = new SlsaCataloger();
    private final Map<String, SwidTag> swid = new LinkedHashMap<>();

    @JsonIgnore
    private final org.jreleaser.model.api.catalog.Catalog immutable = new org.jreleaser.model.api.catalog.Catalog() {
        private static final long serialVersionUID = -4843558796194675065L;

        private Map<String, ? extends org.jreleaser.model.api.catalog.swid.SwidTag> swid;

        @Override
        public org.jreleaser.model.api.catalog.sbom.Sbom getSbom() {
            return sbom.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.catalog.GithubCataloger getGithub() {
            return github.asImmutable();
        }

        @Override
        public org.jreleaser.model.api.catalog.SlsaCataloger getSlsa() {
            return slsa.asImmutable();
        }

        @Override
        public Map<String, ? extends org.jreleaser.model.api.catalog.swid.SwidTag> getSwid() {
            if (null == swid) {
                swid = Catalog.this.swid.values().stream()
                    .map(SwidTag::asImmutable)
                    .collect(toMap(org.jreleaser.model.api.catalog.swid.SwidTag::getName, identity()));
            }
            return swid;
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
        setGithub(source.github);
        setSlsa(source.slsa);
        setSwid(mergeModel(this.swid, source.swid));
    }

    @Override
    public boolean isSet() {
        return super.isSet() ||
            sbom.isSet() ||
            github.isSet() ||
            slsa.isSet() ||
            !swid.isEmpty();
    }

    public Sbom getSbom() {
        return sbom;
    }

    public void setSbom(Sbom sbom) {
        this.sbom.merge(sbom);
    }

    public GithubCataloger getGithub() {
        return github;
    }

    public void setGithub(GithubCataloger github) {
        this.github.merge(github);
    }

    public SlsaCataloger getSlsa() {
        return slsa;
    }

    public void setSlsa(SlsaCataloger slsa) {
        this.slsa.merge(slsa);
    }

    public Map<String, SwidTag> getSwid() {
        return swid;
    }

    public void setSwid(Map<String, SwidTag> swid) {
        this.swid.clear();
        this.swid.putAll(swid);
    }

    public void addSwid(SwidTag swid) {
        this.swid.put(swid.getName(), swid);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", getActive());
        map.putAll(github.asMap(full));
        map.put("sbom", sbom.asMap(full));
        map.putAll(slsa.asMap(full));

        List<Map<String, Object>> swid = this.swid.values()
            .stream()
            .map(d -> d.asMap(full))
            .filter(m -> !m.isEmpty())
            .collect(toList());
        if (!swid.isEmpty()) map.put("swid", swid);

        return map;
    }
}
