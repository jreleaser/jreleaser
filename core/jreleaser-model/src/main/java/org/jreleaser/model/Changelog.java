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

import org.jreleaser.bundle.RB;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.toSafeRegexPattern;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Changelog implements Domain, EnabledAware {
    private final Set<String> includeLabels = new LinkedHashSet<>();
    private final Set<String> excludeLabels = new LinkedHashSet<>();
    private final Set<Category> categories = new TreeSet<>(Category.ORDER);
    private final List<Replacer> replacers = new ArrayList<>();
    private final Set<Labeler> labelers = new TreeSet<>(Labeler.ORDER);
    private final Hide hide = new Hide();
    private final Contributors contributors = new Contributors();

    private Boolean enabled;
    private Boolean links;
    private Sort sort = Sort.DESC;
    private String external;
    private Active formatted;
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
        this.format = changelog.format;
        this.content = changelog.content;
        this.contentTemplate = changelog.contentTemplate;
        this.preset = changelog.preset;
        setIncludeLabels(changelog.includeLabels);
        setExcludeLabels(changelog.excludeLabels);
        setCategories(changelog.categories);
        setReplacers(changelog.replacers);
        setLabelers(changelog.labelers);
        setHide(changelog.hide);
        setContributors(changelog.contributors);
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
            throw new JReleaserException(RB.$("ERROR_unexpected_error_reading_template",
                context.relativizeToBasedir(templatePath)));
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
        return links != null && links;
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
        this.includeLabels.addAll(includeLabels.stream().map(String::trim).collect(Collectors.toSet()));
    }

    public Set<String> getExcludeLabels() {
        return excludeLabels;
    }

    public void setExcludeLabels(Set<String> excludeLabels) {
        this.excludeLabels.clear();
        this.excludeLabels.addAll(excludeLabels.stream().map(String::trim).collect(Collectors.toSet()));
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
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

    public Set<Labeler> getLabelers() {
        return labelers;
    }

    public void setLabelers(Set<Labeler> labelers) {
        this.labelers.clear();
        this.labelers.addAll(labelers);
    }

    @Deprecated
    public String getChange() {
        return this.format;
    }

    @Deprecated
    public void setChange(String change) {
        System.out.println("changelog.change has been deprecated since 0.6.0 and will be removed in the future. Use changelog.format instead");
        this.format = change;
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

    public Hide getHide() {
        return hide;
    }

    public void setHide(Hide hide) {
        this.hide.setAll(hide);
    }

    public Contributors getContributors() {
        return contributors;
    }

    public void setContributors(Contributors contributors) {
        this.contributors.setAll(contributors);
    }

    @Deprecated
    public void setHideUncategorized(boolean hideUncategorized) {
        System.out.println("changelog.hideUncategorized has been deprecated since 0.6.0 and will be removed in the future. Use changelog.hide.uncategorized instead");
        this.hide.uncategorized = hideUncategorized;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("external", external);
        map.put("links", isLinks());
        map.put("sort", sort);
        map.put("formatted", formatted);
        map.put("preset", preset);
        map.put("format", format);
        map.put("content", content);
        map.put("contentTemplate", contentTemplate);
        map.put("includeLabels", includeLabels);
        map.put("excludeLabels", excludeLabels);
        map.put("hide", hide.asMap(full));
        map.put("contributors", contributors.asMap(full));

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
        public static Comparator<Category> ORDER = (o1, o2) -> {
            if (null == o1.getOrder()) return 1;
            if (null == o2.getOrder()) return -1;
            return o1.getOrder().compareTo(o2.getOrder());
        };

        public static Set<Category> sort(Set<Category> categories) {
            TreeSet<Category> tmp = new TreeSet<>(ORDER);
            tmp.addAll(categories);
            return tmp;
        }

        private final Set<String> labels = new LinkedHashSet<>();
        private String key;
        private String title;
        private String format;
        private Integer order;

        void setAll(Category category) {
            this.key = category.key;
            this.title = category.title;
            this.format = category.format;
            this.order = category.order;
            setLabels(category.labels);
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
            if (isBlank(this.key)) {
                this.key = title;
            }
        }

        public Set<String> getLabels() {
            return labels;
        }

        public void setLabels(Set<String> labels) {
            this.labels.clear();
            this.labels.addAll(labels);
        }

        public void addLabels(Set<String> labels) {
            this.labels.addAll(labels);
        }

        public Integer getOrder() {
            return order;
        }

        public void setOrder(Integer order) {
            this.order = order;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("key", key);
            map.put("title", title);
            map.put("labels", labels);
            map.put("format", format);
            map.put("order", order);
            return map;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Category category = (Category) o;
            return key.equals(category.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title);
        }

        public static Category of(String key, String title, String format, String... labels) {
            Category category = new Category();
            category.key = key;
            category.title = title;
            category.format = format;
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
        public static Comparator<Labeler> ORDER = (o1, o2) -> {
            if (null == o1.getOrder()) return 1;
            if (null == o2.getOrder()) return -1;
            return o1.getOrder().compareTo(o2.getOrder());
        };

        private String label;
        private String title;
        private String body;
        private Integer order;

        void setAll(Labeler labeler) {
            this.label = labeler.label;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Labeler labeler = (Labeler) o;
            return Objects.equals(title, labeler.title) &&
                Objects.equals(body, labeler.body);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, body);
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("label", label);
            map.put("title", title);
            map.put("body", body);
            map.put("order", order);
            return map;
        }
    }

    public static class Contributors implements Domain {
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

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("enabled", enabled);
            map.put("format", format);
            return map;
        }
    }

    public static class Hide implements Domain {
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

        public void addCategories(Set<String> categories) {
            this.categories.addAll(categories.stream().map(String::trim).collect(Collectors.toSet()));
        }

        public void addCategory(String category) {
            if (isNotBlank(category)) {
                this.categories.add(category.trim());
            }
        }

        public boolean containsCategory(String category) {
            if (isNotBlank(category)) {
                return this.categories.contains(category.trim());
            }
            return false;
        }

        public Set<String> getContributors() {
            return contributors;
        }

        public void setContributors(Set<String> contributors) {
            this.contributors.clear();
            this.contributors.addAll(contributors.stream().map(String::trim).collect(Collectors.toSet()));
        }

        public void addContributors(Set<String> contributors) {
            this.contributors.addAll(contributors.stream().map(String::trim).collect(Collectors.toSet()));
        }

        public void addContributor(String contributor) {
            if (isNotBlank(contributor)) {
                this.contributors.add(contributor.trim());
            }
        }

        public boolean containsContributor(String name) {
            if (isNotBlank(name)) {
                String n = name.trim();
                for (String contributor : contributors) {
                    if (n.contains(contributor) || n.matches(toSafeRegexPattern(contributor))) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("uncategorized", uncategorized);
            map.put("categories", categories);
            map.put("contributors", contributors);
            return map;
        }
    }
}
