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
package org.jreleaser.sdk.commons;

import feign.form.FormData;
import org.apache.commons.io.IOUtils;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.api.signing.SigningException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.deploy.maven.Maven;
import org.jreleaser.model.spi.deploy.DeployException;
import org.jreleaser.model.spi.deploy.maven.Deployable;
import org.jreleaser.model.spi.deploy.maven.MavenDeployer;
import org.jreleaser.model.spi.upload.UploadException;
import org.jreleaser.sdk.command.Command;
import org.jreleaser.sdk.command.CommandException;
import org.jreleaser.sdk.signing.SigningUtils;
import org.jreleaser.sdk.tool.PomChecker;
import org.jreleaser.sdk.tool.ToolException;
import org.jreleaser.util.Algorithm;
import org.jreleaser.util.ChecksumUtils;
import org.jreleaser.util.CollectionUtils;
import org.jreleaser.util.Errors;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.FileVisitResult.CONTINUE;
import static org.jreleaser.model.spi.deploy.maven.Deployable.EXT_ASC;
import static org.jreleaser.model.spi.deploy.maven.Deployable.EXT_JAR;
import static org.jreleaser.model.spi.deploy.maven.Deployable.EXT_POM;
import static org.jreleaser.model.spi.deploy.maven.Deployable.EXT_WAR;
import static org.jreleaser.model.spi.deploy.maven.Deployable.MAVEN_METADATA_XML;
import static org.jreleaser.model.spi.deploy.maven.Deployable.PACKAGING_JAR;
import static org.jreleaser.model.spi.deploy.maven.Deployable.PACKAGING_WAR;
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

    private static final String BUILD_TAG = "-build";
    private static final Map<String, String> KEY_SERVERS = CollectionUtils.<String, String>map()
        .e("https://keys.openpgp.org", "https://keys.openpgp.org/search?q=%s")
        .e("https://keyserver.ubuntu.com", "https://keyserver.ubuntu.com/pks/lookup?search=%s&fingerprint=on&options=mr&op=index")
        .e("https://pgp.mit.edu", "https://pgp.mit.edu/pks/lookup?op=get&search=0x%s");

    protected final JReleaserContext context;

    protected AbstractMavenDeployer(JReleaserContext context) {
        this.context = context;
    }

    protected Set<Deployable> collectDeployableArtifacts() {
        Set<Deployable> deployables = new TreeSet<>();
        for (String stagingRepository : getDeployer().getStagingRepositories()) {
            collectDeployables(deployables, stagingRepository);
        }

        return deployables;
    }

    protected Set<Deployable> collectDeployables() {
        Set<Deployable> deployables = collectDeployableArtifacts();

        Map<String, Deployable> deployablesMap = deployables.stream()
            .collect(Collectors.toMap(Deployable::getFullDeployPath, Function.identity()));

        Errors errors = checkMavenCentralRules(deployablesMap);
        if (errors.hasErrors()) {
            errors.logErrors(context.getLogger());
            throw new JReleaserException(RB.$("ERROR_deployer_maven_central_rules"));
        }

        signDeployables(deployablesMap, deployables);
        checksumDeployables(deployablesMap, deployables);

        return deployables;
    }

    public Set<Deployable> collectDeployables(Set<Deployable> deployables, String stagingRepository) {
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
            DeployableCollector collector = new DeployableCollector(root, context.getModel().getProject().isSnapshot());

            java.nio.file.Files.walkFileTree(root, collector);
            if (collector.failed) {
                throw new JReleaserException(RB.$("ERROR_deployer_stage_resolution"));
            }

            deployables.addAll(collector.deployables);
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_deployer_unexpected_error_stage"), e);
        }

        return deployables;
    }

    private Errors checkMavenCentralRules(Map<String, Deployable> deployablesMap) {
        Errors errors = new Errors();

        context.getLogger().info(RB.$("deployers.maven.prerequisites"));

        Map<String, Deployable> buildPoms = new LinkedHashMap<>();

        // 1st check jar, sources, javadoc if applicable
        for (Deployable deployable : deployablesMap.values()) {
            if (!deployable.getFilename().endsWith(EXT_POM)) {
                continue;
            }

            String base = deployable.getFilename();
            base = base.substring(0, base.length() - 4);

            boolean buildPom = false;
            if (base.endsWith(BUILD_TAG)) {
                Deployable baseDeployable = deployable.deriveByFilename(base.substring(0, base.length() - BUILD_TAG.length()) + EXT_POM);
                if (deployablesMap.containsKey(baseDeployable.getFullDeployPath())) {
                    buildPom = true;
                    buildPoms.put(deployable.getFullDeployPath(), deployable);
                }
            }

            if (!buildPom) {
                if (requiresJar(deployable)) {
                    Deployable derived = deployable.deriveByFilename(PACKAGING_JAR, base + EXT_JAR);
                    if (!deployablesMap.containsKey(derived.getFullDeployPath())) {
                        errors.configuration(RB.$("validation_is_missing", derived.getFilename()));
                    }
                }

                if (!deployable.isRelocated() && deployable.requiresWar()) {
                    Deployable derived = deployable.deriveByFilename(PACKAGING_WAR, base + EXT_WAR);
                    if (!deployablesMap.containsKey(derived.getFullDeployPath())) {
                        errors.configuration(RB.$("validation_is_missing", derived.getFilename()));
                    }
                }

                if (!deployable.isRelocated() && requiresSourcesJar(deployable, base)) {
                    Deployable derived = deployable.deriveByFilename(PACKAGING_JAR, base + "-sources.jar");
                    if (!deployablesMap.containsKey(derived.getFullDeployPath())) {
                        errors.configuration(RB.$("validation_is_missing", derived.getFilename()));
                    }
                }

                if (!deployable.isRelocated() && requiresJavadocJar(deployable, base)) {
                    Deployable derived = deployable.deriveByFilename(PACKAGING_JAR, base + "-javadoc.jar");
                    if (!deployablesMap.containsKey(derived.getFullDeployPath())) {
                        errors.configuration(RB.$("validation_is_missing", derived.getFilename()));
                    }
                }
            }
        }

        if (!getDeployer().isVerifyPom()) {
            return errors;
        }

        context.getLogger().info(RB.$("deployers.maven.verify.poms"));

        Maven.Pomchecker pomcheckerModel = context.getModel().getDeploy().getMaven().getPomchecker();
        PomChecker pomChecker = new PomChecker(context.asImmutable(),
            pomcheckerModel.getVersion());
        try {
            if (!pomChecker.setup()) {
                context.getLogger().warn(RB.$("tool_unavailable", "pomchecker"));
                return errors;
            }
        } catch (ToolException e) {
            context.getLogger().warn(RB.$("tool_unavailable", "pomchecker"), e);
            return errors;
        }

        Set<String> paths = new LinkedHashSet<>();
        for (String stagingRepository : getDeployer().getStagingRepositories()) {
            paths.add(context.getBasedir().resolve(stagingRepository)
                .toAbsolutePath()
                .normalize()
                .toString());
        }

        // 2nd check pom
        for (Deployable deployable : deployablesMap.values()) {
            if (!deployable.getFilename().endsWith(EXT_POM) || buildPoms.containsKey(deployable.getFullDeployPath()) ||
                !requiresPomVerification(deployable)) {
                continue;
            }

            List<String> args = new ArrayList<>();
            args.add("check-maven-central");
            args.add("--quiet");
            if (context.getModel().getProject().isSnapshot() &&
                getDeployer().isSnapshotSupported()) {
                args.add("--no-release");
            }
            if (pomChecker.isVersionCompatibleWith("1.9.0")) {
                if (!pomcheckerModel.isFailOnWarning()) {
                    args.add("--no-fail-on-warning");
                }
                if (!pomcheckerModel.isFailOnError()) {
                    args.add("--no-fail-on-error");
                }
            }

            if (pomChecker.isVersionCompatibleWith("1.13.0")) {
                for (String path : paths) {
                    args.add("--repository");
                    args.add(path);
                }
            }

            if (pomcheckerModel.isStrict()) {
                args.add("--strict");
            } else {
                args.add("--no-strict");
            }

            args.add("--file");
            args.add(deployable.getLocalPath().toAbsolutePath().toString());

            context.getLogger().debug(RB.$("deployers.maven.verify.pom", deployable.getLocalPath()));

            Command.Result result = Command.Result.empty();
            try {
                result = pomChecker.check(context.getBasedir(), args);
            } catch (CommandException e) {
                handlePomcheckerResult(deployable.getLocalPath().getFileName().toString(), result, e, errors);
            }

            if (result.getExitValue() != 0) {
                handlePomcheckerResult(deployable.getLocalPath().getFileName().toString(), result, null, errors);
            }
        }

        return errors;
    }

    private void handlePomcheckerResult(String filename, Command.Result result, CommandException e, Errors errors) {
        String plumbing = result.getErr();
        String validation = result.getOut();

        // 1st check out -> validation issues
        if (isNotBlank(validation)) {
            errors.configuration(RB.$("ERROR_deployer_pomchecker_header", filename, validation));
        } else if (isNotBlank(plumbing)) {
            // 2nd check err -> plumbing issues
            errors.configuration(RB.$("ERROR_deployer_pomchecker_header", filename, plumbing));
        } else if (null != e) {
            // command failed and we've got no clue!
            errors.configuration(e.getMessage());
        }
    }

    private boolean requiresJar(Deployable deployable) {
        if (!deployable.requiresJar()) return false;

        Optional<org.jreleaser.model.internal.deploy.maven.MavenDeployer.ArtifactOverride> override = getDeployer().getArtifactOverrides().stream()
            .filter(a -> a.getGroupId().equals(deployable.getGroupId()) && a.getArtifactId().equals(deployable.getArtifactId()))
            .findFirst();

        if (override.isPresent() && (override.get().isJarSet())) return override.get().isJar();

        return !deployable.isRelocated();
    }

    private boolean requiresSourcesJar(Deployable deployable, String baseFilename) {
        if (!deployable.requiresSourcesJar()) return false;

        Optional<org.jreleaser.model.internal.deploy.maven.MavenDeployer.ArtifactOverride> override = getDeployer().getArtifactOverrides().stream()
            .filter(a -> a.getGroupId().equals(deployable.getGroupId()) && a.getArtifactId().equals(deployable.getArtifactId()))
            .findFirst();

        if (override.isPresent() && (override.get().isSourceJarSet())) return override.get().isSourceJar();

        if (!getDeployer().isSourceJarSet()) {
            return hasJavaClass(deployable, baseFilename);
        }

        return getDeployer().isSourceJar();
    }

    private boolean requiresJavadocJar(Deployable deployable, String baseFilename) {
        if (!deployable.requiresJavadocJar()) return false;

        Optional<org.jreleaser.model.internal.deploy.maven.MavenDeployer.ArtifactOverride> override = getDeployer().getArtifactOverrides().stream()
            .filter(a -> a.getGroupId().equals(deployable.getGroupId()) && a.getArtifactId().equals(deployable.getArtifactId()))
            .findFirst();

        if (override.isPresent() && (override.get().isJavadocJarSet())) return override.get().isJavadocJar();

        if (!getDeployer().isJavadocJarSet()) {
            return hasJavaClass(deployable, baseFilename);
        }

        return getDeployer().isJavadocJar();
    }

    private boolean hasJavaClass(Deployable deployable, String baseFilename) {
        Deployable jar = deployable.deriveByFilename(PACKAGING_JAR, baseFilename + EXT_JAR);
        JarFile jarFile = null;

        try {
            jarFile = new JarFile(jar.getLocalPath().toFile());
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            context.getLogger().warn(RB.$("ERROR_deployer_read_local_file"), e);
            return false;
        } finally {
            IOUtils.closeQuietly(jarFile);
        }
    }

    private boolean requiresPomVerification(Deployable deployable) {
        Optional<org.jreleaser.model.internal.deploy.maven.MavenDeployer.ArtifactOverride> override = getDeployer().getArtifactOverrides().stream()
            .filter(a -> a.getGroupId().equals(deployable.getGroupId()) && a.getArtifactId().equals(deployable.getArtifactId()))
            .findFirst();

        if (override.isPresent() && (override.get().isVerifyPomSet())) return override.get().isVerifyPom();

        return getDeployer().isVerifyPom();
    }

    private void signDeployables(Map<String, Deployable> deployablesMap, Set<Deployable> deployables) {
        if (!getDeployer().isSign()) {
            return;
        }

        verifyKeyIsValid();

        for (Deployable deployable : deployablesMap.values()) {
            if (deployable.isSignature() || deployable.isChecksum() || deployable.isMavenMetadata()) continue;

            Deployable signedDeployable = deployable.deriveByFilename(deployable.getFilename() + EXT_ASC);

            if (isNewer(signedDeployable, deployable)) {
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

    private boolean isNewer(Deployable source, Deployable target) {
        Path sourcePath = source.getLocalPath();
        if (!Files.exists(sourcePath)) {
            return false;
        }

        Path targetPath = target.getLocalPath();

        FileTime sourceLastModifiedTime = null;
        FileTime targetLastModifiedTime = null;

        try {
            sourceLastModifiedTime = Files.getLastModifiedTime(sourcePath);
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_timestamp_file", sourcePath.getFileName()), e);
        }

        try {
            targetLastModifiedTime = Files.getLastModifiedTime(targetPath);
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_timestamp_file", targetPath.getFileName()), e);
        }

        return sourceLastModifiedTime.compareTo(targetLastModifiedTime) > 0;
    }

    private void verifyKeyIsValid() {
        Optional<String> publicKeyID = Optional.empty();
        Optional<String> fingerprint = Optional.empty();

        try {
            publicKeyID = SigningUtils.getPublicKeyID(context.asImmutable());
        } catch (SigningException e) {
            context.getLogger().warn(RB.$("ERROR_public_key_not_found"));
            return;
        }

        if (!publicKeyID.isPresent()) {
            context.getLogger().warn(RB.$("ERROR_public_key_not_found"));
            return;
        }

        try {
            fingerprint = SigningUtils.getFingerprint(context.asImmutable());
        } catch (SigningException e) {
            context.getLogger().warn(RB.$("ERROR_public_key_not_found"));
            return;
        }

        if (!fingerprint.isPresent()) {
            context.getLogger().warn(RB.$("ERROR_public_key_not_found"));
            return;
        }

        String keyID = publicKeyID.get().toUpperCase(Locale.ENGLISH);
        String fp = fingerprint.get().toUpperCase(Locale.ENGLISH);

        try {
            Optional<Instant> expirationDate = SigningUtils.getExpirationDateOfPublicKey(context.asImmutable());

            if (expirationDate.isPresent()) {
                Instant ed = expirationDate.get();
                if (Instant.EPOCH.equals(ed)) {
                    context.getLogger().warn(RB.$("signing.public.key.no.expiration.date", keyID));

                } else if (Instant.now().isAfter(ed)) {
                    context.getLogger().warn(RB.$("ERROR_public_key_expired", keyID, LocalDateTime.ofInstant(ed, ZoneId.systemDefault())));
                } else {
                    context.getLogger().info(RB.$("signing.public.key.expiration.date", keyID, LocalDateTime.ofInstant(ed, ZoneId.systemDefault())));
                }
            }
        } catch (SigningException e) {
            context.getLogger().warn(RB.$("ERROR_public_key_not_found"));
            return;
        }

        boolean published = false;

        context.getLogger().info(RB.$("signing.check.published.key", keyID));
        for (Map.Entry<String, String> e : KEY_SERVERS.entrySet()) {
            try {
                boolean notfound = false;
                // 1st try with keyId
                URL url = new URI(String.format(e.getValue(), fp)).toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(20_000);
                connection.setReadTimeout(40_000);
                if (connection.getResponseCode() < 400) {
                    context.getLogger().debug(" + " + e.getKey());
                    published = true;
                } else {
                    notfound = true;
                }

                if (!notfound) {
                    // 2nd try with fingerprint
                    url = new URI(String.format(e.getValue(), keyID)).toURL();
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(20_000);
                    connection.setReadTimeout(40_000);
                    if (connection.getResponseCode() < 400) {
                        context.getLogger().debug(" + " + e.getKey());
                        published = true;
                        notfound = false;
                    }
                }

                if (!notfound) {
                    context.getLogger().debug(" x " + e.getKey());
                }
            } catch (MalformedURLException | URISyntaxException ignored) {
                // ignored
            } catch (IOException ex) {
                context.getLogger().debug(RB.$("ERROR_unexpected_error") + " " + ex.getMessage());
            }
        }

        if (published) {
            context.getLogger().info(RB.$("signing.key.published", keyID));
        } else {
            context.getLogger().warn(RB.$("signing.key.not.published", keyID));
        }
    }

    private void checksumDeployables(Map<String, Deployable> deployablesMap, Set<Deployable> deployables) {
        if (!getDeployer().isChecksums()) {
            return;
        }

        for (Deployable deployable : deployablesMap.values()) {
            if (deployable.isChecksum()) continue;

            if (deployable.getFilename().endsWith(EXT_ASC)) {
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

                    if (isNewer(checksumDeployable, deployable)) {
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

    protected void deployPackages() throws DeployException {
        Set<Deployable> deployables = collectDeployables();
        if (deployables.isEmpty()) {
            context.getLogger().info(RB.$("artifacts.no.match"));
        }

        D deployer = getDeployer();
        String baseUrl = deployer.getResolvedUrl(context.fullProps());
        String token = deployer.getPassword();

        if (!baseUrl.endsWith("/")) {
            baseUrl += " ";
        }

        // delete existing packages (if any)
        deleteExistingPackages(baseUrl, token, deployables);

        for (Deployable deployable : deployables) {
            if (deployable.isSignature() || deployable.isChecksum()) continue;
            Path localPath = deployable.getLocalPath();
            context.getLogger().info(" - {}", deployable.getFilename());

            if (!context.isDryrun()) {
                try {
                    Map<String, String> headers = new LinkedHashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    FormData data = ClientUtils.toFormData(localPath);

                    String url = baseUrl + deployable.getFullDeployPath();
                    ClientUtils.putFile(context.getLogger(),
                        url,
                        deployer.getConnectTimeout(),
                        deployer.getReadTimeout(),
                        data,
                        headers);
                } catch (IOException | UploadException e) {
                    context.getLogger().trace(e);
                    throw new DeployException(RB.$("ERROR_unexpected_deploy",
                        context.getBasedir().relativize(localPath), e.getMessage()), e);
                }
            }
        }
    }

    protected void deleteExistingPackages(String baseUrl, String token, Set<Deployable> deployables) throws DeployException {
        // noop
    }

    private class DeployableCollector extends SimpleFileVisitor<Path> {
        private final Path root;
        private final Set<Deployable> deployables = new TreeSet<>();
        private final boolean projectIsSnapshot;
        private boolean failed;

        public DeployableCollector(Path root, boolean projectIsSnapshot) {
            this.root = root;
            this.projectIsSnapshot = projectIsSnapshot;
        }

        private void match(Path path) {
            String filename = path.getFileName().toString();

            if (filename.contains(MAVEN_METADATA_XML)) {
                if (projectIsSnapshot) {
                    addDeployable(path);
                }
            } else {
                addDeployable(path);
            }
        }

        private void addDeployable(Path path) {
            String stagingRepository = root.toAbsolutePath().toString();
            String stagingPath = path.getParent().toAbsolutePath().toString();
            PomResult pomResult = parsePom(path);
            deployables.add(new Deployable(
                stagingRepository,
                stagingPath.substring(stagingRepository.length()),
                pomResult.packaging,
                path.getFileName().toString(),
                pomResult.relocated
            ));
        }

        private PomResult parsePom(Path artifactPath) {
            // only inspect if artifactPath ends with .pom
            if (artifactPath.getFileName().toString().endsWith(EXT_JAR)) return new PomResult(PACKAGING_JAR);
            if (!artifactPath.getFileName().toString().endsWith(EXT_POM)) return new PomResult("");

            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                Document document = factory
                    .newDocumentBuilder()
                    .parse(artifactPath.toFile());
                String query = "/project/packaging";
                String packaging = (String) XPathFactory.newInstance()
                    .newXPath()
                    .compile(query)
                    .evaluate(document, XPathConstants.STRING);

                query = "/project/distributionManagement/relocation";
                Object relocation = XPathFactory.newInstance()
                    .newXPath()
                    .compile(query)
                    .evaluate(document, XPathConstants.NODE);

                return new PomResult(isNotBlank(packaging) ? packaging.trim() : PACKAGING_JAR,
                    relocation != null);
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

        private final class PomResult {
            private String packaging;
            private boolean relocated;

            private PomResult(String packaging) {
                this.packaging = packaging;
            }

            private PomResult(String packaging, boolean relocated) {
                this.packaging = packaging;
                this.relocated = relocated;
            }
        }
    }
}
