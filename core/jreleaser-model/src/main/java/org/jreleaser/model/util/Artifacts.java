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
package org.jreleaser.model.util;

import org.jreleaser.model.Artifact;
import org.jreleaser.model.Files;
import org.jreleaser.model.Glob;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Artifacts {
    public static Set<Artifact> resolveFiles(JReleaserContext context) throws JReleaserException {
        Files files = context.getModel().getFiles();

        if (files.arePathsResolved()) {
            return files.getPaths();
        }

        Set<Artifact> paths = new LinkedHashSet<>();

        // resolve artifacts
        for (Artifact artifact : files.getArtifacts()) {
            artifact.getEffectivePath(context);
            paths.add(artifact);
        }

        // resolve globs
        for (Glob glob : files.getGlobs()) {
            for (Path path : glob.getResolvedPaths(context)) {
                paths.add(Artifact.of(path));
            }
        }

        files.setPaths(paths);

        return files.getPaths();
    }
}
