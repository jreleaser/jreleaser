/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.sdk.zernio.api;

import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 1.24.0
 */
public class Platform {
    private String platform;
    private String accountId;

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public String toString() {
        return "Platform[" +
            "platform='" + platform + '\'' +
            ", accountId='" + accountId + '\'' +
            ']';
    }

    public static Platform of(String platform,
                              String accountId) {
        Platform o = new Platform();
        o.platform = requireNonBlank(platform, "'platform' must not be blank").trim();
        o.accountId = requireNonBlank(accountId, "'accountId' must not be blank").trim();
        return o;
    }
}
