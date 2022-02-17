/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Signing;
import org.jreleaser.model.util.Artifacts;
import org.jreleaser.sdk.tool.Cosign;
import org.jreleaser.sdk.tool.ToolException;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.command.CommandException;
import org.jreleaser.util.signing.GpgCommandSigner;
import org.jreleaser.util.signing.Keyring;
import org.jreleaser.util.signing.SigningException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.bouncycastle.bcpg.CompressionAlgorithmTags.UNCOMPRESSED;
import static org.jreleaser.model.Signing.KEY_SKIP_SIGNING;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Signer {
    static {
        // replace BC provider with our version
        Provider bcProvider = Security.getProvider("BC");
        Security.removeProvider("BC");
        Security.setProperty("crypto.policy", "unlimited");
        Security.addProvider(bcProvider != null ? bcProvider : new BouncyCastleProvider());
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
            if (context.getModel().getSigning().getMode() == Signing.Mode.COMMAND) {
                cmdSign(context);
            } else if (context.getModel().getSigning().getMode() == Signing.Mode.COSIGN) {
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
        List<FilePair> files = collectArtifacts(context, pair -> isValid(context, null, pair));
        if (files.isEmpty()) {
            context.getLogger().info(RB.$("signing.no.match"));
            return;
        }

        files = files.stream()
            .filter(FilePair::isInvalid)
            .collect(Collectors.toList());

        if (files.isEmpty()) {
            context.getLogger().info(RB.$("signing.up.to.date"));
            return;
        }

        sign(context, files);
        verify(context, files);
    }

    private static void cosignSign(JReleaserContext context) throws SigningException {
        Signing signing = context.getModel().getSigning();

        Cosign cosign = new Cosign(context, signing.getCosign().getVersion());
        try {
            if (!cosign.setup()) {
                context.getLogger().warn(RB.$("tool_unavailable", "cosign"));
                return;
            }
        } catch (ToolException e) {
            throw new SigningException(e.getMessage(), e);
        }

        String privateKey = signing.getCosign().getResolvedPrivateKeyFile();
        String publicKey = signing.getCosign().getResolvedPublicKeyFile();

        Path privateKeyFile = isNotBlank(privateKey) ? context.getBasedir().resolve(privateKey) : null;
        Path publicKeyFile = isNotBlank(publicKey) ? context.getBasedir().resolve(publicKey) : null;
        byte[] password = (signing.getResolvedCosignPassword() + System.lineSeparator()).getBytes();

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

        List<FilePair> files = collectArtifacts(context, forceSign, pair -> isValid(context, cosign, thePublicKeyFile, pair));
        if (files.isEmpty()) {
            context.getLogger().info(RB.$("signing.no.match"));
            return;
        }

        files = files.stream()
            .filter(FilePair::isInvalid)
            .collect(Collectors.toList());

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

        List<FilePair> files = collectArtifacts(context, pair -> isValid(context, keyring, pair));
        if (files.isEmpty()) {
            context.getLogger().info(RB.$("signing.no.match"));
            return;
        }

        files = files.stream()
            .filter(FilePair::isInvalid)
            .collect(Collectors.toList());

        if (files.isEmpty()) {
            context.getLogger().info(RB.$("signing.up.to.date"));
            return;
        }

        sign(context, keyring, files);
        verify(context, keyring, files);
    }

    private static void verify(JReleaserContext context, Keyring keyring, List<FilePair> files) throws SigningException {
        if (null == keyring) {
            verify(context, files);
            return;
        }

        context.getLogger().debug(RB.$("signing.verify.signatures"), files.size());

        for (FilePair pair : files) {
            pair.setValid(verify(context, keyring, pair));

            if (!pair.isValid()) {
                throw new SigningException(RB.$("ERROR_signing_verify_file",
                    context.relativizeToBasedir(pair.inputFile),
                    context.relativizeToBasedir(pair.signatureFile)));
            }
        }
    }

    private static void verify(JReleaserContext context, List<FilePair> files) throws SigningException {
        context.getLogger().debug(RB.$("signing.verify.signatures"), files.size());

        for (FilePair pair : files) {
            pair.setValid(verify(context, pair));

            if (!pair.isValid()) {
                throw new SigningException(RB.$("ERROR_signing_verify_file",
                    context.relativizeToBasedir(pair.inputFile),
                    context.relativizeToBasedir(pair.signatureFile)));
            }
        }
    }

    private static boolean verify(JReleaserContext context, Keyring keyring, FilePair filePair) throws SigningException {
        context.getLogger().setPrefix("verify");

        try {
            context.getLogger().debug("{}",
                context.relativizeToBasedir(filePair.signatureFile));

            InputStream sigInputStream = PGPUtil.getDecoderStream(
                new BufferedInputStream(
                    new FileInputStream(filePair.signatureFile.toFile())));

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

            InputStream fileInputStream = new BufferedInputStream(new FileInputStream(filePair.inputFile.toFile()));
            PGPSignature sig = (PGPSignature) pgpSigList.iterator().next();
            PGPPublicKey pubKey = keyring.readPublicKey();
            sig.init(new JcaPGPContentVerifierBuilderProvider()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME), pubKey);

            int ch;
            while ((ch = fileInputStream.read()) >= 0) {
                sig.update((byte) ch);
            }

            fileInputStream.close();
            sigInputStream.close();

            return sig.verify();
        } catch (IOException | PGPException e) {
            throw new SigningException(RB.$("ERROR_signing_verify_signature",
                context.relativizeToBasedir(filePair.inputFile)), e);
        } finally {
            context.getLogger().restorePrefix();
        }
    }

    private static boolean verify(JReleaserContext context, FilePair filePair) throws SigningException {
        context.getLogger().setPrefix("verify");

        try {
            context.getLogger().debug("{}",
                context.relativizeToBasedir(filePair.signatureFile));

            GpgCommandSigner commandSigner = initCommandSigner(context);
            return commandSigner.verify(filePair.signatureFile, filePair.inputFile);
        } catch (CommandException e) {
            throw new SigningException(RB.$("ERROR_signing_verify_signature",
                context.relativizeToBasedir(filePair.inputFile)), e);
        } finally {
            context.getLogger().restorePrefix();
        }
    }

    private static void sign(JReleaserContext context, List<FilePair> files,
                             Cosign cosign, Path privateKeyFile, byte[] password) throws SigningException {
        Path signaturesDirectory = context.getSignaturesDirectory();

        try {
            Files.createDirectories(signaturesDirectory);
        } catch (IOException e) {
            throw new SigningException(RB.$("ERROR_signing_create_signature_dir"), e);
        }

        context.getLogger().debug(RB.$("signing.signing.files"),
            files.size(), context.relativizeToBasedir(signaturesDirectory));

        for (FilePair pair : files) {
            cosign.signBlob(privateKeyFile, password, pair.inputFile, signaturesDirectory);
        }
    }

    private static void verify(JReleaserContext context, List<FilePair> files,
                               Cosign cosign, Path publicKeyFile) throws SigningException {
        context.getLogger().debug(RB.$("signing.verify.signatures"), files.size());

        context.getLogger().setPrefix("verify");
        try {
            for (FilePair pair : files) {
                cosign.verifyBlob(publicKeyFile, pair.signatureFile, pair.inputFile);
                pair.setValid(true);

                if (!pair.isValid()) {
                    throw new SigningException(RB.$("ERROR_signing_verify_file",
                        context.relativizeToBasedir(pair.inputFile),
                        context.relativizeToBasedir(pair.signatureFile)));
                }
            }
        } finally {
            context.getLogger().restorePrefix();
        }
    }

    private static void sign(JReleaserContext context, List<FilePair> files) throws SigningException {
        Path signaturesDirectory = context.getSignaturesDirectory();

        try {
            Files.createDirectories(signaturesDirectory);
        } catch (IOException e) {
            throw new SigningException(RB.$("ERROR_signing_create_signature_dir"), e);
        }

        context.getLogger().debug(RB.$("signing.signing.files"),
            files.size(), context.relativizeToBasedir(signaturesDirectory));

        GpgCommandSigner commandSigner = initCommandSigner(context);

        for (FilePair pair : files) {
            sign(context, commandSigner, pair.inputFile, pair.signatureFile);
        }
    }

    private static GpgCommandSigner initCommandSigner(JReleaserContext context) {
        GpgCommandSigner cmd = new GpgCommandSigner(context.getLogger());
        Signing signing = context.getModel().getSigning();
        cmd.setExecutable(signing.getCommand().getExecutable());
        cmd.setPassphrase(signing.getResolvedPassphrase());
        cmd.setHomeDir(signing.getCommand().getHomeDir());
        cmd.setKeyName(signing.getCommand().getKeyName());
        cmd.setPublicKeyring(signing.getCommand().getPublicKeyring());
        cmd.setDefaultKeyring(signing.getCommand().isDefaultKeyring());
        cmd.setArgs(signing.getCommand().getArgs());
        return cmd;
    }

    private static void sign(JReleaserContext context, GpgCommandSigner commandSigner, Path input, Path output) throws SigningException {
        try {
            context.getLogger().info("{}", context.relativizeToBasedir(input));

            commandSigner.sign(input, output);
        } catch (CommandException e) {
            throw new SigningException(RB.$("ERROR_unexpected_error_signing", input.toAbsolutePath()), e);
        }
    }

    private static void sign(JReleaserContext context, Keyring keyring, List<FilePair> files) throws SigningException {
        Path signaturesDirectory = context.getSignaturesDirectory();

        try {
            Files.createDirectories(signaturesDirectory);
        } catch (IOException e) {
            throw new SigningException(RB.$("ERROR_signing_create_signature_dir"), e);
        }

        context.getLogger().debug(RB.$("signing.signing.files"),
            files.size(), context.relativizeToBasedir(signaturesDirectory));

        PGPSignatureGenerator signatureGenerator = initSignatureGenerator(context.getModel().getSigning(), keyring);

        for (FilePair pair : files) {
            sign(context, signatureGenerator, pair.inputFile, pair.signatureFile);
        }
    }

    private static PGPSignatureGenerator initSignatureGenerator(Signing signing, Keyring keyring) throws SigningException {
        try {
            PGPSecretKey pgpSecretKey = keyring.getSecretKey();

            PGPPrivateKey pgpPrivKey = pgpSecretKey.extractPrivateKey(
                new JcePBESecretKeyDecryptorBuilder()
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .build(signing.getResolvedPassphrase().toCharArray()));

            PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
                new JcaPGPContentSignerBuilder(pgpSecretKey.getPublicKey().getAlgorithm(), PGPUtil.SHA1)
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME));

            signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, pgpPrivKey);

            return signatureGenerator;
        } catch (PGPException e) {
            throw new SigningException(RB.$("ERROR_unexpected_error_signature_gen"), e);
        }
    }

    private static void sign(JReleaserContext context, PGPSignatureGenerator signatureGenerator, Path input, Path output) throws SigningException {
        try {
            context.getLogger().info("{}", context.relativizeToBasedir(input));

            OutputStream out = new BufferedOutputStream(new FileOutputStream(output.toFile()));
            if (context.getModel().getSigning().isArmored()) {
                out = new ArmoredOutputStream(out);
            }

            PGPCompressedDataGenerator compressionStreamGenerator = new PGPCompressedDataGenerator(UNCOMPRESSED);
            BCPGOutputStream bOut = new BCPGOutputStream(compressionStreamGenerator.open(out));

            FileInputStream in = new FileInputStream(input.toFile());

            byte[] buffer = new byte[8192];
            int length = 0;
            while ((length = in.read(buffer)) >= 0) {
                signatureGenerator.update(buffer, 0, length);
            }

            signatureGenerator.generate().encode(bOut);

            compressionStreamGenerator.close();

            in.close();
            out.flush();
            out.close();
        } catch (IOException | PGPException e) {
            throw new SigningException(RB.$("ERROR_unexpected_error_signing", input.toAbsolutePath()), e);
        }
    }

    private static List<FilePair> collectArtifacts(JReleaserContext context, Function<FilePair, Boolean> validator) {
        return collectArtifacts(context, false, validator);
    }

    private static List<FilePair> collectArtifacts(JReleaserContext context, boolean forceSign, Function<FilePair, Boolean> validator) {
        List<FilePair> files = new ArrayList<>();

        Signing signing = context.getModel().getSigning();
        Path signaturesDirectory = context.getSignaturesDirectory();

        String extension = ".sig";
        if (signing.getMode() != Signing.Mode.COSIGN) {
            extension = signing.isArmored() ? ".asc" : ".sig";
        }

        if (signing.isFiles()) {
            for (Artifact artifact : Artifacts.resolveFiles(context)) {
                if (!artifact.isActive() || artifact.extraPropertyIsTrue(KEY_SKIP_SIGNING)) continue;
                Path input = artifact.getEffectivePath(context);
                Path output = signaturesDirectory.resolve(input.getFileName().toString().concat(extension));
                FilePair pair = new FilePair(input, output);
                if (!forceSign) pair.setValid(validator.apply(pair));
                files.add(pair);
            }
        }

        if (signing.isArtifacts()) {
            for (Distribution distribution : context.getModel().getActiveDistributions()) {
                if (distribution.extraPropertyIsTrue(KEY_SKIP_SIGNING)) continue;
                for (Artifact artifact : distribution.getArtifacts()) {
                    if (!artifact.isActive() || artifact.extraPropertyIsTrue(KEY_SKIP_SIGNING)) continue;
                    Path input = artifact.getEffectivePath(context, distribution);
                    Path output = signaturesDirectory.resolve(input.getFileName().toString().concat(extension));
                    FilePair pair = new FilePair(input, output);
                    if (!forceSign) pair.setValid(validator.apply(pair));
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
                    FilePair pair = new FilePair(checksums, output);
                    if (!forceSign) pair.setValid(validator.apply(pair));
                    files.add(pair);
                }
            }
        }

        return files;
    }

    private static boolean isValid(JReleaserContext context, Cosign cosign, Path publicKeyFile, FilePair pair) {
        if (Files.notExists(pair.getSignatureFile())) {
            context.getLogger().debug(RB.$("signing.signature.not.exist"),
                context.relativizeToBasedir(pair.getSignatureFile()));
            return false;
        }

        if (pair.inputFile.toFile().lastModified() > pair.signatureFile.toFile().lastModified()) {
            context.getLogger().debug(RB.$("signing.file.newer"),
                context.relativizeToBasedir(pair.inputFile),
                context.relativizeToBasedir(pair.signatureFile));
            return false;
        }

        try {
            cosign.verifyBlob(publicKeyFile, pair.signatureFile, pair.inputFile);
            return true;
        } catch (SigningException e) {
            return false;
        }
    }

    private static boolean isValid(JReleaserContext context, Keyring keyring, FilePair pair) {
        if (null == keyring) {
            return isValid(context, pair);
        }

        if (Files.notExists(pair.getSignatureFile())) {
            context.getLogger().debug(RB.$("signing.signature.not.exist"),
                context.relativizeToBasedir(pair.getSignatureFile()));
            return false;
        }

        if (pair.inputFile.toFile().lastModified() > pair.signatureFile.toFile().lastModified()) {
            context.getLogger().debug(RB.$("signing.file.newer"),
                context.relativizeToBasedir(pair.inputFile),
                context.relativizeToBasedir(pair.signatureFile));
            return false;
        }

        try {
            return verify(context, keyring, pair);
        } catch (SigningException e) {
            return false;
        }
    }

    private static boolean isValid(JReleaserContext context, FilePair pair) {
        if (Files.notExists(pair.getSignatureFile())) {
            context.getLogger().debug(RB.$("signing.signature.not.exist"),
                context.relativizeToBasedir(pair.getSignatureFile()));
            return false;
        }

        if (pair.inputFile.toFile().lastModified() > pair.signatureFile.toFile().lastModified()) {
            context.getLogger().debug(RB.$("signing.file.newer"),
                context.relativizeToBasedir(pair.inputFile),
                context.relativizeToBasedir(pair.signatureFile));
            return false;
        }

        try {
            return verify(context, pair);
        } catch (SigningException e) {
            return false;
        }
    }

    private static class FilePair {
        private final Path inputFile;
        private final Path signatureFile;
        private boolean valid;

        private FilePair(Path inputFile, Path signatureFile) {
            this.inputFile = inputFile;
            this.signatureFile = signatureFile;
        }

        public Path getInputFile() {
            return inputFile;
        }

        public Path getSignatureFile() {
            return signatureFile;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public boolean isInvalid() {
            return !valid;
        }
    }
}
