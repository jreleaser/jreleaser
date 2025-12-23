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

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.signing.SigningException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.signing.Signing;
import org.jreleaser.model.internal.signing.SigningTool;
import org.jreleaser.sdk.signing.SigningUtils;
import org.jreleaser.sdk.tool.Minisign;
import org.jreleaser.sdk.tool.ToolException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Andres Almiray
 * @since 1.22.0
 */
public final class MinisignSigner extends AbstractSigner {
    public MinisignSigner(JReleaserContext context, SigningTool tool) {
        super(context, tool);
    }

    public void sign() throws SigningException {
        Signing signing = context.getModel().getSigning();

        Minisign minisign = new Minisign(context.asImmutable(), signing.getMinisign().getVersion());
        try {
            if (!minisign.setup()) {
                context.getLogger().warn(RB.$("tool_unavailable", "minisign"));
                return;
            }
        } catch (ToolException e) {
            throw new SigningException(e.getMessage(), e);
        }

        Path secretKeyFile = signing.getMinisign().getResolvedSecretKeyFilePath(context);
        Path publicKeyFile = signing.getMinisign().getResolvedPublicKeyFilePath(context);
        String password = signing.getMinisign().getPassphrase();

        List<SigningUtils.FilePair> files = collectArtifacts(pair -> isValid(minisign, publicKeyFile, pair));
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

        sign(files, minisign, secretKeyFile, password);
        verify(files, minisign, publicKeyFile);
    }

    private void sign(List<SigningUtils.FilePair> files, Minisign minisign, Path privateKeyFile, String password) throws SigningException {
        Path signaturesDirectory = context.getSignaturesDirectory();

        try {
            Files.createDirectories(signaturesDirectory);
        } catch (IOException e) {
            throw new SigningException(RB.$("ERROR_signing_create_signature_dir"), e);
        }

        context.getLogger().debug(RB.$("signing.signing.files"),
            files.size(), context.relativizeToBasedir(signaturesDirectory));

        for (SigningUtils.FilePair pair : files) {
            minisign.sign(privateKeyFile, password, pair.getInputFile(), signaturesDirectory);
        }
    }

    private void verify(List<SigningUtils.FilePair> files, Minisign minisign, Path publicKeyFile) throws SigningException {
        context.getLogger().debug(RB.$("signing.verify.signatures"), files.size());

        context.getLogger().setPrefix("verify");
        try {
            for (SigningUtils.FilePair pair : files) {
                minisign.verify(publicKeyFile, pair.getSignatureFile(), pair.getInputFile());
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

    private boolean isValid(Minisign minisign, Path publicKeyFile, SigningUtils.FilePair pair) {
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
            minisign.verify(publicKeyFile, pair.getSignatureFile(), pair.getInputFile());
            return true;
        } catch (SigningException e) {
            return false;
        }
    }
}
