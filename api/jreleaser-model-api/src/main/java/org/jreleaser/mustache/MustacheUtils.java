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
package org.jreleaser.mustache;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.TemplateFunction;
import org.jreleaser.bundle.RB;
import org.jreleaser.extensions.api.ExtensionManagerHolder;
import org.jreleaser.extensions.api.mustache.MustacheExtensionPoint;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
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

    public static String applyTemplate(Reader reader, TemplateContext context, String templateName) {
        StringWriter input = new StringWriter();
        MustacheFactory mf = new MyMustacheFactory();
        Mustache mustache = mf.compile(reader, templateName);
        context.setAll(envVars());
        applyFunctions(context);
        mustache.execute(input, decorate(context.asMap()));
        input.flush();
        return input.toString();
    }

    private static Map<String, Object> decorate(Map<String, Object> context) {
        for (Map.Entry<String, Object> e : new LinkedHashSet<>(context.entrySet())) {
            Object value = e.getValue();

            if (value instanceof CharSequence) {
                String val = String.valueOf(value);
                if (val.contains("{{")) {
                    context.put(e.getKey(), (TemplateFunction) s -> val);
                }
            }
        }
        return context;
    }

    public static String applyTemplate(Reader reader, TemplateContext context) {
        return applyTemplate(reader, context, UUID.randomUUID().toString()).trim();
    }

    public static String applyTemplate(String template, TemplateContext context, String templateName) {
        return applyTemplate(new StringReader(template), context, templateName);
    }

    public static String applyTemplate(String template, TemplateContext context) {
        return applyTemplate(new StringReader(template), context, UUID.randomUUID().toString()).trim();
    }

    public static void applyTemplates(Map<String, Object> props, TemplateContext templates) {
        applyTemplates(new TemplateContext(props), templates);
    }

    public static void applyTemplates(TemplateContext props, Map<String, Object> templates) {
        for (Map.Entry<String, Object> e : new LinkedHashSet<>(templates.entrySet())) {
            Object value = e.getValue();

            if (value instanceof CharSequence) {
                String val = String.valueOf(value);
                if (val.contains("{{") && val.contains("}}")) {
                    value = applyTemplate(val, props);
                }
            }

            props.set(e.getKey(), value);
        }
    }

    public static void applyTemplates(TemplateContext props, TemplateContext templates) {
        for (Map.Entry<String, Object> e : new LinkedHashSet<>(templates.entries())) {
            Object value = e.getValue();

            if (value instanceof CharSequence) {
                String val = String.valueOf(value);
                if (val.contains("{{") && val.contains("}}")) {
                    value = applyTemplate(val, props);
                }
            }

            props.set(e.getKey(), value);
        }
    }

    public static String passThrough(String str) {
        return isNotBlank(str) ? "!!" + str + "!!" : str;
    }

    private static void applyFunctions(TemplateContext props) {
        ExtensionManagerHolder.get().findExtensionPoints(MustacheExtensionPoint.class)
            .forEach(ep -> ep.apply(props));
    }

    private static class MyMustacheFactory extends DefaultMustacheFactory {
        @Override
        public void encode(String value, Writer writer) {
            if (value.startsWith("!!") && value.endsWith("!!")) {
                try {
                    writer.write(value.substring(2, value.length() - 2));
                } catch (IOException e) {
                    throw new MustacheException(RB.$("ERROR_mustache_write_value", value), e);
                }
            } else {
                super.encode(value, writer);
            }
        }
    }
}
