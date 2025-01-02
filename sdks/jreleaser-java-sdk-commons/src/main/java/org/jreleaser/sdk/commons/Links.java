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
package org.jreleaser.sdk.commons;

import java.util.Collection;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Links {
    private static final Pattern REL_PATTERN = Pattern.compile("rel=\"(.*)\"");

    private String first;
    private String next;
    private String prev;
    private String last;

    private Links(String input) {
        if (!isBlank(input)) {
            for (String s : input.split(",")) {
                String[] parts = s.split(";");
                Matcher matcher = REL_PATTERN.matcher(parts[1].trim());
                if (matcher.matches()) {
                    switch (matcher.group(1).toLowerCase(Locale.ENGLISH)) {
                        case "first":
                            first = normalize(parts[0]);
                            break;
                        case "next":
                            next = normalize(parts[0]);
                            break;
                        case "prev":
                            prev = normalize(parts[0]);
                            break;
                        case "last":
                            last = normalize(parts[0]);
                            break;
                        default:
                            // noop
                    }
                }
            }
        }
    }

    private String normalize(String url) {
        url = url.trim();
        if (url.startsWith("<") && url.endsWith(">")) {
            url = url.substring(1, url.length() - 1);
        }
        return url;
    }

    public boolean isEmpty() {
        return !hasFirst() &&
            !hasLast() &&
            !hasPrev() &&
            !hasNext();
    }

    public boolean hasFirst() {
        return !isBlank(first);
    }

    public boolean hasNext() {
        return !isBlank(next);
    }

    public boolean hasPrev() {
        return !isBlank(prev);
    }

    public boolean hasLast() {
        return !isBlank(last);
    }

    public String first() {
        return first;
    }

    public String next() {
        return next;
    }

    public String prev() {
        return prev;
    }

    public String last() {
        return last;
    }

    @Override
    public String toString() {
        return "Links[" + "first='" + first + '\'' +
            ", next='" + next + '\'' +
            ", prev='" + prev + '\'' +
            ", last='" + last + '\'' +
            ']';
    }

    public static Links of(String input) {
        return new Links(input);
    }

    public static Links of(Collection<String> input) {
        return new Links(null != input && !input.isEmpty() ? input.iterator().next() : "");
    }
}
