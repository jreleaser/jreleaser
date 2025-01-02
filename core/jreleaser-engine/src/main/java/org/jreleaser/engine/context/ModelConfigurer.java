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
package org.jreleaser.engine.context;

import org.jreleaser.bundle.RB;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.JReleaserModel;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.release.CodebergReleaser;
import org.jreleaser.model.internal.release.GithubReleaser;
import org.jreleaser.model.internal.release.GitlabReleaser;
import org.jreleaser.model.spi.release.Commit;
import org.jreleaser.model.spi.release.Repository;
import org.jreleaser.sdk.git.GitSdk;
import org.jreleaser.util.Env;

import static org.jreleaser.model.api.release.Releaser.BRANCH;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public final class ModelConfigurer {
    private ModelConfigurer() {
        // noop
    }

    public static void configure(JReleaserContext context) {
        try {
            Commit head = GitSdk.of(context).head();
            context.getModel().setCommit(new JReleaserModel.Commit(head.getShortHash(),
                head.getFullHash(),
                head.getRefName(),
                head.getCommitTime(),
                head.getTimestamp()));
        } catch (Exception e) {
            if (!requiresGit(context)) return;
            context.getLogger().trace(e);
            throw new JReleaserException(RB.$("ERROR_context_configurer_fail_git_head"), e);
        }

        Repository repository = null;
        try {
            repository = GitSdk.of(context).getRemote();
        } catch (Exception e) {
            if (!requiresGit(context)) return;
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
    }

    private static boolean requiresGit(JReleaserContext context) {
        if (context.getCommand() == JReleaserCommand.CONFIG) {
            switch (context.getMode()) {
                case DOWNLOAD:
                case ASSEMBLE:
                case CHANGELOG:
                case ANNOUNCE:
                    return false;
                default:
                    return true;
            }
        }
        return context.getCommand().requiresGit();
    }

    private static void autoConfigureGithub(JReleaserContext context, Repository repository) {
        BaseReleaser<?, ?> service = context.getModel().getRelease().getReleaser();

        if (null != service) {
            if (!(service instanceof GithubReleaser)) {
                context.getModel().getRelease().getReleaser().setMatch(false);
                context.getModel().getRelease().getReleaser().setSkipTag(true);
                context.getLogger().warn(RB.$("ERROR_context_configurer_detected_git"), "github", service.getServiceName());
            }
        } else {
            context.getModel().getRelease().setGithub(new GithubReleaser());
        }

        fillGitProperties(context.getLogger(), context.getModel().getRelease().getReleaser(),
            repository, context.getModel().getCommit());
    }

    private static void autoConfigureGitlab(JReleaserContext context, Repository repository) {
        BaseReleaser<?, ?> service = context.getModel().getRelease().getReleaser();

        if (null != service) {
            if (!(service instanceof GitlabReleaser)) {
                context.getModel().getRelease().getReleaser().setMatch(false);
                context.getModel().getRelease().getReleaser().setSkipTag(true);
                context.getLogger().warn(RB.$("ERROR_context_configurer_detected_git"), "gitlab", service.getServiceName());
            }
        } else {
            context.getModel().getRelease().setGitlab(new GitlabReleaser());
        }

        fillGitProperties(context.getLogger(), context.getModel().getRelease().getReleaser(),
            repository, context.getModel().getCommit());
    }

    private static void autoConfigureCodeberg(JReleaserContext context, Repository repository) {
        BaseReleaser<?, ?> service = context.getModel().getRelease().getReleaser();

        if (null != service) {
            if (!(service instanceof CodebergReleaser)) {
                context.getModel().getRelease().getReleaser().setMatch(false);
                context.getModel().getRelease().getReleaser().setSkipTag(true);
                context.getLogger().warn(RB.$("ERROR_context_configurer_detected_git"), "codeberg", service.getServiceName());
            }
        } else {
            context.getModel().getRelease().setCodeberg(new CodebergReleaser());
        }

        fillGitProperties(context.getLogger(), context.getModel().getRelease().getReleaser(),
            repository, context.getModel().getCommit());
    }

    private static void autoConfigureOther(JReleaserContext context, Repository repository) {
        BaseReleaser<?, ?> service = context.getModel().getRelease().getReleaser();

        if (null != service) {
            fillGitProperties(context.getLogger(), service, repository, context.getModel().getCommit());
        }
    }

    private static void fillGitProperties(JReleaserLogger logger, BaseReleaser<?, ?> service, Repository repository, JReleaserModel.Commit head) {
        if (isBlank(service.getOwner())) {
            service.setOwner(repository.getOwner());
        }
        if (!service.getOwner().equals(repository.getOwner())) {
            service.setMatch(false);
            service.setSkipTag(true);
            logger.warn(RB.$("ERROR_context_configurer_detected_git_owner"), repository.getOwner(), service.getOwner());
        }

        if (isBlank(service.getName())) {
            service.setName(repository.getName());
        }
        if (!service.getName().equals(repository.getName())) {
            service.setMatch(false);
            service.setSkipTag(true);
            logger.warn(RB.$("ERROR_context_configurer_detected_git_name"), repository.getName(), service.getName());
        }

        service.setBranch(Env.env(BRANCH, service.getBranch()));
        if (isBlank(service.getBranch())) {
            service.setBranch(head.getRefName());
        }
        if (!service.getBranch().equals(head.getRefName())) {
            service.setMatch(false);
            logger.warn(RB.$("ERROR_context_configurer_detected_git_branch"), head.getRefName(), service.getBranch());
        }
    }
}
