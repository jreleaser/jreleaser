/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.engine.sign;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.signing.Keyring;
import org.jreleaser.model.api.signing.SigningException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.signing.SigningTool;
import org.jreleaser.sdk.signing.GpgCommandSigner;
import org.jreleaser.sdk.signing.SigningUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.Files.newInputStream;
import static java.util.stream.Collectors.toList;

/**
 * @author Andres Almiray
 * @since 1.22.0
 */
public final class PgpSigner extends AbstractSigner {
    public PgpSigner(JReleaserContext context, SigningTool tool) {
        super(context, tool);
    }

    public void sign() throws SigningException {
        switch (context.getModel().getSigning().getPgp().getMode()) {
            case COMMAND:
                cmdSign();
                break;
            case FILE:
            case MEMORY:
                bcSign();
                break;
            default:
                // empty
        }
    }

    private void cmdSign() throws SigningException {
        List<SigningUtils.FilePair> files = collectArtifacts(pair -> SigningUtils.isValid(context.asImmutable(), null, pair));
        if (files.isEmpty()) {
            context.getLogger().info(RB.$("signing.no.match"));
            return;
        }

        files = files.stream()
            .filter(SigningUtils.FilePair::isInvalid)
            .collect(toList());

        if (files.isEmpty()) {
            context.getLogger().info(RB.$("signing.up.to.date"));
            return;
        }

        sign(files);
        if (context.getModel().getSigning().getPgp().isVerify()) {
            verify(files);
        }
    }

    private void bcSign() throws SigningException {
        Keyring keyring = context.createKeyring();

        List<SigningUtils.FilePair> files = collectArtifacts(pair -> SigningUtils.isValid(context.asImmutable(), keyring, pair));
        if (files.isEmpty()) {
            context.getLogger().info(RB.$("signing.no.match"));
            return;
        }

        files = files.stream()
            .filter(SigningUtils.FilePair::isInvalid)
            .collect(toList());

        if (files.isEmpty()) {
            context.getLogger().info(RB.$("signing.up.to.date"));
            return;
        }

        sign(keyring, files);
        if (context.getModel().getSigning().getPgp().isVerify()) {
            verify(keyring, files);
        }
    }

    private void verify(Keyring keyring, List<SigningUtils.FilePair> files) throws SigningException {
        if (null == keyring) {
            verify(files);
            return;
        }

        context.getLogger().debug(RB.$("signing.verify.signatures"), files.size());

        for (SigningUtils.FilePair pair : files) {
            pair.setValid(verify(keyring, pair));

            if (!pair.isValid()) {
                throw new SigningException(RB.$("ERROR_signing_verify_file",
                    context.relativizeToBasedir(pair.getInputFile()),
                    context.relativizeToBasedir(pair.getSignatureFile())));
            }
        }
    }

    private void verify(List<SigningUtils.FilePair> files) throws SigningException {
        context.getLogger().debug(RB.$("signing.verify.signatures"), files.size());

        for (SigningUtils.FilePair pair : files) {
            pair.setValid(SigningUtils.verify(context.asImmutable(), pair));

            if (!pair.isValid()) {
                throw new SigningException(RB.$("ERROR_signing_verify_file",
                    context.relativizeToBasedir(pair.getInputFile()),
                    context.relativizeToBasedir(pair.getSignatureFile())));
            }
        }
    }

    private boolean verify(Keyring keyring, SigningUtils.FilePair filePair) throws SigningException {
        context.getLogger().setPrefix("verify");

        context.getLogger().debug("{}",
            context.relativizeToBasedir(filePair.getSignatureFile()));

        try (InputStream sigInputStream = PGPUtil.getDecoderStream(
            new BufferedInputStream(
                newInputStream(filePair.getSignatureFile())))) {
            PGPObjectFactory pgpObjFactory = new PGPObjectFactory(sigInputStream, keyring.getKeyFingerPrintCalculator());
            Iterable<?> pgpSigList = null;

            Object obj = pgpObjFactory.nextObject();
            if (obj instanceof PGPCompressedData) {
                PGPCompressedData c1 = (PGPCompressedData) obj;
                pgpObjFactory = new PGPObjectFactory(c1.getDataStream(), keyring.getKeyFingerPrintCalculator());
                pgpSigList = (Iterable<?>) pgpObjFactory.nextObject();
            } else {
                pgpSigList = (Iterable<?>) obj;
            }

            PGPSignature sig = (PGPSignature) pgpSigList.iterator().next();
            try (InputStream fileInputStream = new BufferedInputStream(newInputStream(filePair.getInputFile()))) {
                PGPPublicKey pubKey = keyring.readPublicKey();
                sig.init(new JcaPGPContentVerifierBuilderProvider()
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME), pubKey);

                int ch;
                while ((ch = fileInputStream.read()) >= 0) {
                    sig.update((byte) ch);
                }
            }

            return sig.verify();
        } catch (IOException | PGPException e) {
            throw new SigningException(RB.$("ERROR_signing_verify_signature",
                context.relativizeToBasedir(filePair.getInputFile())), e);
        } finally {
            context.getLogger().restorePrefix();
        }
    }

    private void sign(List<SigningUtils.FilePair> files) throws SigningException {
        Path signaturesDirectory = context.getSignaturesDirectory();

        try {
            Files.createDirectories(signaturesDirectory);
        } catch (IOException e) {
            throw new SigningException(RB.$("ERROR_signing_create_signature_dir"), e);
        }

        context.getLogger().debug(RB.$("signing.signing.files"),
            files.size(), context.relativizeToBasedir(signaturesDirectory));

        GpgCommandSigner commandSigner = SigningUtils.initCommandSigner(context.asImmutable());

        for (SigningUtils.FilePair pair : files) {
            SigningUtils.sign(context.asImmutable(), commandSigner, pair.getInputFile(), pair.getSignatureFile());
        }
    }

    private void sign(Keyring keyring, List<SigningUtils.FilePair> files) throws SigningException {
        Path signaturesDirectory = context.getSignaturesDirectory();

        try {
            Files.createDirectories(signaturesDirectory);
        } catch (IOException e) {
            throw new SigningException(RB.$("ERROR_signing_create_signature_dir"), e);
        }

        context.getLogger().debug(RB.$("signing.signing.files"),
            files.size(), context.relativizeToBasedir(signaturesDirectory));

        PGPSignatureGenerator signatureGenerator = SigningUtils.initSignatureGenerator(context.asImmutable(), keyring);

        for (SigningUtils.FilePair pair : files) {
            SigningUtils.sign(context.asImmutable(), signatureGenerator, pair.getInputFile(), pair.getSignatureFile());
        }
    }
}
