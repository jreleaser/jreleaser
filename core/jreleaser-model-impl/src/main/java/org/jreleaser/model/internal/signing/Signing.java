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
package org.jreleaser.model.internal.signing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.Active;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.AbstractActivatable;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Domain;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;
import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class Signing extends AbstractActivatable<Signing> implements Domain {
    private static final long serialVersionUID = -7440879442726925285L;

    private final Command command = new Command();
    private final Cosign cosign = new Cosign();

    private Boolean armored;
    private Boolean verify;
    private String publicKey;
    private String secretKey;
    private String passphrase;
    private org.jreleaser.model.Signing.Mode mode;
    private Boolean artifacts;
    private Boolean files;
    private Boolean checksums;
    private Boolean catalogs;

    @JsonIgnore
    private final org.jreleaser.model.api.signing.Signing immutable = new org.jreleaser.model.api.signing.Signing() {
        private static final long serialVersionUID = -3565614952776622685L;

        @Override
        public boolean isArmored() {
            return Signing.this.isArmored();
        }

        @Override
        public boolean isVerify() {
            return Signing.this.isVerify();
        }

        @Override
        public String getPublicKey() {
            return publicKey;
        }

        @Override
        public String getSecretKey() {
            return secretKey;
        }

        @Override
        public String getPassphrase() {
            return passphrase;
        }

        @Override
        public org.jreleaser.model.Signing.Mode getMode() {
            return mode;
        }

        @Override
        public boolean isArtifacts() {
            return Signing.this.isArtifacts();
        }

        @Override
        public boolean isFiles() {
            return Signing.this.isFiles();
        }

        @Override
        public boolean isChecksums() {
            return Signing.this.isChecksums();
        }

        @Override
        public boolean isCatalogs() {
            return Signing.this.isCatalogs();
        }

        @Override
        public Command getCommand() {
            return command.asImmutable();
        }

        @Override
        public Cosign getCosign() {
            return cosign.asImmutable();
        }

        @Override
        public Active getActive() {
            return Signing.this.getActive();
        }

        @Override
        public boolean isEnabled() {
            return Signing.this.isEnabled();
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return Signing.this.asMap(full);
        }
    };

    public org.jreleaser.model.api.signing.Signing asImmutable() {
        return immutable;
    }

    @Override
    public void merge(Signing source) {
        super.merge(source);
        this.armored = merge(this.armored, source.armored);
        this.verify = merge(this.verify, source.verify);
        this.publicKey = merge(this.publicKey, source.publicKey);
        this.secretKey = merge(this.secretKey, source.secretKey);
        this.passphrase = merge(this.passphrase, source.passphrase);
        this.mode = merge(this.mode, source.mode);
        this.artifacts = merge(this.artifacts, source.artifacts);
        this.files = merge(this.files, source.files);
        this.checksums = merge(this.checksums, source.checksums);
        this.catalogs = merge(this.catalogs, source.catalogs);
        setCommand(source.command);
        setCosign(source.cosign);
    }

    public org.jreleaser.model.Signing.Mode resolveMode() {
        if (null == mode) {
            mode = org.jreleaser.model.Signing.Mode.MEMORY;
        }
        return mode;
    }

    public boolean isArmored() {
        return null != armored && armored;
    }

    public void setArmored(Boolean armored) {
        this.armored = armored;
    }

    public boolean isArmoredSet() {
        return null != armored;
    }

    public boolean isVerify() {
        return null == verify || verify;
    }

    public void setVerify(Boolean verify) {
        this.verify = verify;
    }

    public boolean isVerifySet() {
        return null != verify;
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

    public org.jreleaser.model.Signing.Mode getMode() {
        return mode;
    }

    public void setMode(org.jreleaser.model.Signing.Mode mode) {
        this.mode = mode;
    }

    public void setMode(String str) {
        setMode(org.jreleaser.model.Signing.Mode.of(str));
    }

    public boolean isArtifactsSet() {
        return null != artifacts;
    }

    public boolean isArtifacts() {
        return null == artifacts || artifacts;
    }

    public void setArtifacts(Boolean artifacts) {
        this.artifacts = artifacts;
    }

    public boolean isFiles() {
        return null == files || files;
    }

    public void setFiles(Boolean files) {
        this.files = files;
    }

    public boolean isFilesSet() {
        return null != files;
    }

    public boolean isChecksumsSet() {
        return null != checksums;
    }

    public boolean isChecksums() {
        return null == checksums || checksums;
    }

    public void setChecksums(Boolean checksums) {
        this.checksums = checksums;
    }

    public boolean isCatalogsSet() {
        return null != catalogs;
    }

    public boolean isCatalogs() {
        return null == catalogs || catalogs;
    }

    public void setCatalogs(Boolean catalogs) {
        this.catalogs = catalogs;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command.merge(command);
    }

    public Cosign getCosign() {
        return cosign;
    }

    public void setCosign(Cosign cosign) {
        this.cosign.merge(cosign);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("enabled", isEnabled());
        props.put("active", getActive());
        props.put("armored", isArmored());
        props.put("verify", isVerify());
        props.put("mode", mode);
        props.put("artifacts", isArtifacts());
        props.put("files", isFiles());
        props.put("checksums", isChecksums());
        props.put("catalogs", isCatalogs());
        props.put("passphrase", isNotBlank(passphrase) ? HIDE : UNSET);

        if (mode == org.jreleaser.model.Signing.Mode.COMMAND) {
            props.put("command", command.asMap(full));
        } else if (mode == org.jreleaser.model.Signing.Mode.COSIGN) {
            props.put("cosign", cosign.asMap(full));
        } else {
            props.put("publicKey", isNotBlank(publicKey) ? HIDE : UNSET);
            props.put("secretKey", isNotBlank(secretKey) ? HIDE : UNSET);
        }

        return props;
    }

    public String getSignatureExtension() {
        String extension = ".sig";
        if (mode != org.jreleaser.model.Signing.Mode.COSIGN) {
            extension = isArmored() ? ".asc" : ".sig";
        }

        return extension;
    }

    public static class Command extends AbstractModelObject<Command> implements Domain {
        private static final long serialVersionUID = -6208172775388448492L;

        private final List<String> args = new ArrayList<>();

        private String executable;
        private String keyName;
        private String homeDir;
        private String publicKeyring;
        private Boolean defaultKeyring;

        @JsonIgnore
        private final org.jreleaser.model.api.signing.Signing.Command immutable = new org.jreleaser.model.api.signing.Signing.Command() {
            private static final long serialVersionUID = -8636071040086599491L;

            @Override
            public String getExecutable() {
                return executable;
            }

            @Override
            public String getKeyName() {
                return keyName;
            }

            @Override
            public String getHomeDir() {
                return homeDir;
            }

            @Override
            public String getPublicKeyring() {
                return publicKeyring;
            }

            @Override
            public boolean isDefaultKeyring() {
                return Command.this.isDefaultKeyring();
            }

            @Override
            public List<String> getArgs() {
                return unmodifiableList(args);
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return Command.this.asMap(full);
            }
        };

        public org.jreleaser.model.api.signing.Signing.Command asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Command source) {
            this.executable = merge(this.executable, source.executable);
            this.keyName = merge(this.keyName, source.keyName);
            this.homeDir = merge(this.homeDir, source.homeDir);
            this.publicKeyring = merge(this.publicKeyring, source.publicKeyring);
            this.defaultKeyring = merge(this.defaultKeyring, source.defaultKeyring);
            setArgs(merge(this.args, source.args));
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
            return null != defaultKeyring;
        }

        public boolean isDefaultKeyring() {
            return null == defaultKeyring || defaultKeyring;
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

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> props = new LinkedHashMap<>();

            props.put("executable", executable);
            props.put("keyName", keyName);
            props.put("homeDir", homeDir);
            props.put("publicKeyring", publicKeyring);
            props.put("defaultKeyring", isDefaultKeyring());
            props.put("args", args);

            return props;
        }
    }

    public static class Cosign extends AbstractModelObject<Cosign> implements Domain {
        private static final long serialVersionUID = 5608123183696686008L;

        private String version;
        private String privateKeyFile;
        private String publicKeyFile;

        @JsonIgnore
        private final org.jreleaser.model.api.signing.Signing.Cosign immutable = new org.jreleaser.model.api.signing.Signing.Cosign() {
            private static final long serialVersionUID = 3675807300391748445L;

            @Override
            public String getVersion() {
                return version;
            }

            @Override
            public String getPrivateKeyFile() {
                return privateKeyFile;
            }

            @Override
            public String getPublicKeyFile() {
                return publicKeyFile;
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return Cosign.this.asMap(full);
            }
        };

        public org.jreleaser.model.api.signing.Signing.Cosign asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Cosign source) {
            this.version = merge(this.version, source.version);
            this.privateKeyFile = merge(this.privateKeyFile, source.privateKeyFile);
            this.publicKeyFile = merge(this.publicKeyFile, source.publicKeyFile);
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getPrivateKeyFile() {
            return privateKeyFile;
        }

        public void setPrivateKeyFile(String privateKeyFile) {
            this.privateKeyFile = privateKeyFile;
        }

        public String getPublicKeyFile() {
            return publicKeyFile;
        }

        public void setPublicKeyFile(String publicKeyFile) {
            this.publicKeyFile = publicKeyFile;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            Map<String, Object> props = new LinkedHashMap<>();

            props.put("version", version);
            props.put("privateKeyFile", null != privateKeyFile ? HIDE : UNSET);
            props.put("publicKeyFile", publicKeyFile);

            return props;
        }

        public Path getResolvedPrivateKeyFilePath(JReleaserContext context) {
            String privateKey = getPrivateKeyFile();

            if (isNotBlank(privateKey)) {
                return context.getBasedir().resolve(privateKey);
            }

            return resolveJReleaserHomeDir().resolve("cosign.key");
        }

        public Path getResolvedPublicKeyFilePath(JReleaserContext context) {
            String publicKey = getPublicKeyFile();

            if (isNotBlank(publicKey)) {
                return context.getBasedir().resolve(publicKey);
            }

            return resolveJReleaserHomeDir().resolve("cosign.pub");
        }

        private Path resolveJReleaserHomeDir() {
            String home = System.getenv("JRELEASER_USER_HOME");
            if (isBlank(home)) {
                home = System.getProperty("user.home") + File.separator + ".jreleaser";
            }

            return Paths.get(home);
        }
    }
}
