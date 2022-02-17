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
package org.jreleaser.sdk.git;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.UnsupportedSigningFormatException;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.GpgConfig;
import org.eclipse.jgit.lib.GpgObjectSigner;
import org.eclipse.jgit.lib.GpgSignature;
import org.eclipse.jgit.lib.GpgSigner;
import org.eclipse.jgit.lib.ObjectBuilder;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Signing;
import org.jreleaser.util.command.CommandException;
import org.jreleaser.util.signing.GpgCommandSigner;
import org.jreleaser.util.signing.Keyring;
import org.jreleaser.util.signing.SigningException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.util.Iterator;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserGpgSigner extends GpgSigner implements GpgObjectSigner {
    static {
        // replace BC provider with our version
        Provider bcProvider = Security.getProvider("BC");
        Security.removeProvider("BC");
        Security.setProperty("crypto.policy", "unlimited");
        Security.addProvider(bcProvider != null ? bcProvider : new BouncyCastleProvider());
    }

    private final JReleaserContext context;
    private final boolean enabled;

    public JReleaserGpgSigner(JReleaserContext context, boolean enabled) {
        this.context = context;
        this.enabled = enabled;
    }

    @Override
    public boolean canLocateSigningKey(String gpgSigningKey, PersonIdent committer, CredentialsProvider credentialsProvider, GpgConfig config)
        throws CanceledException, UnsupportedSigningFormatException {
        return enabled;
    }

    @Override
    public boolean canLocateSigningKey(String gpgSigningKey, PersonIdent committer, CredentialsProvider credentialsProvider)
        throws CanceledException {
        return enabled;
    }

    @Override
    public void sign(CommitBuilder commit, String gpgSigningKey, PersonIdent committer, CredentialsProvider credentialsProvider)
        throws CanceledException {
        if (!enabled) return;

        try {
            signObject(commit, gpgSigningKey, committer, credentialsProvider, null);
        } catch (UnsupportedSigningFormatException ignored) {
            // noop
        }
    }

    @Override
    public void signObject(ObjectBuilder object, String gpgSigningKey, PersonIdent committer, CredentialsProvider credentialsProvider, GpgConfig config)
        throws CanceledException, UnsupportedSigningFormatException {
        if (!enabled) return;

        try {
            if (context.getModel().getSigning().resolveMode() == Signing.Mode.COMMAND) {
                new CommandSigner(context).sign(object);
            } else {
                new BCSigner(context, committer).sign(object);
            }
        } catch (SigningException e) {
            throw new JGitInternalException(e.getMessage(), e);
        }
    }

    private interface Signer {
        void sign(ObjectBuilder object) throws SigningException;
    }

    private static abstract class AbstractSigner implements Signer {
        protected final JReleaserContext context;

        protected AbstractSigner(JReleaserContext context) {
            this.context = context;
        }
    }

    private static class CommandSigner extends AbstractSigner {
        protected CommandSigner(JReleaserContext context) {
            super(context);
        }

        @Override
        public void sign(ObjectBuilder object) throws SigningException {
            try {
                Signing signing = context.getModel().getSigning();
                GpgCommandSigner cmd = new GpgCommandSigner(context.getLogger());
                cmd.setExecutable(signing.getCommand().getExecutable());
                cmd.setPassphrase(signing.getResolvedPassphrase());
                cmd.setHomeDir(signing.getCommand().getHomeDir());
                cmd.setKeyName(signing.getCommand().getKeyName());
                cmd.setPublicKeyring(signing.getCommand().getPublicKeyring());
                cmd.setDefaultKeyring(signing.getCommand().isDefaultKeyring());
                cmd.setArgs(signing.getCommand().getArgs());
                object.setGpgSignature(new GpgSignature(cmd.sign(object.build())));
            } catch (IOException | CommandException e) {
                throw new SigningException(e.getMessage(), e);
            }
        }
    }

    private static class BCSigner extends AbstractSigner {
        private final PersonIdent committer;

        protected BCSigner(JReleaserContext context, PersonIdent committer) {
            super(context);
            this.committer = committer;
        }

        public void sign(ObjectBuilder object) throws SigningException {
            Keyring keyring = context.createKeyring();
            PGPSignatureGenerator signatureGenerator = initSignatureGenerator(context.getModel().getSigning(), keyring);
            adjustCommitterId(signatureGenerator, committer, keyring);
            signObject(signatureGenerator, object);
        }

        private PGPSignatureGenerator initSignatureGenerator(Signing signing, Keyring keyring) throws SigningException {
            try {
                PGPSecretKey secretKey = keyring.getSecretKey();

                PGPPrivateKey privateKey = secretKey.extractPrivateKey(
                    new JcePBESecretKeyDecryptorBuilder()
                        .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                        .build(signing.getResolvedPassphrase().toCharArray()));

                PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
                    new JcaPGPContentSignerBuilder(secretKey.getPublicKey().getAlgorithm(), PGPUtil.SHA256)
                        .setProvider(BouncyCastleProvider.PROVIDER_NAME));

                signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, privateKey);

                return signatureGenerator;
            } catch (PGPException e) {
                throw new SigningException(RB.$("ERROR_unexpected_error_signature_gen"), e);
            }
        }

        private void adjustCommitterId(PGPSignatureGenerator signatureGenerator, PersonIdent committer, Keyring keyring)
            throws SigningException {
            PGPPublicKey publicKey = keyring.readPublicKey();
            PGPSignatureSubpacketGenerator subpackets = new PGPSignatureSubpacketGenerator();
            subpackets.setIssuerFingerprint(false, publicKey);

            String userId = committer.getEmailAddress();
            Iterator<String> userIds = publicKey.getUserIDs();
            if (userIds.hasNext()) {
                String keyUserId = userIds.next();
                if (isNotBlank(keyUserId)
                    && (isBlank(userId) || !keyUserId.contains(userId))) {
                    userId = extractSignerId(keyUserId);
                }
            }

            if (isNotBlank(userId)) {
                subpackets.addSignerUserID(false, userId);
            }

            signatureGenerator.setHashedSubpackets(subpackets.generate());
        }

        private String extractSignerId(String pgpUserId) {
            int from = pgpUserId.indexOf('<');
            if (from >= 0) {
                int to = pgpUserId.indexOf('>', from + 1);
                if (to > from + 1) {
                    return pgpUserId.substring(from + 1, to);
                }
            }
            return pgpUserId;
        }

        private void signObject(PGPSignatureGenerator signatureGenerator, ObjectBuilder object) throws SigningException {
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                try (BCPGOutputStream out = new BCPGOutputStream(new ArmoredOutputStream(buffer))) {
                    signatureGenerator.update(object.build());
                    signatureGenerator.generate().encode(out);
                }
                object.setGpgSignature(new GpgSignature(buffer.toByteArray()));
            } catch (IOException | PGPException e) {
                throw new SigningException(e.getMessage(), e);
            }
        }
    }
}
