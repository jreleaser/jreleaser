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
package org.jreleaser.model.validation;

import org.jreleaser.model.Active;
import org.jreleaser.model.Artifactory;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.util.Map;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public abstract class ArtifactoryValidator extends Validator {
    public static void validateArtifactory(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        context.getLogger().debug("artifactory");
        Map<String, Artifactory> artifactories = context.getModel().getUpload().getArtifactories();

        for (Map.Entry<String, Artifactory> e : artifactories.entrySet()) {
            Artifactory artifactory = e.getValue();
            if (isBlank(artifactory.getName())) {
                artifactory.setName(e.getKey());
            }
            validateArtifactory(context, mode, artifactory, errors);
        }
    }

    private static void validateArtifactory(JReleaserContext context, JReleaserContext.Mode mode, Artifactory artifactory, Errors errors) {
        context.getLogger().debug("artifactory.{}", artifactory.getName());

        if (!artifactory.isActiveSet()) {
            artifactory.setActive(Active.NEVER);
        }
        if (!artifactory.resolveEnabled(context.getModel().getProject())) return;

        if (!artifactory.isArtifacts() && !artifactory.isFiles() && !artifactory.isSignatures()) {
            artifactory.disable();
            return;
        }

        if (isBlank(artifactory.getName())) {
            errors.configuration("artifactory.name must not be blank");
            return;
        }

        if (isBlank(artifactory.getTarget())) {
            errors.configuration("artifactory.target must not be blank.");
        }

        artifactory.setUsername(
            checkProperty(context.getModel().getEnvironment(),
                "ARTIFACTORY_" + Env.toVar(artifactory.getName()) + "_USERNAME",
                "artifactory.username",
                artifactory.getUsername(),
                new Errors()));

        if (isNotBlank(artifactory.getResolvedUsername())) {
            artifactory.setPassword(
                checkProperty(context.getModel().getEnvironment(),
                    "ARTIFACTORY_" + Env.toVar(artifactory.getName()) + "_PASSWORD",
                    "artifactory.password",
                    artifactory.getPassword(),
                    errors));
        } else {
            artifactory.setToken(
                checkProperty(context.getModel().getEnvironment(),
                    "ARTIFACTORY_" + Env.toVar(artifactory.getName()) + "_TOKEN",
                    "artifactory.token",
                    artifactory.getToken(),
                    errors));
        }

        if (artifactory.getConnectTimeout() <= 0 || artifactory.getConnectTimeout() > 300) {
            artifactory.setConnectTimeout(20);
        }
        if (artifactory.getReadTimeout() <= 0 || artifactory.getReadTimeout() > 300) {
            artifactory.setReadTimeout(60);
        }
    }
}
