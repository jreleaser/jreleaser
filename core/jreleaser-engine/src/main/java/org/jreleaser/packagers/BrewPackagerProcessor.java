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
package org.jreleaser.packagers;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.BrewPackager;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.util.Artifacts;
import org.jreleaser.model.spi.packagers.PackagerProcessingException;
import org.jreleaser.mustache.MustacheUtils;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.FileType;
import org.jreleaser.util.PlatformUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.jreleaser.model.Constants.KEY_BREW_CASK_APP;
import static org.jreleaser.model.Constants.KEY_BREW_CASK_APPCAST;
import static org.jreleaser.model.Constants.KEY_BREW_CASK_DISPLAY_NAME;
import static org.jreleaser.model.Constants.KEY_BREW_CASK_HAS_APP;
import static org.jreleaser.model.Constants.KEY_BREW_CASK_HAS_APPCAST;
import static org.jreleaser.model.Constants.KEY_BREW_CASK_HAS_BINARY;
import static org.jreleaser.model.Constants.KEY_BREW_CASK_HAS_PKG;
import static org.jreleaser.model.Constants.KEY_BREW_CASK_HAS_UNINSTALL;
import static org.jreleaser.model.Constants.KEY_BREW_CASK_HAS_ZAP;
import static org.jreleaser.model.Constants.KEY_BREW_CASK_NAME;
import static org.jreleaser.model.Constants.KEY_BREW_CASK_PKG;
import static org.jreleaser.model.Constants.KEY_BREW_CASK_UNINSTALL;
import static org.jreleaser.model.Constants.KEY_BREW_CASK_ZAP;
import static org.jreleaser.model.Constants.KEY_BREW_DEPENDENCIES;
import static org.jreleaser.model.Constants.KEY_BREW_DOWNLOAD_STRATEGY;
import static org.jreleaser.model.Constants.KEY_BREW_FORMULA_NAME;
import static org.jreleaser.model.Constants.KEY_BREW_HAS_LIVECHECK;
import static org.jreleaser.model.Constants.KEY_BREW_LIVECHECK;
import static org.jreleaser.model.Constants.KEY_BREW_MULTIPLATFORM;
import static org.jreleaser.model.Constants.KEY_BREW_REQUIRE_RELATIVE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_FILE_NAME;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_ARTIFACT_ROOT_ENTRY_NAME;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_CLASS;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_MODULE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_VERSION;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_URL;
import static org.jreleaser.model.Constants.KEY_HOMEBREW_REPOSITORY_ALIAS;
import static org.jreleaser.model.Constants.KEY_HOMEBREW_REPOSITORY_CLONE_URL;
import static org.jreleaser.model.Constants.KEY_HOMEBREW_REPOSITORY_NAME;
import static org.jreleaser.model.Constants.KEY_HOMEBREW_REPOSITORY_OWNER;
import static org.jreleaser.model.Constants.KEY_HOMEBREW_REPOSITORY_URL;
import static org.jreleaser.model.Constants.KEY_HOMEBREW_TAP_NAME;
import static org.jreleaser.model.Constants.KEY_HOMEBREW_TAP_REPO_CLONE_URL;
import static org.jreleaser.model.Constants.KEY_HOMEBREW_TAP_REPO_NAME;
import static org.jreleaser.model.Constants.KEY_HOMEBREW_TAP_REPO_OWNER;
import static org.jreleaser.model.Constants.KEY_HOMEBREW_TAP_REPO_URL;
import static org.jreleaser.mustache.MustacheUtils.passThrough;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.FileType.ZIP;
import static org.jreleaser.util.FileUtils.resolveRootEntryName;
import static org.jreleaser.util.StringUtils.getFilename;
import static org.jreleaser.util.StringUtils.getHyphenatedName;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.isTrue;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class
BrewPackagerProcessor extends AbstractRepositoryPackagerProcessor<BrewPackager> {
    private static final String KEY_DISTRIBUTION_CHECKSUM_SHA_256 = "distributionChecksumSha256";

    private static final String TPL_MAC_ARM = "  if OS.mac? && Hardware::CPU.arm?\n" +
        "    url \"{{distributionUrl}}\"{{#brewDownloadStrategy}}, :using => {{.}}{{/brewDownloadStrategy}}\n" +
        "    sha256 \"{{distributionChecksumSha256}}\"\n" +
        "  end\n";
    private static final String TPL_MAC_INTEL = "  if OS.mac? && Hardware::CPU.intel?\n" +
        "    url \"{{distributionUrl}}\"{{#brewDownloadStrategy}}, :using => {{.}}{{/brewDownloadStrategy}}\n" +
        "    sha256 \"{{distributionChecksumSha256}}\"\n" +
        "  end\n";
    private static final String TPL_LINUX_ARM = "  if OS.linux? && Hardware::CPU.arm? && Hardware::CPU.is_64_bit?\n" +
        "    url \"{{distributionUrl}}\"{{#brewDownloadStrategy}}, :using => {{.}}{{/brewDownloadStrategy}}\n" +
        "    sha256 \"{{distributionChecksumSha256}}\"\n" +
        "  end\n";
    private static final String TPL_LINUX_INTEL = "  if OS.linux? && Hardware::CPU.intel?\n" +
        "    url \"{{distributionUrl}}\"{{#brewDownloadStrategy}}, :using => {{.}}{{/brewDownloadStrategy}}\n" +
        "    sha256 \"{{distributionChecksumSha256}}\"\n" +
        "  end\n";

    private static final String TPL_MAC_ARM_FLAT_BINARY = "  if OS.mac? && Hardware::CPU.arm?\n" +
        "    url \"{{distributionUrl}}\"{{#brewDownloadStrategy}}, :using => {{.}}{{/brewDownloadStrategy}}\n" +
        "    sha256 \"{{distributionChecksumSha256}}\"\n" +
        "\n" +
        "    def install\n" +
        "      bin.install \"{{distributionArtifactFileName}}\" => \"{{distributionExecutableName}}\"\n" +
        "    end\n" +
        "  end\n";
    private static final String TPL_MAC_INTEL_FLAT_BINARY = "  if OS.mac? && Hardware::CPU.intel?\n" +
        "    url \"{{distributionUrl}}\"{{#brewDownloadStrategy}}, :using => {{.}}{{/brewDownloadStrategy}}\n" +
        "    sha256 \"{{distributionChecksumSha256}}\"\n" +
        "\n" +
        "    def install\n" +
        "      bin.install \"{{distributionArtifactFileName}}\" => \"{{distributionExecutableName}}\"\n" +
        "    end\n" +
        "  end\n";
    private static final String TPL_LINUX_ARM_FLAT_BINARY = "  if OS.linux? && Hardware::CPU.arm? && Hardware::CPU.is_64_bit?\n" +
        "    url \"{{distributionUrl}}\"{{#brewDownloadStrategy}}, :using => {{.}}{{/brewDownloadStrategy}}\n" +
        "    sha256 \"{{distributionChecksumSha256}}\"\n" +
        "\n" +
        "    def install\n" +
        "      bin.install \"{{distributionArtifactFileName}}\" => \"{{distributionExecutableName}}\"\n" +
        "    end\n" +
        "  end\n";
    private static final String TPL_LINUX_INTEL_FLAT_BINARY = "  if OS.linux? && Hardware::CPU.intel?\n" +
        "    url \"{{distributionUrl}}\"{{#brewDownloadStrategy}}, :using => {{.}}{{/brewDownloadStrategy}}\n" +
        "    sha256 \"{{distributionChecksumSha256}}\"\n" +
        "\n" +
        "    def install\n" +
        "      bin.install \"{{distributionArtifactFileName}}\" => \"{{distributionExecutableName}}\"\n" +
        "    end\n" +
        "  end\n";

    private static final String CASK_RB = "cask.rb";
    private static final String CASKS = "Casks";
    private static final String FORMULA = "Formula";
    private static final String FORMULA_RB = "formula.rb";
    private static final String FORMULA_MULTI_RB = "formula-multi.rb";
    private static final String RB = ".rb";
    private static final String SKIP_JAVA = "skipJava";
    private static final String USE_VERSIONED_JAVA = "useVersionedJava";

    private static final List<String> BREW_JDK_ALIASES = Arrays.asList("openjdk", "java");

    public BrewPackagerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPackageDistribution(Distribution distribution, TemplateContext props, Path packageDirectory) throws PackagerProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);
        copyPreparedFiles(props);
    }

    @Override
    protected void fillPackagerProperties(TemplateContext props, Distribution distribution) {
        props.set(KEY_DISTRIBUTION_JAVA_MAIN_CLASS, distribution.getJava().getMainClass());
        props.set(KEY_DISTRIBUTION_JAVA_MAIN_MODULE, distribution.getJava().getMainModule());
        BaseReleaser<?, ?> releaser = context.getModel().getRelease().getReleaser();

        props.set(KEY_BREW_FORMULA_NAME, packager.getResolvedFormulaName(props));
        props.set(KEY_BREW_DOWNLOAD_STRATEGY, packager.getDownloadStrategy());
        props.set(KEY_BREW_REQUIRE_RELATIVE, packager.getRequireRelative());

        props.set(KEY_HOMEBREW_TAP_REPO_OWNER, packager.getRepository().getOwner());
        props.set(KEY_HOMEBREW_TAP_REPO_NAME, packager.getRepository().getResolvedName());
        props.set(KEY_HOMEBREW_TAP_NAME, packager.getRepository().getResolvedName().substring("homebrew-".length()));
        props.set(KEY_HOMEBREW_TAP_REPO_URL,
            releaser.getResolvedRepoUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName()));
        props.set(KEY_HOMEBREW_TAP_REPO_CLONE_URL,
            releaser.getResolvedRepoCloneUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName()));

        props.set(KEY_HOMEBREW_REPOSITORY_OWNER, packager.getRepository().getOwner());
        props.set(KEY_HOMEBREW_REPOSITORY_NAME, packager.getRepository().getResolvedName());
        props.set(KEY_HOMEBREW_REPOSITORY_ALIAS, packager.getRepository().getResolvedName().substring("homebrew-".length()));
        props.set(KEY_HOMEBREW_REPOSITORY_URL,
            releaser.getResolvedRepoUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName()));
        props.set(KEY_HOMEBREW_REPOSITORY_CLONE_URL,
            releaser.getResolvedRepoCloneUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName()));

        props.set(KEY_BREW_HAS_LIVECHECK, packager.hasLivecheck());
        if (packager.hasLivecheck()) {
            props.set(KEY_BREW_LIVECHECK, packager.getLivecheck().stream()
                .map(line -> resolveTemplate(line, props))
                .map(MustacheUtils::passThrough)
                .collect(Collectors.toList()));
        }

        BrewPackager.Cask cask = packager.getCask();
        if (cask.isEnabled()) {
            boolean hasPkg = isNotBlank(cask.getPkgName());
            boolean hasApp = isNotBlank(cask.getAppName());

            props.set(KEY_BREW_CASK_NAME, cask.getResolvedCaskName(props));
            props.set(KEY_BREW_CASK_DISPLAY_NAME, cask.getResolvedDisplayName(props));
            props.set(KEY_BREW_CASK_HAS_UNINSTALL, !cask.getUninstallItems().isEmpty());
            props.set(KEY_BREW_CASK_HAS_PKG, hasPkg);
            if (hasPkg) {
                props.set(KEY_BREW_CASK_PKG, cask.getResolvedPkgName(props));
            }
            props.set(KEY_BREW_CASK_HAS_APP, hasApp);
            if (hasApp) {
                props.set(KEY_BREW_CASK_APP, cask.getResolvedAppName(props));
            }
            props.set(KEY_BREW_CASK_UNINSTALL, cask.getUninstallItems());
            props.set(KEY_BREW_CASK_HAS_ZAP, !cask.getZapItems().isEmpty());
            props.set(KEY_BREW_CASK_ZAP, cask.getZapItems());
            String appcast = cask.getResolvedAppcast(props);
            props.set(KEY_BREW_CASK_HAS_APPCAST, isNotBlank(appcast));
            props.set(KEY_BREW_CASK_APPCAST, appcast);

            if (!hasApp && !hasPkg) {
                for (Artifact artifact : collectArtifacts(distribution)) {
                    if (artifact.getPath().endsWith(ZIP.extension())) {
                        props.set(KEY_DISTRIBUTION_URL, resolveArtifactUrl(distribution, artifact));
                        props.set(KEY_BREW_CASK_HAS_BINARY, true);
                        break;
                    }
                }
            }
        } else if (packager.isMultiPlatform()) {
            List<String> multiPlatforms = new ArrayList<>();

            Artifact osxIntelArtifact = null;
            Artifact osxArmArtifact = null;

            boolean flatBinary = distribution.getType() == org.jreleaser.model.Distribution.DistributionType.FLAT_BINARY;

            for (Artifact artifact : collectArtifacts(distribution)) {
                if (!artifact.getPath().endsWith(ZIP.extension()) && !flatBinary ||
                    isBlank(artifact.getPlatform())) {
                    continue;
                }

                String template = null;
                String artifactUrl = resolveArtifactUrl(distribution, artifact);
                String artifactFile = artifact.getEffectivePath().getFileName().toString();
                String artifactFileName = getFilename(artifactFile, FileType.getSupportedExtensions());

                if (PlatformUtils.isMac(artifact.getPlatform())) {
                    if (PlatformUtils.isArm(artifact.getPlatform())) {
                        template = flatBinary ? TPL_MAC_ARM_FLAT_BINARY : TPL_MAC_ARM;
                        osxArmArtifact = artifact;
                    } else {
                        template = flatBinary ? TPL_MAC_INTEL_FLAT_BINARY : TPL_MAC_INTEL;
                        osxIntelArtifact = artifact;
                    }
                } else if (PlatformUtils.isLinux(artifact.getPlatform())) {
                    if (PlatformUtils.isArm(artifact.getPlatform())) {
                        template = flatBinary ? TPL_LINUX_ARM_FLAT_BINARY : TPL_LINUX_ARM;
                    } else {
                        template = flatBinary ? TPL_LINUX_INTEL_FLAT_BINARY : TPL_LINUX_INTEL;
                    }
                }

                if (isNotBlank(template)) {
                    TemplateContext newProps = new TemplateContext(props);
                    newProps.set(KEY_DISTRIBUTION_URL, artifactUrl);
                    newProps.set(KEY_DISTRIBUTION_ARTIFACT_FILE_NAME, artifactFileName);
                    newProps.set(KEY_DISTRIBUTION_ARTIFACT_ROOT_ENTRY_NAME, resolveRootEntryName(artifact.getEffectivePath()));
                    newProps.set(KEY_DISTRIBUTION_CHECKSUM_SHA_256, artifact.getHash(Algorithm.SHA_256));
                    multiPlatforms.add(resolveTemplate(template, newProps));
                }
            }

            // On OSX, use intel binary for arm if there's no match
            if (null != osxIntelArtifact && null == osxArmArtifact) {
                String artifactUrl = resolveArtifactUrl(distribution, osxIntelArtifact);
                String artifactFile = osxIntelArtifact.getEffectivePath().getFileName().toString();
                String artifactFileName = getFilename(artifactFile, FileType.getSupportedExtensions());
                TemplateContext newProps = new TemplateContext(props);
                newProps.set(KEY_DISTRIBUTION_URL, artifactUrl);
                newProps.set(KEY_DISTRIBUTION_ARTIFACT_FILE_NAME, artifactFileName);
                newProps.set(KEY_DISTRIBUTION_ARTIFACT_ROOT_ENTRY_NAME, resolveRootEntryName(osxIntelArtifact.getEffectivePath()));
                newProps.set(KEY_DISTRIBUTION_CHECKSUM_SHA_256, osxIntelArtifact.getHash(Algorithm.SHA_256));
                multiPlatforms.add(resolveTemplate(flatBinary ? TPL_LINUX_ARM_FLAT_BINARY : TPL_MAC_ARM, newProps));
            }

            if (multiPlatforms.isEmpty()) {
                throw new IllegalStateException(org.jreleaser.bundle.RB.$("ERROR_brew_multiplatform_artifacts"));
            }
            props.set(KEY_BREW_MULTIPLATFORM, passThrough(String.join(System.lineSeparator() + "  ", multiPlatforms)));
        } else if (shouldAddJavaDependency(distribution)) {
            boolean useVersionedJava = isTrue(packager.getExtraProperties().get(USE_VERSIONED_JAVA), true);
            String javaDependency = "openjdk" + (useVersionedJava ? "@" + props.get(KEY_DISTRIBUTION_JAVA_VERSION) : "");
            packager.addDependency(javaDependency);
        }

        props.set(KEY_BREW_DEPENDENCIES, packager.getDependenciesAsList()
            .stream()
            // prevent Mustache from converting quotes into &quot;
            .map(dependency -> passThrough(dependency.toString()))
            .collect(Collectors.toList()));
    }

    private boolean shouldAddJavaDependency(Distribution distribution) {
        if ((distribution.getType() == org.jreleaser.model.Distribution.DistributionType.JAVA_BINARY ||
            distribution.getType() == org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR) &&
            !isTrue(packager.getExtraProperties().get(SKIP_JAVA))) {
            return packager.getDependenciesAsList().stream()
                .map(BrewPackager.Dependency::getKey).noneMatch(BrewPackagerProcessor::isJdkDependency);
        }
        return false;
    }

    private static boolean isJdkDependency(String brewDependency) {
        for (String alias : BREW_JDK_ALIASES) {
            if (brewDependency.equals(alias) || brewDependency.startsWith(alias + "@")) {
                return true;
            }
        }
        return false;
    }

    private String resolveArtifactUrl(Distribution distribution, Artifact artifact) {
        return Artifacts.resolveDownloadUrl(context, org.jreleaser.model.api.packagers.BrewPackager.TYPE, distribution, artifact);
    }

    @Override
    protected void writeFile(Distribution distribution,
                             String content,
                             TemplateContext props,
                             Path outputDirectory,
                             String fileName)
        throws PackagerProcessingException {
        fileName = trimTplExtension(fileName);

        if (packager.getCask().isEnabled()) {
            if (FORMULA_RB.equals(fileName) || FORMULA_MULTI_RB.equals(fileName)) return;
            writeFile(content, CASK_RB.equals(fileName) ?
                outputDirectory.resolve(CASKS).resolve(packager.getCask().getResolvedCaskName(props).concat(RB)) :
                outputDirectory.resolve(fileName));
        } else if (packager.isMultiPlatform()) {
            if (CASK_RB.equals(fileName) || FORMULA_RB.equals(fileName)) return;
            writeFile(content, FORMULA_MULTI_RB.equals(fileName) ?
                outputDirectory.resolve(FORMULA)
                    .resolve(getHyphenatedName(packager.getFormulaName()).concat(RB)) :
                outputDirectory.resolve(fileName));
        } else {
            if (CASK_RB.equals(fileName) || FORMULA_MULTI_RB.equals(fileName)) return;
            writeFile(content, FORMULA_RB.equals(fileName) ?
                outputDirectory.resolve(FORMULA)
                    .resolve(getHyphenatedName(packager.getFormulaName()).concat(RB)) :
                outputDirectory.resolve(fileName));
        }
    }
}
