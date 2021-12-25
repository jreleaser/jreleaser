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
package org.jreleaser.tools;

import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.Gofish;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import org.jreleaser.model.util.Artifacts;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.FileType;
import org.jreleaser.util.PlatformUtils;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.Gofish.SKIP_GOFISH;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.Constants.KEY_GOFISH_PACKAGES;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.MustacheUtils.passThrough;
import static org.jreleaser.util.StringUtils.capitalize;
import static org.jreleaser.util.StringUtils.getFilename;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.isTrue;

/**
 * @author Andres Almiray
 * @since 0.9.1
 */
public class GofishToolProcessor extends AbstractRepositoryToolProcessor<Gofish> {
    public GofishToolProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPackageDistribution(Distribution distribution, Map<String, Object> props, Path packageDirectory) throws ToolProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);
        copyPreparedFiles(distribution, props);
    }

    @Override
    protected void fillToolProperties(Map<String, Object> props, Distribution distribution) throws ToolProcessingException {
        List<Artifact> artifacts = collectArtifacts(distribution);

        List<GofishPackage> packages = artifacts.stream()
            .filter(artifact -> isNotBlank(artifact.getPlatform()))
            .map(artifact -> new GofishPackage(props, context, distribution, artifact))
            .collect(toList());

        if (packages.isEmpty()) {
            for (Artifact artifact : artifacts) {
                if (isNotBlank(artifact.getPlatform())) continue;
                for (String os : new String[]{"darwin", "linux", "windows"}) {
                    for (String arch : new String[]{"x86_64", "aarch64"}) {
                        Artifact copy = artifact.copy();
                        copy.setPlatform(os + "-" + arch);
                        packages.add(new GofishPackage(props, context, distribution, copy));
                    }
                }
            }
        }

        props.put(KEY_GOFISH_PACKAGES, packages);
    }

    @Override
    protected void writeFile(Project project,
                             Distribution distribution,
                             String content,
                             Map<String, Object> props,
                             Path outputDirectory,
                             String fileName)
        throws ToolProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputFile = "food.lua".equals(fileName) ?
            outputDirectory.resolve("Food").resolve(distribution.getExecutable().concat(".lua")) :
            outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }

    @Override
    protected boolean isSkipped(Artifact artifact) {
        return isTrue(artifact.getExtraProperties().get(SKIP_GOFISH));
    }

    private static class GofishPackage {
        private final String packageOs;
        private final String packageArch;
        private final String packageUrl;
        private final String packageChecksum;
        private final String packagePath;
        private final String packageInstallPath;

        public GofishPackage(Map<String, Object> props, JReleaserContext context, Distribution distribution, Artifact artifact) {
            String platform = artifact.getPlatform();
            String artifactPlatform = isNotBlank(platform) ? capitalize(platform) : "";
            // add extra properties without clobbering existing keys
            Map<String, Object> artifactProps = artifact.getResolvedExtraProperties("artifact" + artifactPlatform);
            artifactProps.keySet().stream()
                .filter(k -> !props.containsKey(k))
                .forEach(k -> props.put(k, artifactProps.get(k)));

            String artifactFile = artifact.getEffectivePath().getFileName().toString();
            String artifactFileName = getFilename(artifactFile, FileType.getSupportedExtensions());

            boolean windows = PlatformUtils.isWindows(platform);
            String executable = distribution.getExecutable();
            String projectVersion = context.getModel().getProject().getVersion();

            String ps = windows ? "\\\\" : "/";
            String installPath = "\"bin" + ps + "\" .. name";
            String executablePath = artifactFileName.replace(executable, "name .. \"")
                .replace(projectVersion, "\" .. version .. \"")
                + ps + "bin" + ps + "\" .. name";

            if (windows) {
                executablePath += " .. \"." + distribution.getExecutableExtension() + "\"";
                installPath += " .. \"." + distribution.getExecutableExtension() + "\"";
            }
            packagePath = executablePath;
            packageInstallPath = installPath;

            String artifactOs = "";
            String artifactArch = "";
            if (isNotBlank(platform)) {
                if (platform.contains("-")) {
                    String[] parts = platform.split("-");
                    artifactOs = parts[0];
                    artifactArch = parts[1];
                }
            }
            packageOs = "osx".equals(artifactOs) ? "darwin" : artifactOs;
            packageArch = "x86_64".equals(artifactArch) ? "amd64" : "arm64";

            packageChecksum = artifact.getHash(Algorithm.SHA_256);

            Map<String, Object> newProps = new LinkedHashMap<>(props);
            Artifacts.artifactProps(artifact, newProps);
            String url = applyTemplate(context.getModel().getRelease().getGitService().getDownloadUrl(), newProps);
            packageUrl = url.replace(executable, "\" .. name .. \"")
                .replace(projectVersion, "\" .. version .. \"");
        }

        public String getPackageOs() {
            return packageOs;
        }

        public String getPackageArch() {
            return packageArch;
        }

        public String getPackageUrl() {
            return passThrough(packageUrl);
        }

        public String getPackageChecksum() {
            return packageChecksum;
        }

        public String getPackagePath() {
            return passThrough(packagePath);
        }

        public String getPackageInstallPath() {
            return passThrough(packageInstallPath);
        }
    }
}
