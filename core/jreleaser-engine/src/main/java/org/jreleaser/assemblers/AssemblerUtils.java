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
package org.jreleaser.assemblers;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.JavaAssembler;
import org.jreleaser.model.internal.common.Glob;
import org.jreleaser.model.spi.assemble.AssemblerProcessingException;
import org.jreleaser.util.PlatformUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

        try (InputStream in = Files.newInputStream(release)) {
            Properties props = new Properties();
            props.load(in);
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

    public static Set<Path> copyJars(JReleaserContext context, JavaAssembler<?> assembler, Path jarsDirectory, String platform) throws AssemblerProcessingException {
        Set<Path> paths = new LinkedHashSet<>();

        // resolve all first
        if (isBlank(platform) && isNotBlank(assembler.getMainJar().getPath())) {
            paths.add(assembler.getMainJar().getEffectivePath(context, assembler));
        }

        for (Glob glob : assembler.getJars()) {
            if (!glob.resolveActiveAndSelected(context)) continue;
            String globPlatform = glob.getPlatform();
            boolean platformIsBlank = isBlank(platform) && isBlank(globPlatform);
            boolean platformIsCompatible = isNotBlank(platform) && isNotBlank(globPlatform) && PlatformUtils.isCompatible(platform, globPlatform);
            if (platformIsBlank || platformIsCompatible) {
                glob.getResolvedArtifacts(context).stream()
                    .map(artifact -> artifact.getResolvedPath(context, assembler))
                    .forEach(paths::add);
            }
        }

        // copy all next
        Set<Path> copied = new LinkedHashSet<>();
        try {
            Files.createDirectories(jarsDirectory);
            for (Path path : paths) {
                context.getLogger().debug(RB.$("assembler.copying"), path.getFileName());
                Path copy = jarsDirectory.resolve(path.getFileName());
                Files.copy(path, copy, REPLACE_EXISTING);
                copied.add(copy);
            }
        } catch (IOException e) {
            throw new AssemblerProcessingException(RB.$("ERROR_assembler_copying_jars"), e);
        }

        return copied;
    }

    public static String maybeAdjust(String path) {
        if (PlatformUtils.isWindows()) {
            return path.replace("/", File.separator);
        }

        return path;
    }
}
