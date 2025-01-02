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
package org.jreleaser.model.api.release;

import org.jreleaser.model.Active;
import org.jreleaser.model.api.common.Domain;
import org.jreleaser.model.api.common.EnabledAware;
import org.jreleaser.model.api.common.ExtraProperties;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface Changelog extends Domain, EnabledAware, ExtraProperties {
    boolean isLinks();

    boolean isSkipMergeCommits();

    org.jreleaser.model.Changelog.Sort getSort();

    String getExternal();

    Active getFormatted();

    Set<String> getIncludeLabels();

    Set<String> getExcludeLabels();

    Set<? extends Category> getCategories();

    List<? extends Replacer> getReplacers();

    Set<? extends Labeler> getLabelers();

    String getFormat();

    String getCategoryTitleFormat();

    String getContributorsTitleFormat();

    String getContent();

    String getContentTemplate();

    String getPreset();

    Hide getHide();

    Contributors getContributors();

    Append getAppend();

    interface Append extends Domain {
        boolean isEnabled();

        String getTitle();

        String getTarget();

        String getContent();

        String getContentTemplate();
    }

    interface Category extends Domain {
        Comparator<Category> ORDER = (o1, o2) -> {
            if (null == o1.getOrder()) return 1;
            if (null == o2.getOrder()) return -1;
            return o1.getOrder().compareTo(o2.getOrder());
        };

        String getFormat();

        String getKey();

        String getTitle();

        Set<String> getLabels();

        Integer getOrder();
    }

    interface Labeler extends Domain {
        Comparator<Labeler> ORDER = (o1, o2) -> {
            if (null == o1.getOrder()) return 1;
            if (null == o2.getOrder()) return -1;
            return o1.getOrder().compareTo(o2.getOrder());
        };

        String getLabel();

        String getTitle();

        String getBody();

        String getContributor();

        Integer getOrder();
    }

    interface Replacer extends Domain {
        String getSearch();

        String getReplace();
    }

    interface Contributors extends Domain, EnabledAware {
        String getFormat();
    }

    interface Hide extends Domain {
        boolean isUncategorized();

        Set<String> getCategories();

        boolean containsCategory(String category);

        Set<String> getContributors();

        boolean containsContributor(String name);
    }
}
