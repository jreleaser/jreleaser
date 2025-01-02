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
package org.jreleaser.extensions.internal.mustache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.StringEscapeUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentRenderer;
import org.jreleaser.bundle.RB;
import org.jreleaser.extensions.api.mustache.MustacheExtensionPoint;
import org.jreleaser.model.Constants;
import org.jreleaser.mustache.MustacheUtils;
import org.jreleaser.mustache.TemplateContext;
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
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static org.jreleaser.util.ChecksumUtils.checksum;
import static org.jreleaser.util.MarkdownUtils.createMarkdownParser;
import static org.jreleaser.util.MarkdownUtils.createTextContentRenderer;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class DefaultMustacheExtensionPoint implements MustacheExtensionPoint {
    @Override
    public void apply(TemplateContext context) {
        ZonedDateTime now = context.get(Constants.KEY_ZONED_DATE_TIME_NOW);
        if (null == now) {
            now = ZonedDateTime.now();
        }
        context.set("f_now", new TimeFormatFunction(now));
        context.set("f_now_gmt", new TimeFormatFunction(now.withZoneSameInstant(ZoneId.of("GMT"))));

        context.set("f_trim", new TrimFunction());
        context.set("f_underscore", new UnderscoreFunction());
        context.set("f_dash", new DashFunction());
        context.set("f_slash", new SlashFunction());
        context.set("f_upper", new UpperFunction());
        context.set("f_lower", new LowerFunction());
        context.set("f_capitalize", new CapitalizeFunction());
        context.set("f_uncapitalize", new UncapitalizeFunction());
        context.set("f_md2html", new MarkdownToHtmlFunction());
        context.set("f_file_exists", new FileExistsFunction());
        context.set("f_file_read", new FileReadFunction());
        context.set("f_file_size", new FileSizeFunction());
        EnumSet.allOf(Algorithm.class)
            .forEach(algorithm -> context.set("f_checksum_" + algorithm.formatted(), new FileChecksumFunction(algorithm)));
        context.set("f_json", new JsonFunction());
        context.set("f_escape_csv", new DelegatingFunction(StringEscapeUtils::escapeCsv));
        context.set("f_escape_ecma_script", new DelegatingFunction(StringEscapeUtils::escapeEcmaScript));
        context.set("f_escape_html3", new DelegatingFunction(StringEscapeUtils::escapeHtml3));
        context.set("f_escape_html4", new DelegatingFunction(StringEscapeUtils::escapeHtml4));
        context.set("f_escape_java", new DelegatingFunction(StringEscapeUtils::escapeJava));
        context.set("f_escape_json", new DelegatingFunction(StringEscapeUtils::escapeJson));
        context.set("f_escape_xml10", new DelegatingFunction(StringEscapeUtils::escapeXml10));
        context.set("f_escape_xml11", new DelegatingFunction(StringEscapeUtils::escapeXml11));
        context.set("f_escape_xsi", new DelegatingFunction(StringEscapeUtils::escapeXSI));
        context.set("f_chop", new DelegatingFunction(org.apache.commons.lang3.StringUtils::chop));
        context.set("f_chomp", new DelegatingFunction(org.apache.commons.lang3.StringUtils::chomp));
        context.set("f_delete_whitespace", new DelegatingFunction(org.apache.commons.lang3.StringUtils::deleteWhitespace));
        context.set("f_normalize_whitespace", new DelegatingFunction(org.apache.commons.lang3.StringUtils::normalizeSpace));
        context.set("f_reverse", new DelegatingFunction(org.apache.commons.lang3.StringUtils::reverse));
        context.set("f_strip", new DelegatingFunction(org.apache.commons.lang3.StringUtils::strip));
        context.set("f_swapcase", new DelegatingFunction(org.apache.commons.lang3.StringUtils::swapCase));

        context.set("f_recursive_eval", new RecursiveEvalFunction(context));
    }

    private static class TimeFormatFunction implements UnaryOperator<String> {
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

    private static class TrimFunction implements UnaryOperator<String> {
        @Override
        public String apply(String input) {
            return input.trim();
        }
    }

    private static class UnderscoreFunction implements UnaryOperator<String> {
        @Override
        public String apply(String input) {
            return input.replace(".", "_")
                .replace("-", "_")
                .replace("+", "_");
        }
    }

    private static class DashFunction implements UnaryOperator<String> {
        @Override
        public String apply(String input) {
            return input.replace(".", "-")
                .replace("_", "-")
                .replace("+", "-");
        }
    }

    private static class SlashFunction implements UnaryOperator<String> {
        @Override
        public String apply(String input) {
            return input.replace(".", "/")
                .replace("-", "/")
                .replace("+", "/");
        }
    }

    private static class UpperFunction implements UnaryOperator<String> {
        @Override
        public String apply(String input) {
            return input.toUpperCase(Locale.ENGLISH);
        }
    }

    private static class LowerFunction implements UnaryOperator<String> {
        @Override
        public String apply(String input) {
            return input.toLowerCase(Locale.ENGLISH);
        }
    }

    private static class CapitalizeFunction implements UnaryOperator<String> {
        @Override
        public String apply(String input) {
            return StringUtils.capitalize(input);
        }
    }

    private static class UncapitalizeFunction implements UnaryOperator<String> {
        @Override
        public String apply(String input) {
            return StringUtils.uncapitalize(input);
        }
    }

    private static class MarkdownToHtmlFunction implements UnaryOperator<String> {
        @Override
        public String apply(String input) {
            Parser parser = createMarkdownParser();
            TextContentRenderer markdown = createTextContentRenderer();
            String normalizedInput = markdown.render(parser.parse(input));
            Node document = parser.parse(normalizedInput);
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            return renderer.render(document).trim();
        }
    }

    private static class FileExistsFunction implements Function<Object, Boolean> {
        @Override
        public Boolean apply(Object input) {
            if (input instanceof Path) {
                return Files.exists((Path) input);
            } else if (input instanceof File) {
                return Files.exists(((File) input).toPath());
            } else if (input instanceof CharSequence) {
                return Files.exists(Paths.get(String.valueOf(input).trim()));
            }

            throw new IllegalStateException(RB.$("ERROR_invalid_file_input", input));
        }
    }

    private static class FileReadFunction implements Function<Object, String> {
        @Override
        public String apply(Object input) {
            try {
                if (input instanceof Path) {
                    return new String(Files.readAllBytes((Path) input), UTF_8);
                } else if (input instanceof File) {
                    return new String(Files.readAllBytes(((File) input).toPath()), UTF_8);
                } else if (input instanceof CharSequence) {
                    return new String(Files.readAllBytes(Paths.get(String.valueOf(input).trim())), UTF_8);
                }
            } catch (IOException e) {
                throw new IllegalStateException(RB.$("ERROR_unexpected_file_read", input), e);
            }

            throw new IllegalStateException(RB.$("ERROR_invalid_file_input", input));
        }
    }

    private static class FileSizeFunction implements ToLongFunction<Object> {
        @Override
        public long applyAsLong(Object input) {
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

    private static class DelegatingFunction implements UnaryOperator<String> {
        private final UnaryOperator<String> delegate;

        private DelegatingFunction(UnaryOperator<String> delegate) {
            this.delegate = delegate;
        }

        @Override
        public String apply(String input) {
            return delegate.apply(input);
        }
    }

    private static class RecursiveEvalFunction implements UnaryOperator<String> {
        private final TemplateContext context;

        public RecursiveEvalFunction(TemplateContext context) {
            this.context = context;
        }

        @Override
        public String apply(String input) {
            return MustacheUtils.applyTemplate(input, context);
        }
    }
}
