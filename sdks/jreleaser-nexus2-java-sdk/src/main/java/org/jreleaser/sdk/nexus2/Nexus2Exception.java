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
package org.jreleaser.sdk.nexus2;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public class Nexus2Exception extends Exception {
    private static final long serialVersionUID = -5584176258036677240L;

    public Nexus2Exception(String message) {
        super(message);
    }

    public Nexus2Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public Nexus2Exception(Throwable cause) {
        super(cause);
    }
}
