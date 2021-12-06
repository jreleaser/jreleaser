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
    private final List<Replacer> replacers = new ArrayList<>();
    private final List<Labeler> labelers = new ArrayList<>();
    private final Contributors contributors = new Contributors();
    private final Hide hide = new Hide();

    private Boolean enabled;
    private Boolean links;
    private Sort sort = Sort.DESC;
    private String external;
    private Active formatted;
    private String change;
    private String format;
    private String content;
    private String contentTemplate;
    private String preset;

    void setAll(Changelog changelog) {
        this.enabled = changelog.enabled;
        this.links = changelog.links;
        this.sort = changelog.sort;
        this.external = changelog.external;
        this.formatted = changelog.formatted;
        this.change = changelog.change;
        this.format = changelog.format;
        this.content = changelog.content;
        this.contentTemplate = changelog.contentTemplate;
        this.preset = changelog.preset;
        setIncludeLabels(changelog.includeLabels);
        setExcludeLabels(changelog.excludeLabels);
        setCategories(changelog.categories);
        setReplacers(changelog.replacers);
        setLabelers(changelog.labelers);
        setContributors(changelog.contributors);
        setHide(changelog.hide);
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

    public Boolean isLinks() {
        return links != null && links;
    }

    public boolean isLinksSet() {
        return links != null;
    }

    public void setLinks(Boolean links) {
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

    public List<Replacer> getReplacers() {
        return replacers;
    }

    public void setReplacers(List<Replacer> replacers) {
        this.replacers.clear();
        this.replacers.addAll(replacers);
    }

    public List<Labeler> getLabelers() {
        return labelers;
    }

    public void setLabelers(List<Labeler> labelers) {
        this.labelers.clear();
        this.labelers.addAll(labelers);
    }

    public String getChange() {
        return change;
    }

    public void setChange(String change) {
        this.change = change;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
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

    public String getPreset() {
        return preset;
    }

    public void setPreset(String preset) {
        this.preset = preset;
    }

    @Deprecated
    public boolean isHideUncategorized() {
        return this.hide.isUncategorized();
    }

    @Deprecated
    public void setHideUncategorized(boolean hideUncategorized) {
        this.hide.setUncategorized(hideUncategorized);
    }

    public Contributors getContributors() {
        return contributors;
    }

    public void setContributors(Contributors contributors) {
        this.contributors.setAll(contributors);
    }

    public Hide getHide() {
        return hide;
    }

    public void setHide(Hide hide) {
        this.hide.setAll(hide);
    }

    public enum Sort {
        ASC, DESC
    }

    public static class Category {
        private final Set<String> labels = new LinkedHashSet<>();
        private String title;
        private String format;
        private Integer order;

        void setAll(Category category) {
            this.title = category.title;
            setLabels(category.labels);
            this.format = category.format;
            this.order = category.order;
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

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public Integer getOrder() {
            return order;
        }

        public void setOrder(Integer order) {
            this.order = order;
        }

        public static Category of(String title, String format, String... labels) {
            Category category = new Category();
            category.title = title;
            category.format = format;
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
        private Integer order;

        void setAll(Labeler labeler) {
            this.label = labeler.label;
            this.branch = labeler.branch;
            this.title = labeler.title;
            this.body = labeler.body;
            this.order = labeler.order;
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

        public Integer getOrder() {
            return order;
        }

        public void setOrder(Integer order) {
            this.order = order;
        }
    }

    public static class Contributors {
        private Boolean enabled;
        private String format;

        void setAll(Contributors contributor) {
            this.enabled = contributor.enabled;
            this.format = contributor.format;
        }

        public boolean isEnabled() {
            return enabled != null && enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabledSet() {
            return enabled != null;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }
    }

    public static class Hide {
        private final Set<String> categories = new LinkedHashSet<>();
        private final Set<String> contributors = new LinkedHashSet<>();
        private boolean uncategorized;

        void setAll(Hide hide) {
            this.uncategorized = hide.uncategorized;
            setCategories(hide.categories);
            setContributors(hide.contributors);
        }

        public boolean isUncategorized() {
            return uncategorized;
        }

        public void setUncategorized(boolean uncategorized) {
            this.uncategorized = uncategorized;
        }

        public Set<String> getCategories() {
            return categories;
        }

        public void setCategories(Set<String> categories) {
            this.categories.clear();
            this.categories.addAll(categories.stream().map(String::trim).collect(Collectors.toSet()));
        }

        public Set<String> getContributors() {
            return contributors;
        }

        public void setContributors(Set<String> contributors) {
            this.contributors.clear();
            this.contributors.addAll(contributors.stream().map(String::trim).collect(Collectors.toSet()));
        }
    }
}
