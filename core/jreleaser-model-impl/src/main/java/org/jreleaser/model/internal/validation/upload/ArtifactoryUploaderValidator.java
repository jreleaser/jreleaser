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
package org.jreleaser.model.internal.validation.upload;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Http;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.servers.HttpServer;
import org.jreleaser.model.internal.upload.ArtifactoryUploader;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.model.internal.validation.common.HttpValidator.validateHttp;
import static org.jreleaser.model.internal.validation.common.ServerValidator.validateHost;
import static org.jreleaser.model.internal.validation.common.ServerValidator.validateTimeout;
import static org.jreleaser.model.internal.validation.common.Validator.mergeErrors;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.util.CollectionUtils.listOf;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public final class ArtifactoryUploaderValidator {
    private ArtifactoryUploaderValidator() {
        // noop
    }

    public static void validateArtifactory(JReleaserContext context, Mode mode, Errors errors) {
        Map<String, ArtifactoryUploader> artifactory = context.getModel().getUpload().getArtifactory();
        if (!artifactory.isEmpty()) context.getLogger().debug("upload.artifactory");

        for (Map.Entry<String, ArtifactoryUploader> e : artifactory.entrySet()) {
            e.getValue().setName(e.getKey());
            if (mode.validateConfig()) {
                Errors incoming = new Errors();
                validateArtifactory(context, e.getValue(), incoming);
                mergeErrors(context, errors, incoming, e.getValue());
            }
        }
    }

    private static void validateArtifactory(JReleaserContext context, ArtifactoryUploader uploader, Errors errors) {
        context.getLogger().debug("upload.artifactory.{}", uploader.getName());

        resolveActivatable(context, uploader,
            listOf("upload.artifactory." + uploader.getName(), "upload.artifactory"),
            "NEVER");
        if (!uploader.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (!uploader.isArtifacts() && !uploader.isFiles() && !uploader.isSignatures()) {
            errors.warning(RB.$("WARNING.validation.uploader.no.artifacts", uploader.getType(), uploader.getName()));
            context.getLogger().debug(RB.$("validation.disabled.no.artifacts"));
            uploader.disable();
            return;
        }

        if (uploader.getRepositories().isEmpty()) {
            errors.configuration(RB.$("validation_artifactory_no_repositories", "artifactory." + uploader.getName()));
            context.getLogger().debug(RB.$("validation.disabled.no.repositories"));
            uploader.disable();
            return;
        }

        String serverName = uploader.getServerRef();
        HttpServer server = context.getModel().getServers().httpFor(serverName);
        validateHttp(context, uploader, server, "upload", "artifactory", uploader.getName(), errors);
        validateTimeout(context, uploader, server, "upload", "artifactory", uploader.getName(), errors, true);
        validateHost(context, uploader, server, "upload", "artifactory", uploader.getName(), errors, false);

        String baseKey1 = "upload.artifactory." + uploader.getName();

        if (uploader.resolveAuthorization() == Http.Authorization.NONE) {
            errors.configuration(RB.$("validation_value_cannot_be", baseKey1 + ".authorization", "NONE"));
            context.getLogger().debug(RB.$("validation.disabled.error"));
            uploader.disable();
        }

        for (ArtifactoryUploader.ArtifactoryRepository repository : uploader.getRepositories()) {
            resolveActivatable(context, repository, baseKey1 + ".repository", "");
            if (!repository.isActiveSet()) {
                repository.setActive(uploader.getActive());
            }
            repository.resolveEnabledWithSnapshot(context.getModel().getProject());
        }

        if (uploader.getRepositories().stream().noneMatch(ArtifactoryUploader.ArtifactoryRepository::isEnabled)) {
            errors.warning(RB.$("validation_artifactory_disabled_repositories", baseKey1));
            context.getLogger().debug(RB.$("validation.disabled.no.repositories"));
            uploader.disable();
        }
    }
}
