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
package org.jreleaser.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Signing implements Domain, EnabledProvider {
    public static final String GPG_PASSPHRASE = "GPG_PASSPHRASE";
    public static final String GPG_PUBLIC_KEY = "GPG_PUBLIC_KEY";
    public static final String GPG_SECRET_KEY = "GPG_SECRET_KEY";

    private Boolean enabled;
    private Boolean armored;
    private String publicKey;
    private String secretKey;
    private String passphrase;

    void setAll(Signing signing) {
        this.enabled = signing.enabled;
        this.armored = signing.armored;
        this.publicKey = signing.publicKey;
        this.secretKey = signing.secretKey;
        this.passphrase = signing.passphrase;
    }

    @Override
    public Boolean isEnabled() {
        return enabled != null && enabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabledSet() {
        return enabled != null;
    }

    public String getResolvedPublicKey() {
        if (isNotBlank(publicKey)) {
            return publicKey;
        }
        return System.getenv(GPG_PUBLIC_KEY);
    }

    public String getResolvedSecretKey() {
        if (isNotBlank(secretKey)) {
            return secretKey;
        }
        return System.getenv(GPG_SECRET_KEY);
    }

    public String getResolvedPassphrase() {
        if (isNotBlank(passphrase)) {
            return passphrase;
        }
        return System.getenv(GPG_PASSPHRASE);
    }

    public Boolean isArmored() {
        return armored != null && armored;
    }

    public void setArmored(Boolean armored) {
        this.armored = armored;
    }

    public boolean isArmoredSet() {
        return armored != null;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    @Override
    public final Map<String, Object> asMap() {
        if (!isEnabled()) return Collections.emptyMap();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("armored", isArmored());
        map.put("publicKey", isNotBlank(publicKey) ? "************" : "**unset**");
        map.put("secretKey", isNotBlank(secretKey) ? "************" : "**unset**");
        map.put("passphrase", isNotBlank(passphrase) ? "************" : "**unset**");

        return map;
    }
}
