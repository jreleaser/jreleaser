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
package org.jreleaser.sdk.gitea;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.UpdateSection;
import org.jreleaser.model.api.common.Apply;
import org.jreleaser.model.api.common.ExtraProperties;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.util.VersionUtils;
import org.jreleaser.model.spi.release.Asset;
import org.jreleaser.model.spi.release.Release;
import org.jreleaser.model.spi.release.ReleaseException;
import org.jreleaser.model.spi.release.Repository;
import org.jreleaser.model.spi.release.User;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.git.ChangelogProvider;
import org.jreleaser.sdk.git.GitSdk;
import org.jreleaser.sdk.git.release.AbstractReleaser;
import org.jreleaser.sdk.gitea.api.GtAsset;
import org.jreleaser.sdk.gitea.api.GtIssue;
import org.jreleaser.sdk.gitea.api.GtLabel;
import org.jreleaser.sdk.gitea.api.GtMilestone;
import org.jreleaser.sdk.gitea.api.GtRelease;
import org.jreleaser.sdk.gitea.api.GtRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.capitalize;
import static org.jreleaser.util.StringUtils.uncapitalize;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class GiteaReleaser extends AbstractReleaser<org.jreleaser.model.api.release.GiteaReleaser> {
    private static final long serialVersionUID = -8160459963248847787L;

    private final org.jreleaser.model.internal.release.GiteaReleaser gitea;

    public GiteaReleaser(JReleaserContext context, Set<Asset> assets) {
        super(context, assets);
        gitea = context.getModel().getRelease().getGitea();
    }

    @Override
    public org.jreleaser.model.api.release.GiteaReleaser getReleaser() {
        return gitea.asImmutable();
    }

    @Override
    protected void createRelease() throws ReleaseException {
        String pullBranch = gitea.getBranch();
        String pushBranch = gitea.getResolvedBranchPush(context.getModel());
        boolean mustCheckoutBranch = !pushBranch.equals(pullBranch);
        context.getLogger().info(RB.$("git.releaser.releasing"), gitea.getResolvedRepoUrl(context.getModel()), pushBranch);
        String tagName = gitea.getEffectiveTagName(context.getModel());

        try {
            Gitea api = new Gitea(context.asImmutable(),
                gitea.getApiEndpoint(),
                gitea.getToken(),
                gitea.getConnectTimeout(),
                gitea.getReadTimeout());

            if (!context.isDryrun()) {
                List<String> branchNames = api.listBranches(gitea.getOwner(), gitea.getName());
                GitSdk.of(context).checkoutBranch(gitea, pushBranch, mustCheckoutBranch, !branchNames.contains(pushBranch));
            }

            String changelog = context.getChangelog().getResolvedChangelog();

            context.getLogger().debug(RB.$("git.releaser.release.lookup"), tagName, gitea.getCanonicalRepoName());
            GtRelease release = findReleaseByTag(api, tagName);
            boolean snapshot = context.getModel().getProject().isSnapshot();
            if (null != release) {
                context.getLogger().debug(RB.$("git.releaser.release.exists"), tagName);
                if (gitea.isOverwrite() || snapshot) {
                    context.getLogger().debug(RB.$("git.releaser.release.delete"), tagName);
                    if (!context.isDryrun()) {
                        api.deleteRelease(gitea.getOwner(), gitea.getName(), tagName, release.getId());
                    }
                    context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                    createRelease(api, tagName, changelog, gitea.isMatch());
                } else if (gitea.getUpdate().isEnabled()) {
                    context.getLogger().debug(RB.$("git.releaser.release.update"), tagName);
                    if (!context.isDryrun()) {
                        GtRelease updater = new GtRelease();
                        if (gitea.getPrerelease().isEnabledSet()) {
                            updater.setPrerelease(gitea.getPrerelease().isEnabled());
                        }
                        if (gitea.isDraftSet()) {
                            updater.setDraft(gitea.isDraft());
                        }
                        if (gitea.getUpdate().getSections().contains(UpdateSection.TITLE)) {
                            context.getLogger().info(RB.$("git.releaser.release.update.title"), gitea.getEffectiveReleaseName());
                            updater.setName(gitea.getEffectiveReleaseName());
                        }
                        if (gitea.getUpdate().getSections().contains(UpdateSection.BODY)) {
                            context.getLogger().info(RB.$("git.releaser.release.update.body"));
                            updater.setBody(changelog);
                        }
                        api.updateRelease(gitea.getOwner(), gitea.getName(), release.getId(), updater);

                        if (gitea.getUpdate().getSections().contains(UpdateSection.ASSETS)) {
                            updateAssets(api, release);
                        }
                        updateIssues(gitea, api);
                    }
                } else {
                    if (context.isDryrun()) {
                        context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                        createRelease(api, tagName, changelog, false);
                        return;
                    }

                    throw new IllegalStateException(RB.$("ERROR_git_releaser_cannot_release",
                        capitalize(gitea.getServiceName()), tagName));
                }
            } else {
                context.getLogger().debug(RB.$("git.releaser.release.not.found"), tagName);
                context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                createRelease(api, tagName, changelog, snapshot && gitea.isMatch());
            }
        } catch (RestAPIException | IOException | IllegalStateException e) {
            context.getLogger().trace(e);
            throw new ReleaseException(e);
        }
    }

    private GtRelease findReleaseByTag(Gitea api, String tagName) {
        if (context.isDryrun()) return null;
        return api.findReleaseByTag(gitea.getOwner(), gitea.getName(), tagName);
    }

    protected Repository.Kind resolveRepositoryKind() {
        return Repository.Kind.OTHER;
    }

    @Override
    public Repository maybeCreateRepository(String owner, String repo, String password, ExtraProperties extraProperties) throws IOException {
        context.getLogger().debug(RB.$("git.repository.lookup"), owner, repo);

        Gitea api = new Gitea(context.asImmutable(),
            gitea.getApiEndpoint(),
            password,
            gitea.getConnectTimeout(),
            gitea.getReadTimeout());
        GtRepository repository = api.findRepository(owner, repo);
        if (null == repository) {
            repository = api.createRepository(owner, repo);
        }

        return new Repository(
            resolveRepositoryKind(),
            owner,
            repo,
            repository.getHtmlUrl(),
            repository.getCloneUrl());
    }

    @Override
    public Optional<User> findUser(String email, String name) {
        try {
            String host = gitea.getHost();
            String endpoint = gitea.getApiEndpoint();
            if (endpoint.startsWith("https")) {
                host = "https://" + host;
            } else {
                host = "http://" + host;
            }
            if (!host.endsWith("/")) {
                host += "/";
            }

            return new Gitea(context.asImmutable(),
                gitea.getApiEndpoint(),
                gitea.getToken(),
                gitea.getConnectTimeout(),
                gitea.getReadTimeout())
                .findUser(email, name, host);
        } catch (RestAPIException e) {
            context.getLogger().trace(e);
            context.getLogger().debug(RB.$("git.releaser.user.not.found"), email);
        }

        return Optional.empty();
    }

    @Override
    public List<Release> listReleases(String owner, String repo) throws IOException {
        Gitea api = new Gitea(context.asImmutable(),
            gitea.getApiEndpoint(),
            gitea.getToken(),
            gitea.getConnectTimeout(),
            gitea.getReadTimeout());

        List<Release> releases = api.listReleases(owner, repo);

        VersionUtils.clearUnparseableTags();
        Pattern versionPattern = VersionUtils.resolveVersionPattern(context);
        for (Release release : releases) {
            release.setVersion(VersionUtils.version(context, release.getTagName(), versionPattern));
        }

        releases.sort((r1, r2) -> r2.getVersion().compareTo(r1.getVersion()));

        return releases;
    }

    private void createRelease(Gitea api, String tagName, String changelog, boolean deleteTags) throws IOException {
        if (context.isDryrun()) {
            for (Asset asset : assets) {
                if (0 == Files.size(asset.getPath()) || !Files.exists(asset.getPath())) {
                    // do not upload empty or non existent files
                    continue;
                }

                context.getLogger().info(" " + RB.$("git.upload.asset"), asset.getFilename());
            }
            updateIssues(gitea, api);
            return;
        }

        if (deleteTags) {
            deleteTags(api, gitea.getOwner(), gitea.getName(), tagName);
        }

        // local tag
        if (deleteTags || !gitea.isSkipTag()) {
            context.getLogger().debug(RB.$("git.releaser.repository.tag"), tagName, context.getModel().getCommit().getShortHash());
            GitSdk.of(context).tag(tagName, true, context);
        }

        // remote tag/release
        GtRelease release = new GtRelease();
        release.setName(gitea.getEffectiveReleaseName());
        release.setTagName(tagName);
        release.setTargetCommitish(gitea.getResolvedBranchPush(context.getModel()));
        release.setBody(changelog);
        if (gitea.getPrerelease().isEnabledSet()) {
            release.setPrerelease(gitea.getPrerelease().isEnabled());
        }
        if (gitea.isDraftSet()) {
            release.setDraft(gitea.isDraft());
        }

        release = api.createRelease(gitea.getOwner(), gitea.getName(), release);
        api.uploadAssets(gitea.getOwner(), gitea.getName(), release, assets);

        if (gitea.getMilestone().isClose() && !context.getModel().getProject().isSnapshot()) {
            Optional<GtMilestone> milestone = api.findMilestoneByName(
                gitea.getOwner(),
                gitea.getName(),
                gitea.getMilestone().getEffectiveName());
            milestone.ifPresent(gtMilestone -> api.closeMilestone(gitea.getOwner(),
                gitea.getName(),
                gtMilestone));
        }

        updateIssues(gitea, api);
    }

    private void updateIssues(org.jreleaser.model.internal.release.GiteaReleaser gitea, Gitea api) throws IOException {
        if (!gitea.getIssues().isEnabled()) return;

        List<String> issueNumbers = ChangelogProvider.getIssues(context);

        if (!issueNumbers.isEmpty()) {
            context.getLogger().info(RB.$("git.issue.release.mark", issueNumbers.size()));
        }

        if (context.isDryrun()) {
            for (String issueNumber : issueNumbers) {
                context.getLogger().debug(RB.$("git.issue.release", issueNumber));
            }
            return;
        }

        String tagName = gitea.getEffectiveTagName(context.getModel());
        String labelName = gitea.getIssues().getLabel().getName();
        String labelColor = gitea.getIssues().getLabel().getColor();
        TemplateContext props = gitea.props(context.getModel());
        gitea.fillProps(props, context.getModel());
        String comment = resolveTemplate(gitea.getIssues().getComment(), props);

        if (labelColor.startsWith("#")) {
            labelColor = labelColor.substring(1);
        }

        GtLabel gtLabel = null;

        try {
            gtLabel = api.getOrCreateLabel(
                gitea.getOwner(),
                gitea.getName(),
                labelName,
                labelColor,
                gitea.getIssues().getLabel().getDescription());
        } catch (RestAPIException e) {
            throw new IllegalStateException(RB.$("ERROR_git_releaser_fetch_label", tagName, labelName), e);
        }

        Optional<GtMilestone> milestone = Optional.empty();
        Apply applyMilestone = gitea.getIssues().getApplyMilestone();
        if (gitea.getMilestone().isClose() && !context.getModel().getProject().isSnapshot()) {
            milestone = api.findMilestoneByName(
                gitea.getOwner(),
                gitea.getName(),
                gitea.getMilestone().getEffectiveName());

            if (!milestone.isPresent()) {
                milestone = api.findClosedMilestoneByName(
                    gitea.getOwner(),
                    gitea.getName(),
                    gitea.getMilestone().getEffectiveName());
            }
        }

        for (String issueNumber : issueNumbers) {
            Optional<GtIssue> op = api.findIssue(gitea.getOwner(), gitea.getName(), Integer.parseInt(issueNumber));
            if (!op.isPresent()) continue;

            GtIssue gtIssue = op.get();
            if ("closed".equals(gtIssue.getState()) && gtIssue.getLabels().stream().noneMatch(l -> l.getName().equals(labelName))) {
                context.getLogger().debug(RB.$("git.issue.release", issueNumber));
                api.addLabelToIssue(gitea.getOwner(), gitea.getName(), gtIssue, gtLabel);
                api.commentOnIssue(gitea.getOwner(), gitea.getName(), gtIssue, comment);

                milestone.ifPresent(gtMilestone -> applyMilestone(gitea, api, issueNumber, gtIssue, applyMilestone, gtMilestone));
            }
        }
    }

    private void applyMilestone(org.jreleaser.model.internal.release.GiteaReleaser gitea, Gitea api, String issueNumber, GtIssue gtIssue, Apply applyMilestone, GtMilestone targetMilestone) {
        GtMilestone issueMilestone = gtIssue.getMilestone();
        String targetMilestoneTitle = targetMilestone.getTitle();

        if (null == issueMilestone) {
            context.getLogger().debug(RB.$("git.issue.milestone.apply", targetMilestoneTitle, issueNumber));
            api.setMilestoneOnIssue(gitea.getOwner(), gitea.getName(), gtIssue, targetMilestone);
        } else {
            String milestoneTitle = issueMilestone.getTitle();

            if (applyMilestone == Apply.ALWAYS) {
                context.getLogger().debug(uncapitalize(RB.$("git.issue.milestone.warn", issueNumber, milestoneTitle)));
            } else if (applyMilestone == Apply.WARN) {
                if (!milestoneTitle.equals(targetMilestoneTitle)) {
                    context.getLogger().warn(RB.$("git.issue.milestone.warn", issueNumber, milestoneTitle));
                }
            } else if (applyMilestone == Apply.FORCE) {
                if (!milestoneTitle.equals(targetMilestoneTitle)) {
                    context.getLogger().warn(RB.$("git.issue.milestone.force", targetMilestoneTitle, issueNumber, milestoneTitle));
                    api.setMilestoneOnIssue(gitea.getOwner(), gitea.getName(), gtIssue, targetMilestone);
                } else {
                    context.getLogger().debug(uncapitalize(RB.$("git.issue.milestone.warn", issueNumber, milestoneTitle)));
                }
            }
        }
    }

    private void updateAssets(Gitea api, GtRelease release) throws IOException {
        Set<Asset> assetsToBeUpdated = new TreeSet<>();
        Set<Asset> assetsToBeUploaded = new TreeSet<>();

        Map<String, GtAsset> existingAssets = api.listAssets(gitea.getOwner(), gitea.getName(), release);
        Map<String, Asset> assetsToBePublished = new LinkedHashMap<>();
        assets.forEach(asset -> assetsToBePublished.put(asset.getFilename(), asset));

        assetsToBePublished.keySet().forEach(name -> {
            if (existingAssets.containsKey(name)) {
                assetsToBeUpdated.add(assetsToBePublished.get(name));
            } else {
                assetsToBeUploaded.add(assetsToBePublished.get(name));
            }
        });

        api.updateAssets(gitea.getOwner(), gitea.getName(), release, assetsToBeUpdated, existingAssets);
        api.uploadAssets(gitea.getOwner(), gitea.getName(), release, assetsToBeUploaded);
    }

    private void deleteTags(Gitea api, String owner, String repo, String tagName) {
        // delete remote tag
        try {
            api.deleteTag(owner, repo, tagName);
        } catch (RestAPIException ignored) {
            //noop
        }
    }
}
