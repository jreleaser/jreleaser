/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
package org.jreleaser.signer;

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
import org.jreleaser.util.signing.InMemoryKeyring;
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
import java.nio.file.Paths;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import static org.bouncycastle.bcpg.CompressionAlgorithmTags.UNCOMPRESSED;
import static org.jreleaser.util.Logger.DEBUG_TAB;

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
        if (!context.getModel().getSign().isEnabled()) {
            context.getLogger().info("Signing is not enabled");
            return;
        }

        InMemoryKeyring keyring = createInMemoryKeyring(context.getModel().getSign());

        List<Path> paths = collectArtifactsForSigning(context);
        List<FilePair> files = sign(context, keyring, paths);
        verify(context, keyring, files);
    }

    private static InMemoryKeyring createInMemoryKeyring(Signing sign) throws SigningException {
        try {
            InMemoryKeyring keyring = new InMemoryKeyring();
            keyring.addPublicKey(sign.getResolvedPublicKey().getBytes());
            keyring.addSecretKey(sign.getResolvedSecretKey().getBytes());
            return keyring;
        } catch (IOException | PGPException e) {
            throw new SigningException("Could not initialize keyring", e);
        }
    }

    private static void verify(JReleaserContext context, InMemoryKeyring keyring, List<FilePair> files) throws SigningException {
        context.getLogger().debug("Verifying {} signatures", files.size());

        for (FilePair filePair : files) {
            context.getLogger().debug("Verifying:{}{}{}{}{}{}",
                System.lineSeparator(),
                DEBUG_TAB, context.getBasedir().relativize(filePair.inputFile), System.lineSeparator(),
                DEBUG_TAB, context.getBasedir().relativize(filePair.signatureFile));
            if (!verify(context, keyring, filePair)) {
                throw new SigningException("Could not verify file " +
                    context.getBasedir().relativize(filePair.inputFile) + " with signature " +
                    context.getBasedir().relativize(filePair.signatureFile));
            }
        }
    }

    private static boolean verify(JReleaserContext context, InMemoryKeyring keyring, FilePair filePair) throws SigningException {
        try {
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
                context.getBasedir().relativize(filePair.inputFile), e);
        }
    }

    private static List<FilePair> sign(JReleaserContext context, InMemoryKeyring keyring, List<Path> paths) throws SigningException {
        Path signaturesDirectory = context.getSignaturesDirectory();

        try {
            deleteDirectory(signaturesDirectory);
            Files.createDirectories(signaturesDirectory);
        } catch (IOException e) {
            throw new SigningException("Could not create signatures directory", e);
        }

        context.getLogger().debug("Signing {} files into {}",
            paths.size(), context.getBasedir().relativize(signaturesDirectory));

        PGPSignatureGenerator signatureGenerator = initSignatureGenerator(context.getModel().getSign(), keyring);

        List<FilePair> files = new ArrayList<>();
        String extension = context.getModel().getSign().isArmored() ? ".asc" : ".sig";
        for (Path input : paths) {
            Path output = signaturesDirectory.resolve(input.getFileName().toString().concat(extension));
            sign(context, signatureGenerator, input, output);
            files.add(new FilePair(input, output));
        }

        return files;
    }

    private static PGPSignatureGenerator initSignatureGenerator(Signing signing, InMemoryKeyring keyring) throws SigningException {
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
            context.getLogger().debug("Signing:{}{}{}{}{}{}",
                System.lineSeparator(),
                DEBUG_TAB, context.getBasedir().relativize(input), System.lineSeparator(),
                DEBUG_TAB, context.getBasedir().relativize(output));

            OutputStream out = new BufferedOutputStream(new FileOutputStream(output.toFile()));
            if (context.getModel().getSign().isArmored()) {
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

    private static List<Path> collectArtifactsForSigning(JReleaserContext context) {
        List<Path> paths = new ArrayList<>();

        for (Artifact artifact : context.getModel().getFiles()) {
            paths.add(context.getBasedir().resolve(Paths.get(artifact.getPath())).normalize());
        }

        for (Distribution distribution : context.getModel().getDistributions().values()) {
            for (Artifact artifact : distribution.getArtifacts()) {
                paths.add(context.getBasedir().resolve(Paths.get(artifact.getPath())).normalize());
            }
        }

        Path checksums = context.getChecksumsDirectory().resolve("checksums.txt");
        if (checksums.toFile().exists()) {
            paths.add(checksums);
        }

        return paths;
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
    }
}
