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
import org.jreleaser.util.JReleaserException;

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
import java.util.Locale;
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
public class Changelog extends AbstractModelObject<Changelog> implements Domain, EnabledAware {
    private final Set<String> includeLabels = new LinkedHashSet<>();
    private final Set<String> excludeLabels = new LinkedHashSet<>();
    private final Set<Category> categories = new TreeSet<>(Category.ORDER);
    private final List<Replacer> replacers = new ArrayList<>();
    private final Set<Labeler> labelers = new TreeSet<>(Labeler.ORDER);
    private final Hide hide = new Hide();
    private final Contributors contributors = new Contributors();

    private Boolean enabled;
    private Boolean links;
    private Boolean skipMergeCommits;
    private Sort sort;
    private String external;
    private Active formatted;
    private String format;
    private String content;
    private String contentTemplate;
    private String preset;

    public boolean isSet() {
        return !includeLabels.isEmpty() ||
            !excludeLabels.isEmpty() ||
            !categories.isEmpty() ||
            !replacers.isEmpty() ||
            !labelers.isEmpty() ||
            hide.isSet() ||
            contributors.isSet() ||
            null != links ||
            null != skipMergeCommits ||
            null != sort ||
            null != formatted ||
            isNotBlank(external) ||
            isNotBlank(format) ||
            isNotBlank(content) ||
            isNotBlank(contentTemplate) ||
            isNotBlank(preset);
    }

    @Override
    public void freeze() {
        super.freeze();
        categories.forEach(Category::freeze);
        replacers.forEach(Replacer::freeze);
        labelers.forEach(Labeler::freeze);
        hide.freeze();
        contributors.freeze();
    }

    @Override
    public void merge(Changelog changelog) {
        freezeCheck();
        this.enabled = merge(this.enabled, changelog.enabled);
        this.links = merge(this.links, changelog.links);
        this.skipMergeCommits = merge(this.skipMergeCommits, changelog.skipMergeCommits);
        this.sort = merge(this.sort, changelog.sort);
        this.external = merge(this.external, changelog.external);
        this.formatted = merge(this.formatted, changelog.formatted);
        this.format = merge(this.format, changelog.format);
        this.content = merge(this.content, changelog.content);
        this.contentTemplate = merge(this.contentTemplate, changelog.contentTemplate);
        this.preset = merge(this.preset, changelog.preset);
        setIncludeLabels(merge(this.includeLabels, changelog.includeLabels));
        setExcludeLabels(merge(this.excludeLabels, changelog.excludeLabels));
        setCategories(merge(this.categories, changelog.categories));
        setReplacers(merge(this.replacers, changelog.replacers));
        setLabelers(merge(this.labelers, changelog.labelers));
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
        return enabled != null && enabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        freezeCheck();
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
        freezeCheck();
        this.links = links;
    }

    public boolean isSkipMergeCommits() {
        return skipMergeCommits != null && skipMergeCommits;
    }

    public void setSkipMergeCommits(Boolean skipMergeCommits) {
        freezeCheck();
        this.skipMergeCommits = skipMergeCommits;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        freezeCheck();
        this.sort = sort;
    }

    public void setSort(String sort) {
        freezeCheck();
        if (isNotBlank(sort)) {
            setSort(Sort.valueOf(sort.toUpperCase(Locale.ENGLISH)));
        }
    }

    public String getExternal() {
        return external;
    }

    public void setExternal(String external) {
        freezeCheck();
        this.external = external;
    }

    public Active getFormatted() {
        return formatted;
    }

    public void setFormatted(Active formatted) {
        freezeCheck();
        this.formatted = formatted;
    }

    public void setFormatted(String str) {
        setFormatted(Active.of(str));
    }

    public boolean isFormattedSet() {
        return formatted != null;
    }

    public Set<String> getIncludeLabels() {
        return freezeWrap(includeLabels);
    }

    public void setIncludeLabels(Set<String> includeLabels) {
        freezeCheck();
        this.includeLabels.clear();
        this.includeLabels.addAll(includeLabels.stream().map(String::trim).collect(Collectors.toSet()));
    }

    public Set<String> getExcludeLabels() {
        return freezeWrap(excludeLabels);
    }

    public void setExcludeLabels(Set<String> excludeLabels) {
        freezeCheck();
        this.excludeLabels.clear();
        this.excludeLabels.addAll(excludeLabels.stream().map(String::trim).collect(Collectors.toSet()));
    }

    public Set<Category> getCategories() {
        return freezeWrap(categories);
    }

    public void setCategories(Set<Category> categories) {
        freezeCheck();
        this.categories.clear();
        this.categories.addAll(categories);
    }

    public List<Replacer> getReplacers() {
        return freezeWrap(replacers);
    }

    public void setReplacers(List<Replacer> replacers) {
        freezeCheck();
        this.replacers.clear();
        this.replacers.addAll(replacers);
    }

    public Set<Labeler> getLabelers() {
        return freezeWrap(labelers);
    }

    public void setLabelers(Set<Labeler> labelers) {
        freezeCheck();
        this.labelers.clear();
        this.labelers.addAll(labelers);
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        freezeCheck();
        this.format = format;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        freezeCheck();
        this.content = content;
    }

    public String getContentTemplate() {
        return contentTemplate;
    }

    public void setContentTemplate(String contentTemplate) {
        freezeCheck();
        this.contentTemplate = contentTemplate;
    }

    public String getPreset() {
        return preset;
    }

    public void setPreset(String preset) {
        freezeCheck();
        this.preset = preset;
    }

    public Hide getHide() {
        return hide;
    }

    public void setHide(Hide hide) {
        this.hide.merge(hide);
    }

    public Contributors getContributors() {
        return contributors;
    }

    public void setContributors(Contributors contributors) {
        this.contributors.merge(contributors);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("external", external);
        map.put("links", isLinks());
        map.put("skipMergeCommits", isSkipMergeCommits());
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

    public static class Category extends AbstractModelObject<Category> implements Domain {
        public static Comparator<Category> ORDER = (o1, o2) -> {
            if (null == o1.getOrder()) return 1;
            if (null == o2.getOrder()) return -1;
            return o1.getOrder().compareTo(o2.getOrder());
        };
        private final Set<String> labels = new LinkedHashSet<>();
        private String key;
        private String title;
        private String format;
        private Integer order;

        @Override
        public void merge(Category category) {
            freezeCheck();
            this.key = merge(this.key, category.key);
            this.title = merge(this.title, category.title);
            this.format = merge(this.format, category.format);
            this.order = merge(this.order, category.order);
            setLabels(merge(this.labels, category.labels));
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            freezeCheck();
            this.format = format;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            freezeCheck();
            this.key = key;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            freezeCheck();
            this.title = title;
            if (isBlank(this.key)) {
                this.key = title;
            }
        }

        public Set<String> getLabels() {
            return freezeWrap(labels);
        }

        public void setLabels(Set<String> labels) {
            freezeCheck();
            this.labels.clear();
            this.labels.addAll(labels);
        }

        public void addLabels(Set<String> labels) {
            freezeCheck();
            this.labels.addAll(labels);
        }

        public Integer getOrder() {
            return order;
        }

        public void setOrder(Integer order) {
            freezeCheck();
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

        public static Set<Category> sort(Set<Category> categories) {
            TreeSet<Category> tmp = new TreeSet<>(ORDER);
            tmp.addAll(categories);
            return tmp;
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

    public static class Replacer extends AbstractModelObject<Replacer> implements Domain {
        private String search;
        private String replace = "";

        @Override
        public void merge(Replacer replacer) {
            freezeCheck();
            this.search = merge(this.search, replacer.search);
            this.replace = merge(this.replace, replacer.replace);
        }

        public String getSearch() {
            return search;
        }

        public void setSearch(String search) {
            freezeCheck();
            this.search = search;
        }

        public String getReplace() {
            return replace;
        }

        public void setReplace(String replace) {
            freezeCheck();
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

    public static class Labeler extends AbstractModelObject<Labeler> implements Domain {
        public static Comparator<Labeler> ORDER = (o1, o2) -> {
            if (null == o1.getOrder()) return 1;
            if (null == o2.getOrder()) return -1;
            return o1.getOrder().compareTo(o2.getOrder());
        };

        private String label;
        private String title;
        private String body;
        private Integer order;

        @Override
        public void merge(Labeler labeler) {
            freezeCheck();
            this.label = merge(this.label, labeler.label);
            this.title = merge(this.title, labeler.title);
            this.body = merge(this.body, labeler.body);
            this.order = merge(this.order, labeler.order);
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            freezeCheck();
            this.label = label;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            freezeCheck();
            this.title = title;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            freezeCheck();
            this.body = body;
        }

        public Integer getOrder() {
            return order;
        }

        public void setOrder(Integer order) {
            freezeCheck();
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

    public static class Contributors extends AbstractModelObject<Contributors> implements Domain {
        private Boolean enabled;
        private String format;

        @Override
        public void merge(Contributors contributor) {
            freezeCheck();
            this.enabled = merge(this.enabled, contributor.enabled);
            this.format = merge(this.format, contributor.format);
        }

        public boolean isEnabled() {
            return enabled != null && enabled;
        }

        public void setEnabled(Boolean enabled) {
            freezeCheck();
            this.enabled = enabled;
        }

        public boolean isEnabledSet() {
            return enabled != null;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            freezeCheck();
            this.format = format;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("enabled", enabled);
            map.put("format", format);
            return map;
        }

        public boolean isSet() {
            return isNotBlank(format) ||
                null != enabled;
        }
    }

    public static class Hide extends AbstractModelObject<Hide> implements Domain {
        private final Set<String> categories = new LinkedHashSet<>();
        private final Set<String> contributors = new LinkedHashSet<>();
        private Boolean uncategorized;

        @Override
        public void merge(Hide hide) {
            freezeCheck();
            this.uncategorized = merge(this.uncategorized, hide.uncategorized);
            setCategories(merge(this.categories, hide.categories));
            setContributors(merge(this.contributors, hide.contributors));
        }

        public boolean isUncategorized() {
            return uncategorized != null && uncategorized;
        }

        public void setUncategorized(Boolean uncategorized) {
            freezeCheck();
            this.uncategorized = uncategorized;
        }

        public Set<String> getCategories() {
            return freezeWrap(categories);
        }

        public void setCategories(Set<String> categories) {
            freezeCheck();
            this.categories.clear();
            this.categories.addAll(categories.stream().map(String::trim).collect(Collectors.toSet()));
        }

        public void addCategories(Set<String> categories) {
            freezeCheck();
            this.categories.addAll(categories.stream().map(String::trim).collect(Collectors.toSet()));
        }

        public void addCategory(String category) {
            freezeCheck();
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
            return freezeWrap(contributors);
        }

        public void setContributors(Set<String> contributors) {
            freezeCheck();
            this.contributors.clear();
            this.contributors.addAll(contributors.stream().map(String::trim).collect(Collectors.toSet()));
        }

        public void addContributors(Set<String> contributors) {
            freezeCheck();
            this.contributors.addAll(contributors.stream().map(String::trim).collect(Collectors.toSet()));
        }

        public void addContributor(String contributor) {
            freezeCheck();
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

        public boolean isSet() {
            return !categories.isEmpty() ||
                !contributors.isEmpty() ||
                null != uncategorized;
        }
    }
}
