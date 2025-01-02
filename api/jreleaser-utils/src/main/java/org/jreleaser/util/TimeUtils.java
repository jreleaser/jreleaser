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
package org.jreleaser.util;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.jreleaser.util.StringUtils.padLeft;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public final class TimeUtils {
    public static final DateTimeFormatter TIMESTAMP_FORMATTER = new DateTimeFormatterBuilder()
        .append(ISO_LOCAL_DATE_TIME)
        .optionalStart()
        .appendOffset("+HH:MM", "Z")
        .optionalEnd()
        .toFormatter();

    private TimeUtils() {
        // noop
    }

    public static String formatDuration(double time) {
        if (time <= 0d) time = 0d;

        String formatted = String.format(Locale.ENGLISH, "%.3f", time) + " s";

        if (time >= 60d) {
            int seconds = (int) time;
            String m = String.valueOf(seconds / 60);
            String s = String.valueOf(seconds % 60);
            formatted = padLeft(m, 2, "0") + ":" + padLeft(s, 2, "0") + " m";
        }

        return formatted;
    }
}
