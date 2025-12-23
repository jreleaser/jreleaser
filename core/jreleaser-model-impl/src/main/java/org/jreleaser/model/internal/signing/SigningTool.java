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

import org.jreleaser.model.internal.common.Activatable;
import org.jreleaser.model.internal.common.Domain;

import java.io.Serializable;

/**
 * @author Andres Almiray
 * @since 1.22.0
 */
public interface SigningTool extends Activatable, Serializable, Domain {
    String getPublicKey();

    void setPublicKey(String publicKey);

    String getSecretKey();

    void setSecretKey(String secretKey);

    String getPassphrase();

    void setPassphrase(String passphrase);

    boolean isVerify();

    void setVerify(Boolean verify);

    boolean isArtifacts();

    void setArtifacts(Boolean artifacts);

    boolean isFiles();

    void setFiles(Boolean files);

    boolean isChecksums();

    void setChecksums(Boolean checksums);

    boolean isCatalogs();

    void setCatalogs(Boolean catalogs);

    String getSignatureExtension();
}
