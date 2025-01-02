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
    String COSIGN_PUBLIC_KEY = "COSIGN_PUBLIC_KEY";
    String GPG_PASSPHRASE = "GPG_PASSPHRASE";
    String GPG_PUBLIC_KEY = "GPG_PUBLIC_KEY";
    String GPG_SECRET_KEY = "GPG_SECRET_KEY";
    String GPG_EXECUTABLE = "GPG_EXECUTABLE";
    String GPG_KEYNAME = "GPG_KEYNAME";
    String GPG_HOMEDIR = "GPG_HOMEDIR";
    String GPG_PUBLIC_KEYRING = "GPG_PUBLIC_KEYRING";

    boolean isArmored();

    boolean isVerify();

    String getPublicKey();

    String getSecretKey();

    String getPassphrase();

    org.jreleaser.model.Signing.Mode getMode();

    boolean isArtifacts();

    boolean isFiles();

    boolean isChecksums();

    boolean isCatalogs();

    Command getCommand();

    Cosign getCosign();

    interface Command extends Domain {
        String getExecutable();

        String getKeyName();

        String getHomeDir();

        String getPublicKeyring();

        boolean isDefaultKeyring();

        List<String> getArgs();
    }

    interface Cosign extends Domain {
        String getVersion();

        String getPrivateKeyFile();

        String getPublicKeyFile();
    }
}
