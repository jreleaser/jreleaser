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
package org.jreleaser.sdk.slack.api;

import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Message {
    private String channel;
    private String text;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Message[" +
            "channel='" + channel + '\'' +
            ", content='" + text + '\'' +
            ']';
    }

    public static Message of(String text) {
        Message o = new Message();
        o.text = requireNonBlank(text, "'text' must not be blank").trim();
        return o;
    }

    public static Message of(String channel, String text) {
        Message o = new Message();
        o.channel = requireNonBlank(channel, "'channel' must not be blank").trim();
        o.text = requireNonBlank(text, "'text' must not be blank").trim();
        return o;
    }
}
