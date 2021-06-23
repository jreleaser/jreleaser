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
package org.jreleaser.maven.plugin;

import org.jreleaser.model.Active;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Changelog implements EnabledAware {
    private final Set<String> includeLabels = new LinkedHashSet<>();
    private final Set<String> excludeLabels = new LinkedHashSet<>();
    private final List<Category> categories = new ArrayList<>();
    private final Set<Replacer> replacers = new LinkedHashSet<>();
    private final Set<Labeler> labelers = new LinkedHashSet<>();

    private Boolean enabled;
    private boolean links;
    private Sort sort = Sort.DESC;
    private String external;
    private Active formatted;
    private String change;
    private String content;
    private String contentTemplate;
    private boolean hideUncategorized;

    void setAll(Changelog changelog) {
        this.enabled = changelog.enabled;
        this.links = changelog.links;
        this.sort = changelog.sort;
        this.external = changelog.external;
        this.formatted = changelog.formatted;
        this.change = changelog.change;
        this.content = changelog.content;
        this.contentTemplate = changelog.contentTemplate;
        this.hideUncategorized = changelog.hideUncategorized;
        setIncludeLabels(changelog.includeLabels);
        setExcludeLabels(changelog.excludeLabels);
        setCategories(changelog.categories);
        setReplacers(changelog.replacers);
        setLabelers(changelog.labelers);
    }

    @Override
    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabledSet() {
        return enabled != null;
    }

    public boolean isLinks() {
        return links;
    }

    public void setLinks(boolean links) {
        this.links = links;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public void setSort(String sort) {
        if (isNotBlank(sort)) {
            setSort(Sort.valueOf(sort.toUpperCase()));
        }
    }

    public String getExternal() {
        return external;
    }

    public void setExternal(String external) {
        this.external = external;
    }

    public String resolveFormatted() {
        return formatted != null ? formatted.name() : null;
    }

    public Active getFormatted() {
        return formatted;
    }

    public void setFormatted(Active formatted) {
        this.formatted = formatted;
    }

    public void setFormatted(String str) {
        this.formatted = Active.of(str);
    }

    public boolean isFormattedSet() {
        return formatted != null;
    }

    public Set<String> getIncludeLabels() {
        return includeLabels;
    }

    public void setIncludeLabels(Set<String> includeLabels) {
        this.includeLabels.clear();
        this.includeLabels.addAll(includeLabels);
    }

    public Set<String> getExcludeLabels() {
        return excludeLabels;
    }

    public void setExcludeLabels(Set<String> excludeLabels) {
        this.excludeLabels.clear();
        this.excludeLabels.addAll(excludeLabels);
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories.clear();
        this.categories.addAll(categories);
    }

    public Set<Replacer> getReplacers() {
        return replacers;
    }

    public void setReplacers(Set<Replacer> replacers) {
        this.replacers.clear();
        this.replacers.addAll(replacers);
    }

    public Set<Labeler> getLabelers() {
        return labelers;
    }

    public void setLabelers(Set<Labeler> labelers) {
        this.labelers.clear();
        this.labelers.addAll(labelers);
    }

    public String getChange() {
        return change;
    }

    public void setChange(String change) {
        this.change = change;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentTemplate() {
        return contentTemplate;
    }

    public void setContentTemplate(String contentTemplate) {
        this.contentTemplate = contentTemplate;
    }

    public boolean isHideUncategorized() {
        return hideUncategorized;
    }

    public void setHideUncategorized(boolean hideUncategorized) {
        this.hideUncategorized = hideUncategorized;
    }

    public enum Sort {
        ASC, DESC
    }

    public static class Category {
        private final Set<String> labels = new LinkedHashSet<>();
        private String title;

        void setAll(Category category) {
            this.title = category.title;
            setLabels(category.labels);
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Set<String> getLabels() {
            return labels;
        }

        public void setLabels(Set<String> labels) {
            this.labels.clear();
            this.labels.addAll(labels);
        }

        public void setLabelsAsString(String str) {
            if (isNotBlank(str)) {
                setLabels(Stream.of(str.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet()));
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Category category = (Category) o;
            return title.equals(category.title);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title);
        }

        public static Category of(String title, String... labels) {
            Category category = new Category();
            category.title = title;
            category.labels.addAll(Arrays.asList(labels));
            return category;
        }
    }

    public static class Replacer {
        private String search;
        private String replace = "";

        void setAll(Replacer replacer) {
            this.search = replacer.search;
            this.replace = replacer.replace;
        }

        public String getSearch() {
            return search;
        }

        public void setSearch(String search) {
            this.search = search;
        }

        public String getReplace() {
            return replace;
        }

        public void setReplace(String replace) {
            this.replace = replace;
        }
    }

    public static class Labeler {
        private String label;
        private String branch;
        private String title;
        private String body;

        void setAll(Labeler labeler) {
            this.label = labeler.label;
            this.branch = labeler.branch;
            this.title = labeler.title;
            this.body = labeler.body;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getBranch() {
            return branch;
        }

        public void setBranch(String branch) {
            this.branch = branch;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }
}
