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
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Domain;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Comparator.comparing;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.11.0
 */
public class Entity extends AbstractModelObject<Entity> implements Domain, Comparable<Entity> {
    private static final long serialVersionUID = -4316021076553932863L;

    private final Set<String> roles = new TreeSet<>();

    private String name;
    private String regid;

    @JsonIgnore
    private final org.jreleaser.model.api.catalog.swid.Entity immutable = new org.jreleaser.model.api.catalog.swid.Entity() {
        private static final long serialVersionUID = -8205490334587909973L;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getRegid() {
            return regid;
        }

        @Override
        public Set<String> getRoles() {
            return unmodifiableSet(roles);
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Entity.this.asMap(full));
        }
    };

    public org.jreleaser.model.api.catalog.swid.Entity asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Entity source) {
        this.name = merge(name, source.name);
        this.regid = merge(regid, source.regid);
        setRoles(merge(this.roles, source.roles));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegid() {
        return regid;
    }

    public void setRegid(String regid) {
        this.regid = regid;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles.clear();
        this.roles.addAll(roles);
    }

    public void addRoles(Set<String> roles) {
        this.roles.addAll(roles);
    }

    public void addRole(String role) {
        if (isNotBlank(role)) {
            this.roles.add(role.trim());
        }
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("name", name);
        props.put("regid", regid);
        props.put("roles", roles);
        return props;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return name.equals(entity.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(Entity o) {
        return comparing(Entity::getName).compare(this, o);
    }
}
