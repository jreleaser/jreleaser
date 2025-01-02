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
package org.jreleaser.model.internal.catalog.swid;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.Domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static java.util.stream.Collectors.toSet;

/**
 * @author Andres Almiray
 * @since 1.11.0
 */
public class SwidTag extends AbstractActivatable<SwidTag> implements Domain {
    private static final long serialVersionUID = -5231801224571096074L;

    private final Set<Entity> entities = new TreeSet<>();

    private String tagRef;
    private String name;
    private String path;
    private String tagId;
    private Integer tagVersion;
    private String lang;
    private Boolean corpus;
    private Boolean patch;

    @JsonIgnore
    private final org.jreleaser.model.api.catalog.swid.SwidTag immutable = new org.jreleaser.model.api.catalog.swid.SwidTag() {
        private static final long serialVersionUID = -566446887267136193L;

        private Set<? extends org.jreleaser.model.api.catalog.swid.Entity> entities;

        @Override
        public String getTagRef() {
            return tagRef;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public String getTagId() {
            return tagId;
        }

        @Override
        public Integer getTagVersion() {
            return tagVersion;
        }

        @Override
        public String getLang() {
            return lang;
        }

        @Override
        public Set<? extends org.jreleaser.model.api.catalog.swid.Entity> getEntities() {
            if (null == entities) {
                entities = SwidTag.this.entities.stream()
                    .map(Entity::asImmutable)
                    .collect(toSet());
            }
            return entities;
        }

        @Override
        public boolean isCorpus() {
            return SwidTag.this.isCorpus();
        }

        @Override
        public boolean isPatch() {
            return SwidTag.this.isPatch();
        }

        @Override
        public Active getActive() {
            return SwidTag.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return SwidTag.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return SwidTag.this.asMap(full);
        }
    };

    public org.jreleaser.model.api.catalog.swid.SwidTag asImmutable() {
        return immutable;
    }

    @Override
    public void merge(SwidTag source) {
        super.merge(source);
        this.tagRef = merge(tagRef, source.tagRef);
        this.name = merge(name, source.name);
        this.path = merge(path, source.path);
        this.tagId = merge(tagId, source.tagId);
        this.tagVersion = merge(tagVersion, source.tagVersion);
        this.lang = merge(lang, source.lang);
        this.corpus = merge(corpus, source.corpus);
        this.patch = merge(patch, source.patch);
        setEntities(merge(entities, source.entities));
    }

    public void copyFrom(SwidTag other) {
        if (null == other) return;
        setActive(merge(other.getActive(), getActive()));
        setName(merge(other.getName(), getName()));
        setPath(merge(other.getPath(), getPath()));
        setTagId(merge(other.getTagId(), getTagId()));
        setTagVersion(merge(other.getTagVersion(), getTagVersion()));
        setLang(merge(other.getLang(), getLang()));
        setCorpus(merge(other.getCorpus(), getCorpus()));
        setPatch(merge(other.getPatch(), getPatch()));
        setEntities(merge(other.getEntities(), getEntities()));
    }

    public String getTagRef() {
        return tagRef;
    }

    public void setTagRef(String tagRef) {
        this.tagRef = tagRef;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public Integer getTagVersion() {
        return tagVersion;
    }

    public void setTagVersion(Integer tagVersion) {
        this.tagVersion = tagVersion;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Boolean getCorpus() {
        return corpus;
    }

    public void setCorpus(Boolean corpus) {
        this.corpus = corpus;
    }

    public Boolean getPatch() {
        return patch;
    }

    public void setPatch(Boolean patch) {
        this.patch = patch;
    }

    public Set<Entity> getEntities() {
        return entities;
    }

    public void setEntities(Set<Entity> entities) {
        this.entities.clear();
        this.entities.addAll(entities);
    }

    public void addEntity(Entity entity) {
        if (null != entity) {
            this.entities.add(entity);
        }
    }

    public boolean isCorpus() {
        return null != corpus && corpus;
    }

    public boolean isPatch() {
        return null != patch && patch;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("active", getActive());
        props.put("tagRef", tagRef);
        props.put("name", name);
        props.put("path", path);
        props.put("tagId", tagId);
        props.put("tagVersion", tagVersion);
        props.put("lang", lang);
        props.put("corpus", isCorpus());
        props.put("patch", isPatch());

        Map<String, Map<String, Object>> mappedEntities = new LinkedHashMap<>();
        int i = 0;
        for (Entity entity : entities) {
            mappedEntities.put("entity " + (i++), entity.asMap(full));
        }
        props.put("entities", mappedEntities);

        return props;
    }
}
