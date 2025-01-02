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
package org.jreleaser.sdk.signing;

import org.bouncycastle.openpgp.PGPException;
import org.jreleaser.model.api.signing.Keyring;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class InMemoryKeyring extends Keyring {
    private final byte[] encodedPublicKey;
    private final byte[] encodedPrivateKey;

    public InMemoryKeyring(byte[] encodedPublicKey, byte[] encodedPrivateKey) throws IOException, PGPException {
        this.encodedPublicKey = encodedPublicKey;
        this.encodedPrivateKey = encodedPrivateKey;
    }

    @Override
    protected InputStream getPublicKeyRingStream() {
        if (null != encodedPublicKey) {
            return new ByteArrayInputStream(encodedPublicKey);
        }
        return new EmptyInputStream();
    }

    @Override
    protected InputStream getSecretKeyRingStream() {
        return new ByteArrayInputStream(encodedPrivateKey);
    }
}
