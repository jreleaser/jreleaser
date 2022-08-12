/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
package org.jreleaser.packagers;

import org.jreleaser.model.Artifact;
import org.jreleaser.model.Brew;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.packager.spi.PackagerProcessingException;
import org.jreleaser.model.util.Artifacts;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.MustacheUtils;
import org.jreleaser.util.PlatformUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.Constants.KEY_BREW_CASK_APP;
import static org.jreleaser.util.Constants.KEY_BREW_CASK_APPCAST;
import static org.jreleaser.util.Constants.KEY_BREW_CASK_DISPLAY_NAME;
import static org.jreleaser.util.Constants.KEY_BREW_CASK_HAS_APP;
import static org.jreleaser.util.Constants.KEY_BREW_CASK_HAS_APPCAST;
import static org.jreleaser.util.Constants.KEY_BREW_CASK_HAS_BINARY;
import static org.jreleaser.util.Constants.KEY_BREW_CASK_HAS_PKG;
import static org.jreleaser.util.Constants.KEY_BREW_CASK_HAS_UNINSTALL;
import static org.jreleaser.util.Constants.KEY_BREW_CASK_HAS_ZAP;
import static org.jreleaser.util.Constants.KEY_BREW_CASK_NAME;
import static org.jreleaser.util.Constants.KEY_BREW_CASK_PKG;
import static org.jreleaser.util.Constants.KEY_BREW_CASK_UNINSTALL;
import static org.jreleaser.util.Constants.KEY_BREW_CASK_ZAP;
import static org.jreleaser.util.Constants.KEY_BREW_DEPENDENCIES;
import static org.jreleaser.util.Constants.KEY_BREW_FORMULA_NAME;
import static org.jreleaser.util.Constants.KEY_BREW_HAS_LIVECHECK;
import static org.jreleaser.util.Constants.KEY_BREW_LIVECHECK;
import static org.jreleaser.util.Constants.KEY_BREW_MULTIPLATFORM;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_JAVA_VERSION;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_URL;
import static org.jreleaser.util.Constants.KEY_HOMEBREW_TAP_REPO_CLONE_URL;
import static org.jreleaser.util.Constants.KEY_HOMEBREW_TAP_REPO_URL;
import static org.jreleaser.util.FileType.ZIP;
import static org.jreleaser.util.MustacheUtils.passThrough;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.isTrue;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class BrewPackagerProcessor extends AbstractRepositoryPackagerProcessor<Brew> {
    private static final String KEY_DISTRIBUTION_CHECKSUM_SHA_256 = "distributionChecksumSha256";

    private static final String TPL_MAC_ARM = "  if OS.mac? && Hardware::CPU.arm?\n" +
        "    url \"{{distributionUrl}}\"\n" +
        "    sha256 \"{{distributionChecksumSha256}}\"\n" +
        "  end\n";
    private static final String TPL_MAC_INTEL = "  if OS.mac? && Hardware::CPU.intel?\n" +
        "    url \"{{distributionUrl}}\"\n" +
        "    sha256 \"{{distributionChecksumSha256}}\"\n" +
        "  end\n";
    private static final String TPL_LINUX_ARM = "  if OS.linux? && Hardware::CPU.arm? && Hardware::CPU.is_64_bit?\n" +
        "    url \"{{distributionUrl}}\"\n" +
        "    sha256 \"{{distributionChecksumSha256}}\"\n" +
        "  end\n";
    private static final String TPL_LINUX_INTEL = "  if OS.linux? && Hardware::CPU.intel?\n" +
        "    url \"{{distributionUrl}}\"\n" +
        "    sha256 \"{{distributionChecksumSha256}}\"\n" +
        "  end\n";

    private static final String CASK_RB = "cask.rb";
    private static final String CASKS = "Casks";
    private static final String FORMULA = "Formula";
    private static final String FORMULA_RB = "formula.rb";
    private static final String FORMULA_MULTI_RB = "formula-multi.rb";
    private static final String RB = ".rb";
    private static final String SKIP_JAVA = "skipJava";

    public BrewPackagerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPackageDistribution(Distribution distribution, Map<String, Object> props, Path packageDirectory) throws PackagerProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);
        copyPreparedFiles(distribution, props);
    }

    @Override
    protected void fillPackagerProperties(Map<String, Object> props, Distribution distribution, ProcessingStep processingStep) throws PackagerProcessingException {
        GitService gitService = context.getModel().getRelease().getGitService();

        props.put(KEY_BREW_FORMULA_NAME, packager.getResolvedFormulaName(props));

        props.put(KEY_HOMEBREW_TAP_REPO_URL,
            gitService.getResolvedRepoUrl(context.getModel(), packager.getTap().getOwner(), packager.getTap().getResolvedName()));
        props.put(KEY_HOMEBREW_TAP_REPO_CLONE_URL,
            gitService.getResolvedRepoCloneUrl(context.getModel(), packager.getTap().getOwner(), packager.getTap().getResolvedName()));

        props.put(KEY_BREW_HAS_LIVECHECK, packager.hasLivecheck());
        if (packager.hasLivecheck()) {
            props.put(KEY_BREW_LIVECHECK, packager.getLivecheck().stream()
                .map(line -> resolveTemplate(line, props))
                .map(MustacheUtils::passThrough)
                .collect(Collectors.toList()));
        }

        Brew.Cask cask = packager.getCask();
        if (cask.isEnabled()) {
            boolean hasPkg = isNotBlank(cask.getPkgName());
            boolean hasApp = isNotBlank(cask.getAppName());

            props.put(KEY_BREW_CASK_NAME, cask.getResolvedCaskName(props));
            props.put(KEY_BREW_CASK_DISPLAY_NAME, cask.getResolvedDisplayName(props));
            props.put(KEY_BREW_CASK_HAS_UNINSTALL, !cask.getUninstallItems().isEmpty());
            props.put(KEY_BREW_CASK_HAS_PKG, hasPkg);
            if (hasPkg) {
                props.put(KEY_BREW_CASK_PKG, cask.getResolvedPkgName(props));
            }
            props.put(KEY_BREW_CASK_HAS_APP, hasApp);
            if (hasApp) {
                props.put(KEY_BREW_CASK_APP, cask.getResolvedAppName(props));
            }
            props.put(KEY_BREW_CASK_UNINSTALL, cask.getUninstallItems());
            props.put(KEY_BREW_CASK_HAS_ZAP, !cask.getZapItems().isEmpty());
            props.put(KEY_BREW_CASK_ZAP, cask.getZapItems());
            String appcast = cask.getResolvedAppcast(props);
            props.put(KEY_BREW_CASK_HAS_APPCAST, isNotBlank(appcast));
            props.put(KEY_BREW_CASK_APPCAST, appcast);

            if (!hasApp && !hasPkg) {
                for (Artifact artifact : collectArtifacts(distribution)) {
                    if (artifact.getPath().endsWith(ZIP.extension())) {
                        props.put(KEY_DISTRIBUTION_URL, resolveArtifactUrl(props, distribution, artifact));
                        props.put(KEY_BREW_CASK_HAS_BINARY, true);
                        break;
                    }
                }
            }
        } else if (packager.isMultiPlatform()) {
            List<String> multiPlatforms = new ArrayList<>();
            for (Artifact artifact : collectArtifacts(distribution)) {
                if (!artifact.getPath().endsWith(ZIP.extension()) || isBlank(artifact.getPlatform())) continue;

                String template = null;
                String artifactUrl = resolveArtifactUrl(props, distribution, artifact);

                if (PlatformUtils.isMac(artifact.getPlatform())) {
                    if (PlatformUtils.isArm(artifact.getPlatform())) {
                        template = TPL_MAC_ARM;
                    } else {
                        template = TPL_MAC_INTEL;
                    }
                } else if (PlatformUtils.isLinux(artifact.getPlatform())) {
                    if (PlatformUtils.isArm(artifact.getPlatform())) {
                        template = TPL_LINUX_ARM;
                    } else {
                        template = TPL_LINUX_INTEL;
                    }
                }

                if (isNotBlank(template)) {
                    Map<String, Object> newProps = new LinkedHashMap<>(props);
                    newProps.put(KEY_DISTRIBUTION_URL, artifactUrl);
                    newProps.put(KEY_DISTRIBUTION_CHECKSUM_SHA_256, artifact.getHash(Algorithm.SHA_256));
                    multiPlatforms.add(resolveTemplate(template, newProps));
                }
            }

            if (multiPlatforms.isEmpty()) {
                throw new PackagerProcessingException(org.jreleaser.bundle.RB.$("ERROR_brew_multiplatform_artifacts"));
            }
            props.put(KEY_BREW_MULTIPLATFORM, passThrough(String.join(System.lineSeparator() + "  ", multiPlatforms)));
        } else if ((distribution.getType() == Distribution.DistributionType.JAVA_BINARY ||
            distribution.getType() == Distribution.DistributionType.SINGLE_JAR) &&
            !isTrue(packager.getExtraProperties().get(SKIP_JAVA))) {
            packager.addDependency("openjdk@" + props.get(KEY_DISTRIBUTION_JAVA_VERSION));
        }

        props.put(KEY_BREW_DEPENDENCIES, packager.getDependenciesAsList()
            .stream()
            // prevent Mustache from converting quotes into &quot;
            .map(dependency -> passThrough(dependency.toString()))
            .collect(Collectors.toList()));
    }

    private String resolveArtifactUrl(Map<String, Object> props, Distribution distribution, Artifact artifact) {
        return Artifacts.resolveDownloadUrl(context, Brew.TYPE, distribution, artifact);
    }

    @Override
    protected void writeFile(Project project,
                             Distribution distribution,
                             String content,
                             Map<String, Object> props,
                             Path outputDirectory,
                             String fileName)
        throws PackagerProcessingException {
        fileName = trimTplExtension(fileName);

        if (packager.getCask().isEnabled()) {
            if (FORMULA_RB.equals(fileName) || FORMULA_MULTI_RB.equals(fileName)) return;
            Path outputFile = CASK_RB.equals(fileName) ?
                outputDirectory.resolve(CASKS).resolve(packager.getCask().getResolvedCaskName(props).concat(RB)) :
                outputDirectory.resolve(fileName);
            writeFile(content, outputFile);
        } else if (packager.isMultiPlatform()) {
            if (CASK_RB.equals(fileName) || FORMULA_RB.equals(fileName)) return;
            Path outputFile = FORMULA_MULTI_RB.equals(fileName) ?
                outputDirectory.resolve(FORMULA).resolve(distribution.getExecutable().getName().concat(RB)) :
                outputDirectory.resolve(fileName);
            writeFile(content, outputFile);
        } else {
            if (CASK_RB.equals(fileName) || FORMULA_MULTI_RB.equals(fileName)) return;
            Path outputFile = FORMULA_RB.equals(fileName) ?
                outputDirectory.resolve(FORMULA).resolve(distribution.getExecutable().getName().concat(RB)) :
                outputDirectory.resolve(fileName);
            writeFile(content, outputFile);
        }
    }
}
