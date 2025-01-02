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
package org.jreleaser.model.internal;

import org.jreleaser.model.JReleaserException;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class JReleaserModelPrinter {
    private static final String SECRET_KEYWORDS = "password,secret,credential,token,apikey,login,passphrase,consumerkey,publickey,accesskey,webhook";

    private final PrintWriter out;

    protected JReleaserModelPrinter(PrintWriter out) {
        this.out = out;
    }

    public void print(Object value) {
        print(value, 0);
    }

    public void print(Object value, int offset) {
        if (value instanceof Map) {
            doPrintMap((Map<String, ?>) value, offset);
        } else if (value instanceof Collection) {
            doPrintCollection((Collection<?>) value, offset);
        } else if (null != value) {
            Class<?> clazz = value.getClass();
            if (clazz.isArray()) {
                doPrintArray((Object[]) value, offset);
            } else {
                doPrintElement(value, offset);
            }
        }
    }

    private void doPrintMap(Map<String, ?> map, final int offset) {
        if (null != map) {
            map.forEach((key, value) -> {
                if (value instanceof Map) {
                    if (!((Map) value).isEmpty()) {
                        out.println(multiply("    ", offset) + key + ":");
                        doPrintMap((Map) value, offset + 1);
                    }
                } else if (value instanceof Collection) {
                    if (!((Collection) value).isEmpty()) {
                        out.println(multiply("    ", offset) + key + ":");
                        doPrintCollection((Collection) value, offset + 1);
                    }
                } else if (null != value && value.getClass().isArray()) {
                    if (((Object[]) value).length > 0) {
                        out.println(multiply("    ", offset) + key + ":");
                        doPrintArray((Object[]) value, offset + 1);
                    }
                } else if (isNotNullNorBlank(value)) {
                    doPrintMapEntry(key, value, offset);
                }

                if (offset == 0) {
                    out.println(" ");
                }
            });
        }
    }

    private void doPrintMapEntry(String key, Object value, int offset) {
        if (value instanceof Map) {
            doPrintMap(key, (Map) value, offset);
        } else if (value instanceof Collection) {
            doPrintCollection(key, (Collection<?>) value, offset);
        } else if (null != value) {
            Class<?> clazz = value.getClass();
            if (clazz.isArray()) {
                doPrintArray(key, (Object[]) value, offset);
            } else {
                String result = formatValue(value, isSecret(key));
                if (isNotNullNorBlank(result)) {
                    out.println(multiply("    ", offset) + key + ": " + result);
                }
            }
        }
    }

    private void doPrintCollection(Collection<?> collection, final int offset) {
        if (null != collection) {
            collection.forEach(value -> {
                if (value instanceof Map) {
                    if (!((Map) value).isEmpty()) {
                        doPrintMap((Map) value, offset);
                    }
                } else if (value instanceof Collection) {
                    if (!((Collection) value).isEmpty()) {
                        doPrintCollection((Collection) value, offset + 1);
                    }
                } else if (null != value && value.getClass().isArray()) {
                    if (((Object[]) value).length > 0) {
                        doPrintArray((Object[]) value, offset + 1);
                    }
                } else if (isNotNullNorBlank(value)) {
                    doPrintElement(value, offset);
                }
            });
        }
    }

    private void doPrintArray(Object[] array, final int offset) {
        if (null != array) {
            Arrays.stream(array).forEach(value -> {
                if (value instanceof Map) {
                    if (!((Map) value).isEmpty()) {
                        doPrintMap((Map) value, offset);
                    }
                } else if (value instanceof Collection) {
                    if (!((Collection) value).isEmpty()) {
                        doPrintCollection((Collection) value, offset + 1);
                    }
                } else if (null != value && value.getClass().isArray()) {
                    if (((Object[]) value).length > 0) {
                        doPrintArray((Object[]) value, offset + 1);
                    }
                } else if (isNotNullNorBlank(value)) {
                    doPrintElement(value, offset);
                }
            });
        }
    }

    private void doPrintMap(String key, Map<String, ?> map, int offset) {
        if (null != map && !map.isEmpty()) {
            out.println(multiply("    ", offset) + key + ':');
            doPrintMap(map, offset + 1);
        }
    }

    private void doPrintCollection(String key, Collection<?> collection, int offset) {
        if (null != collection && !collection.isEmpty()) {
            out.println(multiply("    ", offset) + key + ':');
            doPrintCollection(collection, offset + 1);
        }
    }

    private void doPrintArray(String key, Object[] array, int offset) {
        if (null != array && array.length > 0) {
            out.println(multiply("    ", offset) + key + ':');
            doPrintArray(array, offset + 1);
        }
    }

    private void doPrintElement(Object value, int offset) {
        String result = formatValue(value);
        if (isNotNullNorBlank(result)) {
            out.println(multiply("    ", offset) + result);
        }
    }

    private boolean isNotNullNorBlank(Object value) {
        if (value instanceof CharSequence) {
            return isNotBlank(String.valueOf(value));
        }

        return null != value;
    }

    private String formatValue(Object value) {
        return formatValue(value, false);
    }

    private String formatValue(Object value, boolean secret) {
        if (value instanceof Boolean) {
            Boolean b = (Boolean) value;
            return b ? green(String.valueOf(b)) : red(String.valueOf(b));
        } else if (value instanceof Number) {
            return cyan(String.valueOf(value));
        } else if (null != value) {
            String s = String.valueOf(value);
            if (secret && !UNSET.equals(s)) {
                s = HIDE;
            }

            String r = parseAsBoolean(s);
            if (null != r) return r;
            r = parseAsInteger(s);
            if (null != r) return r;
            r = parseAsDouble(s);
            if (null != r) return r;

            return secret ? magenta(s) : yellow(s);
        }

        return String.valueOf(value);
    }

    private String parseAsBoolean(String s) {
        if ("true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s)) {
            boolean b = Boolean.parseBoolean(s);
            return b ? green(String.valueOf(b)) : red(String.valueOf(b));
        } else {
            return null;
        }
    }

    private String parseAsInteger(String s) {
        try {
            Integer.parseInt(s);
            return cyan(s);
        } catch (Exception e) {
            return null;
        }
    }

    private String parseAsDouble(String s) {
        try {
            Double.parseDouble(s);
            return cyan(s);
        } catch (Exception e) {
            return null;
        }
    }

    private String cyan(String s) {
        return color("cyan", s);
    }

    private String red(String s) {
        return color("red", s);
    }

    private String green(String s) {
        return color("green", s);
    }

    private String magenta(String s) {
        return color("magenta", s);
    }

    private String yellow(String s) {
        return color("yellow", s);
    }

    protected abstract String color(String color, String input);

    public static boolean isSecret(String key) {
        String lower = key.toLowerCase(Locale.ENGLISH);

        for (String keyword : SECRET_KEYWORDS.split(",")) {
            if (lower.contains(keyword.trim().toLowerCase(Locale.ENGLISH))) return true;
        }

        return false;
    }

    private static String multiply(CharSequence self, Number factor) {
        int size = factor.intValue();
        if (size == 0) {
            return "";
        } else if (size < 0) {
            throw new JReleaserException("multiply() should be called with a number of 0 or greater not: " + size);
        } else {
            StringBuilder answer = new StringBuilder(self);

            for (int i = 1; i < size; ++i) {
                answer.append(self);
            }

            return answer.toString();
        }
    }

    public static class Plain extends JReleaserModelPrinter {
        public Plain(PrintWriter out) {
            super(out);
        }

        @Override
        protected String color(String color, String input) {
            return input;
        }
    }
}
