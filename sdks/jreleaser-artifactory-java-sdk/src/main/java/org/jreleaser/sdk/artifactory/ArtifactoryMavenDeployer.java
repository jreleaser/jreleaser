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
package org.jreleaser.sdk.artifactory;

import feign.form.FormData;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.deploy.DeployException;
import org.jreleaser.model.spi.deploy.maven.Deployable;
import org.jreleaser.model.spi.upload.UploadException;
import org.jreleaser.sdk.commons.AbstractMavenDeployer;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.ChecksumUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public class ArtifactoryMavenDeployer extends AbstractMavenDeployer<org.jreleaser.model.api.deploy.maven.ArtifactoryMavenDeployer, org.jreleaser.model.internal.deploy.maven.ArtifactoryMavenDeployer> {
    private org.jreleaser.model.internal.deploy.maven.ArtifactoryMavenDeployer deployer;

    public ArtifactoryMavenDeployer(JReleaserContext context) {
        super(context);
    }

    @Override
    public org.jreleaser.model.internal.deploy.maven.ArtifactoryMavenDeployer getDeployer() {
        return deployer;
    }

    @Override
    public void setDeployer(org.jreleaser.model.internal.deploy.maven.ArtifactoryMavenDeployer deployer) {
        this.deployer = deployer;
    }

    @Override
    public String getType() {
        return org.jreleaser.model.api.deploy.maven.ArtifactoryMavenDeployer.TYPE;
    }

    @Override
    public void deploy(String name) throws DeployException {
        Set<Deployable> deployables = collectDeployables();
        if (deployables.isEmpty()) {
            context.getLogger().info(RB.$("artifacts.no.match"));
        }

        String baseUrl = deployer.getResolvedUrl(context.fullProps());
        String username = deployer.getUsername();
        String password = deployer.getPassword();

        if (!baseUrl.endsWith("/")) {
            baseUrl += " ";
        }

        for (Deployable deployable : deployables) {
            if (deployable.isChecksum()) continue;
            Path localPath = Paths.get(deployable.getStagingRepository(), deployable.getPath(), deployable.getFilename());
            context.getLogger().info(" - {}", deployable.getFilename());

            if (!context.isDryrun()) {
                try {
                    FormData data = ClientUtils.toFormData(localPath);

                    Map<String, String> headers = new LinkedHashMap<>();
                    switch (deployer.resolveAuthorization()) {
                        case BASIC:
                            String auth = username + ":" + password;
                            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(UTF_8));
                            auth = new String(encodedAuth, UTF_8);
                            headers.put("Authorization", "Basic " + auth);
                            break;
                        case BEARER:
                            headers.put("Authorization", "Bearer " + password);
                            break;
                        default:
                            // noop
                    }

                    headers.put("X-Checksum-Deploy", "false");
                    headers.put("X-Checksum-Sha1", ChecksumUtils.checksum(Algorithm.SHA_1, data.getData()));
                    headers.put("X-Checksum-Sha256", ChecksumUtils.checksum(Algorithm.SHA_256, data.getData()));
                    headers.put("X-Checksum", ChecksumUtils.checksum(Algorithm.MD5, data.getData()));

                    String url = baseUrl + deployable.getFullDeployPath();
                    ClientUtils.putFile(context.getLogger(),
                        url,
                        deployer.getConnectTimeout(),
                        deployer.getReadTimeout(),
                        data,
                        headers);
                } catch (IOException | UploadException e) {
                    context.getLogger().trace(e);
                    throw new DeployException(RB.$("ERROR_unexpected_deploy",
                        context.getBasedir().relativize(localPath)), e);
                }
            }
        }
    }
}
