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
import org.jreleaser.model.internal.catalog.sbom.SbomCataloger;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.signing.Signing;
import org.jreleaser.model.internal.util.Artifacts;
import org.jreleaser.model.spi.catalog.sbom.SbomCatalogerProcessorHelper;
import org.jreleaser.sdk.signing.GpgCommandSigner;
import org.jreleaser.sdk.signing.SigningUtils;
import org.jreleaser.sdk.tool.Cosign;
import org.jreleaser.sdk.tool.ToolException;
import org.jreleaser.util.Algorithm;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.nio.file.Files.newInputStream;
import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.api.signing.Signing.KEY_SKIP_SIGNING;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class Signer {
    private Signer() {
        // noop
    }

    public static void sign(JReleaserContext context) throws SigningException {
        context.getLogger().info(RB.$("signing.header"));
        context.getLogger().increaseIndent();
        context.getLogger().setPrefix("sign");

        if (!context.getModel().getSigning().isEnabled()) {
            context.getLogger().info(RB.$("signing.not.enabled"));
            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
            return;
        }

        try {
            if (context.getModel().getSigning().getMode() == org.jreleaser.model.Signing.Mode.COMMAND) {
                cmdSign(context);
            } else if (context.getModel().getSigning().getMode() == org.jreleaser.model.Signing.Mode.COSIGN) {
                cosignSign(context);
            } else {
                bcSign(context);
            }
        } finally {
            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
        }
    }

    private static void cmdSign(JReleaserContext context) throws SigningException {
        List<SigningUtils.FilePair> files = collectArtifacts(context, pair -> SigningUtils.isValid(context.asImmutable(), null, pair));
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

        sign(context, files);
        if (context.getModel().getSigning().isVerify()) {
            verify(context, files);
        }
    }

    private static void cosignSign(JReleaserContext context) throws SigningException {
        Signing signing = context.getModel().getSigning();

        Cosign cosign = new Cosign(context.asImmutable(), signing.getCosign().getVersion());
        try {
            if (!cosign.setup()) {
                context.getLogger().warn(RB.$("tool_unavailable", "cosign"));
                return;
            }
        } catch (ToolException e) {
            throw new SigningException(e.getMessage(), e);
        }

        String privateKey = signing.getCosign().getPrivateKeyFile();
        String publicKey = signing.getCosign().getPublicKeyFile();

        Path privateKeyFile = isNotBlank(privateKey) ? context.getBasedir().resolve(privateKey) : null;
        Path publicKeyFile = isNotBlank(publicKey) ? context.getBasedir().resolve(publicKey) : null;
        String password = signing.getPassphrase();

        boolean forceSign = false;
        if (null == privateKeyFile) {
            privateKeyFile = signing.getCosign().getResolvedPrivateKeyFilePath(context);
            publicKeyFile = privateKeyFile.resolveSibling("cosign.pub");
            if (!Files.exists(privateKeyFile)) {
                privateKeyFile = cosign.generateKeyPair(password);
                forceSign = true;
            }
        }
        Path thePublicKeyFile = publicKeyFile;

        List<SigningUtils.FilePair> files = collectArtifacts(context, forceSign, pair -> isValid(context, cosign, thePublicKeyFile, pair));
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

        if (!cosign.checkPassword(privateKeyFile, password)) {
            context.getLogger().warn(RB.$("WARN_cosign_password_does_not_match", "cosign"));
            return;
        }

        sign(context, files, cosign, privateKeyFile, password);
        verify(context, files, cosign, publicKeyFile);
    }

    private static void bcSign(JReleaserContext context) throws SigningException {
        Keyring keyring = context.createKeyring();

        List<SigningUtils.FilePair> files = collectArtifacts(context, pair -> SigningUtils.isValid(context.asImmutable(), keyring, pair));
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

        sign(context, keyring, files);
        if (context.getModel().getSigning().isVerify()) {
            verify(context, keyring, files);
        }
    }


    private static void verify(JReleaserContext context, Keyring keyring, List<SigningUtils.FilePair> files) throws SigningException {
        if (null == keyring) {
            verify(context, files);
            return;
        }

        context.getLogger().debug(RB.$("signing.verify.signatures"), files.size());

        for (SigningUtils.FilePair pair : files) {
            pair.setValid(verify(context, keyring, pair));

            if (!pair.isValid()) {
                throw new SigningException(RB.$("ERROR_signing_verify_file",
                    context.relativizeToBasedir(pair.getInputFile()),
                    context.relativizeToBasedir(pair.getSignatureFile())));
            }
        }
    }

    private static void verify(JReleaserContext context, List<SigningUtils.FilePair> files) throws SigningException {
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

    private static boolean verify(JReleaserContext context, Keyring keyring, SigningUtils.FilePair filePair) throws SigningException {
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

    private static void sign(JReleaserContext context, List<SigningUtils.FilePair> files,
                             Cosign cosign, Path privateKeyFile, String password) throws SigningException {
        Path signaturesDirectory = context.getSignaturesDirectory();

        try {
            Files.createDirectories(signaturesDirectory);
        } catch (IOException e) {
            throw new SigningException(RB.$("ERROR_signing_create_signature_dir"), e);
        }

        context.getLogger().debug(RB.$("signing.signing.files"),
            files.size(), context.relativizeToBasedir(signaturesDirectory));

        for (SigningUtils.FilePair pair : files) {
            cosign.signBlob(privateKeyFile, password, pair.getInputFile(), signaturesDirectory);
        }
    }

    private static void verify(JReleaserContext context, List<SigningUtils.FilePair> files,
                               Cosign cosign, Path publicKeyFile) throws SigningException {
        context.getLogger().debug(RB.$("signing.verify.signatures"), files.size());

        context.getLogger().setPrefix("verify");
        try {
            for (SigningUtils.FilePair pair : files) {
                cosign.verifyBlob(publicKeyFile, pair.getSignatureFile(), pair.getInputFile());
                pair.setValid(true);

                if (!pair.isValid()) {
                    throw new SigningException(RB.$("ERROR_signing_verify_file",
                        context.relativizeToBasedir(pair.getInputFile()),
                        context.relativizeToBasedir(pair.getSignatureFile())));
                }
            }
        } finally {
            context.getLogger().restorePrefix();
        }
    }

    private static void sign(JReleaserContext context, List<SigningUtils.FilePair> files) throws SigningException {
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

    private static void sign(JReleaserContext context, Keyring keyring, List<SigningUtils.FilePair> files) throws SigningException {
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

    private static List<SigningUtils.FilePair> collectArtifacts(JReleaserContext context, Predicate<SigningUtils.FilePair> validator) {
        return collectArtifacts(context, false, validator);
    }

    private static List<SigningUtils.FilePair> collectArtifacts(JReleaserContext context, boolean forceSign, Predicate<SigningUtils.FilePair> validator) {
        List<SigningUtils.FilePair> files = new ArrayList<>();

        Signing signing = context.getModel().getSigning();
        Path signaturesDirectory = context.getSignaturesDirectory();

        String extension = ".sig";
        if (signing.getMode() != org.jreleaser.model.Signing.Mode.COSIGN) {
            extension = signing.isArmored() ? ".asc" : ".sig";
        }

        if (signing.isFiles()) {
            for (Artifact artifact : Artifacts.resolveFiles(context)) {
                if (!artifact.isActiveAndSelected() || artifact.extraPropertyIsTrue(KEY_SKIP_SIGNING) ||
                    artifact.isOptional(context) && !artifact.resolvedPathExists()) continue;
                Path input = artifact.getEffectivePath(context);
                Path output = signaturesDirectory.resolve(input.getFileName().toString().concat(extension));
                SigningUtils.FilePair pair = new SigningUtils.FilePair(input, output);
                if (!forceSign) pair.setValid(validator.test(pair));
                files.add(pair);
            }
        }

        if (signing.isArtifacts()) {
            for (Distribution distribution : context.getModel().getActiveDistributions()) {
                if (distribution.extraPropertyIsTrue(KEY_SKIP_SIGNING)) continue;
                for (Artifact artifact : distribution.getArtifacts()) {
                    if (!artifact.isActiveAndSelected() || artifact.extraPropertyIsTrue(KEY_SKIP_SIGNING)) continue;
                    Path input = artifact.getEffectivePath(context, distribution);
                    if (artifact.isOptional(context) && !artifact.resolvedPathExists()) continue;
                    Path output = signaturesDirectory.resolve(input.getFileName().toString().concat(extension));
                    SigningUtils.FilePair pair = new SigningUtils.FilePair(input, output);
                    if (!forceSign) pair.setValid(validator.test(pair));
                    files.add(pair);
                }
            }
        }

        if (signing.isCatalogs()) {
            List<? extends SbomCataloger<?>> catalogers = context.getModel().getCatalog().getSbom().findAllActiveSbomCatalogers();
            for (SbomCataloger<?> cataloger : catalogers) {
                if (!cataloger.getPack().isEnabled()) continue;
                for (Artifact artifact : SbomCatalogerProcessorHelper.resolveArtifacts(context, cataloger)) {
                    Path input = artifact.getEffectivePath(context);
                    Path output = signaturesDirectory.resolve(input.getFileName().toString().concat(extension));
                    SigningUtils.FilePair pair = new SigningUtils.FilePair(input, output);
                    if (!forceSign) pair.setValid(validator.test(pair));
                    files.add(pair);
                }
            }
        }

        if (signing.isChecksums()) {
            for (Algorithm algorithm : context.getModel().getChecksum().getAlgorithms()) {
                Path checksums = context.getChecksumsDirectory()
                    .resolve(context.getModel().getChecksum().getResolvedName(context, algorithm));
                if (Files.exists(checksums)) {
                    Path output = signaturesDirectory.resolve(checksums.getFileName().toString().concat(extension));
                    SigningUtils.FilePair pair = new SigningUtils.FilePair(checksums, output);
                    if (!forceSign) pair.setValid(validator.test(pair));
                    files.add(pair);
                }
            }
        }

        return files;
    }

    private static boolean isValid(JReleaserContext context, Cosign cosign, Path publicKeyFile, SigningUtils.FilePair pair) {
        if (Files.notExists(pair.getSignatureFile())) {
            context.getLogger().debug(RB.$("signing.signature.not.exist"),
                context.relativizeToBasedir(pair.getSignatureFile()));
            return false;
        }

        if (pair.getInputFile().toFile().lastModified() > pair.getSignatureFile().toFile().lastModified()) {
            context.getLogger().debug(RB.$("signing.file.newer"),
                context.relativizeToBasedir(pair.getInputFile()),
                context.relativizeToBasedir(pair.getSignatureFile()));
            return false;
        }

        try {
            cosign.verifyBlob(publicKeyFile, pair.getSignatureFile(), pair.getInputFile());
            return true;
        } catch (SigningException e) {
            return false;
        }
    }
}
