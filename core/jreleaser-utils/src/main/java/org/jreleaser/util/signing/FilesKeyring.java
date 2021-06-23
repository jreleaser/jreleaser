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
package org.jreleaser.util.signing;

import org.bouncycastle.openpgp.PGPException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public final class FilesKeyring extends Keyring {
    private final Path publicKeyring;
    private final Path secretKeyring;

    public FilesKeyring(Path publicKeyring, Path secretKeyring) throws IOException, PGPException {
        this.publicKeyring = publicKeyring;
        this.secretKeyring = secretKeyring;
    }

    @Override
    protected InputStream getPublicKeyRingStream() throws IOException {
        return Files.newInputStream(publicKeyring);
    }

    @Override
    protected InputStream getSecretKeyRingStream() throws IOException {
        return Files.newInputStream(secretKeyring);
    }
}
