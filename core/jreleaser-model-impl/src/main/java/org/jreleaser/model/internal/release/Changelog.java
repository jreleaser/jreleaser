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
package org.jreleaser.model.internal.release;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.EnabledAware;
import org.jreleaser.model.internal.project.Project;

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

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.toSafeRegexPattern;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class Changelog extends AbstractModelObject<Changelog> implements Domain, EnabledAware {
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
    private org.jreleaser.model.Changelog.Sort sort;
    private String external;
    private Active formatted;
    private String format;
    private String content;
    private String contentTemplate;
    private String preset;

    private final org.jreleaser.model.api.release.Changelog immutable = new org.jreleaser.model.api.release.Changelog() {
        private Set<? extends org.jreleaser.model.api.release.Changelog.Category> categories;
        private List<? extends org.jreleaser.model.api.release.Changelog.Replacer> replacers;
        private Set<? extends org.jreleaser.model.api.release.Changelog.Labeler> labelers;

        @Override
        public boolean isLinks() {
            return Changelog.this.isLinks();
        }

        @Override
        public boolean isSkipMergeCommits() {
            return Changelog.this.isSkipMergeCommits();
        }

        @Override
        public org.jreleaser.model.Changelog.Sort getSort() {
            return sort;
        }

        @Override
        public String getExternal() {
            return external;
        }

        @Override
        public Active getFormatted() {
            return formatted;
        }

        @Override
        public Set<String> getIncludeLabels() {
            return unmodifiableSet(includeLabels);
        }

        @Override
        public Set<String> getExcludeLabels() {
            return unmodifiableSet(excludeLabels);
        }

        @Override
        public Set<? extends org.jreleaser.model.api.release.Changelog.Category> getCategories() {
            if (null == categories) {
                categories = Changelog.this.categories.stream()
                    .map(Changelog.Category::asImmutable)
                    .collect(toSet());
            }
            return categories;
        }

        @Override
        public List<? extends org.jreleaser.model.api.release.Changelog.Replacer> getReplacers() {
            if (null == replacers) {
                replacers = Changelog.this.replacers.stream()
                    .map(Changelog.Replacer::asImmutable)
                    .collect(toList());
            }
            return replacers;
        }

        @Override
        public Set<? extends org.jreleaser.model.api.release.Changelog.Labeler> getLabelers() {
            if (null == labelers) {
                labelers = Changelog.this.labelers.stream()
                    .map(Changelog.Labeler::asImmutable)
                    .collect(toSet());
            }
            return labelers;
        }

        @Override
        public String getFormat() {
            return format;
        }

        @Override
        public String getContent() {
            return content;
        }

        @Override
        public String getContentTemplate() {
            return contentTemplate;
        }

        @Override
        public String getPreset() {
            return preset;
        }

        @Override
        public Hide getHide() {
            return hide.asImmutable();
        }

        @Override
        public Contributors getContributors() {
            return contributors.asImmutable();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return unmodifiableMap(Changelog.this.asMap(full));
        }

        @Override
        public boolean isEnabled() {
            return Changelog.this.isEnabled();
        }
    };

    public org.jreleaser.model.api.release.Changelog asImmutable() {
        return immutable;
    }

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
    public void merge(Changelog source) {
        this.enabled = merge(this.enabled, source.enabled);
        this.links = merge(this.links, source.links);
        this.skipMergeCommits = merge(this.skipMergeCommits, source.skipMergeCommits);
        this.sort = merge(this.sort, source.sort);
        this.external = merge(this.external, source.external);
        this.formatted = merge(this.formatted, source.formatted);
        this.format = merge(this.format, source.format);
        this.content = merge(this.content, source.content);
        this.contentTemplate = merge(this.contentTemplate, source.contentTemplate);
        this.preset = merge(this.preset, source.preset);
        setIncludeLabels(merge(this.includeLabels, source.includeLabels));
        setExcludeLabels(merge(this.excludeLabels, source.excludeLabels));
        setCategories(merge(this.categories, source.categories));
        setReplacers(merge(this.replacers, source.replacers));
        setLabelers(merge(this.labelers, source.labelers));
        setHide(source.hide);
        setContributors(source.contributors);
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

    public boolean isSkipMergeCommits() {
        return skipMergeCommits != null && skipMergeCommits;
    }

    public void setSkipMergeCommits(Boolean skipMergeCommits) {
        this.skipMergeCommits = skipMergeCommits;
    }

    public org.jreleaser.model.Changelog.Sort getSort() {
        return sort;
    }

    public void setSort(org.jreleaser.model.Changelog.Sort sort) {
        this.sort = sort;
    }

    public void setSort(String sort) {
        if (isNotBlank(sort)) {
            setSort(org.jreleaser.model.Changelog.Sort.valueOf(sort.toUpperCase(Locale.ENGLISH)));
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
        setFormatted(Active.of(str));
    }

    public boolean isFormattedSet() {
        return formatted != null;
    }

    public Set<String> getIncludeLabels() {
        return includeLabels;
    }

    public void setIncludeLabels(Set<String> includeLabels) {
        this.includeLabels.clear();
        this.includeLabels.addAll(includeLabels.stream().map(String::trim).collect(toSet()));
    }

    public Set<String> getExcludeLabels() {
        return excludeLabels;
    }

    public void setExcludeLabels(Set<String> excludeLabels) {
        this.excludeLabels.clear();
        this.excludeLabels.addAll(excludeLabels.stream().map(String::trim).collect(toSet()));
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

    public static final class Category extends AbstractModelObject<Category> implements Domain {
        public static final Comparator<Category> ORDER = (o1, o2) -> {
            if (null == o1.getOrder()) return 1;
            if (null == o2.getOrder()) return -1;
            return o1.getOrder().compareTo(o2.getOrder());
        };

        private final Set<String> labels = new LinkedHashSet<>();

        private String key;
        private String title;
        private String format;
        private Integer order;

        private final org.jreleaser.model.api.release.Changelog.Category immutable = new org.jreleaser.model.api.release.Changelog.Category() {
            @Override
            public String getFormat() {
                return format;
            }

            @Override
            public String getKey() {
                return key;
            }

            @Override
            public String getTitle() {
                return title;
            }

            @Override
            public Set<String> getLabels() {
                return unmodifiableSet(labels);
            }

            @Override
            public Integer getOrder() {
                return order;
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Category.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.release.Changelog.Category asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Category source) {
            this.key = merge(this.key, source.key);
            this.title = merge(this.title, source.title);
            this.format = merge(this.format, source.format);
            this.order = merge(this.order, source.order);
            setLabels(merge(this.labels, source.labels));
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

    public static final class Replacer extends AbstractModelObject<Replacer> implements Domain {
        private String search;
        private String replace = "";

        private final org.jreleaser.model.api.release.Changelog.Replacer immutable = new org.jreleaser.model.api.release.Changelog.Replacer() {
            @Override
            public String getSearch() {
                return search;
            }

            @Override
            public String getReplace() {
                return replace;
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Replacer.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.release.Changelog.Replacer asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Replacer source) {
            this.search = merge(this.search, source.search);
            this.replace = merge(this.replace, source.replace);
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

    public static final class Labeler extends AbstractModelObject<Labeler> implements Domain {
        public static final Comparator<Labeler> ORDER = (o1, o2) -> {
            if (null == o1.getOrder()) return 1;
            if (null == o2.getOrder()) return -1;
            return o1.getOrder().compareTo(o2.getOrder());
        };

        private String label;
        private String title;
        private String body;
        private Integer order;

        private final org.jreleaser.model.api.release.Changelog.Labeler immutable = new org.jreleaser.model.api.release.Changelog.Labeler() {
            @Override
            public String getLabel() {
                return label;
            }

            @Override
            public String getTitle() {
                return title;
            }

            @Override
            public String getBody() {
                return body;
            }

            @Override
            public Integer getOrder() {
                return order;
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Labeler.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.release.Changelog.Labeler asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Labeler source) {
            this.label = merge(this.label, source.label);
            this.title = merge(this.title, source.title);
            this.body = merge(this.body, source.body);
            this.order = merge(this.order, source.order);
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

    public static final class Contributors extends AbstractModelObject<Contributors> implements Domain, EnabledAware {
        private Boolean enabled;
        private String format;

        private final org.jreleaser.model.api.release.Changelog.Contributors immutable = new org.jreleaser.model.api.release.Changelog.Contributors() {
            @Override
            public String getFormat() {
                return format;
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Contributors.this.asMap(full));
            }

            @Override
            public boolean isEnabled() {
                return Contributors.this.isEnabled();
            }
        };

        public org.jreleaser.model.api.release.Changelog.Contributors asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Contributors source) {
            this.enabled = merge(this.enabled, source.enabled);
            this.format = merge(this.format, source.format);
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

        public boolean isSet() {
            return isNotBlank(format) ||
                null != enabled;
        }
    }

    public static final class Hide extends AbstractModelObject<Hide> implements Domain {
        private final Set<String> categories = new LinkedHashSet<>();
        private final Set<String> contributors = new LinkedHashSet<>();
        private Boolean uncategorized;

        private final org.jreleaser.model.api.release.Changelog.Hide immutable = new org.jreleaser.model.api.release.Changelog.Hide() {
            @Override
            public boolean isUncategorized() {
                return Hide.this.isUncategorized();
            }

            @Override
            public Set<String> getCategories() {
                return unmodifiableSet(categories);
            }

            @Override
            public boolean containsCategory(String category) {
                return Hide.this.containsCategory(category);
            }

            @Override
            public Set<String> getContributors() {
                return unmodifiableSet(contributors);
            }

            @Override
            public boolean containsContributor(String name) {
                return Hide.this.containsContributor(name);
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Hide.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.release.Changelog.Hide asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Hide source) {
            this.uncategorized = merge(this.uncategorized, source.uncategorized);
            setCategories(merge(this.categories, source.categories));
            setContributors(merge(this.contributors, source.contributors));
        }

        public boolean isUncategorized() {
            return uncategorized != null && uncategorized;
        }

        public void setUncategorized(Boolean uncategorized) {
            this.uncategorized = uncategorized;
        }

        public Set<String> getCategories() {
            return categories;
        }

        public void setCategories(Set<String> categories) {
            this.categories.clear();
            this.categories.addAll(categories.stream().map(String::trim).collect(toSet()));
        }

        public void addCategories(Set<String> categories) {
            this.categories.addAll(categories.stream().map(String::trim).collect(toSet()));
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
            this.contributors.addAll(contributors.stream().map(String::trim).collect(toSet()));
        }

        public void addContributors(Set<String> contributors) {
            this.contributors.addAll(contributors.stream().map(String::trim).collect(toSet()));
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
                    if (n.contains(contributor) || n.matches(contributor)) {
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
