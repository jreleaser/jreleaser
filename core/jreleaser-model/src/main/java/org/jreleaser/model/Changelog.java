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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Changelog implements Domain, EnabledAware {
    private final Set<String> includeLabels = new LinkedHashSet<>();
    private final Set<String> excludeLabels = new LinkedHashSet<>();
    private final Set<String> hiddenCategories = new LinkedHashSet<>();
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
        setHiddenCategories(changelog.hiddenCategories);
        setIncludeLabels(changelog.includeLabels);
        setExcludeLabels(changelog.excludeLabels);
        setCategories(changelog.categories);
        setReplacers(changelog.replacers);
        setLabelers(changelog.labelers);
    }

    public boolean resolveFormatted(Project project) {
        if (null == formatted) {
            formatted = Active.NEVER;
        }
        return formatted.check(project);
    }

    public Reader getResolvedContentTemplate(JReleaserContext context) {
        if (isNotBlank(content)) {
            return new StringReader(content);
        }

        Path templatePath = context.getBasedir().resolve(contentTemplate);
        try {
            return java.nio.file.Files.newBufferedReader(templatePath);
        } catch (IOException e) {
            throw new JReleaserException("Unexpected error reading template " +
                context.relativizeToBasedir(templatePath));
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
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

    public Set<String> getHiddenCategories() {
        return hiddenCategories;
    }

    public void setHiddenCategories(Set<String> hiddenCategories) {
        this.hiddenCategories.clear();
        this.hiddenCategories.addAll(hiddenCategories.stream().map(String::trim).collect(Collectors.toSet()));
    }

    public void addHiddenCategory(String category) {
        if (isNotBlank(category)) {
            this.hiddenCategories.add(category.trim());
        }
    }

    public boolean containsHiddenCategory(String category) {
        if (isNotBlank(category)) {
            return this.hiddenCategories.contains(category.trim());
        }
        return false;
    }

    public Set<String> getIncludeLabels() {
        return includeLabels;
    }

    public void setIncludeLabels(Set<String> includeLabels) {
        this.includeLabels.clear();
        this.includeLabels.addAll(includeLabels.stream().map(String::trim).collect(Collectors.toSet()));
    }

    public Set<String> getExcludeLabels() {
        return excludeLabels;
    }

    public void setExcludeLabels(Set<String> excludeLabels) {
        this.excludeLabels.clear();
        this.excludeLabels.addAll(excludeLabels.stream().map(String::trim).collect(Collectors.toSet()));
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

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("external", external);
        map.put("links", links);
        map.put("sort", sort);
        map.put("formatted", formatted);
        map.put("change", change);
        map.put("content", content);
        map.put("contentTemplate", contentTemplate);
        map.put("includeLabels", includeLabels);
        map.put("excludeLabels", excludeLabels);
        map.put("hideUncategorized", hideUncategorized);
        map.put("hiddenCategories", hiddenCategories);

        Map<String, Map<String, Object>> m = new LinkedHashMap<>();
        int i = 0;
        for (Category category : categories) {
            m.put("category " + (i++), category.asMap(full));
        }
        map.put("categories", m);

        m = new LinkedHashMap<>();
        i = 0;
        for (Labeler labeler : labelers) {
            m.put("labeler " + (i++), labeler.asMap(full));
        }
        map.put("labelers", m);

        m = new LinkedHashMap<>();
        i = 0;
        for (Replacer replacer : replacers) {
            m.put("replacer " + (i++), replacer.asMap(full));
        }
        map.put("replacers", m);

        return map;
    }

    public enum Sort {
        ASC, DESC
    }

    public static class Category implements Domain {
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

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("title", title);
            map.put("labels", labels);
            return map;
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

    public static class Replacer implements Domain {
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

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("search", search);
            map.put("replace", replace);
            return map;
        }
    }

    public static class Labeler implements Domain {
        private String label;
        private String title;
        private String body;

        void setAll(Labeler labeler) {
            this.label = labeler.label;
            this.title = labeler.title;
            this.body = labeler.body;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
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

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("label", label);
            map.put("title", title);
            map.put("body", body);
            return map;
        }
    }
}
