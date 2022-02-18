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
package org.jreleaser.model;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public class HttpDownloader extends AbstractDownloader {
    public static final String TYPE = "http";

    private final Map<String, String> headers = new LinkedHashMap<>();
    private String input;
    private String output;

    public HttpDownloader() {
        super(TYPE);
    }

    void setAll(HttpDownloader http) {
        super.setAll(http);
        this.input = http.input;
        this.output = http.output;
        setHeaders(http.headers);
    }

    public String getResolvedInput(JReleaserContext context) {
        Map<String, Object> p = context.getModel().props();
        p.putAll(getResolvedExtraProperties());
        return resolveTemplate(input, p);
    }

    public String getResolvedOutput(JReleaserContext context) {
        if (isBlank(output)) return output;
        Map<String, Object> p = context.getModel().props();
        p.putAll(getResolvedExtraProperties());
        return resolveTemplate(output, p);
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    public void addHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    @Override
    protected void asMap(Map<String, Object> props, boolean full) {
        props.put("input", input);
        props.put("output", output);
        props.put("headers", headers);
    }
}
