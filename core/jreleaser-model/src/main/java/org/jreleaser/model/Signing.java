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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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
    public static final String KEY_SKIP_SIGNING = "skipSigning";
    public static final String GPG_PASSPHRASE = "GPG_PASSPHRASE";
    public static final String GPG_PUBLIC_KEY = "GPG_PUBLIC_KEY";
    public static final String GPG_SECRET_KEY = "GPG_SECRET_KEY";
    public static final String GPG_EXECUTABLE = "GPG_EXECUTABLE";
    public static final String GPG_KEYNAME = "GPG_KEYNAME";
    public static final String GPG_HOMEDIR = "GPG_HOMEDIR";
    public static final String GPG_PUBLIC_KEYRING = "GPG_PUBLIC_KEYRING";

    private final List<String> args = new ArrayList<>();

    private Active active;
    private boolean enabled;
    private Boolean armored;
    private String publicKey;
    private String secretKey;
    private String passphrase;
    private Mode mode;
    private Boolean artifacts;
    private Boolean files;
    private Boolean checksums;
    private String executable;
    private String keyName;
    private String homeDir;
    private String publicKeyring;
    private Boolean defaultKeyring;

    void setAll(Signing signing) {
        this.active = signing.active;
        this.enabled = signing.enabled;
        this.armored = signing.armored;
        this.publicKey = signing.publicKey;
        this.secretKey = signing.secretKey;
        this.passphrase = signing.passphrase;
        this.mode = signing.mode;
        this.artifacts = signing.artifacts;
        this.files = signing.files;
        this.checksums = signing.checksums;
        this.executable = signing.executable;
        this.keyName = signing.keyName;
        this.homeDir = signing.homeDir;
        this.publicKeyring = signing.publicKeyring;
        this.defaultKeyring = signing.defaultKeyring;
        setArgs(signing.args);
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

    public Mode resolveMode() {
        if (null == mode) {
            mode = Mode.MEMORY;
        }
        return mode;
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

    public boolean isArtifactsSet() {
        return artifacts != null;
    }

    public Boolean isArtifacts() {
        return artifacts == null || artifacts;
    }

    public void setArtifacts(Boolean artifacts) {
        this.artifacts = artifacts;
    }

    public Boolean isFiles() {
        return files == null || files;
    }

    public boolean isFilesSet() {
        return files != null;
    }

    public void setFiles(Boolean files) {
        this.files = files;
    }

    public boolean isChecksumsSet() {
        return checksums != null;
    }

    public Boolean isChecksums() {
        return checksums == null || checksums;
    }

    public void setChecksums(Boolean checksums) {
        this.checksums = checksums;
    }

    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getHomeDir() {
        return homeDir;
    }

    public void setHomeDir(String homeDir) {
        this.homeDir = homeDir;
    }

    public String getPublicKeyring() {
        return publicKeyring;
    }

    public void setPublicKeyring(String publicKeyring) {
        this.publicKeyring = publicKeyring;
    }

    public boolean isDefaultKeyringSet() {
        return defaultKeyring != null;
    }

    public Boolean isDefaultKeyring() {
        return defaultKeyring == null || defaultKeyring;
    }

    public void setDefaultKeyring(Boolean defaultKeyring) {
        this.defaultKeyring = defaultKeyring;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args.clear();
        this.args.addAll(args);
    }

    public void addArgs(List<String> args) {
        this.args.addAll(args);
    }

    public void addArg(String arg) {
        if (isNotBlank(arg)) {
            this.args.add(arg.trim());
        }
    }

    public void removeArg(String arg) {
        if (isNotBlank(arg)) {
            this.args.remove(arg.trim());
        }
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("active", active);
        props.put("armored", isArmored());
        props.put("mode", mode);
        props.put("artifacts", isArtifacts());
        props.put("files", isFiles());
        props.put("checksums", isChecksums());
        props.put("passphrase", isNotBlank(passphrase) ? HIDE : UNSET);
        if (mode != Mode.COMMAND) {
            props.put("publicKey", isNotBlank(publicKey) ? HIDE : UNSET);
            props.put("secretKey", isNotBlank(secretKey) ? HIDE : UNSET);
        } else {
            props.put("executable", executable);
            props.put("keyName", keyName);
            props.put("homeDir", homeDir);
            props.put("publicKeyring", publicKeyring);
            props.put("defaultKeyring", isDefaultKeyring());
            props.put("args", args);
        }

        return props;
    }

    public enum Mode {
        MEMORY,
        FILE,
        COMMAND;

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
