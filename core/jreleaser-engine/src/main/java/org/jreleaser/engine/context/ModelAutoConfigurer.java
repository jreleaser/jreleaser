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
package org.jreleaser.engine.context;

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

import java.io.IOException;

import static org.jreleaser.model.GitService.BRANCH;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class ModelAutoConfigurer {
    public static void autoConfigure(JReleaserContext context) {
        try {
            context.getModel().setCommit(GitSdk.head(context.getBasedir()));
        } catch (IOException e) {
            context.getLogger().trace(e);
            throw new JReleaserException("Could not determine git HEAD", e);
        }

        Repository repository = null;
        try {
            repository = GitSdk.of(context).getRemote();

        } catch (IOException e) {
            context.getLogger().trace(e);
            throw new JReleaserException("Could not determine remote", e);
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
        }

        try {
            if (!context.validateModel().isEmpty()) {
                throw new JReleaserException("JReleaser has not been properly configured.");
            }
            new JReleaserModelPrinter.Plain(context.getLogger().getTracer())
                .print(context.getModel().asMap());
        } catch (JReleaserException e) {
            context.getLogger().trace(e);
            throw e;
        } catch (Exception e) {
            context.getLogger().trace(e);
            throw new JReleaserException("JReleaser has not been properly configured.");
        }
    }

    private static void autoConfigureGithub(JReleaserContext context, Repository repository) {
        GitService service = context.getModel().getRelease().getGitService();
        Github github = context.getModel().getRelease().getGithub();

        if (service != null) {
            if (service != github) {
                context.getLogger().warn("Auto configure detected github but project has " +
                    service.getServiceName() + " configured");

                if (isBlank(Env.resolve(BRANCH, service.getBranch()))) {
                    service.setBranch(context.getModel().getCommit().getRefName());
                }
                return;
            }
        }

        if (null == github) {
            github = new Github();
            context.getModel().getRelease().setGithub(github);
        }

        fillGitProperties(github, repository, context.getModel().getCommit());
    }

    private static void autoConfigureGitlab(JReleaserContext context, Repository repository) {
        GitService service = context.getModel().getRelease().getGitService();
        Gitlab gitlab = context.getModel().getRelease().getGitlab();

        if (service != null) {
            if (service != gitlab) {
                context.getLogger().warn("Auto configure detected gitlab but project has " +
                    service.getServiceName() + " configured");

                if (isBlank(Env.resolve(BRANCH, service.getBranch()))) {
                    service.setBranch(context.getModel().getCommit().getRefName());
                }
                return;
            }
        }

        if (null == gitlab) {
            gitlab = new Gitlab();
            context.getModel().getRelease().setGitlab(gitlab);
        }

        fillGitProperties(gitlab, repository, context.getModel().getCommit());
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
