/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.MustacheFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class MustacheUtils {
    private MustacheUtils() {
        //noop
    }

    private static Map<String, String> envVars() {
        Map<String, String> vars = new LinkedHashMap<>();
        System.getenv().forEach((k, v) -> {
            if (!k.startsWith("JRELEASER_")) {
                vars.put("Env." + k, v);
            }
        });
        return vars;
    }

    public static String applyTemplate(Reader reader, Map<String, Object> context, String templateName) {
        StringWriter input = new StringWriter();
        MustacheFactory mf = new MyMustacheFactory();
        Mustache mustache = mf.compile(reader, templateName);
        context.putAll(envVars());
        mustache.execute(input, context);
        input.flush();
        return input.toString();
    }

    public static String applyTemplate(Reader reader, Map<String, Object> context) {
        return applyTemplate(reader, context, UUID.randomUUID().toString()).trim();
    }

    public static String applyTemplate(String template, Map<String, Object> context, String templateName) {
        return applyTemplate(new StringReader(template), context, templateName);
    }

    public static String applyTemplate(String template, Map<String, Object> context) {
        return applyTemplate(new StringReader(template), context, UUID.randomUUID().toString()).trim();
    }

    public static void applyTemplates(Map<String, Object> props, Map<String, Object> templates) {
        for (Map.Entry<String, Object> e : templates.entrySet()) {
            String value = String.valueOf(e.getValue());
            if (value.contains("{{") && value.contains("}}")) {
                props.put(e.getKey(), applyTemplate(value, props));
            } else {
                props.put(e.getKey(), value);
            }
        }
    }

    public static String passThrough(String str) {
        return isNotBlank(str) ? "!!" + str + "!!" : str;
    }

    private static class MyMustacheFactory extends DefaultMustacheFactory {
        @Override
        public void encode(String value, Writer writer) {
            if (value.startsWith("!!") && value.endsWith("!!")) {
                try {
                    writer.write(value.substring(2, value.length() - 2));
                } catch (IOException e) {
                    throw new MustacheException("Failed to write value: " + value, e);
                }
            } else {
                super.encode(value, writer);
            }
        }
    }
}
