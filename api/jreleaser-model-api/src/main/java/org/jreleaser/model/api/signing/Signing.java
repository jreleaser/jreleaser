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
package org.jreleaser.model.api.signing;

import org.jreleaser.model.api.common.Activatable;
import org.jreleaser.model.api.common.Domain;

import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface Signing extends Domain, Activatable {
    String KEY_SKIP_SIGNING = "skipSigning";
    String COSIGN_PASSWORD = "COSIGN_PASSWORD";
    String COSIGN_PRIVATE_KEY = "COSIGN_PRIVATE_KEY";
    String COSIGN_SECRET_KEY = "COSIGN_SECRET_KEY";
    String COSIGN_PUBLIC_KEY = "COSIGN_PUBLIC_KEY";
    String MINISIGN_PASSWORD = "MINISIGN_PASSWORD";
    String MINISIGN_SECRET_KEY = "MINISIGN_SECRET_KEY";
    String MINISIGN_PUBLIC_KEY = "MINISIGN_PUBLIC_KEY";
    String GPG_PASSPHRASE = "GPG_PASSPHRASE";
    String GPG_PUBLIC_KEY = "GPG_PUBLIC_KEY";
    String GPG_SECRET_KEY = "GPG_SECRET_KEY";
    String GPG_EXECUTABLE = "GPG_EXECUTABLE";
    String GPG_KEYNAME = "GPG_KEYNAME";
    String GPG_HOMEDIR = "GPG_HOMEDIR";
    String GPG_PUBLIC_KEYRING = "GPG_PUBLIC_KEYRING";

    @Deprecated
    boolean isArmored();

    @Deprecated
    boolean isVerify();

    @Deprecated
    String getPublicKey();

    @Deprecated
    String getSecretKey();

    @Deprecated
    String getPassphrase();

    @Deprecated
    org.jreleaser.model.Signing.Mode getMode();

    @Deprecated
    boolean isArtifacts();

    @Deprecated
    boolean isFiles();

    @Deprecated
    boolean isChecksums();

    @Deprecated
    boolean isCatalogs();

    @Deprecated
    Command getCommand();

    Pgp getPgp();

    Cosign getCosign();

    Minisign getMinisign();

    interface SigningTool extends Domain, Activatable {
        boolean isVerify();

        boolean isArtifacts();

        boolean isFiles();

        boolean isChecksums();

        boolean isCatalogs();
    }

    interface Pgp extends SigningTool {
        boolean isArmored();

        String getPublicKey();

        String getSecretKey();

        String getPassphrase();

        org.jreleaser.model.Signing.Mode getMode();

        Command getCommand();
    }

    interface Command extends Domain {
        String getExecutable();

        String getKeyName();

        String getHomeDir();

        String getPublicKeyring();

        boolean isDefaultKeyring();

        List<String> getArgs();
    }

    interface Cosign extends SigningTool {
        String getVersion();

        String getPassphrase();

        @Deprecated
        String getPrivateKeyFile();

        String getSecretKeyFile();

        String getPublicKeyFile();
    }

    interface Minisign extends SigningTool {
        String getVersion();

        String getPassphrase();

        String getSecretKeyFile();

        String getPublicKeyFile();
    }
}
