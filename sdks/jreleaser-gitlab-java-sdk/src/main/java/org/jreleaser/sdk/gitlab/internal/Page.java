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
package org.jreleaser.sdk.gitlab.internal;

import org.jreleaser.sdk.commons.Links;

import java.util.Collection;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public class Page<T> {
    private final int nextPage;
    private final int page;
    private final int perPage;
    private final int prevPage;
    private final int total;
    private final int totalPages;
    private final Links links;
    private final T content;

    public Page(Map<String, Collection<String>> headers, T content) {
        this.nextPage = resolveInt(headers, "x-next-page");
        this.page = resolveInt(headers, "x-page");
        this.perPage = resolveInt(headers, "x-per-page");
        this.prevPage = resolveInt(headers, "x-prev-page");
        this.total = resolveInt(headers, "x-total");
        this.totalPages = resolveInt(headers, "x-total-pages");
        this.links = Links.of(headers.get("link"));
        this.content = content;
    }

    public boolean hasLinks() {
        return !links.isEmpty();
    }

    public int getNextPage() {
        return nextPage;
    }

    public int getPage() {
        return page;
    }

    public int getPerPage() {
        return perPage;
    }

    public int getPrevPage() {
        return prevPage;
    }

    public int getTotal() {
        return total;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public Links getLinks() {
        return links;
    }

    public T getContent() {
        return content;
    }

    private int resolveInt(Map<String, Collection<String>> headers, String key) {
        Collection<String> values = headers.get(key);
        if (null != values && !values.isEmpty()) {
            try {
                return Integer.parseInt(values.iterator().next());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Page[" +
            "nextPage=" + nextPage +
            ", page=" + page +
            ", perPage=" + perPage +
            ", prevPage=" + prevPage +
            ", total=" + total +
            ", totalPages=" + totalPages +
            ", links=" + links +
            "]";
    }
}
