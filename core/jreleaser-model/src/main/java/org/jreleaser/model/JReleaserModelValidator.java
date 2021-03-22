/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import org.jreleaser.util.Constants;
import org.jreleaser.util.Logger;
import org.jreleaser.util.PlatformUtils;

import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.jreleaser.model.Project.JRELEASER_PROJECT_VERSION;
import static org.jreleaser.model.Sdkman.SDKMAN_CONSUMER_KEY;
import static org.jreleaser.model.Sdkman.SDKMAN_CONSUMER_TOKEN;
import static org.jreleaser.model.Signing.GPG_PASSPHRASE;
import static org.jreleaser.model.Twitter.TWITTER_ACCESS_TOKEN;
import static org.jreleaser.model.Twitter.TWITTER_ACCESS_TOKEN_SECRET;
import static org.jreleaser.model.Twitter.TWITTER_CONSUMER_KEY;
import static org.jreleaser.model.Twitter.TWITTER_CONSUMER_SECRET;
import static org.jreleaser.model.Zulip.ZULIP_API_KEY;
import static org.jreleaser.util.Constants.KEY_REVERSE_REPO_HOST;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.getFilenameExtension;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class JReleaserModelValidator {
    private JReleaserModelValidator() {
        // noop
    }

    public static List<String> validate(Logger logger, Path basedir, JReleaserModel model) {
        List<String> errors = new ArrayList<>();
        validateModel(logger, basedir, model, errors);
        resolveArtifactPaths(logger, model);
        logger.info("Project version set to {}", model.getProject().getResolvedVersion());
        logger.info("Release is{}snapshot", model.getProject().isSnapshot() ? " " : " not ");
        return Collections.unmodifiableList(errors);
    }

    private static void validateModel(Logger logger, Path basedir, JReleaserModel model, List<String> errors) {
        validateProject(logger, basedir, model.getProject(), errors);
        validateRelease(logger, basedir, model.getProject(), model.getRelease(), errors);
        validatePackagers(logger, basedir, model, model.getPackagers(), errors);
        validateAnnouncers(logger, basedir, model, model.getAnnounce(), errors);
        validateSign(logger, basedir, model, model.getSign(), errors);
        validateDistributions(logger, basedir, model, model.getDistributions(), errors);
    }

    private static void validateProject(Logger logger, Path basedir, Project project, List<String> errors) {
        if (isBlank(project.getName())) {
            errors.add("project.name must not be blank");
        }
        checkEnvSetting(logger, errors, project.getVersion(), JRELEASER_PROJECT_VERSION, "project.version");
        if (isBlank(project.getArtifactId())) {
            project.setArtifactId(project.getName());
        }
        if (isBlank(project.getGroupId())) {
            errors.add("project.groupId must not be blank");
        }
        if (isBlank(project.getArtifactId())) {
            errors.add("project.artifactId must not be blank");
        }
        if (isBlank(project.getDescription())) {
            errors.add("project.description must not be blank");
        }
        if (isBlank(project.getWebsite())) {
            errors.add("project.website must not be blank");
        }
        if (isBlank(project.getLicense())) {
            errors.add("project.license must not be blank");
        }
        if (isBlank(project.getLongDescription())) {
            project.setLongDescription(project.getDescription());
        }
        if (project.getAuthors().isEmpty()) {
            errors.add("project.authors must not be empty");
        }
    }

    private static void validateRelease(Logger logger, Path basedir, Project project, Release release, List<String> errors) {
        int count = 0;
        count += validateGithub(logger, basedir, project, release.getGithub(), errors);
        count += validateGitlab(logger, basedir, project, release.getGitlab(), errors);
        count += validateGitea(logger, basedir, project, release.getGitea(), errors);

        if (0 == count) {
            errors.add("One of release.github, release.gitlab, release.gitea must be defined");
            return;
        }
        if (count > 1) {
            errors.add("Only one of release.github, release.gitlab, release.gitea can be defined");
        }
    }

    private static void validatePackagers(Logger logger, Path basedir, JReleaserModel model, Packagers packagers, List<String> errors) {
        validateCommitAuthor(packagers.getBrew(), model.getRelease().getGitService());
        validateOwner(packagers.getBrew().getTap(), model.getRelease().getGitService());

        validateCommitAuthor(packagers.getChocolatey(), model.getRelease().getGitService());
        validateOwner(packagers.getChocolatey().getBucket(), model.getRelease().getGitService());

        validateCommitAuthor(packagers.getJbang(), model.getRelease().getGitService());
        validateOwner(packagers.getJbang().getCatalog(), model.getRelease().getGitService());

        validateCommitAuthor(packagers.getScoop(), model.getRelease().getGitService());
        validateOwner(packagers.getScoop().getBucket(), model.getRelease().getGitService());

        validateCommitAuthor(packagers.getSnap(), model.getRelease().getGitService());
        validateOwner(packagers.getSnap().getTap(), model.getRelease().getGitService());
    }

    private static void validateAnnouncers(Logger logger, Path basedir, JReleaserModel model, Announce announce, List<String> errors) {
        validateSdkman(logger, basedir, model, announce.getSdkman(), errors);
        validateTwitter(logger, basedir, model, announce.getTwitter(), errors);
        validateZulip(logger, basedir, model, announce.getZulip(), errors);

        boolean enabled = announce.getSdkman().isEnabled() ||
            announce.getTwitter().isEnabled() ||
            announce.getZulip().isEnabled();
        if (!announce.isEnabledSet()) {
            announce.setEnabled(enabled);
        }
    }

    private static void validateSdkman(Logger logger, Path basedir, JReleaserModel model, Sdkman sdkman, List<String> errors) {
        if (!sdkman.isEnabled()) return;

        checkEnvSetting(logger, errors, sdkman.getConsumerKey(), SDKMAN_CONSUMER_KEY, "sdkman.consumerKey");
        checkEnvSetting(logger, errors, sdkman.getConsumerToken(), SDKMAN_CONSUMER_TOKEN, "sdkman.consumerToken");
    }

    private static void validateTwitter(Logger logger, Path basedir, JReleaserModel model, Twitter twitter, List<String> errors) {
        if (!twitter.isEnabled()) return;

        checkEnvSetting(logger, errors, twitter.getConsumerKey(), TWITTER_CONSUMER_KEY, "twitter.consumerKey");
        checkEnvSetting(logger, errors, twitter.getConsumerSecret(), TWITTER_CONSUMER_SECRET, "twitter.consumerSecret");
        checkEnvSetting(logger, errors, twitter.getAccessToken(), TWITTER_ACCESS_TOKEN, "twitter.accessToken");
        checkEnvSetting(logger, errors, twitter.getAccessTokenSecret(), TWITTER_ACCESS_TOKEN_SECRET, "twitter.accessTokenSecret");
        if (isBlank(twitter.getStatus())) {
            errors.add("twitter.status must not be blank.");
        }
    }

    private static void validateZulip(Logger logger, Path basedir, JReleaserModel model, Zulip zulip, List<String> errors) {
        if (!zulip.isEnabled()) return;

        if (isBlank(zulip.getAccount())) {
            errors.add("zulip.account must not be blank.");
        }
        checkEnvSetting(logger, errors, zulip.getApiKey(), ZULIP_API_KEY, "zulip.apiKey");
        if (isBlank(zulip.getApiHost())) {
            errors.add("zulip.apiHost must not be blank.");
        }
        if (isBlank(zulip.getChannel())) {
            zulip.setChannel("announce");
        }
    }

    private static void validateSign(Logger logger, Path basedir, JReleaserModel model, Signing signing, List<String> errors) {
        if (!signing.isEnabled()) return;

        checkEnvSetting(logger, errors, signing.getPassphrase(), GPG_PASSPHRASE, "sign.passphrase");
        if (isBlank(signing.getKeyRingFile())) {
            errors.add("sign.keyRingFile must not be blank");
        }
    }

    private static void validateGitService(Logger logger, Path basedir, Project project, GitService service, List<String> errors) {
        if (isBlank(service.getOwner())) {
            errors.add(service.getServiceName() + ".owner must not be blank");
        }
        if (isBlank(service.getName())) {
            service.setName(project.getName());
        }
        if (isBlank(service.getUsername())) {
            service.setUsername(service.getOwner());
        }

        checkEnvSetting(logger, errors, service.getToken(),
            service.getServiceName().toUpperCase() + "_TOKEN",
            service.getServiceName() + ".password");

        service.setTagName(service.getResolvedTagName(project));
        if (isBlank(service.getTagName())) {
            service.setTagName("v" + project.getResolvedVersion());
        }
        if (isBlank(service.getReleaseName())) {
            service.setReleaseName("Release " + service.getTagName());
        }
        if (!service.getChangelog().isEnabledSet()) {
            service.getChangelog().setEnabled(true);
        }
        if (service.isSign() && isBlank(service.getSigningKey())) {
            errors.add(service.getServiceName() + ".signingKey must not be blank if sign is set to `true`");
        }
        if (isBlank(service.getCommitAuthor().getName())) {
            service.getCommitAuthor().setName("jreleaser-bot");
        }
        if (isBlank(service.getCommitAuthor().getEmail())) {
            service.getCommitAuthor().setEmail("jreleaser-bot@jreleaser.org");
        }
    }

    private static int validateGithub(Logger logger, Path basedir, Project project, Github github, List<String> errors) {
        if (null == github) return 0;

        validateGitService(logger, basedir, project, github, errors);

        if (isBlank(github.getTargetCommitish())) {
            github.setTargetCommitish("main");
        }

        return 1;
    }

    private static int validateGitlab(Logger logger, Path basedir, Project project, Gitlab gitlab, List<String> errors) {
        if (null == gitlab) return 0;

        validateGitService(logger, basedir, project, gitlab, errors);

        if (isBlank(gitlab.getRef())) {
            gitlab.setRef("main");
        }

        return 1;
    }

    private static int validateGitea(Logger logger, Path basedir, Project project, Gitea gitea, List<String> errors) {
        if (null == gitea) return 0;

        validateGitService(logger, basedir, project, gitea, errors);

        if (isBlank(gitea.getTargetCommitish())) {
            gitea.setTargetCommitish("main");
        }

        return 1;
    }

    private static void validateDistributions(Logger logger, Path basedir, JReleaserModel model, Map<String, Distribution> distributions, List<String> errors) {
        if (distributions.isEmpty()) {
            errors.add("Missing distributions configuration");
            return;
        }

        if (distributions.size() == 1) {
            distributions.values().stream()
                .findFirst().ifPresent(distribution -> distribution.setName(model.getProject().getName()));
        }

        for (Map.Entry<String, Distribution> e : distributions.entrySet()) {
            Distribution distribution = e.getValue();
            if (isBlank(distribution.getName())) {
                distribution.setName(e.getKey());
            }
            validateDistribution(logger, basedir, model, distribution, errors);
        }
    }

    private static void validateDistribution(Logger logger, Path basedir, JReleaserModel model, Distribution distribution, List<String> errors) {
        if (isBlank(distribution.getName())) {
            errors.add("distribution.name must not be blank");
            return;
        }
        if (null == distribution.getType()) {
            errors.add("distribution." + distribution.getName() + ".type must not be null");
            return;
        }
        if (isBlank(distribution.getGroupId())) {
            distribution.setGroupId(model.getProject().getGroupId());
        }
        if (isBlank(distribution.getArtifactId())) {
            distribution.setArtifactId(model.getProject().getArtifactId());
        }
        if (isBlank(distribution.getExecutable())) {
            distribution.setExecutable(distribution.getName());
        }
        if (isBlank(distribution.getJavaVersion())) {
            distribution.setJavaVersion(model.getProject().getJavaVersion());
        }
        if (null == distribution.getArtifacts() || distribution.getArtifacts().isEmpty()) {
            errors.add("distribution." + distribution.getName() + ".artifacts is empty");
            return;
        }

        List<String> tags = new ArrayList<>();
        tags.addAll(model.getProject().getTags());
        tags.addAll(distribution.getTags());
        distribution.setTags(tags);

        for (int i = 0; i < distribution.getArtifacts().size(); i++) {
            validateArtifact(logger, basedir, model, distribution, distribution.getArtifacts().get(i), i, errors);
        }
        // validate artifact.platform is unique
        Map<String, List<Artifact>> byPlatform = distribution.getArtifacts().stream()
            .collect(groupingBy(artifact -> isBlank(artifact.getPlatform()) ? "<nil>" : artifact.getPlatform()));
        // check platforms by extension
        byPlatform.entrySet().forEach(p -> {
            String platform = "<nil>".equals(p.getKey()) ? "no" : p.getKey();
            p.getValue().stream()
                .collect(groupingBy(artifact -> getFilenameExtension(artifact.getPath())))
                .entrySet().forEach(e -> {
                if (e.getValue().size() > 1) {
                    errors.add("distribution." + distribution.getName() +
                        " has more than one artifact with " + platform +
                        " platform for extension " + e.getValue());
                }
            });
        });

        validateBrew(logger, basedir, model, distribution, distribution.getBrew(), errors);
        validateChocolatey(logger, basedir, model, distribution, distribution.getChocolatey(), errors);
        validateJbang(logger, basedir, model, distribution, distribution.getJbang(), errors);
        validateScoop(logger, basedir, model, distribution, distribution.getScoop(), errors);
        validateSnap(logger, basedir, model, distribution, distribution.getSnap(), errors);
    }

    private static void validateArtifact(Logger logger, Path basedir, JReleaserModel model, Distribution distribution, Artifact artifact, int index, List<String> errors) {
        if (null == artifact) {
            errors.add("distribution." + distribution.getName() + ".artifact[" + index + "] is null");
            return;
        }
        if (isBlank(artifact.getPath())) {
            errors.add("distribution." + distribution.getName() + ".artifact[" + index + "].path must not be null");
        }
        if (isBlank(artifact.getJavaVersion())) {
            artifact.setJavaVersion(distribution.getJavaVersion());
        }
        if (isNotBlank(artifact.getPlatform()) && !PlatformUtils.isSupported(artifact.getPlatform().trim())) {
            logger.warn("distribution.{}.artifact[{}].platform ({}) is not supported. Please use `${name}` or `${name}-${arch}` from{}       name = {}{}       arch = {}",
                distribution.getName(), index, artifact.getPlatform(), System.lineSeparator(),
                PlatformUtils.getSupportedOsNames(), System.lineSeparator(), PlatformUtils.getSupportedOsArchs());
        }
    }

    private static void validateBrew(Logger logger, Path basedir, JReleaserModel model, Distribution distribution, Brew tool, List<String> errors) {
        if (!tool.isEnabledSet() && model.getPackagers().getBrew().isEnabledSet()) {
            tool.setEnabled(model.getPackagers().getBrew().isEnabled());
        }
        if (!tool.isEnabled()) return;

        validateCommitAuthor(tool, model.getPackagers().getBrew());
        validateOwner(tool.getTap(), model.getPackagers().getBrew().getTap());
        validateTemplate(logger, basedir, model, distribution, tool, model.getPackagers().getBrew(), errors);
        mergeExtraProperties(tool, model.getPackagers().getBrew());

        Map<String, String> dependencies = new LinkedHashMap<>(model.getPackagers().getBrew().getDependencies());
        dependencies.putAll(tool.getDependencies());

        tool.setDependencies(dependencies);

        if (isBlank(tool.getTap().getName())) {
            tool.getTap().setName(model.getPackagers().getBrew().getTap().getName());
        }
        if (isBlank(tool.getTap().getUsername())) {
            tool.getTap().setUsername(model.getPackagers().getBrew().getTap().getUsername());
        }
        if (isBlank(tool.getTap().getToken())) {
            tool.getTap().setToken(model.getPackagers().getBrew().getTap().getToken());
        }
    }

    private static void validateChocolatey(Logger logger, Path basedir, JReleaserModel model, Distribution distribution, Chocolatey tool, List<String> errors) {
        if (!tool.isEnabledSet() && model.getPackagers().getChocolatey().isEnabledSet()) {
            tool.setEnabled(model.getPackagers().getChocolatey().isEnabled());
        }
        if (!tool.isEnabled()) return;

        validateCommitAuthor(tool, model.getPackagers().getChocolatey());
        validateOwner(tool.getBucket(), model.getPackagers().getChocolatey().getBucket());
        validateTemplate(logger, basedir, model, distribution, tool, model.getPackagers().getChocolatey(), errors);
        mergeExtraProperties(tool, model.getPackagers().getChocolatey());

        if (isBlank(tool.getUsername())) {
            tool.setUsername(model.getRelease().getGitService().getOwner());
        }
        if (!tool.isRemoteBuildSet() && model.getPackagers().getChocolatey().isRemoteBuildSet()) {
            tool.setRemoteBuild(model.getPackagers().getChocolatey().isRemoteBuild());
        }

        if (isBlank(tool.getBucket().getName())) {
            tool.getBucket().setName(distribution.getName() + "-bucket");
        }
        tool.getBucket().setBasename(distribution.getName() + "-bucket");
        if (isBlank(tool.getBucket().getUsername())) {
            tool.getBucket().setUsername(model.getPackagers().getChocolatey().getBucket().getUsername());
        }
        if (isBlank(tool.getBucket().getToken())) {
            tool.getBucket().setToken(model.getPackagers().getChocolatey().getBucket().getToken());
        }
    }

    private static void validateJbang(Logger logger, Path basedir, JReleaserModel model, Distribution distribution, Jbang tool, List<String> errors) {
        if (!tool.isEnabledSet() && model.getPackagers().getJbang().isEnabledSet()) {
            tool.setEnabled(model.getPackagers().getJbang().isEnabled());
        }
        if (!tool.isEnabled()) return;

        validateCommitAuthor(tool, model.getPackagers().getJbang());
        validateOwner(tool.getCatalog(), model.getPackagers().getJbang().getCatalog());
        validateTemplate(logger, basedir, model, distribution, tool, model.getPackagers().getJbang(), errors);
        mergeExtraProperties(tool, model.getPackagers().getJbang());

        if (isBlank(tool.getCatalog().getName())) {
            tool.getCatalog().setName(model.getPackagers().getJbang().getCatalog().getName());
        }
        if (isBlank(tool.getCatalog().getUsername())) {
            tool.getCatalog().setUsername(model.getPackagers().getJbang().getCatalog().getUsername());
        }
        if (isBlank(tool.getCatalog().getToken())) {
            tool.getCatalog().setToken(model.getPackagers().getJbang().getCatalog().getToken());
        }

        if (!tool.getExtraProperties().containsKey(KEY_REVERSE_REPO_HOST) &&
            model.getPackagers().getJbang().getExtraProperties().containsKey(KEY_REVERSE_REPO_HOST)) {
            tool.getExtraProperties().put(KEY_REVERSE_REPO_HOST,
                model.getPackagers().getJbang().getExtraProperties().get(KEY_REVERSE_REPO_HOST));
        }
        if (isBlank(model.getRelease().getGitService().getReverseRepoHost()) &&
            !tool.getExtraProperties().containsKey(KEY_REVERSE_REPO_HOST)) {
            errors.add("distribution." + distribution.getName() +
                ".jbang must define an extra property named '" +
                KEY_REVERSE_REPO_HOST + "'");
        }
    }

    private static void validateScoop(Logger logger, Path basedir, JReleaserModel model, Distribution distribution, Scoop tool, List<String> errors) {
        if (!tool.isEnabledSet() && model.getPackagers().getScoop().isEnabledSet()) {
            tool.setEnabled(model.getPackagers().getScoop().isEnabled());
        }
        if (!tool.isEnabled()) return;

        validateCommitAuthor(tool, model.getPackagers().getScoop());
        validateOwner(tool.getBucket(), model.getPackagers().getScoop().getBucket());
        validateTemplate(logger, basedir, model, distribution, tool, model.getPackagers().getScoop(), errors);
        Scoop commonScoop = model.getPackagers().getScoop();
        mergeExtraProperties(tool, model.getPackagers().getScoop());

        if (isBlank(tool.getCheckverUrl())) {
            tool.setCheckverUrl(commonScoop.getCheckverUrl());
            if (isBlank(tool.getCheckverUrl())) {
                tool.setCheckverUrl(model.getRelease().getGitService().getLatestReleaseUrlFormat());
            }
        }
        if (isBlank(tool.getAutoupdateUrl())) {
            tool.setAutoupdateUrl(commonScoop.getAutoupdateUrl());
            if (isBlank(tool.getAutoupdateUrl())) {
                tool.setAutoupdateUrl(model.getRelease().getGitService().getDownloadUrlFormat());
            }
        }

        if (isBlank(tool.getBucket().getName())) {
            tool.getBucket().setName(model.getPackagers().getScoop().getBucket().getName());
        }
        if (isBlank(tool.getBucket().getUsername())) {
            tool.getBucket().setUsername(model.getPackagers().getScoop().getBucket().getUsername());
        }
        if (isBlank(tool.getBucket().getToken())) {
            tool.getBucket().setToken(model.getPackagers().getScoop().getBucket().getToken());
        }
    }

    private static void validateSnap(Logger logger, Path basedir, JReleaserModel model, Distribution distribution, Snap tool, List<String> errors) {
        if (!tool.isEnabledSet() && model.getPackagers().getSnap().isEnabledSet()) {
            tool.setEnabled(model.getPackagers().getSnap().isEnabled());
        }
        if (!tool.isEnabled()) return;

        validateCommitAuthor(tool, model.getPackagers().getSnap());
        validateOwner(tool.getTap(), model.getPackagers().getSnap().getTap());
        validateTemplate(logger, basedir, model, distribution, tool, model.getPackagers().getSnap(), errors);
        Snap commonSnap = model.getPackagers().getSnap();
        mergeExtraProperties(tool, model.getPackagers().getSnap());
        mergeSnapPlugs(tool, model.getPackagers().getSnap());
        mergeSnapSlots(tool, model.getPackagers().getSnap());

        if (isBlank(tool.getBase())) {
            tool.setBase(commonSnap.getBase());
            if (isBlank(tool.getBase())) {
                errors.add("distribution." + distribution.getName() + ".snap.base must not be blank");
            }
        }
        if (isBlank(tool.getGrade())) {
            tool.setGrade(commonSnap.getGrade());
            if (isBlank(tool.getGrade())) {
                errors.add("distribution." + distribution.getName() + ".snap.grade must not be blank");
            }
        }
        if (isBlank(tool.getConfinement())) {
            tool.setConfinement(commonSnap.getConfinement());
            if (isBlank(tool.getConfinement())) {
                errors.add("distribution." + distribution.getName() + ".snap.confinement must not be blank");
            }
        }
        if (!tool.isRemoteBuildSet() && model.getPackagers().getSnap().isRemoteBuildSet()) {
            tool.setRemoteBuild(model.getPackagers().getSnap().isRemoteBuild());
        }
        if (!tool.isRemoteBuild() && isBlank(tool.getExportedLogin())) {
            tool.setExportedLogin(commonSnap.getExportedLogin());
            if (isBlank(tool.getExportedLogin())) {
                errors.add("distribution." + distribution.getName() + ".snap.exportedLogin must not be empty");
            } else if (!basedir.resolve(tool.getExportedLogin()).toFile().exists()) {
                errors.add("distribution." + distribution.getName() + ".snap.exportedLogin does not exist. " + basedir.resolve(tool.getExportedLogin()));
            }
        }

        if (isBlank(tool.getTap().getName())) {
            tool.getTap().setName(distribution.getName() + "-snap");
        }
        tool.getTap().setBasename(distribution.getName() + "-snap");
        if (isBlank(tool.getTap().getUsername())) {
            tool.getTap().setUsername(model.getPackagers().getSnap().getTap().getUsername());
        }
        if (isBlank(tool.getTap().getToken())) {
            tool.getTap().setToken(model.getPackagers().getSnap().getTap().getToken());
        }
    }

    private static void mergeExtraProperties(Tool tool, Tool common) {
        Map<String, Object> extraProperties = new LinkedHashMap<>(common.getExtraProperties());
        extraProperties.putAll(tool.getExtraProperties());
        tool.setExtraProperties(extraProperties);
    }

    private static void mergeSnapPlugs(Snap tool, Snap common) {
        Map<String, Plug> commonPlugs = common.getPlugs().stream()
            .collect(Collectors.toMap(Plug::getName, Plug::copyOf));
        Map<String, Plug> toolPlugs = tool.getPlugs().stream()
            .collect(Collectors.toMap(Plug::getName, Plug::copyOf));
        commonPlugs.forEach((name, cp) -> {
            Plug tp = toolPlugs.remove(name);
            if (null != tp) {
                cp.getAttributes().putAll(tp.getAttributes());
            }
        });
        commonPlugs.putAll(toolPlugs);
        tool.setPlugs(new ArrayList<>(commonPlugs.values()));
    }

    private static void mergeSnapSlots(Snap tool, Snap common) {
        Map<String, Slot> commonSlots = common.getSlots().stream()
            .collect(Collectors.toMap(Slot::getName, Slot::copyOf));
        Map<String, Slot> toolSlots = tool.getSlots().stream()
            .collect(Collectors.toMap(Slot::getName, Slot::copyOf));
        commonSlots.forEach((name, cp) -> {
            Slot tp = toolSlots.remove(name);
            if (null != tp) {
                cp.getAttributes().putAll(tp.getAttributes());
            }
        });
        commonSlots.putAll(toolSlots);
        tool.setSlots(new ArrayList<>(commonSlots.values()));
    }

    private static void validateTemplate(Logger logger, Path basedir, JReleaserModel model, Distribution distribution,
                                         Tool tool, Tool parentTool, List<String> errors) {
        if (isBlank(tool.getTemplateDirectory())) {
            tool.setTemplateDirectory(parentTool.getTemplateDirectory());
            if (isNotBlank(tool.getTemplateDirectory()) &&
                !(basedir.resolve(tool.getTemplateDirectory().trim()).toFile().exists())) {
                errors.add("distribution." + distribution.getName() + "." + tool.getName() + ".template does not exist. " + tool.getTemplateDirectory());
            } else {
                tool.setTemplateDirectory("src/distributions/" + distribution.getName() + "/" + tool.getName());
            }
            return;
        }

        if (isNotBlank(tool.getTemplateDirectory()) &&
            !(basedir.resolve(tool.getTemplateDirectory().trim()).toFile().exists())) {
            errors.add("distribution." + distribution.getName() + "." + tool.getName() + ".template does not exist. " + tool.getTemplateDirectory());
        } else {
            tool.setTemplateDirectory("src/distributions/" + distribution.getName() + "/" + tool.getName());
        }
    }

    private static void validateOwner(OwnerProvider self, OwnerProvider other) {
        if (isBlank(self.getOwner())) self.setOwner(other.getOwner());
    }

    private static void validateCommitAuthor(CommitAuthorProvider self, CommitAuthorProvider other) {
        CommitAuthor author = new CommitAuthor();
        author.setName(self.getCommitAuthor().getName());
        author.setEmail(self.getCommitAuthor().getEmail());
        if (isBlank(author.getName())) author.setName(other.getCommitAuthor().getName());
        if (isBlank(author.getEmail())) author.setEmail(other.getCommitAuthor().getEmail());
        self.setCommitAuthor(author);
    }

    private static void checkEnvSetting(Logger logger, List<String> errors, String value, String key, String property) {
        if (isBlank(value)) {
            if (isBlank(System.getenv(key))) {
                errors.add(property + " must not be blank. Alternatively define a " + key + " environment variable.");
            }
        }
    }

    private static void resolveArtifactPaths(Logger logger, JReleaserModel model) {
        for (Distribution distribution : model.getDistributions().values()) {
            Map<String, Object> props = model.props();
            props.put(Constants.KEY_DISTRIBUTION_NAME, distribution.getName());
            props.put(Constants.KEY_DISTRIBUTION_EXECUTABLE, distribution.getExecutable());
            props.putAll(distribution.getResolvedExtraProperties());

            for (int i = 0; i < distribution.getArtifacts().size(); i++) {
                Artifact artifact = distribution.getArtifacts().get(i);
                String path = artifact.getPath();
                if (path.contains("{{")) {
                    String newpath = applyTemplate(new StringReader(path), props);
                    logger.debug("Adjusting distribution.artifact[{}].path{}        from {}{}        to {}",
                        i, System.lineSeparator(), path, System.lineSeparator(), newpath);
                    artifact.setPath(newpath);
                }
            }
        }

        Map<String, Object> props = model.props();
        int i = 0;
        for (Artifact artifact : model.getFiles()) {
            String path = artifact.getPath();
            if (path.contains("{{")) {
                String newpath = applyTemplate(new StringReader(path), props);
                logger.debug("Adjusting files[{i}].path{}        from {}{}        to {}",
                    i++, System.lineSeparator(), path, System.lineSeparator(), newpath);
                artifact.setPath(newpath);
            }
        }
    }
}
