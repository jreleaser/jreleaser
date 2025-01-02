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
package org.jreleaser.model.internal.validation.packagers;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.model.internal.packagers.Packagers;
import org.jreleaser.model.internal.packagers.RepositoryPackager;
import org.jreleaser.model.internal.packagers.RepositoryTap;
import org.jreleaser.model.internal.packagers.SdkmanPackager;
import org.jreleaser.model.internal.packagers.TemplatePackager;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.release.Releaser;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateCommitAuthor;
import static org.jreleaser.model.internal.validation.common.Validator.validateOwner;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class PackagersValidator {
    private PackagersValidator() {
        // noop
    }

    public static void validatePackagers(JReleaserContext context, Mode mode, Errors errors) {
        if (!mode.validateConfig()) {
            return;
        }
        context.getLogger().debug("packagers");

        JReleaserModel model = context.getModel();
        Packagers<?> packagers = model.getPackagers();
        Project project = model.getProject();
        Releaser<?> gitService = model.getRelease().getReleaser();

        validatePackager(context,
            packagers.getAppImage(),
            packagers.getAppImage().getRepository()
        );
        if (packagers.getAppImage().getScreenshots().isEmpty()) {
            packagers.getAppImage().setScreenshots(project.getScreenshots());
        }
        if (packagers.getAppImage().getIcons().isEmpty()) {
            packagers.getAppImage().setIcons(project.getIcons());
        }
        if (isBlank(packagers.getAppImage().getDeveloperName())) {
            packagers.getAppImage().setDeveloperName(String.join(", ", project.getAuthors()));
        }

        validatePackager(context,
            packagers.getAsdf(),
            packagers.getAsdf().getRepository()
        );

        validatePackager(context,
            packagers.getBrew(),
            packagers.getBrew().getRepository()
        );

        validatePackager(context,
            packagers.getChocolatey(),
            packagers.getChocolatey().getRepository()
        );

        validatePackager(context,
            packagers.getDocker(),
            packagers.getDocker().getPackagerRepository()
        );

        if (!packagers.getDocker().getSpecs().isEmpty()) {
            errors.configuration(RB.$("validation_packagers_docker_specs"));
        }

        validatePackager(context,
            packagers.getFlatpak(),
            packagers.getFlatpak().getRepository()
        );
        if (packagers.getFlatpak().getScreenshots().isEmpty()) {
            packagers.getFlatpak().setScreenshots(project.getScreenshots());
        }
        if (packagers.getFlatpak().getIcons().isEmpty()) {
            packagers.getFlatpak().setIcons(project.getIcons());
        }
        if (isBlank(packagers.getFlatpak().getDeveloperName())) {
            packagers.getFlatpak().setDeveloperName(String.join(", ", project.getAuthors()));
        }

        validatePackager(context,
            packagers.getGofish(),
            packagers.getGofish().getRepository()
        );

        if (isBlank(packagers.getGofish().getRepository().getName())) {
            packagers.getGofish().getRepository().setName(gitService.getOwner() + "-fish-food");
        }
        packagers.getGofish().getRepository().setTapName(gitService.getOwner() + "-fish-food");

        validatePackager(context,
            packagers.getJbang(),
            packagers.getJbang().getRepository()
        );

        validatePackager(context,
            packagers.getJib(),
            packagers.getJib().getPackagerRepository()
        );

        if (!packagers.getJib().getSpecs().isEmpty()) {
            errors.configuration(RB.$("validation_packagers_jib_specs"));
        }

        validatePackager(context,
            packagers.getMacports(),
            packagers.getMacports().getRepository()
        );
        if (packagers.getMacports().getMaintainers().isEmpty()) {
            packagers.getMacports().getMaintainers().addAll(project.getMaintainers());
        }

        validatePackager(context,
            packagers.getScoop(),
            packagers.getScoop().getRepository()
        );

        if (isBlank(packagers.getScoop().getRepository().getName())) {
            packagers.getScoop().getRepository().setName("scoop-" + gitService.getOwner());
        }
        packagers.getScoop().getRepository().setTapName("scoop-" + gitService.getOwner());

        validatePackager(context,
            packagers.getSnap(),
            packagers.getSnap().getRepository()
        );

        validatePackager(context,
            packagers.getSpec(),
            packagers.getSpec().getRepository()
        );

        if (isBlank(packagers.getSpec().getRepository().getName())) {
            packagers.getSpec().getRepository().setName(gitService.getOwner() + "-spec");
        }
        packagers.getSpec().getRepository().setTapName(gitService.getOwner() + "-spec");

        validateSdkman(context, packagers.getSdkman());

        validatePackager(context,
            packagers.getWinget(),
            packagers.getWinget().getRepository()
        );
    }

    private static void validateSdkman(JReleaserContext context, SdkmanPackager packager) {
        packager.resolveEnabled(context.getModel().getProject());
        validateTimeout(packager);
    }

    private static void validatePackager(JReleaserContext context,
                                         TemplatePackager<?> packager) {
        resolveActivatable(context, packager, "packagers." + packager.getType(), "NEVER");
        packager.resolveEnabled(context.getModel().getProject());
    }

    private static void validatePackager(JReleaserContext context,
                                         RepositoryPackager<?> packager,
                                         RepositoryTap tap) {
        validatePackager(context, packager);

        BaseReleaser<?, ?> service = context.getModel().getRelease().getReleaser();
        validateCommitAuthor(packager, service);
        validateOwner(tap, service);

        packager.getRepositoryTap().resolveEnabled(context.getModel().getProject());

        tap.setUsername(
            checkProperty(context,
                tap.getBasename() + "." + service.getServiceName() + ".username",
                "<empty>",
                tap.getUsername(),
                service.getUsername()));

        tap.setToken(
            checkProperty(context,
                tap.getBasename() + "." + service.getServiceName() + ".token",
                "<empty>",
                tap.getToken(),
                service.getToken()));

        if (isBlank(tap.getTagName())) {
            tap.setTagName(service.getTagName());
        }

        if (isBlank(tap.getBranch())) {
            tap.setBranch("HEAD");
        }

        if (isBlank(tap.getBranchPush())) {
            tap.setBranchPush(tap.getBranch());
        }
    }
}