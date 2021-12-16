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
package org.jreleaser.assemblers;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Glob;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JavaAssembler;
import org.jreleaser.model.assembler.spi.AssemblerProcessingException;
import org.jreleaser.util.PlatformUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.9.0
 */
public final class AssemblerUtils {
    private static final String KEY_JAVA_VERSION = "JAVA_VERSION";

    private AssemblerUtils() {
        // noop
    }

    public static String readJavaVersion(Path path) throws AssemblerProcessingException {
        Path release = path.resolve("release");
        if (!Files.exists(release)) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_invalid_jdk_release", path.toAbsolutePath()));
        }

        try {
            Properties props = new Properties();
            props.load(Files.newInputStream(release));
            if (props.containsKey(KEY_JAVA_VERSION)) {
                String version = props.getProperty(KEY_JAVA_VERSION);
                if (version.startsWith("\"") && version.endsWith("\"")) {
                    return version.substring(1, version.length() - 1);
                }
                return version;
            } else {
                throw new AssemblerProcessingException(RB.$("ERROR_assembler_invalid_jdk_release_file", release.toAbsolutePath()));
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_invalid_jdk_release_file", release.toAbsolutePath()), e);
        }
    }

    public static Set<Path> copyJars(JReleaserContext context, JavaAssembler assembler, Path jarsDirectory, String platform) throws AssemblerProcessingException {
        Set<Path> paths = new LinkedHashSet<>();

        // resolve all first
        if (isBlank(platform)) {
            paths.add(assembler.getMainJar().getEffectivePath(context, assembler));
        }

        for (Glob glob : assembler.getJars()) {
            if ((isBlank(platform) && isBlank(glob.getPlatform())) ||
                (isNotBlank(platform) && PlatformUtils.isCompatible(platform, glob.getPlatform()))) {
                glob.getResolvedArtifacts(context).stream()
                    .map(artifact -> artifact.getResolvedPath(context, assembler))
                    .forEach(paths::add);
            }
        }

        // copy all next
        try {
            Files.createDirectories(jarsDirectory);
            for (Path path : paths) {
                context.getLogger().debug(RB.$("assembler.copying"), path.getFileName());
                Files.copy(path, jarsDirectory.resolve(path.getFileName()), REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_copying_jars"), e);
        }

        return paths;
    }
}
