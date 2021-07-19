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
package org.jreleaser.model;

import org.bouncycastle.openpgp.PGPException;
import org.jreleaser.util.Constants;
import org.jreleaser.util.Errors;
import org.jreleaser.util.JReleaserLogger;
import org.jreleaser.util.Version;
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
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import static org.jreleaser.util.CollectionUtils.safePut;
import static org.jreleaser.util.Constants.KEY_COMMIT_FULL_HASH;
import static org.jreleaser.util.Constants.KEY_COMMIT_SHORT_HASH;
import static org.jreleaser.util.Constants.KEY_MILESTONE_NAME;
import static org.jreleaser.util.Constants.KEY_PROJECT_NAME;
import static org.jreleaser.util.Constants.KEY_PROJECT_SNAPSHOT;
import static org.jreleaser.util.Constants.KEY_PROJECT_VERSION;
import static org.jreleaser.util.Constants.KEY_RELEASE_NAME;
import static org.jreleaser.util.Constants.KEY_TAG_NAME;
import static org.jreleaser.util.Constants.KEY_TIMESTAMP;
import static org.jreleaser.util.Constants.KEY_VERSION_BUILD;
import static org.jreleaser.util.Constants.KEY_VERSION_MAJOR;
import static org.jreleaser.util.Constants.KEY_VERSION_MINOR;
import static org.jreleaser.util.Constants.KEY_VERSION_NUMBER;
import static org.jreleaser.util.Constants.KEY_VERSION_PATCH;
import static org.jreleaser.util.Constants.KEY_VERSION_PRERELEASE;
import static org.jreleaser.util.Constants.KEY_VERSION_TAG;
import static org.jreleaser.util.StringUtils.capitalize;
import static org.jreleaser.util.StringUtils.isNotBlank;

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
    private final Errors errors = new Errors();

    private String distributionName;
    private String toolName;
    private String announcerName;
    private String uploaderName;
    private String uploaderType;
    private String assemblerName;
    private String changelog;

    public JReleaserContext(JReleaserLogger logger,
                            Mode mode,
                            JReleaserModel model,
                            Path basedir,
                            Path outputDirectory,
                            boolean dryrun,
                            boolean gitRootSearch) {
        this.logger = logger;
        this.mode = mode;
        this.model = model;
        this.basedir = basedir;
        this.outputDirectory = outputDirectory;
        this.dryrun = dryrun;
        this.gitRootSearch = gitRootSearch;
    }

    public Path relativizeToBasedir(Path other) {
        return basedir.toAbsolutePath().relativize(other.toAbsolutePath());
    }

    public Errors validateModel() {
        if (errors.hasErrors()) return errors;

        this.model.getEnvironment().initProps(this);

        logger.info("Validating configuration");

        if (mode == Mode.FULL) {
            adjustDistributions();
        }

        try {
            JReleaserModelValidator.validate(this, this.mode, errors);
        } catch (Exception e) {
            logger.trace(e);
            errors.configuration(e.toString());
        }

        if (errors.hasErrors()) {
            logger.error("== JReleaser ==");
            errors.logErrors(logger);
        }

        return errors;
    }

    private void adjustDistributions() {
        logger.debug("adjusting distributions with assemblies");

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
                distribution.setType(assembler.getType());
                distribution.setName(assembler.getName());
                model.getDistributions().put(assembler.getName(), distribution);
            }
            distribution.setName(assembler.getName());
            distribution.setType(assembler.getType());
            distribution.setExecutable(assembler.getExecutable());
            distribution.setActive(assembler.getActive());
            distribution.setJava(assembler.getJava());
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
                    Path p1 = incoming.getResolvedPath(this, assembler);
                    Path p2 = a.getResolvedPath(this, distribution);
                    return p1.equals(p2);
                })
                .findFirst();
            if (artifact.isPresent()) {
                artifact.get().merge(incoming);
            } else {
                distribution.addArtifact(incoming);
            }
        }
    }

    public JReleaserLogger getLogger() {
        return logger;
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

    public boolean hasDistributionName() {
        return isNotBlank(distributionName);
    }

    public boolean hasToolName() {
        return isNotBlank(toolName);
    }

    public boolean hasAnnouncerName() {
        return isNotBlank(announcerName);
    }

    public boolean hasUploaderName() {
        return isNotBlank(uploaderName);
    }

    public boolean hasUploaderType() {
        return isNotBlank(uploaderType);
    }

    public boolean hasAssemblerName() {
        return isNotBlank(assemblerName);
    }

    public String getDistributionName() {
        return distributionName;
    }

    public void setDistributionName(String distributionName) {
        this.distributionName = distributionName;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getAnnouncerName() {
        return announcerName;
    }

    public void setAnnouncerName(String announcerName) {
        this.announcerName = announcerName;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public String getUploaderType() {
        return uploaderType;
    }

    public void setUploaderType(String uploaderType) {
        this.uploaderType = uploaderType;
    }

    public String getAssemblerName() {
        return assemblerName;
    }

    public void setAssemblerName(String assemblerName) {
        this.assemblerName = assemblerName;
    }

    public Map<String, Object> props() {
        Map<String, Object> props = new LinkedHashMap<>(model.props());
        props.put(Constants.KEY_OUTPUT_DIRECTORY, getOutputDirectory());
        props.put(Constants.KEY_CHECKSUMS_DIRECTORY, getChecksumsDirectory());
        props.put(Constants.KEY_SIGNATURES_DIRECTORY, getSignaturesDirectory());
        props.put(Constants.KEY_PREPARE_DIRECTORY, getPrepareDirectory());
        props.put(Constants.KEY_PACKAGE_DIRECTORY, getPackageDirectory());
        props.put(Constants.KEY_ASSEMBLE_DIRECTORY, getAssembleDirectory());
        props.put(Constants.KEY_ARTIFACTS_DIRECTORY, getArtifactsDirectory());
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
        props.put(KEY_COMMIT_SHORT_HASH, model.getCommit().getShortHash());
        props.put(KEY_COMMIT_FULL_HASH, model.getCommit().getFullHash());
        props.put(KEY_PROJECT_NAME, project.getName());
        props.put(KEY_PROJECT_VERSION, project.getVersion());
        props.put(KEY_PROJECT_SNAPSHOT, String.valueOf(project.isSnapshot()));
        GitService gitService = model.getRelease().getGitService();
        props.put(KEY_TAG_NAME, gitService.getEffectiveTagName(model));
        if (gitService.isReleaseSupported()) {
            props.put(KEY_RELEASE_NAME, gitService.getEffectiveReleaseName());
            props.put(KEY_MILESTONE_NAME, gitService.getMilestone().getEffectiveName());
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

        Path output = getOutputDirectory().resolve("output.properties");

        try (FileOutputStream out = new FileOutputStream(output.toFile())) {
            logger.info("Writing output properties to {}",
                relativizeToBasedir(output));
            props.store(out, "JReleaser " + JReleaserVersion.getPlainVersion());
        } catch (IOException ignored) {
            logger.warn("Could not write output properties to {}",
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
            throw new SigningException("Could not initialize keyring", e);
        }
    }

    public enum Mode {
        ASSEMBLE,
        FULL
    }

    private static class SortedProperties extends Properties {
        // Java 11 calls entrySet() when storing properties
        @Override
        public Set<Map.Entry<Object, Object>> entrySet() {
            int javaMajorVersion = Version.javaMajorVersion();
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
            int javaMajorVersion = Version.javaMajorVersion();
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
