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
package org.jreleaser.engine.context;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Codeberg;
import org.jreleaser.model.GitService;
import org.jreleaser.model.Github;
import org.jreleaser.model.Gitlab;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserModelPrinter;
import org.jreleaser.model.releaser.spi.Commit;
import org.jreleaser.model.releaser.spi.Repository;
import org.jreleaser.sdk.git.GitSdk;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.GitService.BRANCH;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class ModelConfigurer {
    public static void configure(JReleaserContext context) {
        try {
            context.getModel().setCommit(GitSdk.of(context).head());
        } catch (Exception e) {
            context.getLogger().trace(e);
            throw new JReleaserException(RB.$("ERROR_context_configurer_fail_git_head"), e);
        }

        Repository repository = null;
        try {
            repository = GitSdk.of(context).getRemote();
        } catch (Exception e) {
            context.getLogger().trace(e);
            throw new JReleaserException(RB.$("ERROR_context_configurer_fail_git_remote"), e);
        }

        if (isBlank(context.getModel().getProject().getResolvedName())) {
            context.getModel().getProject().setName(repository.getName());
        }

        switch (repository.getKind()) {
            case GITHUB:
                autoConfigureGithub(context, repository);
                break;
            case GITLAB:
                autoConfigureGitlab(context, repository);
                break;
            case CODEBERG:
                autoConfigureCodeberg(context, repository);
                break;
            default:
                autoConfigureOther(context, repository);
        }

        try {
            Errors errors = context.validateModel();

            if (context.getMode() != JReleaserContext.Mode.CHANGELOG) {
                new JReleaserModelPrinter.Plain(context.getLogger().getTracer())
                    .print(context.getModel().asMap(true));
            }

            switch (context.getMode()) {
                case ASSEMBLE:
                    if (errors.hasConfigurationErrors()) {
                        throw new JReleaserException(RB.$("ERROR_context_configurer_jreleaser_misconfigured") +
                            System.lineSeparator() + errors.asString());
                    }
                    break;
                case FULL:
                default:
                    if (errors.hasErrors()) {
                        throw new JReleaserException(RB.$("ERROR_context_configurer_jreleaser_misconfigured") +
                            System.lineSeparator() + errors.asString());
                    }
                    break;
            }
        } catch (JReleaserException e) {
            context.getLogger().trace(e);
            throw e;
        } catch (Exception e) {
            context.getLogger().trace(e);
            throw new JReleaserException(RB.$("ERROR_context_configurer_jreleaser_misconfigured"), e);
        }
    }

    private static void autoConfigureGithub(JReleaserContext context, Repository repository) {
        GitService service = context.getModel().getRelease().getGitService();

        if (service != null) {
            if (!(service instanceof Github)) {
                context.getLogger().warn(RB.$("ERROR_context_configurer_detected_git"), "github", service.getServiceName());
            }
        } else {
            context.getModel().getRelease().setGithub(new Github());
        }

        fillGitProperties(context.getModel().getRelease().getGitService(),
            repository, context.getModel().getCommit());
    }

    private static void autoConfigureGitlab(JReleaserContext context, Repository repository) {
        GitService service = context.getModel().getRelease().getGitService();

        if (service != null) {
            if (!(service instanceof Gitlab)) {
                context.getLogger().warn(RB.$("ERROR_context_configurer_detected_git"), "gitlab", service.getServiceName());
            }
        } else {
            context.getModel().getRelease().setGitlab(new Gitlab());
        }

        fillGitProperties(context.getModel().getRelease().getGitService(),
            repository, context.getModel().getCommit());
    }

    private static void autoConfigureCodeberg(JReleaserContext context, Repository repository) {
        GitService service = context.getModel().getRelease().getGitService();

        if (service != null) {
            if (!(service instanceof Codeberg)) {
                context.getLogger().warn(RB.$("ERROR_context_configurer_detected_git"), "codeberg", service.getServiceName());
            }
        } else {
            context.getModel().getRelease().setCodeberg(new Codeberg());
        }

        fillGitProperties(context.getModel().getRelease().getGitService(),
            repository, context.getModel().getCommit());
    }

    private static void autoConfigureOther(JReleaserContext context, Repository repository) {
        GitService service = context.getModel().getRelease().getGitService();

        if (service != null) {
            fillGitProperties(service, repository, context.getModel().getCommit());
        }
    }

    private static void fillGitProperties(GitService service, Repository repository, Commit head) {
        if (isBlank(service.getOwner())) {
            service.setOwner(repository.getOwner());
        }
        if (isBlank(service.getName())) {
            service.setName(repository.getName());
        }
        if (isBlank(Env.resolve(BRANCH, service.getBranch()))) {
            service.setBranch(head.getRefName());
        }
    }
}
