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
package org.jreleaser.maven.plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Signing implements Activatable {
    private final List<String> args = new ArrayList<>();

    private Active active;
    private boolean armored;
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
        this.armored = signing.armored;
        this.publicKey = signing.publicKey;
        this.secretKey = signing.secretKey;
        this.passphrase = signing.passphrase;
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
    public Active getActive() {
        return active;
    }

    @Override
    public void setActive(Active active) {
        this.active = active;
    }

    @Override
    public String resolveActive() {
        return active != null ? active.name() : null;
    }

    public boolean isArmored() {
        return armored;
    }

    public void setArmored(boolean armored) {
        this.armored = armored;
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

    public String resolveMode() {
        return mode != null ? mode.name() : null;
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

    public enum Mode {
        MEMORY,
        FILE,
        COMMAND
    }
}
