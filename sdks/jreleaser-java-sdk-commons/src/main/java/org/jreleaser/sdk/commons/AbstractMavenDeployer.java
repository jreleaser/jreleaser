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
package org.jreleaser.sdk.commons;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.api.signing.SigningException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.deploy.maven.MavenDeployer;
import org.jreleaser.sdk.command.CommandException;
import org.jreleaser.sdk.signing.SigningUtils;
import org.jreleaser.sdk.tool.PomChecker;
import org.jreleaser.sdk.tool.ToolException;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.ChecksumUtils;
import org.jreleaser.util.DefaultVersions;
import org.jreleaser.util.Errors;
import org.jreleaser.util.IoUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.FileVisitResult.CONTINUE;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.4.0
 */
public abstract class AbstractMavenDeployer<A extends org.jreleaser.model.api.deploy.maven.MavenDeployer,
    D extends org.jreleaser.model.internal.deploy.maven.MavenDeployer<A>> implements MavenDeployer<A, D> {
    private static final Algorithm[] ALGORITHMS = {
        Algorithm.MD5,
        Algorithm.SHA_1,
        Algorithm.SHA_256,
        Algorithm.SHA_512
    };

    private static final String PACKAGING_JAR = "jar";
    private static final String PACKAGING_POM = "pom";
    private static final String PACKAGING_MAVEN_ARCHETYPE = "maven-archetype";

    protected final JReleaserContext context;

    protected AbstractMavenDeployer(JReleaserContext context) {
        this.context = context;
    }

    protected Set<Deployable> collectDeployables() {
        Set<Deployable> deployables = new TreeSet<>();

        for (String stagingRepository : getDeployer().getStagingRepositories()) {
            Path root = context.getBasedir().resolve(stagingRepository).normalize();

            if (!Files.exists(root)) {
                throw new JReleaserException(RB.$("validation_directory_not_exist",
                    "maven." + getDeployer().getType() + "." + getDeployer().getName() + ".stagingRepository",
                    context.relativizeToBasedir(root).toString()));
            }

            if (!root.toFile().isDirectory()) {
                throw new JReleaserException(RB.$("validation_is_not_a_directory",
                    "maven." + getDeployer().getType() + "." + getDeployer().getName() + ".stagingRepository",
                    context.relativizeToBasedir(root).toString()));
            }

            try {
                DeployableCollector collector = new DeployableCollector(root);

                java.nio.file.Files.walkFileTree(root, collector);
                if (collector.failed) {
                    throw new JReleaserException(RB.$("ERROR_deployer_stage_resolution"));
                }

                deployables.addAll(collector.deployables);
            } catch (IOException e) {
                throw new JReleaserException(RB.$("ERROR_deployer_unexpected_error_stage"), e);
            }
        }

        Map<String, Deployable> deployablesMap = deployables.stream()
            .collect(Collectors.toMap(Deployable::getFilename, Function.identity()));

        Errors errors = new Errors();
        checkMavenCentralRules(deployablesMap, errors);
        if (errors.hasErrors()) {
            errors.logErrors(context.getLogger());
            throw new JReleaserException(RB.$("ERROR_deployer_maven_central_rules"));
        }

        signDeployables(deployablesMap, deployables);
        checksumDeployables(deployablesMap, deployables);

        return deployables;
    }

    private void checkMavenCentralRules(Map<String, Deployable> deployablesMap, Errors errors) {
        if (!getDeployer().isApplyMavenCentralRules()) {
            return;
        }

        // 1st check jar, sources, javadoc if applicable
        for (Deployable deployable : deployablesMap.values()) {
            if (!deployable.getFilename().endsWith("." + PACKAGING_POM)) {
                continue;
            }

            String base = deployable.getFilename();
            base = base.substring(0, base.length() - 4);

            if (deployable.requiresJar()) {
                Deployable derived = deployable.deriveByFilename(PACKAGING_JAR, base + ".jar");
                if (!deployablesMap.containsKey(derived.getFilename())) {
                    errors.configuration(RB.$("validation_is_missing", derived.getFilename()));
                }
            }

            if (deployable.requiresSourcesJar()) {
                Deployable derived = deployable.deriveByFilename(PACKAGING_JAR, base + "-sources.jar");
                if (!deployablesMap.containsKey(derived.getFilename())) {
                    errors.configuration(RB.$("validation_is_missing", derived.getFilename()));
                }
            }

            if (deployable.requiresJavadocJar()) {
                Deployable derived = deployable.deriveByFilename(PACKAGING_JAR, base + "-javadoc.jar");
                if (!deployablesMap.containsKey(derived.getFilename())) {
                    errors.configuration(RB.$("validation_is_missing", derived.getFilename()));
                }
            }
        }

        if (!getDeployer().isVerifyPom()) {
            return;
        }

        PomChecker pomChecker = new PomChecker(context.asImmutable(), DefaultVersions.getInstance().getPomcheckerVersion());
        try {
            if (!pomChecker.setup()) {
                context.getLogger().warn(RB.$("tool_unavailable", "pomchecker"));
                return;
            }
        } catch (ToolException e) {
            context.getLogger().warn(RB.$("tool_unavailable", "pomchecker"), e);
            return;
        }

        // 2nd check pom
        for (Deployable deployable : deployablesMap.values()) {
            if (!deployable.getFilename().endsWith("." + PACKAGING_POM)) {
                continue;
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();

            List<String> args = new ArrayList<>();
            args.add("check-maven-central");
            args.add("--quiet");
            if (context.getModel().getProject().isSnapshot() &&
                getDeployer().isSnapshotAllowed()) {
                args.add("--no-release");
            }
            args.add("--file");
            args.add(deployable.getLocalPath().toAbsolutePath().toString());
            try {
                pomChecker.invoke(context.getBasedir(), args, out, err);
            } catch (CommandException e) {
                String plumbing = IoUtils.toString(err).trim();
                String validation = IoUtils.toString(out).trim();

                // 1st check out -> validation issues
                if (isNotBlank(validation)) {
                    errors.configuration(validation);
                } else if (isNotBlank(plumbing)) {
                    // 2nd check err -> plumbing issues
                    errors.configuration(plumbing);
                } else {
                    // command failed and we've got no clue!
                    errors.configuration(e.getMessage());
                }
            }
        }
    }

    private void signDeployables(Map<String, Deployable> deployablesMap, Set<Deployable> deployables) {
        if (!getDeployer().isSign()) {
            return;
        }

        for (Deployable deployable : deployablesMap.values()) {
            if (!deployable.getFilename().endsWith(".jar") &&
                !deployable.getFilename().endsWith(".pom")) {
                continue;
            }

            Deployable signedDeployable = deployable.deriveByFilename(deployable.getFilename() + ".asc");
            if (deployablesMap.containsKey(signedDeployable.getFilename())) {
                continue;
            }

            try {
                context.getLogger().setPrefix("sign");
                SigningUtils.sign(context.asImmutable(), deployable.getLocalPath());
                deployables.add(signedDeployable);
            } catch (SigningException e) {
                throw new JReleaserException(RB.$("ERROR_unexpected_error_signing_file", deployable.getFilename()), e);
            } finally {
                context.getLogger().restorePrefix();
            }
        }
    }

    private void checksumDeployables(Map<String, Deployable> deployablesMap, Set<Deployable> deployables) {
        for (Deployable deployable : deployablesMap.values()) {
            if (!deployable.getFilename().endsWith(".jar") &&
                !deployable.getFilename().endsWith(".pom") &&
                !deployable.getFilename().endsWith(".asc")) {
                continue;
            }

            if (deployable.getFilename().endsWith(".asc")) {
                // remove checksum for signature files
                for (Algorithm algorithm : ALGORITHMS) {
                    Deployable checksumDeployable = deployable.deriveByFilename(deployable.getFilename() + "." + algorithm.formatted());
                    deployables.remove(checksumDeployable);
                }
                continue;
            }

            try {
                byte[] data = Files.readAllBytes(deployable.getLocalPath());
                for (Algorithm algorithm : ALGORITHMS) {
                    Deployable checksumDeployable = deployable.deriveByFilename(deployable.getFilename() + "." + algorithm.formatted());

                    if (deployablesMap.containsKey(checksumDeployable.getFilename())) {
                        continue;
                    }

                    context.getLogger().debug(RB.$("checksum.calculating", algorithm.formatted(), deployable.getFilename()));
                    String checksum = ChecksumUtils.checksum(algorithm, data);
                    Files.write(checksumDeployable.getLocalPath(), checksum.getBytes(UTF_8));
                    deployables.add(checksumDeployable);
                }
            } catch (IOException e) {
                throw new JReleaserException(RB.$("ERROR_unexpected_error_calculate_checksum", deployable.getFilename()), e);
            }
        }
    }

    public static class Deployable implements Comparable<Deployable> {
        private final String stagingRepository;
        private final String path;
        private final String filename;
        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String packaging;

        public Deployable(String stagingRepository, String path, String filename) {
            this(stagingRepository, path, PACKAGING_JAR, filename);
        }

        public Deployable(String stagingRepository, String path, String packaging, String filename) {
            this.stagingRepository = stagingRepository;
            this.path = path;
            this.filename = filename;
            this.packaging = packaging;

            Path p = Paths.get(path);
            this.version = p.getFileName().toString();
            p = p.getParent();
            this.artifactId = p.getFileName().toString();
            p = p.getParent();
            String gid = p.toString()
                .replace("/", ".")
                .replace("\\", ".");
            if (gid.startsWith(".")) {
                gid = gid.substring(1);
            }
            this.groupId = gid;
        }

        public boolean requiresJar() {
            return !PACKAGING_POM.equals(packaging);
        }

        public boolean requiresSourcesJar() {
            return !PACKAGING_POM.equals(packaging);
        }

        public boolean requiresJavadocJar() {
            return !PACKAGING_POM.equals(packaging) && !PACKAGING_MAVEN_ARCHETYPE.equals(packaging);
        }

        public String getGav() {
            return groupId + ":" + artifactId + ":" + version;
        }

        public String getStagingRepository() {
            return stagingRepository;
        }

        public String getPath() {
            return path;
        }

        public String getDeployPath() {
            return path.replace("\\", "/");
        }

        public String getFilename() {
            return filename;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getVersion() {
            return version;
        }

        public Path getLocalPath() {
            return Paths.get(stagingRepository, path, filename);
        }

        public Deployable deriveByFilename(String filename) {
            return new Deployable(stagingRepository, path, packaging, filename);
        }

        public Deployable deriveByFilename(String packaging, String filename) {
            return new Deployable(stagingRepository, path, packaging, filename);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Deployable that = (Deployable) o;
            return stagingRepository.equals(that.stagingRepository) &&
                path.equals(that.path) &&
                filename.equals(that.filename);
        }

        @Override
        public int hashCode() {
            return Objects.hash(stagingRepository, path, filename);
        }

        @Override
        public int compareTo(Deployable o) {
            if (o == null) return -1;
            return filename.compareTo(o.filename);
        }
    }

    private class DeployableCollector extends SimpleFileVisitor<Path> {
        private final Path root;
        private final Set<Deployable> deployables = new TreeSet<>();
        private final List<PathMatcher> matchers = new ArrayList<>();
        private boolean failed;

        public DeployableCollector(Path root) {
            this.root = root;

            FileSystem fileSystem = FileSystems.getDefault();
            String[] extensions = {".jar", ".jar.asc", ".pom", ".pom.asc"};
            String[] checksums = {".md5", ".sha1", ".sha256", ".sha512"};
            for (String ext : extensions) {
                matchers.add(fileSystem.getPathMatcher("glob:**/*" + ext));
                for (String cs : checksums) {
                    matchers.add(fileSystem.getPathMatcher("glob:**/*" + ext + cs));
                }
            }
        }

        private void match(Path path) {
            if (matchers.stream()
                .anyMatch(matcher -> matcher.matches(path))) {
                String stagingRepository = root.toAbsolutePath().toString();
                String stagingPath = path.getParent().toAbsolutePath().toString();
                deployables.add(new Deployable(
                    stagingRepository,
                    stagingPath.substring(stagingRepository.length()),
                    resolvePackaging(path),
                    path.getFileName().toString()
                ));
            }
        }

        private String resolvePackaging(Path artifactPath) {
            // only inspect if artifactPath ends with .pom
            if (!artifactPath.getFileName().toString().endsWith("." + PACKAGING_POM)) return PACKAGING_JAR;

            try {
                Document document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(artifactPath.toFile());
                String query = "/project/packaging";
                String packaging = (String) XPathFactory.newInstance()
                    .newXPath()
                    .compile(query)
                    .evaluate(document, XPathConstants.STRING);
                return isNotBlank(packaging) ? packaging.trim() : PACKAGING_JAR;
            } catch (ParserConfigurationException | IOException | SAXException | XPathExpressionException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            match(file);
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
            failed = true;
            context.getLogger().trace(e);
            context.getLogger().error(RB.$("ERROR_artifacts_unexpected_error_path"),
                root.toAbsolutePath().relativize(file.toAbsolutePath()), e);
            return CONTINUE;
        }
    }
}
