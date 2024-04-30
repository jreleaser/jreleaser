/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2024 The JReleaser authors.
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
package org.jreleaser.sdk.bluesky.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Tom Cools
 * @since 1.12.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Index {
    private int byteStart;
    private int byteEnd;

    public int getByteStart() {
        return byteStart;
    }

    public void setByteStart(int byteStart) {
        this.byteStart = byteStart;
    }

    public int getByteEnd() {
        return byteEnd;
    }

    public void setByteEnd(int byteEnd) {
        this.byteEnd = byteEnd;
    }
}
