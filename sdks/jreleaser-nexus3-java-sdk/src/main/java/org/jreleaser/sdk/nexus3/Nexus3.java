/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.sdk.nexus3;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext;
import org.jreleaser.model.spi.deploy.maven.Deployable;
import org.jreleaser.model.spi.upload.UploadException;
import org.jreleaser.sdk.commons.ClientUtils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 1.18.0
 */
public class Nexus3 {
    private final JReleaserContext context;
    private final boolean dryrun;
    private final String publishUrl;
    private final String username;
    private final String password;
    private final int connectTimeout;
    private final int readTimeout;

    public Nexus3(JReleaserContext context,
                  String publishUrl,
                  String username,
                  String password,
                  int connectTimeout,
                  int readTimeout,
                  boolean dryrun) {
        this.context = requireNonNull(context, "'context' must not be blank");
        this.publishUrl = requireNonBlank(publishUrl, "'publishUrl' must not be blank").trim();
        this.username = requireNonBlank(username, "'username' must not be blank").trim();
        this.password = requireNonBlank(password, "'password' must not be blank").trim();

        this.dryrun = dryrun;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    public void deploy(String groupId, String artifactId, Set<Deployable> deployables) throws Nexus3Exception {
        context.getLogger().debug(" - " + RB.$("nexus3.deploy.artifact", groupId, artifactId));

        try {
            List<ClientUtils.FormData> data = new ArrayList<>();
            int assetId = 1;
            for (Deployable deployable : deployables) {
                data.add(new ClientUtils.FieldFormData("maven2.asset" + assetId + ".extension", deployable.getExtension()));
                data.add(new ClientUtils.FileFormData("maven2.asset" + assetId, deployable.getLocalPath()));
                if (isNotBlank(deployable.getClassifier())) {
                    data.add(new ClientUtils.FieldFormData("maven2.asset" + assetId + ".classifier", deployable.getClassifier()));
                }
                assetId++;
            }

            Map<String, String> headers = new LinkedHashMap<>();

            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(UTF_8));
            auth = new String(encodedAuth, UTF_8);
            headers.put("Authorization", "Basic " + auth);

            ClientUtils.postData(context.getLogger(),
                publishUrl,
                connectTimeout,
                readTimeout,
                data,
                headers);
        } catch (UploadException e) {
            context.getLogger().error(" x {}:{}", groupId, artifactId, e);
            throw fail(RB.$("ERROR_nexus3_deploy_artifact", groupId, artifactId, e.getMessage()), e);
        }
    }

    private Nexus3Exception fail(String message) {
        return new Nexus3Exception(message);
    }

    private Nexus3Exception fail(String message, Exception e) {
        return new Nexus3Exception(message, e);
    }
}
