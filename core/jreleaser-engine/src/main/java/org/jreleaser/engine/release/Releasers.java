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
package org.jreleaser.engine.release;

import org.jreleaser.bundle.RB;
import org.jreleaser.extensions.api.workflow.WorkflowListenerException;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.hooks.ExecutionEvent;
import org.jreleaser.model.api.release.CodebergReleaser;
import org.jreleaser.model.api.release.GenericGitReleaser;
import org.jreleaser.model.api.release.GiteaReleaser;
import org.jreleaser.model.api.release.GithubReleaser;
import org.jreleaser.model.api.release.GitlabReleaser;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.release.ReleaseException;
import org.jreleaser.model.spi.release.Releaser;
import org.jreleaser.model.spi.release.ReleaserBuilder;
import org.jreleaser.model.spi.release.ReleaserBuilderFactory;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class Releasers {
    private Releasers() {
        // noop
    }

    public static void release(JReleaserContext context) throws ReleaseException {
        org.jreleaser.model.api.release.Releaser releaser = context.getModel().getRelease().releaser();

        boolean failure = false;
        try {
            fireAssembleEvent(ExecutionEvent.before(JReleaserCommand.RELEASE.toStep()), context, releaser);
            releaserFor(context).release();
        } catch (RuntimeException e) {
            failure = true;
            fireAssembleEvent(ExecutionEvent.failure(JReleaserCommand.RELEASE.toStep(), e), context, releaser);
        }

        if (!failure) {
            fireAssembleEvent(ExecutionEvent.success(JReleaserCommand.RELEASE.toStep()), context, releaser);
        }
    }

    public static Releaser<?> releaserFor(JReleaserContext context) {
        return Releasers.findReleaser(context)
            .configureWith(context)
            .build();
    }

    private static <T extends ReleaserBuilder<?>> T findReleaser(JReleaserContext context) {
        Map<String, ReleaserBuilder<?>> builders = StreamSupport.stream(ServiceLoader.load(ReleaserBuilderFactory.class,
                Releasers.class.getClassLoader()).spliterator(), false)
            .collect(Collectors.toMap(ReleaserBuilderFactory::getName, ReleaserBuilderFactory::getBuilder));

        if (null != context.getModel().getRelease().getGithub()) {
            return (T) builders.get(GithubReleaser.TYPE);
        }
        if (null != context.getModel().getRelease().getGitlab()) {
            return (T) builders.get(GitlabReleaser.TYPE);
        }
        if (null != context.getModel().getRelease().getGitea()) {
            return (T) builders.get(GiteaReleaser.TYPE);
        }
        if (null != context.getModel().getRelease().getCodeberg()) {
            return (T) builders.get(CodebergReleaser.TYPE);
        }
        if (null != context.getModel().getRelease().getGeneric()) {
            return (T) builders.get(GenericGitReleaser.TYPE);
        }

        throw new JReleaserException(RB.$("ERROR_releaser_no_match"));
    }

    private static void fireAssembleEvent(ExecutionEvent event, JReleaserContext context, org.jreleaser.model.api.release.Releaser releaser) {
        try {
            context.fireReleaseStepEvent(event, releaser);
        } catch (WorkflowListenerException e) {
            context.getLogger().error(RB.$("listener.failure", e.getListener().getClass().getName()));
            context.getLogger().trace(e);
            if (event.getType() != ExecutionEvent.Type.FAILURE && !e.getListener().isContinueOnError()) {
                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                } else {
                    throw new JReleaserException(RB.$("ERROR_unexpected_error"), e.getCause());
                }
            }
        }
    }
}
