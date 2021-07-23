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
package org.jreleaser.model.validation;

import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Packagers;
import org.jreleaser.model.Project;
import org.jreleaser.model.RepositoryTap;
import org.jreleaser.model.RepositoryTool;
import org.jreleaser.model.Sdkman;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.Sdkman.SDKMAN_CONSUMER_KEY;
import static org.jreleaser.model.Sdkman.SDKMAN_CONSUMER_TOKEN;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class PackagersValidator extends Validator {
    public static void validatePackagers(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        if (mode != JReleaserContext.Mode.FULL) {
            return;
        }

        context.getLogger().debug("packagers");

        JReleaserModel model = context.getModel();
        Packagers packagers = model.getPackagers();
        Project project = model.getProject();

        packagers.getBrew().resolveEnabled(project);
        validatePackager(context,
            packagers.getBrew(),
            packagers.getBrew().getTap(),
            errors);

        packagers.getChocolatey().resolveEnabled(project);
        validatePackager(context,
            packagers.getChocolatey(),
            packagers.getChocolatey().getBucket(),
            errors);

        packagers.getDocker().resolveEnabled(project);
        validatePackager(context,
            packagers.getDocker(),
            packagers.getDocker().getRepository(),
            errors);

        if (!packagers.getDocker().getSpecs().isEmpty()) {
            errors.configuration("docker.specs can only be defined inside distributions.");
        }

        packagers.getDocker().resolveEnabled(project);
        validatePackager(context,
            packagers.getJbang(),
            packagers.getJbang().getCatalog(),
            errors);

        packagers.getScoop().resolveEnabled(project);
        validatePackager(context,
            packagers.getScoop(),
            packagers.getScoop().getBucket(),
            errors);

        if (isBlank(packagers.getScoop().getBucket().getName())) {
            packagers.getScoop().getBucket().setName("scoop-" + model.getRelease().getGitService().getOwner());
        }
        packagers.getScoop().getBucket().setBasename("scoop-" + model.getRelease().getGitService().getOwner());

        packagers.getSnap().resolveEnabled(project);
        validatePackager(context,
            packagers.getSnap(),
            packagers.getSnap().getSnap(),
            errors);

        validateSdkman(context, packagers.getSdkman(), errors);
    }

    private static void validateSdkman(JReleaserContext context, Sdkman tool, Errors errors) {
        tool.setConsumerKey(
            checkProperty(context,
                SDKMAN_CONSUMER_KEY,
                "sdkman.consumerKey",
                tool.getConsumerKey(),
                errors,
                context.isDryrun()));

        tool.setConsumerToken(
            checkProperty(context,
                SDKMAN_CONSUMER_TOKEN,
                "sdkman.consumerToken",
                tool.getConsumerToken(),
                errors,
                context.isDryrun()));

        if (tool.getConnectTimeout() <= 0 || tool.getConnectTimeout() > 300) {
            tool.setConnectTimeout(20);
        }
        if (tool.getReadTimeout() <= 0 || tool.getReadTimeout() > 300) {
            tool.setReadTimeout(60);
        }
    }

    private static void validatePackager(JReleaserContext context,
                                         RepositoryTool tool,
                                         RepositoryTap tap,
                                         Errors errors) {
        GitService service = context.getModel().getRelease().getGitService();
        validateCommitAuthor(tool, service);
        validateOwner(tap, service);

        tap.setUsername(
            checkProperty(context,
                Env.toVar(tap.getBasename() + "_" + service.getServiceName()) + "_USERNAME",
                "<empty>",
                tap.getUsername(),
                service.getResolvedUsername()));

        tap.setToken(
            checkProperty(context,
                Env.toVar(tap.getBasename() + "_" + service.getServiceName()) + "_TOKEN",
                "<empty>",
                tap.getToken(),
                service.getResolvedToken()));
    }
}