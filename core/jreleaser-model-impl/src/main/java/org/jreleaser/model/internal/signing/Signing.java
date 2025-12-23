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
import static org.jreleaser.model.JReleaserOutput.nag;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class Signing extends AbstractActivatable<Signing> implements Domain {
    private static final long serialVersionUID = -2921377312639467517L;

    private final Pgp pgp = new Pgp();
    private final Cosign cosign = new Cosign();
    private final Minisign minisign = new Minisign();

    @JsonIgnore
    private final org.jreleaser.model.api.signing.Signing immutable = new org.jreleaser.model.api.signing.Signing() {
        private static final long serialVersionUID = 7518353475296750193L;

        @Override
        public boolean isArmored() {
            return Signing.this.isArmored();
        }

        @Override
        public boolean isVerify() {
            return pgp.isVerify();
        }

        @Override
        public String getPublicKey() {
            return pgp.getPublicKey();
        }

        @Override
        public String getSecretKey() {
            return pgp.getSecretKey();
        }

        @Override
        public String getPassphrase() {
            return pgp.getPassphrase();
        }

        @Override
        public org.jreleaser.model.Signing.Mode getMode() {
            return pgp.getMode();
        }

        @Override
        public boolean isArtifacts() {
            return pgp.isArtifacts();
        }

        @Override
        public boolean isFiles() {
            return pgp.isFiles();
        }

        @Override
        public boolean isChecksums() {
            return pgp.isChecksums();
        }

        @Override
        public boolean isCatalogs() {
            return pgp.isCatalogs();
        }

        @Override
        public Command getCommand() {
            return pgp.command.asImmutable();
        }

        @Override
        public Pgp getPgp() {
            return pgp.asImmutable();
        }

        @Override
        public Cosign getCosign() {
            return cosign.asImmutable();
        }

        @Override
        public Minisign getMinisign() {
            return minisign.asImmutable();
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
        setPgp(source.pgp);
        setCosign(source.cosign);
        setMinisign(source.minisign);
    }

    public boolean isArmored() {
        return pgp.isArmored();
    }

    @Deprecated
    public void setArmored(Boolean armored) {
        nag("signing.armored is deprecated since 1.22.0 and will be removed in 2.0.0. Use signing.pgp.armored instead");
        pgp.setArmored(armored);
    }

    public boolean isArmoredSet() {
        return pgp.isArmoredSet();
    }

    public boolean isVerify() {
        return pgp.isVerify();
    }

    @Deprecated
    public void setVerify(Boolean verify) {
        nag("signing.verify is deprecated since 1.22.0 and will be removed in 2.0.0. Use signing.pgp.verify instead");
        pgp.setVerify(verify);
    }

    public boolean isVerifySet() {
        return pgp.isVerifySet();
    }

    public String getPublicKey() {
        return pgp.getPublicKey();
    }

    @Deprecated
    public void setPublicKey(String publicKey) {
        nag("signing.publicKey is deprecated since 1.22.0 and will be removed in 2.0.0. Use signing.pgp.publicKey instead");
        pgp.setPublicKey(publicKey);
    }

    public String getSecretKey() {
        return pgp.getSecretKey();
    }

    @Deprecated
    public void setSecretKey(String secretKey) {
        nag("signing.secretKey is deprecated since 1.22.0 and will be removed in 2.0.0. Use signing.pgp.secretKey instead");
        pgp.setSecretKey(secretKey);
    }

    public String getPassphrase() {
        return pgp.getPassphrase();
    }

    @Deprecated
    public void setPassphrase(String passphrase) {
        nag("signing.passphrase is deprecated since 1.22.0 and will be removed in 2.0.0. Use signing.pgp.passphrase instead");
        pgp.setPassphrase(passphrase);
    }

    public org.jreleaser.model.Signing.Mode getMode() {
        return pgp.getMode();
    }

    @Deprecated
    public void setMode(org.jreleaser.model.Signing.Mode mode) {
        nag("signing.mode is deprecated since 1.22.0 and will be removed in 2.0.0. Use signing.pgp.mode instead");
        pgp.setMode(mode);
    }

    @Deprecated
    public void setMode(String str) {
        setMode(org.jreleaser.model.Signing.Mode.of(str));
    }

    public boolean isArtifactsSet() {
        return pgp.isArtifactsSet();
    }

    public boolean isArtifacts() {
        return pgp.isArtifacts();
    }

    @Deprecated
    public void setArtifacts(Boolean artifacts) {
        nag("signing.artifacts is deprecated since 1.22.0 and will be removed in 2.0.0. Use signing.pgp.artifacts instead");
        pgp.setArtifacts(artifacts);
    }

    public boolean isFiles() {
        return pgp.isFiles();
    }

    @Deprecated
    public void setFiles(Boolean files) {
        nag("signing.files is deprecated since 1.22.0 and will be removed in 2.0.0. Use signing.pgp.files instead");
        pgp.setFiles(files);
    }

    public boolean isFilesSet() {
        return pgp.isFilesSet();
    }

    public boolean isChecksumsSet() {
        return pgp.isChecksums();
    }

    public boolean isChecksums() {
        return pgp.isChecksums();
    }

    @Deprecated
    public void setChecksums(Boolean checksums) {
        nag("signing.checksums is deprecated since 1.22.0 and will be removed in 2.0.0. Use signing.pgp.checksums instead");
        pgp.setChecksums(checksums);
    }

    public boolean isCatalogsSet() {
        return pgp.isEnabled();
    }

    public boolean isCatalogs() {
        return pgp.isCatalogs();
    }

    @Deprecated
    public void setCatalogs(Boolean catalogs) {
        nag("signing.catalogs is deprecated since 1.22.0 and will be removed in 2.0.0. Use signing.pgp.catalogs instead");
        pgp.setCatalogs(catalogs);
    }

    public Command getCommand() {
        return pgp.getCommand();
    }

    @Deprecated
    public void setCommand(Command command) {
        nag("signing.command is deprecated since 1.22.0 and will be removed in 2.0.0. Use signing.pgp.command instead");
        pgp.setCommand(command);
    }

    public Pgp getPgp() {
        return pgp;
    }

    public void setPgp(Pgp pgp) {
        this.pgp.merge(pgp);
    }

    public Cosign getCosign() {
        return cosign;
    }

    public void setCosign(Cosign cosign) {
        this.cosign.merge(cosign);
    }

    public Minisign getMinisign() {
        return minisign;
    }

    public void setMinisign(Minisign minisign) {
        this.minisign.merge(minisign);
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        if (!full && !pgp.isEnabled()) return Collections.emptyMap();

        Map<String, Object> props = new LinkedHashMap<>();

        props.put("enabled", isEnabled());
        props.put("active", getActive());
        props.put("pgp", pgp.asMap(full));
        props.put("cosign", cosign.asMap(full));
        props.put("minisign", minisign.asMap(full));

        return props;
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

    private static abstract class AbstractSigningTool<S extends AbstractSigningTool<S>> extends AbstractActivatable<S> implements SigningTool {
        private static final long serialVersionUID = -1852513790818848365L;

        protected String publicKey;
        protected String secretKey;
        protected String passphrase;
        protected Boolean verify;
        protected Boolean artifacts;
        protected Boolean files;
        protected Boolean checksums;
        protected Boolean catalogs;

        @Override
        public void merge(S source) {
            super.merge(source);
            this.publicKey = merge(this.publicKey, source.publicKey);
            this.secretKey = merge(this.secretKey, source.secretKey);
            this.passphrase = merge(this.passphrase, source.passphrase);
            this.verify = merge(this.verify, source.verify);
            this.artifacts = merge(this.artifacts, source.artifacts);
            this.files = merge(this.files, source.files);
            this.checksums = merge(this.checksums, source.checksums);
            this.catalogs = merge(this.catalogs, source.catalogs);
        }

        @Override
        public String getPublicKey() {
            return publicKey;
        }

        @Override
        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        @Override
        public String getSecretKey() {
            return secretKey;
        }

        @Override
        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        @Override
        public String getPassphrase() {
            return passphrase;
        }

        @Override
        public void setPassphrase(String passphrase) {
            this.passphrase = passphrase;
        }

        @Override
        public boolean isVerify() {
            return null == verify || verify;
        }

        @Override
        public void setVerify(Boolean verify) {
            this.verify = verify;
        }

        public boolean isVerifySet() {
            return null != verify;
        }

        public boolean isArtifactsSet() {
            return null != artifacts;
        }

        @Override
        public boolean isArtifacts() {
            return null == artifacts || artifacts;
        }

        @Override
        public void setArtifacts(Boolean artifacts) {
            this.artifacts = artifacts;
        }

        @Override
        public boolean isFiles() {
            return null == files || files;
        }

        @Override
        public void setFiles(Boolean files) {
            this.files = files;
        }

        public boolean isFilesSet() {
            return null != files;
        }

        public boolean isChecksumsSet() {
            return null != checksums;
        }

        @Override
        public boolean isChecksums() {
            return null == checksums || checksums;
        }

        @Override
        public void setChecksums(Boolean checksums) {
            this.checksums = checksums;
        }

        public boolean isCatalogsSet() {
            return null != catalogs;
        }

        @Override
        public boolean isCatalogs() {
            return null == catalogs || catalogs;
        }

        @Override
        public void setCatalogs(Boolean catalogs) {
            this.catalogs = catalogs;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            if (!full && !isEnabled()) return Collections.emptyMap();

            Map<String, Object> props = new LinkedHashMap<>();

            props.put("enabled", isEnabled());
            props.put("active", getActive());
            props.put("verify", isVerify());
            props.put("artifacts", isArtifacts());
            props.put("files", isFiles());
            props.put("checksums", isChecksums());
            props.put("catalogs", isCatalogs());

            return props;
        }
    }

    public static class Pgp extends AbstractSigningTool<Pgp> implements Domain {
        private static final long serialVersionUID = -5762843351226833958L;

        private final Command command = new Command();

        private Boolean armored;
        private org.jreleaser.model.Signing.Mode mode;

        @JsonIgnore
        private final org.jreleaser.model.api.signing.Signing.Pgp immutable = new org.jreleaser.model.api.signing.Signing.Pgp() {
            private static final long serialVersionUID = -7221508841211382727L;

            @Override
            public boolean isArmored() {
                return Pgp.this.isArmored();
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
            public org.jreleaser.model.Signing.Mode getMode() {
                return mode;
            }

            @Override
            public org.jreleaser.model.api.signing.Signing.Command getCommand() {
                return command.asImmutable();
            }

            @Override
            public Active getActive() {
                return Pgp.this.getActive();
            }

            @Override
            public boolean isEnabled() {
                return Pgp.this.isEnabled();
            }

            @Override
            public String getPassphrase() {
                return passphrase;
            }

            @Override
            public boolean isVerify() {
                return Pgp.this.isVerify();
            }

            @Override
            public boolean isArtifacts() {
                return Pgp.this.isArtifacts();
            }

            @Override
            public boolean isFiles() {
                return Pgp.this.isFiles();
            }

            @Override
            public boolean isChecksums() {
                return Pgp.this.isChecksums();
            }

            @Override
            public boolean isCatalogs() {
                return Pgp.this.isCatalogs();
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return Pgp.this.asMap(full);
            }
        };

        public org.jreleaser.model.api.signing.Signing.Pgp asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Pgp source) {
            super.merge(source);
            this.armored = merge(this.armored, source.armored);
            this.mode = merge(this.mode, source.mode);
            setCommand(source.command);
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

        public org.jreleaser.model.Signing.Mode getMode() {
            return mode;
        }

        public void setMode(org.jreleaser.model.Signing.Mode mode) {
            this.mode = mode;
        }

        public void setMode(String str) {
            setMode(org.jreleaser.model.Signing.Mode.of(str));
        }

        public Command getCommand() {
            return command;
        }

        public void setCommand(Command command) {
            this.command.merge(command);
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            if (!full && !isEnabled()) return Collections.emptyMap();

            Map<String, Object> props = super.asMap(full);

            props.put("armored", isArmored());
            props.put("mode", mode);

            if (mode == org.jreleaser.model.Signing.Mode.COMMAND) {
                props.put("command", command.asMap(full));
            } else {
                props.put("publicKey", isNotBlank(publicKey) ? HIDE : UNSET);
                props.put("secretKey", isNotBlank(secretKey) ? HIDE : UNSET);
            }

            return props;
        }

        @Override
        public String getSignatureExtension() {
            return isArmored() ? ".asc" : ".sig";
        }
    }

    public static class Cosign extends AbstractSigningTool<Cosign> implements Domain {
        private static final long serialVersionUID = 3142049026548421252L;

        private String version;
        private String secretKeyFile;
        private String publicKeyFile;

        @JsonIgnore
        private final org.jreleaser.model.api.signing.Signing.Cosign immutable = new org.jreleaser.model.api.signing.Signing.Cosign() {
            private static final long serialVersionUID = 7075702207538130646L;

            @Override
            public String getVersion() {
                return version;
            }

            @Override
            public String getPrivateKeyFile() {
                return getSecretKeyFile();
            }

            @Override
            public String getSecretKeyFile() {
                return secretKeyFile;
            }

            @Override
            public String getPublicKeyFile() {
                return publicKeyFile;
            }

            @Override
            public String getPassphrase() {
                return passphrase;
            }

            @Override
            public boolean isVerify() {
                return Cosign.this.isVerify();
            }

            @Override
            public boolean isArtifacts() {
                return Cosign.this.isArtifacts();
            }

            @Override
            public boolean isFiles() {
                return Cosign.this.isFiles();
            }

            @Override
            public boolean isChecksums() {
                return Cosign.this.isChecksums();
            }

            @Override
            public boolean isCatalogs() {
                return Cosign.this.isCatalogs();
            }

            @Override
            public Active getActive() {
                return Cosign.this.getActive();
            }

            @Override
            public boolean isEnabled() {
                return Cosign.this.isEnabled();
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
            super.merge(source);
            this.version = merge(this.version, source.version);
            this.secretKeyFile = merge(this.secretKeyFile, source.secretKeyFile);
            this.publicKeyFile = merge(this.publicKeyFile, source.publicKeyFile);
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getSecretKeyFile() {
            return secretKeyFile;
        }

        public void setSecretKeyFile(String secretKeyFile) {
            this.secretKeyFile = secretKeyFile;
        }

        public String getPublicKeyFile() {
            return publicKeyFile;
        }

        public void setPublicKeyFile(String publicKeyFile) {
            this.publicKeyFile = publicKeyFile;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            if (!full && !isEnabled()) return Collections.emptyMap();

            Map<String, Object> props = super.asMap(full);

            props.put("version", version);
            props.put("secretKeyFile", null != secretKeyFile ? HIDE : UNSET);
            props.put("publicKeyFile", publicKeyFile);
            props.put("passphrase", isNotBlank(passphrase) ? HIDE : UNSET);

            return props;
        }

        public Path getResolvedSecretKeyFilePath(JReleaserContext context) {
            String secretKey = getSecretKeyFile();

            if (isNotBlank(secretKey)) {
                return context.getBasedir().resolve(secretKey);
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

        @Override
        public String getSignatureExtension() {
            return ".cosign";
        }
    }

    public static class Minisign extends AbstractSigningTool<Minisign> implements Domain {
        private static final long serialVersionUID = -6158083413967906887L;

        private String version;
        private String secretKeyFile;
        private String publicKeyFile;

        @JsonIgnore
        private final org.jreleaser.model.api.signing.Signing.Minisign immutable = new org.jreleaser.model.api.signing.Signing.Minisign() {
            private static final long serialVersionUID = 5488348796140511090L;

            @Override
            public String getVersion() {
                return version;
            }

            @Override
            public String getSecretKeyFile() {
                return secretKeyFile;
            }

            @Override
            public String getPublicKeyFile() {
                return publicKeyFile;
            }

            @Override
            public String getPassphrase() {
                return passphrase;
            }

            @Override
            public boolean isVerify() {
                return Minisign.this.isVerify();
            }

            @Override
            public boolean isArtifacts() {
                return Minisign.this.isArtifacts();
            }

            @Override
            public boolean isFiles() {
                return Minisign.this.isFiles();
            }

            @Override
            public boolean isChecksums() {
                return Minisign.this.isChecksums();
            }

            @Override
            public boolean isCatalogs() {
                return Minisign.this.isCatalogs();
            }

            @Override
            public Active getActive() {
                return Minisign.this.getActive();
            }

            @Override
            public boolean isEnabled() {
                return Minisign.this.isEnabled();
            }

            @Override
            public Map<String, Object> asMap(boolean full) {
                return Minisign.this.asMap(full);
            }
        };

        public org.jreleaser.model.api.signing.Signing.Minisign asImmutable() {
            return immutable;
        }

        @Override
        public void merge(Minisign source) {
            super.merge(source);
            this.version = merge(this.version, source.version);
            this.secretKeyFile = merge(this.secretKeyFile, source.secretKeyFile);
            this.publicKeyFile = merge(this.publicKeyFile, source.publicKeyFile);
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        @Deprecated
        public String getPrivate() {
            return getSecretKeyFile();
        }

        public void setPrivateKeyFile(String secretKeyFile) {
            nag("cosign.privateKeyFile is deprecated since 1.22.0 and will be removed in 2.0.0. Use cosign.secretKeyFile instead");
            setSecretKeyFile(secretKeyFile);
        }

        public String getSecretKeyFile() {
            return secretKeyFile;
        }

        public void setSecretKeyFile(String secretKeyFile) {
            this.secretKeyFile = secretKeyFile;
        }

        public String getPublicKeyFile() {
            return publicKeyFile;
        }

        public void setPublicKeyFile(String publicKeyFile) {
            this.publicKeyFile = publicKeyFile;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            if (!full && !isEnabled()) return Collections.emptyMap();

            Map<String, Object> props = super.asMap(full);

            props.put("version", version);
            props.put("secretKeyFile", null != secretKeyFile ? HIDE : UNSET);
            props.put("publicKeyFile", publicKeyFile);
            props.put("passphrase", isNotBlank(passphrase) ? HIDE : UNSET);

            return props;
        }

        public Path getResolvedSecretKeyFilePath(JReleaserContext context) {
            String secretKey = getSecretKeyFile();

            if (isNotBlank(secretKey)) {
                return context.getBasedir().resolve(secretKey);
            }

            return resolveMinisigHomeDir().resolve("minisign.key");
        }

        public Path getResolvedPublicKeyFilePath(JReleaserContext context) {
            String publicKey = getPublicKeyFile();

            if (isNotBlank(publicKey)) {
                return context.getBasedir().resolve(publicKey);
            }

            return context.getBasedir().resolve("minisign.pub");
        }

        private Path resolveMinisigHomeDir() {
            return Paths.get(System.getProperty("user.home") + File.separator + ".minisign");
        }

        @Override
        public String getSignatureExtension() {
            return ".minisig";
        }
    }
}
