/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
package org.jreleaser.sdk.googlechat;

import static java.text.MessageFormat.format;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Anyul rivas
 * @since 0.1.5
 */
public class Message {
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return format("Message[, content=''{0}'']", text);
    }

    public static Message of(String text) {
        Message message = new Message();
        message.text = requireNonBlank(text, "'text' must not be blank").trim();
        return message;
    }
}
