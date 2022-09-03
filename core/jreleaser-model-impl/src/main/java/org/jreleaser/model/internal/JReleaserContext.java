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
package org.jreleaser.model.internal;

import org.bouncycastle.openpgp.PGPException;
import org.jreleaser.bundle.RB;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.Constants;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.model.Signing;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.internal.assemble.Assembler;
import org.jreleaser.model.internal.assemble.JavaAssembler;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.spi.release.Releaser;
import org.jreleaser.sdk.signing.FilesKeyring;
import org.jreleaser.sdk.signing.InMemoryKeyring;
import org.jreleaser.sdk.signing.Keyring;
import org.jreleaser.sdk.signing.SigningException;
import org.jreleaser.util.Errors;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.StringUtils;
import org.jreleaser.version.SemanticVersion;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;
import static org.jreleaser.model.Constants.KEY_COMMIT_FULL_HASH;
import static org.jreleaser.model.Constants.KEY_COMMIT_SHORT_HASH;
import static org.jreleaser.model.Constants.KEY_MILESTONE_NAME;
import static org.jreleaser.model.Constants.KEY_PLATFORM;
import static org.jreleaser.model.Constants.KEY_PLATFORM_REPLACED;
import static org.jreleaser.model.Constants.KEY_PROJECT_NAME;
import static org.jreleaser.model.Constants.KEY_PROJECT_SNAPSHOT;
import static org.jreleaser.model.Constants.KEY_PROJECT_VERSION;
import static org.jreleaser.model.Constants.KEY_RELEASE_NAME;
import static org.jreleaser.model.Constants.KEY_TAG_NAME;
import static org.jreleaser.model.Constants.KEY_TIMESTAMP;
import static org.jreleaser.model.Constants.KEY_VERSION_BUILD;
import static org.jreleaser.model.Constants.KEY_VERSION_DAY;
import static org.jreleaser.model.Constants.KEY_VERSION_MAJOR;
import static org.jreleaser.model.Constants.KEY_VERSION_MICRO;
import static org.jreleaser.model.Constants.KEY_VERSION_MINOR;
import static org.jreleaser.model.Constants.KEY_VERSION_MODIFIER;
import static org.jreleaser.model.Constants.KEY_VERSION_MONTH;
import static org.jreleaser.model.Constants.KEY_VERSION_NUMBER;
import static org.jreleaser.model.Constants.KEY_VERSION_OPTIONAL;
import static org.jreleaser.model.Constants.KEY_VERSION_PATCH;
import static org.jreleaser.model.Constants.KEY_VERSION_PRERELEASE;
import static org.jreleaser.model.Constants.KEY_VERSION_TAG;
import static org.jreleaser.model.Constants.KEY_VERSION_WEEK;
import static org.jreleaser.model.Constants.KEY_VERSION_YEAR;
import static org.jreleaser.util.CollectionUtils.safePut;
import static org.jreleaser.util.StringUtils.capitalize;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserContext {
    private final JReleaserLogger logger;
    private final JReleaserModel model;
    private final Path basedir;
    private final Path outputDirectory;
    private final boolean dryrun;
    private final boolean gitRootSearch;
    private final org.jreleaser.model.api.JReleaserContext.Mode mode;
    private final Configurer configurer;
    private final Errors errors = new Errors();

    private final List<String> selectedPlatforms = new ArrayList<>();
    private final List<String> includedAnnouncers = new ArrayList<>();
    private final List<String> includedAssemblers = new ArrayList<>();
    private final List<String> includedDistributions = new ArrayList<>();
    private final List<String> includedPackagers = new ArrayList<>();
    private final List<String> includedDownloaderTypes = new ArrayList<>();
    private final List<String> includedDownloaderNames = new ArrayList<>();
    private final List<String> includedUploaderTypes = new ArrayList<>();
    private final List<String> includedUploaderNames = new ArrayList<>();
    private final List<String> excludedAnnouncers = new ArrayList<>();
    private final List<String> excludedAssemblers = new ArrayList<>();
    private final List<String> excludedDistributions = new ArrayList<>();
    private final List<String> excludedPackagers = new ArrayList<>();
    private final List<String> excludedDownloaderTypes = new ArrayList<>();
    private final List<String> excludedDownloaderNames = new ArrayList<>();
    private final List<String> excludedUploaderTypes = new ArrayList<>();
    private final List<String> excludedUploaderNames = new ArrayList<>();

    private String changelog;
    private Releaser releaser;
    private JReleaserCommand command;

    private final org.jreleaser.model.api.JReleaserContext immutable = new org.jreleaser.model.api.JReleaserContext() {
        @Override
        public Path relativize(Path basedir, Path other) {
            return JReleaserContext.this.relativize(basedir, other);
        }

        @Override
        public Path relativizeToBasedir(Path other) {
            return JReleaserContext.this.relativizeToBasedir(other);
        }

        @Override
        public JReleaserLogger getLogger() {
            return logger;
        }

        @Override
        public Mode getMode() {
            return mode;
        }

        @Override
        public org.jreleaser.model.api.JReleaserModel getModel() {
            return model.asImmutable();
        }

        @Override
        public Path getBasedir() {
            return JReleaserContext.this.getBasedir();
        }

        @Override
        public Path getOutputDirectory() {
            return JReleaserContext.this.getOutputDirectory();
        }

        @Override
        public Path getChecksumsDirectory() {
            return JReleaserContext.this.getChecksumsDirectory();
        }

        @Override
        public Path getSignaturesDirectory() {
            return JReleaserContext.this.getSignaturesDirectory();
        }

        @Override
        public Path getPrepareDirectory() {
            return JReleaserContext.this.getPrepareDirectory();
        }

        @Override
        public Path getPackageDirectory() {
            return JReleaserContext.this.getPackageDirectory();
        }

        @Override
        public Path getAssembleDirectory() {
            return JReleaserContext.this.getAssembleDirectory();
        }

        @Override
        public Path getDownloadDirectory() {
            return JReleaserContext.this.getDownloadDirectory();
        }

        @Override
        public Path getArtifactsDirectory() {
            return JReleaserContext.this.getArtifactsDirectory();
        }

        @Override
        public boolean isDryrun() {
            return JReleaserContext.this.isDryrun();
        }

        @Override
        public boolean isGitRootSearch() {
            return JReleaserContext.this.isGitRootSearch();
        }

        @Override
        public List<String> getIncludedAnnouncers() {
            return unmodifiableList(JReleaserContext.this.getIncludedAnnouncers());
        }

        @Override
        public List<String> getIncludedAssemblers() {
            return unmodifiableList(JReleaserContext.this.getIncludedAssemblers());
        }

        @Override
        public List<String> getIncludedDistributions() {
            return unmodifiableList(JReleaserContext.this.getIncludedDistributions());
        }

        @Override
        public List<String> getIncludedPackagers() {
            return unmodifiableList(JReleaserContext.this.getIncludedPackagers());
        }

        @Override
        public List<String> getIncludedDownloaderTypes() {
            return unmodifiableList(JReleaserContext.this.getIncludedDownloaderTypes());
        }

        @Override
        public List<String> getIncludedDownloaderNames() {
            return unmodifiableList(JReleaserContext.this.getIncludedDownloaderNames());
        }

        @Override
        public List<String> getIncludedUploaderTypes() {
            return unmodifiableList(JReleaserContext.this.getIncludedUploaderTypes());
        }

        @Override
        public List<String> getIncludedUploaderNames() {
            return unmodifiableList(JReleaserContext.this.getIncludedUploaderNames());
        }

        @Override
        public List<String> getExcludedAnnouncers() {
            return unmodifiableList(JReleaserContext.this.getExcludedAnnouncers());
        }

        @Override
        public List<String> getExcludedAssemblers() {
            return unmodifiableList(JReleaserContext.this.getExcludedAssemblers());
        }

        @Override
        public List<String> getExcludedDistributions() {
            return unmodifiableList(JReleaserContext.this.getExcludedDistributions());
        }

        @Override
        public List<String> getExcludedPackagers() {
            return unmodifiableList(JReleaserContext.this.getExcludedPackagers());
        }

        @Override
        public List<String> getExcludedDownloaderTypes() {
            return unmodifiableList(JReleaserContext.this.getExcludedDownloaderTypes());
        }

        @Override
        public List<String> getExcludedDownloaderNames() {
            return unmodifiableList(JReleaserContext.this.getExcludedDownloaderNames());
        }

        @Override
        public List<String> getExcludedUploaderTypes() {
            return unmodifiableList(JReleaserContext.this.getExcludedUploaderTypes());
        }

        @Override
        public List<String> getExcludedUploaderNames() {
            return unmodifiableList(JReleaserContext.this.getExcludedUploaderNames());
        }

        @Override
        public JReleaserCommand getCommand() {
            return JReleaserContext.this.getCommand();
        }

        @Override
        public Map<String, Object> props() {
            return JReleaserContext.this.props();
        }

        @Override
        public Map<String, Object> fullProps() {
            return JReleaserContext.this.fullProps();
        }

        @Override
        public void nag(String version, String message) {
            JReleaserContext.this.nag(version, message);
        }
    };

    public JReleaserContext(JReleaserLogger logger,
                            Configurer configurer,
                            org.jreleaser.model.api.JReleaserContext.Mode mode,
                            JReleaserModel model,
                            Path basedir,
                            Path outputDirectory,
                            boolean dryrun,
                            boolean gitRootSearch,
                            List<String> selectedPlatforms) {
        this.logger = logger;
        this.configurer = configurer;
        this.mode = mode;
        this.model = model;
        this.basedir = basedir;
        this.outputDirectory = outputDirectory;
        this.dryrun = dryrun;
        this.gitRootSearch = gitRootSearch;
        this.selectedPlatforms.addAll(selectedPlatforms.stream()
            .filter(PlatformUtils::isSupported)
            .collect(Collectors.toList()));

        try {
            logger.increaseIndent();
            logger.debug(RB.$("context.path.set", Constants.KEY_BASEDIR, getBasedir()));
            logger.debug(RB.$("context.path.set", Constants.KEY_BASE_OUTPUT_DIRECTORY, getOutputDirectory().getParent()));
            logger.debug(RB.$("context.path.set", Constants.KEY_OUTPUT_DIRECTORY, getOutputDirectory()));
            logger.debug(RB.$("context.path.set", Constants.KEY_CHECKSUMS_DIRECTORY, getChecksumsDirectory()));
            logger.debug(RB.$("context.path.set", Constants.KEY_SIGNATURES_DIRECTORY, getSignaturesDirectory()));
            logger.debug(RB.$("context.path.set", Constants.KEY_PREPARE_DIRECTORY, getPrepareDirectory()));
            logger.debug(RB.$("context.path.set", Constants.KEY_PACKAGE_DIRECTORY, getPackageDirectory()));
            logger.debug(RB.$("context.path.set", Constants.KEY_DOWNLOAD_DIRECTORY, getDownloadDirectory()));
            logger.debug(RB.$("context.path.set", Constants.KEY_ASSEMBLE_DIRECTORY, getAssembleDirectory()));
            logger.debug(RB.$("context.path.set", Constants.KEY_ARTIFACTS_DIRECTORY, getArtifactsDirectory()));
        } finally {
            logger.decreaseIndent();
        }

        List<String> unmatchedPlatforms = new ArrayList<>(selectedPlatforms);
        unmatchedPlatforms.removeAll(this.selectedPlatforms);
        if (!unmatchedPlatforms.isEmpty()) {
            logger.warn(RB.$("context.platform.selection.active"));
            logger.error(RB.$("context.platform.selection.no.match"), unmatchedPlatforms);
            logger.error(RB.$("context.platform.selection.valid"),
                System.lineSeparator(), PlatformUtils.getSupportedOsNames(),
                System.lineSeparator(), PlatformUtils.getSupportedOsArchs());
            throw new JReleaserException(RB.$("context.platform.selection.unmatched", unmatchedPlatforms));
        }

        if (!this.selectedPlatforms.isEmpty()) {
            logger.warn(RB.$("context.platform.selection.active"));
            logger.warn(RB.$("context.platform.selection.artifacts"), this.selectedPlatforms);
        }
    }

    public org.jreleaser.model.api.JReleaserContext asImmutable() {
        return immutable;
    }

    public Path relativize(Path basedir, Path other) {
        return basedir.toAbsolutePath().relativize(other.toAbsolutePath());
    }

    public Path relativizeToBasedir(Path other) {
        return relativize(basedir, other);
    }

    public Errors validateModel() {
        if (errors.hasErrors()) return errors;

        this.model.getEnvironment().initProps(this);

        logger.info(RB.$("context.configuration.validation"));

        if (mode.validateConfig()) {
            adjustDistributions();
        }

        try {
            JReleaserModelValidator.validate(this, this.mode, errors);
        } catch (Exception e) {
            logger.trace(e);
            errors.configuration(e.toString());
        }

        if (errors.hasWarnings()) {
            logger.warn("== JReleaser ==");
            errors.logWarnings(logger);
        }
        if (errors.hasErrors()) {
            logger.error("== JReleaser ==");
            errors.logErrors(logger);
        }

        return errors;
    }

    private void adjustDistributions() {
        logger.debug(RB.$("context.adjust.assemblies"));

        // resolve assemblers
        try {
            JReleaserModelValidator.validate(this, org.jreleaser.model.api.JReleaserContext.Mode.ASSEMBLE, errors);
            JReleaserModelResolver.resolve(this, errors);
        } catch (Exception e) {
            logger.trace(e);
            errors.configuration(e.toString());
        }

        // match distributions
        for (Assembler assembler : model.getAssemble().findAllAssemblers()) {
            if (!assembler.isExported()) continue;

            Distribution distribution = model.getDistributions().get(assembler.getName());
            if (null == distribution) {
                distribution = new Distribution();
                distribution.setType(assembler.getDistributionType());
                distribution.setStereotype(assembler.getStereotype());
                distribution.setName(assembler.getName());
                model.getDistributions().put(assembler.getName(), distribution);
            }
            distribution.setName(assembler.getName());
            distribution.setType(assembler.getDistributionType());
            distribution.setActive(assembler.getActive());
            if (assembler instanceof JavaAssembler) {
                distribution.getExecutable().setName(((JavaAssembler) assembler).getExecutable());
                distribution.setJava(((JavaAssembler) assembler).getJava());
            }
            mergeArtifacts(assembler, distribution);

            Map<String, Object> extraProperties = new LinkedHashMap<>(distribution.getExtraProperties());
            extraProperties.putAll(assembler.getExtraProperties());
            distribution.mergeExtraProperties(extraProperties);
        }
    }

    private void mergeArtifacts(Assembler assembler, Distribution distribution) {
        for (Artifact incoming : assembler.getOutputs()) {
            Optional<Artifact> artifact = distribution.getArtifacts().stream()
                .filter(a -> {
                    if (isPlatformSelected(incoming)) incoming.activate();
                    if (isPlatformSelected(a)) a.activate();
                    if (incoming.isActive() && a.isActive()) {
                        Path p1 = incoming.getResolvedPath(this, assembler);
                        Path p2 = a.getResolvedPath(this, distribution);
                        return p1.equals(p2);
                    }
                    return false;
                })
                .findFirst();
            if (artifact.isPresent()) {
                artifact.get().mergeWith(incoming);
            } else {
                distribution.addArtifact(incoming);
            }
        }
    }

    public boolean isPlatformSelected(Artifact artifact) {
        return isPlatformSelected(artifact.getPlatform());
    }

    public boolean isPlatformSelected(String platform) {
        if (isBlank(platform) || selectedPlatforms.isEmpty()) return true;
        return selectedPlatforms.stream()
            .anyMatch(selected -> PlatformUtils.isCompatible(selected, platform));
    }

    public JReleaserLogger getLogger() {
        return logger;
    }

    public Configurer getConfigurer() {
        return configurer;
    }

    public org.jreleaser.model.api.JReleaserContext.Mode getMode() {
        return mode;
    }

    public JReleaserModel getModel() {
        return model;
    }

    public Path getBasedir() {
        return basedir;
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public Path getChecksumsDirectory() {
        return outputDirectory.resolve("checksums");
    }

    public Path getSignaturesDirectory() {
        return outputDirectory.resolve("signatures");
    }

    public Path getPrepareDirectory() {
        return outputDirectory.resolve("prepare");
    }

    public Path getPackageDirectory() {
        return outputDirectory.resolve("package");
    }

    public Path getAssembleDirectory() {
        return outputDirectory.resolve("assemble");
    }

    public Path getDownloadDirectory() {
        return outputDirectory.resolve("download");
    }

    public Path getArtifactsDirectory() {
        return outputDirectory.resolve("artifacts");
    }

    public boolean isDryrun() {
        return dryrun;
    }

    public boolean isGitRootSearch() {
        return gitRootSearch;
    }

    public String getChangelog() {
        return changelog;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }

    public Releaser getReleaser() {
        return releaser;
    }

    public void setReleaser(Releaser releaser) {
        this.releaser = releaser;
    }

    private List<String> normalize(List<String> list) {
        if (list == null || list.isEmpty()) return Collections.emptyList();

        List<String> tmp = new ArrayList<>(list);
        for (int i = 0; i < tmp.size(); i++) {
            String s = tmp.get(i).trim();
            if (!s.contains("-")) {
                s = StringUtils.getHyphenatedName(s);
            }
            tmp.set(i, s.toLowerCase(Locale.ENGLISH));
        }

        return tmp;
    }

    public List<String> getIncludedAnnouncers() {
        return includedAnnouncers;
    }

    public void setIncludedAnnouncers(List<String> includedAnnouncers) {
        this.includedAnnouncers.clear();
        this.includedAnnouncers.addAll(normalize(includedAnnouncers));
    }

    public List<String> getIncludedAssemblers() {
        return includedAssemblers;
    }

    public void setIncludedAssemblers(List<String> includedAssemblerTypes) {
        this.includedAssemblers.clear();
        this.includedAssemblers.addAll(normalize(includedAssemblerTypes));
    }

    public List<String> getIncludedDistributions() {
        return includedDistributions;
    }

    public void setIncludedDistributions(List<String> includedDistributions) {
        this.includedDistributions.clear();
        this.includedDistributions.addAll(includedDistributions);
    }

    public List<String> getIncludedPackagers() {
        return includedPackagers;
    }

    public void setIncludedPackagers(List<String> includedPackagers) {
        this.includedPackagers.clear();
        this.includedPackagers.addAll(includedPackagers);
    }

    public List<String> getIncludedDownloaderTypes() {
        return includedDownloaderTypes;
    }

    public void setIncludedDownloaderTypes(List<String> includedDownloaderTypes) {
        this.includedDownloaderTypes.clear();
        this.includedDownloaderTypes.addAll(normalize(includedDownloaderTypes));
    }

    public List<String> getIncludedDownloaderNames() {
        return includedDownloaderNames;
    }

    public void setIncludedDownloaderNames(List<String> includedDownloaderNames) {
        this.includedDownloaderNames.clear();
        this.includedDownloaderNames.addAll(includedDownloaderNames);
    }

    public List<String> getIncludedUploaderTypes() {
        return includedUploaderTypes;
    }

    public void setIncludedUploaderTypes(List<String> includedUploaderTypes) {
        this.includedUploaderTypes.clear();
        this.includedUploaderTypes.addAll(normalize(includedUploaderTypes));
    }

    public List<String> getIncludedUploaderNames() {
        return includedUploaderNames;
    }

    public void setIncludedUploaderNames(List<String> includedUploaderNames) {
        this.includedUploaderNames.clear();
        this.includedUploaderNames.addAll(includedUploaderNames);
    }

    public List<String> getExcludedAnnouncers() {
        return excludedAnnouncers;
    }

    public void setExcludedAnnouncers(List<String> excludedAnnouncers) {
        this.excludedAnnouncers.clear();
        this.excludedAnnouncers.addAll(normalize(excludedAnnouncers));
    }

    public List<String> getExcludedAssemblers() {
        return excludedAssemblers;
    }

    public void setExcludedAssemblers(List<String> excludedAssemblerTypes) {
        this.excludedAssemblers.clear();
        this.excludedAssemblers.addAll(normalize(excludedAssemblerTypes));
    }

    public List<String> getExcludedDistributions() {
        return excludedDistributions;
    }

    public void setExcludedDistributions(List<String> excludedDistributions) {
        this.excludedDistributions.clear();
        this.excludedDistributions.addAll(excludedDistributions);
    }

    public List<String> getExcludedPackagers() {
        return excludedPackagers;
    }

    public void setExcludedPackagers(List<String> excludedPackagers) {
        this.excludedPackagers.clear();
        this.excludedPackagers.addAll(normalize(excludedPackagers));
    }

    public List<String> getExcludedDownloaderTypes() {
        return excludedDownloaderTypes;
    }

    public void setExcludedDownloaderTypes(List<String> excludedDownloaderTypes) {
        this.excludedDownloaderTypes.clear();
        this.excludedDownloaderTypes.addAll(normalize(excludedDownloaderTypes));
    }

    public List<String> getExcludedDownloaderNames() {
        return excludedDownloaderNames;
    }

    public void setExcludedDownloaderNames(List<String> excludedDownloaderNames) {
        this.excludedDownloaderNames.clear();
        this.excludedDownloaderNames.addAll(excludedDownloaderNames);
    }

    public List<String> getExcludedUploaderTypes() {
        return excludedUploaderTypes;
    }

    public void setExcludedUploaderTypes(List<String> excludedUploaderTypes) {
        this.excludedUploaderTypes.clear();
        this.excludedUploaderTypes.addAll(normalize(excludedUploaderTypes));
    }

    public List<String> getExcludedUploaderNames() {
        return excludedUploaderNames;
    }

    public void setExcludedUploaderNames(List<String> excludedUploaderNames) {
        this.excludedUploaderNames.clear();
        this.excludedUploaderNames.addAll(excludedUploaderNames);
    }

    public JReleaserCommand getCommand() {
        return command;
    }

    public void setCommand(JReleaserCommand command) {
        this.command = command;
    }

    public Map<String, Object> props() {
        Map<String, Object> props = new LinkedHashMap<>(model.props());
        props.put(Constants.KEY_BASEDIR, getBasedir());
        props.put(Constants.KEY_BASE_OUTPUT_DIRECTORY, getOutputDirectory().getParent());
        props.put(Constants.KEY_OUTPUT_DIRECTORY, getOutputDirectory());
        props.put(Constants.KEY_CHECKSUMS_DIRECTORY, getChecksumsDirectory());
        props.put(Constants.KEY_SIGNATURES_DIRECTORY, getSignaturesDirectory());
        props.put(Constants.KEY_PREPARE_DIRECTORY, getPrepareDirectory());
        props.put(Constants.KEY_PACKAGE_DIRECTORY, getPackageDirectory());
        props.put(Constants.KEY_DOWNLOAD_DIRECTORY, getDownloadDirectory());
        props.put(Constants.KEY_ASSEMBLE_DIRECTORY, getAssembleDirectory());
        props.put(Constants.KEY_ARTIFACTS_DIRECTORY, getArtifactsDirectory());
        return props;
    }

    public Map<String, Object> fullProps() {
        LinkedHashMap<String, Object> props = new LinkedHashMap<>(props());
        props.putAll(model.props());
        return props;
    }

    @Override
    public String toString() {
        return "JReleaserContext[" +
            "basedir=" + basedir.toAbsolutePath() +
            ", outputDirectory=" + outputDirectory.toAbsolutePath() +
            ", dryrun=" + dryrun +
            ", gitRootSearch=" + gitRootSearch +
            ", mode=" + mode +
            "]";
    }

    public void report() {
        Project project = model.getProject();

        SortedProperties props = new SortedProperties();
        props.put(KEY_TIMESTAMP, model.getTimestamp());
        props.put(KEY_PLATFORM, PlatformUtils.getCurrentFull());
        props.put(KEY_PLATFORM_REPLACED, model.getPlatform().applyReplacements(PlatformUtils.getCurrentFull()));
        if (model.getCommit() != null) {
            props.put(KEY_COMMIT_SHORT_HASH, model.getCommit().getShortHash());
            props.put(KEY_COMMIT_FULL_HASH, model.getCommit().getFullHash());
        }
        props.put(KEY_PROJECT_NAME, project.getName());
        props.put(KEY_PROJECT_VERSION, project.getVersion());
        props.put(KEY_PROJECT_SNAPSHOT, String.valueOf(project.isSnapshot()));
        if (model.getCommit() != null) {
            BaseReleaser releaser = model.getRelease().getReleaser();
            props.put(KEY_TAG_NAME, releaser.getEffectiveTagName(model));
            if (releaser.isReleaseSupported()) {
                props.put(KEY_RELEASE_NAME, releaser.getEffectiveReleaseName());
                props.put(KEY_MILESTONE_NAME, releaser.getMilestone().getEffectiveName());
            }
        }
        props.put("javaVersion", System.getProperty("java.version"));

        Map<String, Object> resolvedExtraProperties = project.getResolvedExtraProperties();
        safePut(project.getPrefix() + capitalize(KEY_VERSION_MAJOR), resolvedExtraProperties, props);
        safePut(project.getPrefix() + capitalize(KEY_VERSION_MINOR), resolvedExtraProperties, props);
        safePut(project.getPrefix() + capitalize(KEY_VERSION_PATCH), resolvedExtraProperties, props);
        safePut(project.getPrefix() + capitalize(KEY_VERSION_NUMBER), resolvedExtraProperties, props);
        safePut(project.getPrefix() + capitalize(KEY_VERSION_PRERELEASE), resolvedExtraProperties, props);
        safePut(project.getPrefix() + capitalize(KEY_VERSION_TAG), resolvedExtraProperties, props);
        safePut(project.getPrefix() + capitalize(KEY_VERSION_BUILD), resolvedExtraProperties, props);
        safePut(project.getPrefix() + capitalize(KEY_VERSION_OPTIONAL), resolvedExtraProperties, props);
        safePut(project.getPrefix() + capitalize(KEY_VERSION_YEAR), resolvedExtraProperties, props);
        safePut(project.getPrefix() + capitalize(KEY_VERSION_MONTH), resolvedExtraProperties, props);
        safePut(project.getPrefix() + capitalize(KEY_VERSION_DAY), resolvedExtraProperties, props);
        safePut(project.getPrefix() + capitalize(KEY_VERSION_WEEK), resolvedExtraProperties, props);
        safePut(project.getPrefix() + capitalize(KEY_VERSION_MICRO), resolvedExtraProperties, props);
        safePut(project.getPrefix() + capitalize(KEY_VERSION_MODIFIER), resolvedExtraProperties, props);

        Path output = getOutputDirectory().resolve("output.properties");

        try (FileOutputStream out = new FileOutputStream(output.toFile())) {
            logger.info(RB.$("context.writing.properties"),
                relativizeToBasedir(output));
            props.store(out, "JReleaser " + JReleaserVersion.getPlainVersion());
        } catch (IOException ignored) {
            logger.warn(RB.$("context.writing.properties.error"),
                relativizeToBasedir(output));
        }
    }

    public void nag(String version, String message) {
        logger.warn(RB.$("context.nag", message, version));
    }

    public Keyring createKeyring() throws SigningException {
        try {
            if (model.getSigning().getMode() == Signing.Mode.FILE) {
                return new FilesKeyring(
                    basedir.resolve(model.getSigning().getResolvedPublicKey()),
                    basedir.resolve(model.getSigning().getResolvedSecretKey())
                ).initialize(model.getSigning().isArmored());
            }

            return new InMemoryKeyring(
                model.getSigning().getResolvedPublicKey().getBytes(),
                model.getSigning().getResolvedSecretKey().getBytes()
            ).initialize(model.getSigning().isArmored());
        } catch (IOException | PGPException e) {
            throw new SigningException(RB.$("ERROR_signing_init_keyring"), e);
        }
    }

    public boolean isDistributionIncluded(Distribution distribution) {
        String distributionName = distribution.getName();

        if (!includedDistributions.isEmpty()) {
            return includedDistributions.contains(distributionName);
        }

        if (!excludedDistributions.isEmpty()) {
            return !excludedDistributions.contains(distributionName);
        }

        return true;
    }

    public enum Configurer {
        CLI("CLI flags"),
        CLI_YAML("CLI yaml DSL"),
        CLI_TOML("CLI toml DSL"),
        CLI_JSON("CLI json DSL"),
        MAVEN("Maven DSL"),
        GRADLE("Gradle DSL");

        private final String dsl;

        Configurer(String dsl) {
            this.dsl = dsl;
        }

        @Override
        public String toString() {
            return this.dsl;
        }
    }

    private static class SortedProperties extends Properties {
        // Java 11 calls entrySet() when storing properties
        @Override
        public Set<Map.Entry<Object, Object>> entrySet() {
            int javaMajorVersion = SemanticVersion.javaMajorVersion();
            if (javaMajorVersion < 11) {
                return super.entrySet();
            }

            Map<Object, Object> map = new TreeMap<>();
            for (Object k : keySet()) {
                map.put(String.valueOf(k), get(k));
            }
            return map.entrySet();
        }

        // Java 8 calls keys() when storing properties
        @Override
        public synchronized Enumeration<Object> keys() {
            int javaMajorVersion = SemanticVersion.javaMajorVersion();
            if (javaMajorVersion >= 11) {
                return super.keys();
            }

            Set<Object> keySet = keySet();
            List<String> keys = new ArrayList<>(keySet.size());
            for (Object key : keySet) {
                keys.add(key.toString());
            }
            Collections.sort(keys);
            return new IteratorEnumeration<>(keys.iterator());
        }
    }

    private static class IteratorEnumeration<E> implements Enumeration<E> {
        private final Iterator<? extends E> iterator;

        public IteratorEnumeration(Iterator<? extends E> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasMoreElements() {
            return iterator.hasNext();
        }

        @Override
        public E nextElement() {
            return iterator.next();
        }
    }
}
