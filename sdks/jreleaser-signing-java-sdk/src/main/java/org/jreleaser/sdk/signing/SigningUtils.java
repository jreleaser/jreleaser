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

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
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
import org.bouncycastle.util.encoders.Hex;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext;
import org.jreleaser.model.api.signing.Keyring;
import org.jreleaser.model.api.signing.Signing;
import org.jreleaser.model.api.signing.SigningException;
import org.jreleaser.sdk.command.CommandException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Provider;
import java.security.Security;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static org.bouncycastle.bcpg.CompressionAlgorithmTags.UNCOMPRESSED;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class SigningUtils {
    static {
        // replace BC provider with our version
        Provider bcProvider = Security.getProvider("BC");
        Security.removeProvider("BC");
        Security.setProperty("crypto.policy", "unlimited");
        Security.addProvider(null != bcProvider ? bcProvider : new BouncyCastleProvider());
    }

    private SigningUtils() {
        // noop
    }

    public static Optional<String> getPublicKeyID(JReleaserContext context) throws SigningException {
        if (context.getModel().getSigning().getMode() != org.jreleaser.model.Signing.Mode.COMMAND &&
            context.getModel().getSigning().getMode() != org.jreleaser.model.Signing.Mode.COSIGN) {
            Keyring keyring = context.createKeyring();
            return Optional.of(Long.toHexString(keyring.readPublicKey().getKeyID()));
        }

        return Optional.empty();
    }

    public static Optional<String> getFingerprint(JReleaserContext context) throws SigningException {
        if (context.getModel().getSigning().getMode() != org.jreleaser.model.Signing.Mode.COMMAND &&
            context.getModel().getSigning().getMode() != org.jreleaser.model.Signing.Mode.COSIGN) {
            Keyring keyring = context.createKeyring();
            return Optional.of(Hex.toHexString(keyring.readPublicKey().getFingerprint()));
        }

        return Optional.empty();
    }

    public static Optional<Instant> getExpirationDateOfPublicKey(JReleaserContext context) throws SigningException {
        if (context.getModel().getSigning().getMode() != org.jreleaser.model.Signing.Mode.COMMAND &&
            context.getModel().getSigning().getMode() != org.jreleaser.model.Signing.Mode.COSIGN) {
            PGPPublicKey publicKey = context.createKeyring().readPublicKey();
            if (publicKey.getValidSeconds() <= 0) {
                return Optional.of(Instant.EPOCH);
            }
            return Optional.of(Instant.ofEpochMilli(publicKey.getCreationTime().getTime()).plus(publicKey.getValidDays(), ChronoUnit.DAYS));
        }

        return Optional.empty();
    }

    public static void sign(JReleaserContext context, Path file) throws SigningException {
        if (context.getModel().getSigning().getMode() == org.jreleaser.model.Signing.Mode.COMMAND) {
            cmdSign(context, file);
        } else if (context.getModel().getSigning().getMode() != org.jreleaser.model.Signing.Mode.COSIGN) {
            bcSign(context, file);
        }
    }

    private static void cmdSign(JReleaserContext context, Path input) throws SigningException {
        FilePair pair = checkInput(context, input);

        if (pair.isValid()) {
            return;
        }

        sign(context, pair);
        if (context.getModel().getSigning().isVerify()) {
            verify(context, pair);
        } else {
            context.getLogger().debug(RB.$("signing.verify.disabled"));
        }
    }

    private static void bcSign(JReleaserContext context, Path input) throws SigningException {
        Keyring keyring = context.createKeyring();

        FilePair pair = checkInput(context, input);

        if (pair.isValid()) {
            return;
        }

        sign(context, keyring, pair);
        if (context.getModel().getSigning().isVerify()) {
            verify(context, keyring, pair);
        } else {
            context.getLogger().debug(RB.$("signing.verify.disabled"));
        }
    }

    private static FilePair checkInput(JReleaserContext context, Path input) {
        Signing signing = context.getModel().getSigning();

        String extension = ".sig";
        if (signing.getMode() != org.jreleaser.model.Signing.Mode.COSIGN) {
            extension = signing.isArmored() ? ".asc" : ".sig";
        }

        Path output = input.getParent().resolve(input.getFileName().toString().concat(extension));
        FilePair pair = new FilePair(input, output);
        pair.setValid(isValid(context, pair));

        return pair;
    }

    public static boolean verify(JReleaserContext context, Keyring keyring, FilePair filePair) throws SigningException {
        context.getLogger().setPrefix("verify");

        context.getLogger().debug("{}",
            context.relativizeToBasedir(filePair.signatureFile));

        try (InputStream sigInputStream = PGPUtil.getDecoderStream(
            new BufferedInputStream(
                newInputStream(filePair.signatureFile)))) {

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
            try (InputStream fileInputStream = new BufferedInputStream(newInputStream(filePair.inputFile))) {
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
                context.relativizeToBasedir(filePair.inputFile)), e);
        } finally {
            context.getLogger().restorePrefix();
        }
    }

    public static boolean verify(JReleaserContext context, FilePair filePair) throws SigningException {
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

    private static void sign(JReleaserContext context, FilePair pair) throws SigningException {
        GpgCommandSigner commandSigner = initCommandSigner(context);

        sign(context, commandSigner, pair.inputFile, pair.signatureFile);
    }

    public static GpgCommandSigner initCommandSigner(JReleaserContext context) {
        GpgCommandSigner cmd = new GpgCommandSigner(context.getLogger());
        Signing signing = context.getModel().getSigning();
        cmd.setExecutable(signing.getCommand().getExecutable());
        cmd.setPassphrase(signing.getPassphrase());
        cmd.setHomeDir(signing.getCommand().getHomeDir());
        cmd.setKeyName(signing.getCommand().getKeyName());
        cmd.setPublicKeyring(signing.getCommand().getPublicKeyring());
        cmd.setDefaultKeyring(signing.getCommand().isDefaultKeyring());
        cmd.setArgs(signing.getCommand().getArgs());
        return cmd;
    }

    public static void sign(JReleaserContext context, GpgCommandSigner commandSigner, Path input, Path output) throws SigningException {
        try {
            context.getLogger().info("{}", context.relativizeToBasedir(input));

            commandSigner.sign(input, output);
        } catch (CommandException e) {
            throw new SigningException(RB.$("ERROR_unexpected_error_signing", input.toAbsolutePath()), e);
        }
    }

    private static void sign(JReleaserContext context, Keyring keyring, FilePair pair) throws SigningException {
        PGPSignatureGenerator signatureGenerator = initSignatureGenerator(context, keyring);

        sign(context, signatureGenerator, pair.inputFile, pair.signatureFile);
    }

    public static PGPSignatureGenerator initSignatureGenerator(JReleaserContext context, Keyring keyring) throws SigningException {
        Signing signing = context.getModel().getSigning();
        if (context.isDryrun() && isBlank(signing.getPassphrase())) {
            return null;
        }

        try {
            PGPSecretKey pgpSecretKey = keyring.readSecretKey();

            PGPPrivateKey pgpPrivKey = pgpSecretKey.extractPrivateKey(
                new JcePBESecretKeyDecryptorBuilder()
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .build(signing.getPassphrase().toCharArray()));

            PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
                new JcaPGPContentSignerBuilder(pgpSecretKey.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1)
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME));

            signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, pgpPrivKey);

            return signatureGenerator;
        } catch (PGPException e) {
            throw new SigningException(RB.$("ERROR_unexpected_error_signature_gen"), e);
        }
    }

    public static void sign(JReleaserContext context, PGPSignatureGenerator signatureGenerator, Path input, Path output) throws SigningException {
        context.getLogger().info("{}", context.relativizeToBasedir(input));

        if (null == signatureGenerator) return;

        PGPCompressedDataGenerator compressionStreamGenerator = new PGPCompressedDataGenerator(UNCOMPRESSED);
        try (OutputStream out = createOutputStream(context, output);
             BCPGOutputStream bOut = new BCPGOutputStream(compressionStreamGenerator.open(out));
             InputStream in = newInputStream(input)) {

            byte[] buffer = new byte[8192];
            int length = 0;
            while ((length = in.read(buffer)) >= 0) {
                signatureGenerator.update(buffer, 0, length);
            }

            signatureGenerator.generate().encode(bOut);
            compressionStreamGenerator.close();
        } catch (IOException | PGPException e) {
            throw new SigningException(RB.$("ERROR_unexpected_error_signing", input.toAbsolutePath()), e);
        }
    }

    private static OutputStream createOutputStream(JReleaserContext context, Path output) throws IOException {
        OutputStream out = new BufferedOutputStream(newOutputStream(output.toFile().toPath()));
        if (context.getModel().getSigning().isArmored()) {
            out = new ArmoredOutputStream(out);
        }
        return out;
    }

    public static boolean isValid(JReleaserContext context, FilePair pair) {
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
            if (context.getModel().getSigning().isVerify()) {
                return verify(context, pair);
            } else {
                // force signing as we can't verify
                return false;
            }
        } catch (SigningException e) {
            return false;
        }
    }

    public static boolean isValid(JReleaserContext context, Keyring keyring, SigningUtils.FilePair pair) {
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
            if (context.getModel().getSigning().isVerify()) {
                return verify(context, keyring, pair);
            } else {
                // force signing as we can't verify
                return false;
            }
        } catch (SigningException e) {
            return false;
        }
    }

    public static class FilePair {
        private final Path inputFile;
        private final Path signatureFile;
        private boolean valid;

        public FilePair(Path inputFile, Path signatureFile) {
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
