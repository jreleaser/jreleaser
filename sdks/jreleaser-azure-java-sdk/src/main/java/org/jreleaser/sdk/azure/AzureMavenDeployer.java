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
package org.jreleaser.sdk.azure;

import feign.form.FormData;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.deploy.DeployException;
import org.jreleaser.model.spi.deploy.maven.Deployable;
import org.jreleaser.model.spi.upload.UploadException;
import org.jreleaser.sdk.commons.AbstractMavenDeployer;
import org.jreleaser.sdk.commons.ClientUtils;

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
 * @since 1.5.0
 */
public class AzureMavenDeployer extends AbstractMavenDeployer<org.jreleaser.model.api.deploy.maven.AzureMavenDeployer, org.jreleaser.model.internal.deploy.maven.AzureMavenDeployer> {
    private org.jreleaser.model.internal.deploy.maven.AzureMavenDeployer deployer;

    public AzureMavenDeployer(JReleaserContext context) {
        super(context);
    }

    @Override
    public org.jreleaser.model.internal.deploy.maven.AzureMavenDeployer getDeployer() {
        return deployer;
    }

    @Override
    public void setDeployer(org.jreleaser.model.internal.deploy.maven.AzureMavenDeployer deployer) {
        this.deployer = deployer;
    }

    @Override
    public String getType() {
        return org.jreleaser.model.api.deploy.maven.AzureMavenDeployer.TYPE;
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
                        context.getBasedir().relativize(localPath), e.getMessage(), e.getMessage()), e);
                }
            }
        }
    }
}
