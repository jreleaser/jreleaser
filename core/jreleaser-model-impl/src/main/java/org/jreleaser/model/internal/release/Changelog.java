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
package org.jreleaser.model.internal.release;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.Active;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Domain;
import org.jreleaser.model.internal.common.EnabledAware;
import org.jreleaser.model.internal.common.ExtraProperties;
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
import static org.jreleaser.util.StringUtils.normalizeRegexPattern;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class Changelog extends AbstractModelObject<Changelog> implements Domain, EnabledAware, ExtraProperties {
    private static final long serialVersionUID = -2693712593082430980L;

    private final Map<String, Object> extraProperties = new LinkedHashMap<>();
    private final Set<String> includeLabels = new LinkedHashSet<>();
    private final Set<String> excludeLabels = new LinkedHashSet<>();
    private final Set<Category> categories = new TreeSet<>(Category.ORDER_COMPARATOR);
    private final List<Replacer> replacers = new ArrayList<>();
    private final Set<Labeler> labelers = new TreeSet<>(Labeler.ORDER_COMPARATOR);
    private final Hide hide = new Hide();
    private final Contributors contributors = new Contributors();
    private final Append append = new Append();

    private Boolean enabled;
    private Boolean links;
    private Boolean skipMergeCommits;
    private org.jreleaser.model.Changelog.Sort sort;
    private String external;
    private Active formatted;
    private String format;
    private String categoryTitleFormat;
    private String contributorsTitleFormat;
    private String content;
    private String contentTemplate;
    private String preset;

    @JsonIgnore
    private final org.jreleaser.model.api.release.Changelog immutable = new org.jreleaser.model.api.release.Changelog() {
        private static final long serialVersionUID = 3830727279862963658L;

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
        public String getPrefix() {
            return Changelog.this.prefix();
        }

        @Override
        public Map<String, Object> getExtraProperties() {
            return unmodifiableMap(Changelog.this.getExtraProperties());
        }

        @Override
        public String getFormat() {
            return format;
        }

        @Override
        public String getCategoryTitleFormat() {
            return categoryTitleFormat;
        }

        @Override
        public String getContributorsTitleFormat() {
            return contributorsTitleFormat;
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
        public Append getAppend() {
            return append.asImmutable();
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
            append.isSet() ||
            null != links ||
            null != skipMergeCommits ||
            null != sort ||
            null != formatted ||
            isNotBlank(external) ||
            isNotBlank(format) ||
            isNotBlank(categoryTitleFormat) ||
            isNotBlank(contributorsTitleFormat) ||
            isNotBlank(content) ||
            isNotBlank(contentTemplate) ||
            isNotBlank(preset) ||
            !extraProperties.isEmpty();
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
        this.categoryTitleFormat = merge(this.categoryTitleFormat, source.categoryTitleFormat);
        this.contributorsTitleFormat = merge(this.contributorsTitleFormat, source.contributorsTitleFormat);
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
        setAppend(source.append);
        setExtraProperties(merge(this.extraProperties, source.getExtraProperties()));
    }

    @Override
    public String prefix() {
        return "changelog";
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
        return null != enabled && enabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabledSet() {
        return null != enabled;
    }

    public boolean isLinks() {
        return null != links && links;
    }

    public void setLinks(Boolean links) {
        this.links = links;
    }

    public boolean isSkipMergeCommits() {
        return null != skipMergeCommits && skipMergeCommits;
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
        return null != formatted;
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

    public String getCategoryTitleFormat() {
        return categoryTitleFormat;
    }

    public void setCategoryTitleFormat(String categoryTitleFormat) {
        this.categoryTitleFormat = categoryTitleFormat;
    }

    public String getContributorsTitleFormat() {
        return contributorsTitleFormat;
    }

    public void setContributorsTitleFormat(String contributorsTitleFormat) {
        this.contributorsTitleFormat = contributorsTitleFormat;
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

    public Append getAppend() {
        return append;
    }

    public void setAppend(Append append) {
        this.append.merge(append);
    }

    @Override
    public Map<String, Object> getExtraProperties() {
        return extraProperties;
    }

    @Override
    public void setExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.clear();
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public void addExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties.putAll(extraProperties);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("external", external);
        map.put("append", append.asMap(full));
        map.put("links", isLinks());
        map.put("skipMergeCommits", isSkipMergeCommits());
        map.put("sort", sort);
        map.put("formatted", formatted);
        map.put("preset", preset);
        map.put("format", format);
        map.put("categoryTitleFormat", categoryTitleFormat);
        map.put("contributorsTitleFormat", contributorsTitleFormat);
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
        map.put("extraProperties", getExtraProperties());

        return map;
    }

    public static final class Append extends AbstractModelObject<Append> implements Domain {
        private static final long serialVersionUID = -7396820796498154377L;

        private Boolean enabled;
        private String title;
        private String target;
        private String content;
        private String contentTemplate;

        @JsonIgnore
        private final org.jreleaser.model.api.release.Changelog.Append immutable = new org.jreleaser.model.api.release.Changelog.Append() {
            private static final long serialVersionUID = -5635998660542618226L;

            @Override
            public boolean isEnabled() {
                return Append.this.isEnabled();
            }

            @Override
            public String getTitle() {
                return title;
            }

            @Override
            public String getTarget() {
                return target;
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
            public Map<String, Object> asMap(boolean full) {
                return unmodifiableMap(Append.this.asMap(full));
            }
        };

        public org.jreleaser.model.api.release.Changelog.Append asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Append source) {
            this.enabled = merge(this.enabled, source.enabled);
            this.title = merge(this.title, source.title);
            this.target = merge(this.target, source.target);
            this.content = merge(this.content, source.content);
            this.contentTemplate = merge(this.contentTemplate, source.contentTemplate);
        }

        public boolean isEnabled() {
            return null != enabled && enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
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

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("enabled", isEnabled());
            map.put("title", title);
            map.put("target", target);
            map.put("content", content);
            map.put("contentTemplate", contentTemplate);
            return map;
        }

        public boolean isSet() {
            return isNotBlank(title) ||
                isNotBlank(target) ||
                isNotBlank(content) ||
                isNotBlank(contentTemplate) ||
                null != enabled;
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
    }

    public static final class Category extends AbstractModelObject<Category> implements Domain {
        public static final Comparator<Category> ORDER_COMPARATOR = (o1, o2) -> {
            if (null == o1.getOrder()) return 1;
            if (null == o2.getOrder()) return -1;
            return o1.getOrder().compareTo(o2.getOrder());
        };

        private static final long serialVersionUID = 8812582603331073781L;

        private final Set<String> labels = new LinkedHashSet<>();

        private String key;
        private String title;
        private String format;
        private Integer order;

        @JsonIgnore
        private final org.jreleaser.model.api.release.Changelog.Category immutable = new org.jreleaser.model.api.release.Changelog.Category() {
            private static final long serialVersionUID = -6331412945094114818L;

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
            if (null == o || getClass() != o.getClass()) return false;
            Category category = (Category) o;
            return key.equals(category.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title);
        }

        public static Set<Category> sort(Set<Category> categories) {
            TreeSet<Category> tmp = new TreeSet<>(ORDER_COMPARATOR);
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
        private static final long serialVersionUID = -3996062461946189421L;

        private String search;
        private String replace = "";

        @JsonIgnore
        private final org.jreleaser.model.api.release.Changelog.Replacer immutable = new org.jreleaser.model.api.release.Changelog.Replacer() {
            private static final long serialVersionUID = -8515498818759834354L;

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
        public static final Comparator<Labeler> ORDER_COMPARATOR = (o1, o2) -> {
            if (null == o1.getOrder()) return 1;
            if (null == o2.getOrder()) return -1;
            return o1.getOrder().compareTo(o2.getOrder());
        };

        private static final long serialVersionUID = -4123935426541119426L;

        private String label;
        private String title;
        private String body;
        private String contributor;
        private Integer order;

        @JsonIgnore
        private final org.jreleaser.model.api.release.Changelog.Labeler immutable = new org.jreleaser.model.api.release.Changelog.Labeler() {
            private static final long serialVersionUID = -2795460013799421769L;

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
            public String getContributor() {
                return contributor;
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
            this.contributor = merge(this.contributor, source.contributor);
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

        public String getContributor() {
            return contributor;
        }

        public void setContributor(String contributor) {
            this.contributor = contributor;
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
            if (null == o || getClass() != o.getClass()) return false;
            Labeler labeler = (Labeler) o;
            return Objects.equals(title, labeler.title) &&
                Objects.equals(body, labeler.body) &&
                Objects.equals(contributor, labeler.contributor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, body, contributor);
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("label", label);
            map.put("title", title);
            map.put("body", body);
            map.put("contributor", contributor);
            map.put("order", order);
            return map;
        }
    }

    public static final class Contributors extends AbstractModelObject<Contributors> implements Domain, EnabledAware {
        private static final long serialVersionUID = 3162308397837135084L;

        private Boolean enabled;
        private String format;

        @JsonIgnore
        private final org.jreleaser.model.api.release.Changelog.Contributors immutable = new org.jreleaser.model.api.release.Changelog.Contributors() {
            private static final long serialVersionUID = 1849581704581927871L;

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
            return null != enabled && enabled;
        }

        @Override
        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public boolean isEnabledSet() {
            return null != enabled;
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
        private static final long serialVersionUID = 314185207203186567L;

        private static final String REGEX_PREFIX = "regex:";

        private final Set<String> categories = new LinkedHashSet<>();
        private final Set<String> contributors = new LinkedHashSet<>();
        private Boolean uncategorized;

        @JsonIgnore
        private final org.jreleaser.model.api.release.Changelog.Hide immutable = new org.jreleaser.model.api.release.Changelog.Hide() {
            private static final long serialVersionUID = 4820100134325634530L;

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
            return null != uncategorized && uncategorized;
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
                    if (contributor.startsWith(REGEX_PREFIX)) {
                        String regex = contributor.substring(REGEX_PREFIX.length());
                        if (n.matches(normalizeRegexPattern(regex))) {
                            return true;
                        }
                    } else if (n.contains(contributor) || n.matches(contributor)) {
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
