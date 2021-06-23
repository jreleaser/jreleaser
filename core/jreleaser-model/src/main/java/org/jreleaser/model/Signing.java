/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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

import org.jreleaser.util.Env;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.Constants.HIDE;
import static org.jreleaser.util.Constants.UNSET;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Signing implements Domain, Activatable {
    public static final String GPG_PASSPHRASE = "GPG_PASSPHRASE";
    public static final String GPG_PUBLIC_KEY = "GPG_PUBLIC_KEY";
    public static final String GPG_SECRET_KEY = "GPG_SECRET_KEY";

    private Active active;
    private boolean enabled;
    private Boolean armored;
    private String publicKey;
    private String secretKey;
    private String passphrase;
    private Mode mode;

    void setAll(Signing signing) {
        this.active = signing.active;
        this.enabled = signing.enabled;
        this.armored = signing.armored;
        this.publicKey = signing.publicKey;
        this.secretKey = signing.secretKey;
        this.passphrase = signing.passphrase;
        this.mode = signing.mode;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void disable() {
        active = Active.NEVER;
        enabled = false;
    }

    public boolean resolveEnabled(Project project) {
        if (null == active) {
            active = Active.NEVER;
        }
        enabled = active.check(project);
        return enabled;
    }

    @Override
    public Active getActive() {
        return active;
    }

    @Override
    public void setActive(Active active) {
        this.active = active;
    }

    @Override
    public void setActive(String str) {
        this.active = Active.of(str);
    }

    @Override
    public boolean isActiveSet() {
        return active != null;
    }

    public String getResolvedPublicKey() {
        return Env.resolve(GPG_PUBLIC_KEY, publicKey);
    }

    public String getResolvedSecretKey() {
        return Env.resolve(GPG_SECRET_KEY, secretKey);
    }

    public String getResolvedPassphrase() {
        return Env.resolve(GPG_PASSPHRASE, passphrase);
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

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void setMode(String str) {
        this.mode = Mode.of(str);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("active", active);
        map.put("armored", isArmored());
        map.put("mode", mode);
        map.put("publicKey", isNotBlank(publicKey) ? HIDE : UNSET);
        map.put("secretKey", isNotBlank(secretKey) ? HIDE : UNSET);
        map.put("passphrase", isNotBlank(passphrase) ? HIDE : UNSET);

        return map;
    }

    public enum Mode {
        MEMORY,
        FILE;

        @Override
        public String toString() {
            return name().toLowerCase();
        }

        public static Mode of(String str) {
            if (isBlank(str)) return null;
            return Mode.valueOf(str.toUpperCase().trim());
        }
    }
}
