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

import org.bouncycastle.bcpg.PublicKeyAlgorithmTags;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.jreleaser.bundle.RB;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;

/**
 * Adapted from {@code name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.keyrings.InMemoryKeyring}
 * Original author: Jens Neuhalfen
 *
 * @author Andres Almiray
 * @since 0.4.0
 */
public abstract class Keyring {
    private final KeyFingerPrintCalculator keyFingerPrintCalculator = new BcKeyFingerprintCalculator();
    private PGPPublicKeyRingCollection publicKeyRings;
    private PGPSecretKeyRingCollection secretKeyRings;

    protected Keyring() throws IOException, PGPException {
        this.publicKeyRings = new PGPPublicKeyRingCollection(Collections.emptyList());
        this.secretKeyRings = new PGPSecretKeyRingCollection(Collections.emptyList());
    }

    public Keyring initialize(boolean armored) throws IOException, PGPException {
        try (InputStream pub = getPublicKeyRingStream();
             InputStream sec = getSecretKeyRingStream()) {
            if (!(pub instanceof EmptyInputStream)) {
                addPublicKey(armored, pub);
            }
            addSecretKey(armored, sec);
        }

        return this;
    }

    public KeyFingerPrintCalculator getKeyFingerPrintCalculator() {
        return keyFingerPrintCalculator;
    }

    protected abstract InputStream getPublicKeyRingStream() throws IOException;

    protected abstract InputStream getSecretKeyRingStream() throws IOException;

    public void addPublicKey(boolean armored, InputStream raw) throws IOException {
        if (!armored) {
            addPublicKeyRing(new PGPPublicKeyRing(raw, keyFingerPrintCalculator));
            return;
        }

        try (InputStream decoded = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(raw)) {
            addPublicKeyRing(new PGPPublicKeyRing(decoded, keyFingerPrintCalculator));
        }
    }

    public void addSecretKey(boolean armored, InputStream raw) throws IOException, PGPException {
        if (!armored) {
            addSecretKeyRing(new PGPSecretKeyRing(raw, keyFingerPrintCalculator));
            return;
        }

        try (InputStream decoded = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(raw)) {
            addSecretKeyRing(new PGPSecretKeyRing(decoded, keyFingerPrintCalculator));
        }
    }

    public void addSecretKeyRing(PGPSecretKeyRing keyring) {
        this.secretKeyRings = PGPSecretKeyRingCollection.addSecretKeyRing(this.secretKeyRings, keyring);
    }

    public void addPublicKeyRing(PGPPublicKeyRing keyring) {
        this.publicKeyRings = PGPPublicKeyRingCollection.addPublicKeyRing(this.publicKeyRings, keyring);
    }

    public PGPPublicKey readPublicKey() throws SigningException {
        Iterator<PGPPublicKeyRing> keyRingIter = publicKeyRings.getKeyRings();
        while (keyRingIter.hasNext()) {
            PGPPublicKeyRing keyRing = keyRingIter.next();

            Iterator<PGPPublicKey> keyIter = keyRing.getPublicKeys();
            while (keyIter.hasNext()) {
                PGPPublicKey key = keyIter.next();
                if (isSigningKey(key)) {
                    return key;
                }
            }
        }

        throw new SigningException(RB.$("ERROR_public_key_not_found"));
    }

    public PGPSecretKey readSecretKey() throws SigningException {
        Iterator<PGPSecretKeyRing> keyRingIter = secretKeyRings.getKeyRings();
        while (keyRingIter.hasNext()) {
            PGPSecretKeyRing keyRing = keyRingIter.next();

            Iterator<PGPSecretKey> keyIter = keyRing.getSecretKeys();
            while (keyIter.hasNext()) {
                PGPSecretKey key = keyIter.next();
                if (key.isSigningKey()) {
                    return key;
                }
            }
        }

        throw new SigningException(RB.$("ERROR_secret_key_not_found"));
    }

    /**
     * Returns {@code true} if the given key can be used for signing.
     *
     * <p>There is no Method key.isSigningKey(), and encryption does not always mean signing.
     * The algorithms here need to be kept in sync with {@code org.bouncycastle.openpgp.operator.jcajce.OperatorHelper#createSignature}.
     *
     * @param key they key to check if it is usable for signing.
     * @return {@code true} if the given key can be used for signing.
     */
    private static boolean isSigningKey(PGPPublicKey key) {
        final int algorithm = key.getAlgorithm();

        return algorithm == PublicKeyAlgorithmTags.EDDSA ||
            algorithm == PublicKeyAlgorithmTags.ECDSA ||
            algorithm == PublicKeyAlgorithmTags.ELGAMAL_GENERAL ||
            algorithm == PublicKeyAlgorithmTags.ELGAMAL_ENCRYPT ||
            algorithm == PublicKeyAlgorithmTags.RSA_SIGN ||
            algorithm == PublicKeyAlgorithmTags.RSA_GENERAL ||
            algorithm == PublicKeyAlgorithmTags.DSA;
    }

    public static class EmptyInputStream extends ByteArrayInputStream {
        public EmptyInputStream() {
            super(new byte[0]);
        }
    }
}
