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
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Sign;
import org.jreleaser.util.Logger;

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
import java.util.Iterator;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Signer {
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.setProperty("crypto.policy", "unlimited");
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static void sign(Logger logger, JReleaserModel model, Path outputDirectory) throws SigningException {
        if (!model.getSign().isEnabled()) {
            logger.info("Signing is not enabled");
            return;
        }

        PGPSignatureGenerator signatureGenerator = initSignatureGenerator(model.getSign());
        List<Path> paths = collectArtifactsForSigning(model, outputDirectory);
        sign(logger, signatureGenerator, paths, model.getSign(), outputDirectory.resolve("signatures"));
    }

    private static PGPSignatureGenerator initSignatureGenerator(Sign sign) throws SigningException {
        File keyRingFile = Paths.get(sign.getKeyRingFile()).toFile();
        if (!keyRingFile.exists()) {
            throw new SigningException("sign.keyRingFile does not exist");
        }

        try {
            PGPSecretKey pgpSec = readSecretKey(new FileInputStream(keyRingFile));
            PGPPrivateKey privateKey = pgpSec.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder()
                .setProvider("BC")
                .build(sign.getResolvedPassphrase().toCharArray()));
            PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
                new JcaPGPContentSignerBuilder(pgpSec.getPublicKey().getAlgorithm(), PGPUtil.SHA256)
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME));
            signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, privateKey);

            Iterator<String> userIds = pgpSec.getPublicKey().getUserIDs();
            if (userIds.hasNext()) {
                PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
                spGen.addSignerUserID(false, userIds.next());
                signatureGenerator.setHashedSubpackets(spGen.generate());
            }

            return signatureGenerator;
        } catch (IOException | PGPException e) {
            throw new SigningException("Unexpected error when initializing signature generator", e);
        }
    }

    private static PGPSecretKey readSecretKey(InputStream input) throws SigningException {
        PGPSecretKeyRingCollection pgpSec = null;
        try {
            pgpSec = new PGPSecretKeyRingCollection(
                PGPUtil.getDecoderStream(input),
                new JcaKeyFingerprintCalculator());
        } catch (IOException | PGPException e) {
            throw new SigningException("Unexpected error decoding sign.keyRingFile");
        }

        Iterator<PGPSecretKeyRing> keyRingIter = pgpSec.getKeyRings();
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

        throw new SigningException("Can't find signing key in sign.keyRingFile");
    }

    private static List<Path> collectArtifactsForSigning(JReleaserModel model, Path outputDirectory) {
        List<Path> paths = new ArrayList<>();

        for (Distribution distribution : model.getDistributions().values()) {
            for (Artifact artifact : distribution.getArtifacts()) {
                paths.add(Paths.get(artifact.getPath()));
            }
        }
        Path checksums = outputDirectory.resolve("checksums").resolve("checksums.txt");
        if (checksums.toFile().exists()) {
            paths.add(checksums);
        }

        return paths;
    }

    private static void sign(Logger logger, PGPSignatureGenerator signatureGenerator, List<Path> paths, Sign sign, Path outputDirectory) throws SigningException {
        try {
            deleteDirectory(outputDirectory);
            Files.createDirectories(outputDirectory);
        } catch (IOException e) {
            throw new SigningException("Could not create signatures directory", e);
        }

        logger.debug("Signing {} files into {}", paths.size(), outputDirectory.toAbsolutePath());
        for (Path input : paths) {
            sign(logger, signatureGenerator, input, outputDirectory.resolve(input.getFileName()), sign.isArmored());
        }
    }

    private static void deleteDirectory(Path outputDirectory) throws IOException {
        File dir = outputDirectory.toFile();
        if (!dir.exists()) return;
        for (File file : dir.listFiles()) {
            file.delete();
        }
        dir.delete();
    }

    private static void sign(Logger logger, PGPSignatureGenerator signatureGenerator, Path input, Path output, boolean armored) throws SigningException {
        try {
            String extension = armored ? ".asc" : ".bpg";
            logger.debug("Signing {} into {}", input.toAbsolutePath(), output.toAbsolutePath() + extension);

            OutputStream out = new FileOutputStream(output.toFile() + extension);
            if (armored) {
                out = new ArmoredOutputStream(out);
            }
            BCPGOutputStream bOut = new BCPGOutputStream(out);

            signatureGenerator.generateOnePassVersion(false).encode(bOut);

            PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
            OutputStream lOut = literalDataGenerator.open(bOut, PGPLiteralData.BINARY, input.toFile());
            FileInputStream in = new FileInputStream(input.toFile());

            int ch;
            while ((ch = in.read()) >= 0) {
                lOut.write(ch);
                signatureGenerator.update((byte) ch);
            }

            literalDataGenerator.close();
            signatureGenerator.generate().encode(bOut);
            out.close();
        } catch (IOException | PGPException e) {
            throw new SigningException("Unexpected error when signing " + input.toAbsolutePath(), e);
        }
    }
}
