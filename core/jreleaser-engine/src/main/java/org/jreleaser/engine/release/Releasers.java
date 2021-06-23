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
package org.jreleaser.engine.release;

import org.jreleaser.model.Codeberg;
import org.jreleaser.model.GenericGit;
import org.jreleaser.model.Gitea;
import org.jreleaser.model.Github;
import org.jreleaser.model.Gitlab;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.releaser.spi.ReleaseException;
import org.jreleaser.model.releaser.spi.Releaser;
import org.jreleaser.model.releaser.spi.ReleaserBuilder;
import org.jreleaser.model.releaser.spi.ReleaserBuilderFactory;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Releasers {
    public static void release(JReleaserContext context) throws ReleaseException {
        releaserFor(context).release();
    }

    public static Releaser releaserFor(JReleaserContext context) {
        return Releasers.findReleaser(context)
            .configureWith(context)
            .build();
    }

    private static <RB extends ReleaserBuilder> RB findReleaser(JReleaserContext context) {
        Map<String, ReleaserBuilder> builders = StreamSupport.stream(ServiceLoader.load(ReleaserBuilderFactory.class,
            Releasers.class.getClassLoader()).spliterator(), false)
            .collect(Collectors.toMap(ReleaserBuilderFactory::getName, ReleaserBuilderFactory::getBuilder));

        if (null != context.getModel().getRelease().getGithub()) {
            return (RB) builders.get(Github.NAME);
        }
        if (null != context.getModel().getRelease().getGitlab()) {
            return (RB) builders.get(Gitlab.NAME);
        }
        if (null != context.getModel().getRelease().getGitea()) {
            return (RB) builders.get(Gitea.NAME);
        }
        if (null != context.getModel().getRelease().getCodeberg()) {
            return (RB) builders.get(Codeberg.NAME);
        }
        if (null != context.getModel().getRelease().getGeneric()) {
            return (RB) builders.get(GenericGit.NAME);
        }

        throw new JReleaserException("No suitable git releaser has been configured");
    }
}
