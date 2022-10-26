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
package org.jreleaser.extensions.internal.mustache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.StringEscapeUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jreleaser.bundle.RB;
import org.jreleaser.extensions.api.mustache.MustacheExtensionPoint;
import org.jreleaser.model.Constants;
import org.jreleaser.mustache.MustacheUtils;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static java.nio.file.Files.readAllBytes;
import static org.jreleaser.util.ChecksumUtils.checksum;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class DefaultMustacheExtensionPoint implements MustacheExtensionPoint {
    public void apply(Map<String, Object> context) {
        ZonedDateTime now = (ZonedDateTime) context.get(Constants.KEY_ZONED_DATE_TIME_NOW);
        if (null == now) {
            now = ZonedDateTime.now();
        }
        context.put("f_now", new TimeFormatFunction(now));
        context.put("f_now_gmt", new TimeFormatFunction(now.withZoneSameInstant(ZoneId.of("GMT"))));

        context.put("f_trim", new TrimFunction());
        context.put("f_underscore", new UnderscoreFunction());
        context.put("f_dash", new DashFunction());
        context.put("f_slash", new SlashFunction());
        context.put("f_upper", new UpperFunction());
        context.put("f_lower", new LowerFunction());
        context.put("f_capitalize", new CapitalizeFunction());
        context.put("f_uncapitalize", new UncapitalizeFunction());
        context.put("f_md2html", new MarkdownToHtmlFunction());
        context.put("f_file_read", new FileReadFunction());
        context.put("f_file_size", new FileSizeFunction());
        EnumSet.allOf(Algorithm.class)
            .forEach(algorithm -> context.put("f_checksum_" + algorithm.formatted(), new FileChecksumFunction(algorithm)));
        context.put("f_json", new JsonFunction());
        context.put("f_escape_csv", new DelegatingFunction(StringEscapeUtils::escapeCsv));
        context.put("f_escape_ecma_script", new DelegatingFunction(StringEscapeUtils::escapeEcmaScript));
        context.put("f_escape_html3", new DelegatingFunction(StringEscapeUtils::escapeHtml3));
        context.put("f_escape_html4", new DelegatingFunction(StringEscapeUtils::escapeHtml4));
        context.put("f_escape_java", new DelegatingFunction(StringEscapeUtils::escapeJava));
        context.put("f_escape_json", new DelegatingFunction(StringEscapeUtils::escapeJson));
        context.put("f_escape_xml10", new DelegatingFunction(StringEscapeUtils::escapeXml10));
        context.put("f_escape_xml11", new DelegatingFunction(StringEscapeUtils::escapeXml11));
        context.put("f_escape_xsi", new DelegatingFunction(StringEscapeUtils::escapeXSI));
        context.put("f_chop", new DelegatingFunction(org.apache.commons.lang3.StringUtils::chop));
        context.put("f_delete_whitespace", new DelegatingFunction(org.apache.commons.lang3.StringUtils::deleteWhitespace));
        context.put("f_normalize_whitespace", new DelegatingFunction(org.apache.commons.lang3.StringUtils::normalizeSpace));
        context.put("f_reverse", new DelegatingFunction(org.apache.commons.lang3.StringUtils::reverse));
        context.put("f_strip", new DelegatingFunction(org.apache.commons.lang3.StringUtils::strip));
        context.put("f_swapcase", new DelegatingFunction(org.apache.commons.lang3.StringUtils::swapCase));

        context.put("f_recursive_eval", new RecursiveEvalFunction(context));
    }

    private static class TimeFormatFunction implements Function<String, String> {
        private final ZonedDateTime now;

        private TimeFormatFunction(ZonedDateTime now) {
            this.now = now;
        }

        @Override
        public String apply(String input) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(input);
            return now.format(formatter);
        }
    }

    private static class TrimFunction implements Function<String, String> {
        @Override
        public String apply(String input) {
            return input.trim();
        }
    }

    private static class UnderscoreFunction implements Function<String, String> {
        @Override
        public String apply(String input) {
            return input.replace(".", "_")
                .replace("-", "_")
                .replace("+", "_");
        }
    }

    private static class DashFunction implements Function<String, String> {
        @Override
        public String apply(String input) {
            return input.replace(".", "-")
                .replace("_", "-")
                .replace("+", "-");
        }
    }

    private static class SlashFunction implements Function<String, String> {
        @Override
        public String apply(String input) {
            return input.replace(".", "/")
                .replace("-", "/")
                .replace("+", "/");
        }
    }

    private static class UpperFunction implements Function<String, String> {
        @Override
        public String apply(String input) {
            return input.toUpperCase(Locale.ENGLISH);
        }
    }

    private static class LowerFunction implements Function<String, String> {
        @Override
        public String apply(String input) {
            return input.toLowerCase(Locale.ENGLISH);
        }
    }

    private static class CapitalizeFunction implements Function<String, String> {
        @Override
        public String apply(String input) {
            return StringUtils.capitalize(input);
        }
    }

    private static class UncapitalizeFunction implements Function<String, String> {
        @Override
        public String apply(String input) {
            return StringUtils.uncapitalize(input);
        }
    }

    private static class MarkdownToHtmlFunction implements Function<String, String> {
        @Override
        public String apply(String input) {
            Parser parser = Parser.builder().build();
            Node document = parser.parse(input);
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            return renderer.render(document).trim();
        }
    }

    private static class FileReadFunction implements Function<Object, String> {
        @Override
        public String apply(Object input) {
            try {
                if (input instanceof Path) {
                    return new String(Files.readAllBytes((Path) input));
                } else if (input instanceof File) {
                    return new String(Files.readAllBytes(((File) input).toPath()));
                } else if (input instanceof CharSequence) {
                    return new String(Files.readAllBytes(Paths.get(String.valueOf(input).trim())));
                }
            } catch (IOException e) {
                throw new IllegalStateException(RB.$("ERROR_unexpected_file_read", input), e);
            }

            throw new IllegalStateException(RB.$("ERROR_invalid_file_input", input));
        }
    }

    private static class FileSizeFunction implements Function<Object, Long> {
        @Override
        public Long apply(Object input) {
            try {
                if (input instanceof Path) {
                    return Files.size((Path) input);
                } else if (input instanceof File) {
                    return Files.size(((File) input).toPath());
                } else if (input instanceof CharSequence) {
                    return Files.size(Paths.get(String.valueOf(input).trim()));
                }
            } catch (IOException e) {
                throw new IllegalStateException(RB.$("ERROR_unexpected_file_read", input), e);
            }

            throw new IllegalStateException(RB.$("ERROR_invalid_file_input", input));
        }
    }

    private static class FileChecksumFunction implements Function<Object, String> {
        private final Algorithm algorithm;

        public FileChecksumFunction(Algorithm algorithm) {
            this.algorithm = algorithm;
        }

        @Override
        public String apply(Object input) {
            try {
                if (input instanceof Path) {
                    return checksum(algorithm, readAllBytes((Path) input));
                } else if (input instanceof File) {
                    return checksum(algorithm, readAllBytes(((File) input).toPath()));
                } else if (input instanceof CharSequence) {
                    return checksum(algorithm, readAllBytes(Paths.get(String.valueOf(input).trim())));
                }
            } catch (IOException e) {
                throw new IllegalStateException(RB.$("ERROR_unexpected_file_read", input), e);
            }

            throw new IllegalStateException(RB.$("ERROR_invalid_file_input", input));
        }
    }

    private static class JsonFunction implements Function<Object, String> {
        @Override
        public String apply(Object input) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.writeValueAsString(input);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(RB.$("ERROR_invalid_json_input", input));
            }
        }
    }

    private static class DelegatingFunction implements Function<String, String> {
        private final Function<String, String> delegate;

        private DelegatingFunction(Function<String, String> delegate) {
            this.delegate = delegate;
        }

        @Override
        public String apply(String input) {
            return delegate.apply(input);
        }
    }

    private class RecursiveEvalFunction implements Function<String, String> {
        private final Map<String, Object> context;

        public RecursiveEvalFunction(Map<String, Object> context) {
            this.context = context;
        }

        @Override
        public String apply(String input) {
            return MustacheUtils.applyTemplate(input, context);
        }
    }
}
