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
package org.jreleaser.sdk.discord.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class DiscordAPIException extends RuntimeException {
    private final int status;
    private final String reason;
    private final Map<String, Collection<String>> headers;

    public DiscordAPIException(int status, String reason) {
        this(status, reason, Collections.emptyMap());
    }

    public DiscordAPIException(int status, String reason, Map<String, Collection<String>> headers) {
        this.status = status;
        this.reason = reason;
        this.headers = headers;
    }

    public int getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public Map<String, Collection<String>> getHeaders() {
        return headers;
    }

    public boolean isNotFound() {
        return 404 == status;
    }

    public boolean isForbidden() {
        return 403 == status;
    }
}
