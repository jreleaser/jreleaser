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
import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Signing;
import org.jreleaser.model.util.Artifacts;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.signing.Keyring;
import org.jreleaser.util.signing.SigningException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.bouncycastle.bcpg.CompressionAlgorithmTags.UNCOMPRESSED;
import static org.jreleaser.model.Signing.KEY_SKIP_SIGNING;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Signer {
    static {
        // replace BC provider with our version
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.setProperty("crypto.policy", "unlimited");
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void sign(JReleaserContext context) throws SigningException {
        context.getLogger().info("Signing files");
        if (!context.getModel().getSigning().isEnabled()) {
            context.getLogger().info("Signing is not enabled. Skipping");
            return;
        }

        context.getLogger().increaseIndent();
        context.getLogger().setPrefix("sign");

        Keyring keyring = context.createKeyring();

        List<FilePair> files = collectArtifacts(context, keyring);
        if (files.isEmpty()) {
            context.getLogger().info("No files configured for signing. Skipping");
            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
            return;
        }

        files = files.stream()
            .filter(FilePair::isInvalid)
            .collect(Collectors.toList());

        if (files.isEmpty()) {
            context.getLogger().info("All signatures are up-to-date and valid. Skipping");
            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
            return;
        }

        sign(context, keyring, files);
        verify(context, keyring, files);

        context.getLogger().restorePrefix();
        context.getLogger().decreaseIndent();
    }

    private static void verify(JReleaserContext context, Keyring keyring, List<FilePair> files) throws SigningException {
        context.getLogger().debug("verifying {} signatures", files.size());

        for (FilePair pair : files) {
            pair.setValid(verify(context, keyring, pair));

            if (!pair.isValid()) {
                throw new SigningException("Could not verify file " +
                    context.relativizeToBasedir(pair.inputFile) + " with signature " +
                    context.relativizeToBasedir(pair.signatureFile));
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
            throw new SigningException("Error when verifying signature of " +
                context.relativizeToBasedir(filePair.inputFile), e);
        } finally {
            context.getLogger().restorePrefix();
        }
    }

    private static void sign(JReleaserContext context, Keyring keyring, List<FilePair> files) throws SigningException {
        Path signaturesDirectory = context.getSignaturesDirectory();

        try {
            Files.createDirectories(signaturesDirectory);
        } catch (IOException e) {
            throw new SigningException("Could not create signatures directory", e);
        }

        context.getLogger().debug("signing {} files into {}",
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
            throw new SigningException("Unexpected error when initializing signature generator", e);
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

            int ch;
            while ((ch = in.read()) >= 0) {
                signatureGenerator.update((byte) ch);
            }

            signatureGenerator.generate().encode(bOut);

            compressionStreamGenerator.close();

            in.close();
            out.flush();
            out.close();
        } catch (IOException | PGPException e) {
            throw new SigningException("Unexpected error when signing " + input.toAbsolutePath(), e);
        }
    }

    private static List<FilePair> collectArtifacts(JReleaserContext context, Keyring keyring) {
        List<FilePair> files = new ArrayList<>();

        Path signaturesDirectory = context.getSignaturesDirectory();
        String extension = context.getModel().getSigning().isArmored() ? ".asc" : ".sig";

        if (context.getModel().getSigning().isFiles()) {
            for (Artifact artifact : Artifacts.resolveFiles(context)) {
                if (!artifact.isActive() || artifact.extraPropertyIsTrue(KEY_SKIP_SIGNING)) continue;
                Path input = artifact.getEffectivePath(context);
                Path output = signaturesDirectory.resolve(input.getFileName().toString().concat(extension));
                FilePair pair = new FilePair(input, output);
                pair.setValid(isValid(context, keyring, pair));
                files.add(pair);
            }
        }

        if (context.getModel().getSigning().isArtifacts()) {
            for (Distribution distribution : context.getModel().getActiveDistributions()) {
                for (Artifact artifact : distribution.getArtifacts()) {
                    if (!artifact.isActive() || artifact.extraPropertyIsTrue(KEY_SKIP_SIGNING)) continue;
                    Path input = artifact.getEffectivePath(context, distribution);
                    Path output = signaturesDirectory.resolve(input.getFileName().toString().concat(extension));
                    FilePair pair = new FilePair(input, output);
                    pair.setValid(isValid(context, keyring, pair));
                    files.add(pair);
                }
            }
        }

        if (context.getModel().getSigning().isChecksums()) {
            for (Algorithm algorithm : context.getModel().getChecksum().getAlgorithms()) {
                Path checksums = context.getChecksumsDirectory()
                    .resolve(context.getModel().getChecksum().getResolvedName(context, algorithm));
                if (Files.exists(checksums)) {
                    Path output = signaturesDirectory.resolve(checksums.getFileName().toString().concat(extension));
                    FilePair pair = new FilePair(checksums, output);
                    pair.setValid(isValid(context, keyring, pair));
                    files.add(pair);
                }
            }
        }

        return files;
    }

    private static boolean isValid(JReleaserContext context, Keyring keyring, FilePair pair) {
        if (Files.notExists(pair.getSignatureFile())) {
            context.getLogger().debug("signature does not exist: {}",
                context.relativizeToBasedir(pair.getSignatureFile()));
            return false;
        }

        if (pair.inputFile.toFile().lastModified() > pair.signatureFile.toFile().lastModified()) {
            context.getLogger().debug("{} is newer than {}",
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

    private static void deleteDirectory(Path outputDirectory) {
        File dir = outputDirectory.toFile();
        if (!dir.exists()) return;
        for (File file : dir.listFiles()) {
            file.delete();
        }
        dir.delete();
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
