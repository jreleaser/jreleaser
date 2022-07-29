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
package org.jreleaser.model;

import org.bouncycastle.openpgp.PGPException;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.releaser.spi.Releaser;
import org.jreleaser.util.Constants;
import org.jreleaser.util.Errors;
import org.jreleaser.util.JReleaserException;
import org.jreleaser.util.JReleaserLogger;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.SemVer;
import org.jreleaser.util.StringUtils;
import org.jreleaser.util.signing.FilesKeyring;
import org.jreleaser.util.signing.InMemoryKeyring;
import org.jreleaser.util.signing.Keyring;
import org.jreleaser.util.signing.SigningException;

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

import static org.jreleaser.util.CollectionUtils.safePut;
import static org.jreleaser.util.Constants.KEY_COMMIT_FULL_HASH;
import static org.jreleaser.util.Constants.KEY_COMMIT_SHORT_HASH;
import static org.jreleaser.util.Constants.KEY_MILESTONE_NAME;
import static org.jreleaser.util.Constants.KEY_PLATFORM;
import static org.jreleaser.util.Constants.KEY_PLATFORM_REPLACED;
import static org.jreleaser.util.Constants.KEY_PROJECT_NAME;
import static org.jreleaser.util.Constants.KEY_PROJECT_SNAPSHOT;
import static org.jreleaser.util.Constants.KEY_PROJECT_VERSION;
import static org.jreleaser.util.Constants.KEY_RELEASE_NAME;
import static org.jreleaser.util.Constants.KEY_TAG_NAME;
import static org.jreleaser.util.Constants.KEY_TIMESTAMP;
import static org.jreleaser.util.Constants.KEY_VERSION_BUILD;
import static org.jreleaser.util.Constants.KEY_VERSION_DAY;
import static org.jreleaser.util.Constants.KEY_VERSION_MAJOR;
import static org.jreleaser.util.Constants.KEY_VERSION_MICRO;
import static org.jreleaser.util.Constants.KEY_VERSION_MINOR;
import static org.jreleaser.util.Constants.KEY_VERSION_MODIFIER;
import static org.jreleaser.util.Constants.KEY_VERSION_MONTH;
import static org.jreleaser.util.Constants.KEY_VERSION_NUMBER;
import static org.jreleaser.util.Constants.KEY_VERSION_OPTIONAL;
import static org.jreleaser.util.Constants.KEY_VERSION_PATCH;
import static org.jreleaser.util.Constants.KEY_VERSION_PRERELEASE;
import static org.jreleaser.util.Constants.KEY_VERSION_TAG;
import static org.jreleaser.util.Constants.KEY_VERSION_WEEK;
import static org.jreleaser.util.Constants.KEY_VERSION_YEAR;
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
    private final Mode mode;
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
    private boolean frozen;

    public JReleaserContext(JReleaserLogger logger,
                            Configurer configurer,
                            Mode mode,
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
            logger.debug("- " + Constants.KEY_BASEDIR + " set to " + getBasedir());
            logger.debug("- " + Constants.KEY_BASE_OUTPUT_DIRECTORY + " set to " + getOutputDirectory().getParent());
            logger.debug("- " + Constants.KEY_OUTPUT_DIRECTORY + " set to " + getOutputDirectory());
            logger.debug("- " + Constants.KEY_CHECKSUMS_DIRECTORY + " set to " + getChecksumsDirectory());
            logger.debug("- " + Constants.KEY_SIGNATURES_DIRECTORY + " set to " + getSignaturesDirectory());
            logger.debug("- " + Constants.KEY_PREPARE_DIRECTORY + " set to " + getPrepareDirectory());
            logger.debug("- " + Constants.KEY_PACKAGE_DIRECTORY + " set to " + getPackageDirectory());
            logger.debug("- " + Constants.KEY_DOWNLOAD_DIRECTORY + " set to " + getDownloadDirectory());
            logger.debug("- " + Constants.KEY_ASSEMBLE_DIRECTORY + " set to " + getAssembleDirectory());
            logger.debug("- " + Constants.KEY_ARTIFACTS_DIRECTORY + " set to " + getArtifactsDirectory());
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

    public void freeze() {
        frozen = true;
        model.freeze();
    }

    private void freezeCheck() {
        if (frozen) throw new UnsupportedOperationException();
    }

    private <T> List<T> freezeWrap(List<T> list) {
        return frozen ? Collections.unmodifiableList(list) : list;
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
            JReleaserModelValidator.validate(this, Mode.ASSEMBLE, errors);
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

    public Mode getMode() {
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
        freezeCheck();
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
        return freezeWrap(includedAnnouncers);
    }

    public void setIncludedAnnouncers(List<String> includedAnnouncers) {
        freezeCheck();
        this.includedAnnouncers.clear();
        this.includedAnnouncers.addAll(normalize(includedAnnouncers));
    }

    public List<String> getIncludedAssemblers() {
        return freezeWrap(includedAssemblers);
    }

    public void setIncludedAssemblers(List<String> includedAssemblerTypes) {
        freezeCheck();
        this.includedAssemblers.clear();
        this.includedAssemblers.addAll(normalize(includedAssemblerTypes));
    }

    public List<String> getIncludedDistributions() {
        return freezeWrap(includedDistributions);
    }

    public void setIncludedDistributions(List<String> includedDistributions) {
        freezeCheck();
        this.includedDistributions.clear();
        this.includedDistributions.addAll(includedDistributions);
    }

    public List<String> getIncludedPackagers() {
        return freezeWrap(includedPackagers);
    }

    public void setIncludedPackagers(List<String> includedPackagers) {
        freezeCheck();
        this.includedPackagers.clear();
        this.includedPackagers.addAll(includedPackagers);
    }

    public List<String> getIncludedDownloaderTypes() {
        return freezeWrap(includedDownloaderTypes);
    }

    public void setIncludedDownloaderTypes(List<String> includedDownloaderTypes) {
        freezeCheck();
        this.includedDownloaderTypes.clear();
        this.includedDownloaderTypes.addAll(normalize(includedDownloaderTypes));
    }

    public List<String> getIncludedDownloaderNames() {
        return freezeWrap(includedDownloaderNames);
    }

    public void setIncludedDownloaderNames(List<String> includedDownloaderNames) {
        freezeCheck();
        this.includedDownloaderNames.clear();
        this.includedDownloaderNames.addAll(includedDownloaderNames);
    }

    public List<String> getIncludedUploaderTypes() {
        return freezeWrap(includedUploaderTypes);
    }

    public void setIncludedUploaderTypes(List<String> includedUploaderTypes) {
        freezeCheck();
        this.includedUploaderTypes.clear();
        this.includedUploaderTypes.addAll(normalize(includedUploaderTypes));
    }

    public List<String> getIncludedUploaderNames() {
        return freezeWrap(includedUploaderNames);
    }

    public void setIncludedUploaderNames(List<String> includedUploaderNames) {
        freezeCheck();
        this.includedUploaderNames.clear();
        this.includedUploaderNames.addAll(includedUploaderNames);
    }

    public List<String> getExcludedAnnouncers() {
        return freezeWrap(excludedAnnouncers);
    }

    public void setExcludedAnnouncers(List<String> excludedAnnouncers) {
        freezeCheck();
        this.excludedAnnouncers.clear();
        this.excludedAnnouncers.addAll(normalize(excludedAnnouncers));
    }

    public List<String> getExcludedAssemblers() {
        return freezeWrap(excludedAssemblers);
    }

    public void setExcludedAssemblers(List<String> excludedAssemblerTypes) {
        freezeCheck();
        this.excludedAssemblers.clear();
        this.excludedAssemblers.addAll(normalize(excludedAssemblerTypes));
    }

    public List<String> getExcludedDistributions() {
        return freezeWrap(excludedDistributions);
    }

    public void setExcludedDistributions(List<String> excludedDistributions) {
        freezeCheck();
        this.excludedDistributions.clear();
        this.excludedDistributions.addAll(excludedDistributions);
    }

    public List<String> getExcludedPackagers() {
        return freezeWrap(excludedPackagers);
    }

    public void setExcludedPackagers(List<String> excludedPackagers) {
        freezeCheck();
        this.excludedPackagers.clear();
        this.excludedPackagers.addAll(normalize(excludedPackagers));
    }

    public List<String> getExcludedDownloaderTypes() {
        return freezeWrap(excludedDownloaderTypes);
    }

    public void setExcludedDownloaderTypes(List<String> excludedDownloaderTypes) {
        freezeCheck();
        this.excludedDownloaderTypes.clear();
        this.excludedDownloaderTypes.addAll(normalize(excludedDownloaderTypes));
    }

    public List<String> getExcludedDownloaderNames() {
        return freezeWrap(excludedDownloaderNames);
    }

    public void setExcludedDownloaderNames(List<String> excludedDownloaderNames) {
        freezeCheck();
        this.excludedDownloaderNames.clear();
        this.excludedDownloaderNames.addAll(excludedDownloaderNames);
    }

    public List<String> getExcludedUploaderTypes() {
        return freezeWrap(excludedUploaderTypes);
    }

    public void setExcludedUploaderTypes(List<String> excludedUploaderTypes) {
        freezeCheck();
        this.excludedUploaderTypes.clear();
        this.excludedUploaderTypes.addAll(normalize(excludedUploaderTypes));
    }

    public List<String> getExcludedUploaderNames() {
        return freezeWrap(excludedUploaderNames);
    }

    public void setExcludedUploaderNames(List<String> excludedUploaderNames) {
        freezeCheck();
        this.excludedUploaderNames.clear();
        this.excludedUploaderNames.addAll(excludedUploaderNames);
    }

    public JReleaserCommand getCommand() {
        return command;
    }

    public void setCommand(JReleaserCommand command) {
        freezeCheck();
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
            GitService gitService = model.getRelease().getGitService();
            props.put(KEY_TAG_NAME, gitService.getEffectiveTagName(model));
            if (gitService.isReleaseSupported()) {
                props.put(KEY_RELEASE_NAME, gitService.getEffectiveReleaseName());
                props.put(KEY_MILESTONE_NAME, gitService.getMilestone().getEffectiveName());
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
        logger.warn(message + " since {}. This warning will become an error in a future release.", version);
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

    public enum Mode {
        CONFIG,
        DOWNLOAD,
        ASSEMBLE,
        FULL,
        CHANGELOG;

        public boolean validateDownload() {
            return this == DOWNLOAD;
        }

        public boolean validateAssembly() {
            return this == ASSEMBLE ;
        }

        public boolean validateStandalone() {
            return validateAssembly() || validateDownload();
        }

        public boolean validateConfig() {
            return this == CONFIG || this == FULL;
        }

        public boolean validatePaths() {
            return this == FULL;
        }
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
            int javaMajorVersion = SemVer.javaMajorVersion();
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
            int javaMajorVersion = SemVer.javaMajorVersion();
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
