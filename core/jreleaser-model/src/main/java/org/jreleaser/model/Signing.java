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
public class Signing implements Domain {
    private Boolean enabled;
    private boolean enabledSet;
    private Boolean armored;
    private boolean armoredSet;
    private String keyRingFile;
    private String passphrase;

    void setAll(Signing signing) {
        this.enabled = signing.enabled;
        this.enabledSet = signing.enabledSet;
        this.armored = signing.armored;
        this.armoredSet = signing.armoredSet;
        this.keyRingFile = signing.keyRingFile;
        this.passphrase = signing.passphrase;
    }

    public Boolean isEnabled() {
        return enabled != null && enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabledSet = true;
        this.enabled = enabled;
    }

    public boolean isEnabledSet() {
        return enabledSet;
    }

    public String getResolvedPassphrase() {
        if (isNotBlank(passphrase)) {
            return passphrase;
        }
        return System.getenv("GPG_PASSPHRASE");
    }

    public Boolean isArmored() {
        return armored == null || armored;
    }

    public void setArmored(Boolean armored) {
        this.armoredSet = true;
        this.armored = armored;
    }

    public boolean isArmoredSet() {
        return armoredSet;
    }

    public String getKeyRingFile() {
        return keyRingFile;
    }

    public void setKeyRingFile(String keyRingFile) {
        this.keyRingFile = keyRingFile;
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
        map.put("keyRingFile", keyRingFile);
        map.put("passphrase", isNotBlank(passphrase) ? "************" : "**unset**");

        return map;
    }
}
