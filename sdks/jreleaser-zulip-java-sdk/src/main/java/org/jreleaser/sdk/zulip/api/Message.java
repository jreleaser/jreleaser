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
package org.jreleaser.sdk.zulip.api;

import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Message {
    private String type = "stream";
    private String to;
    private String subject;
    private String content;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Message[" +
            "type='" + type + '\'' +
            ", channel='" + to + '\'' +
            ", subject='" + subject + '\'' +
            ", content='" + content + '\'' +
            ']';
    }

    public static Message of(String to,
                             String subject,
                             String content) {
        Message o = new Message();
        o.to = requireNonBlank(to, "'to' must not be blank").trim();
        o.subject = requireNonBlank(subject, "'subject' must not be blank").trim();
        o.content = requireNonBlank(content, "'content' must not be blank").trim();
        return o;
    }
}
