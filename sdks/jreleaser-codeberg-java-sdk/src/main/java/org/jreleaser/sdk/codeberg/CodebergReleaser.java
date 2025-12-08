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
package org.jreleaser.sdk.codeberg;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.UpdateSection;
import org.jreleaser.model.api.common.Apply;
import org.jreleaser.model.api.common.ExtraProperties;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.util.VersionUtils;
import org.jreleaser.model.spi.release.ReleaseException;
import org.jreleaser.model.spi.release.User;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.forgejo.Forgejo;
import org.jreleaser.sdk.forgejo.api.Asset;
import org.jreleaser.sdk.forgejo.api.Issue;
import org.jreleaser.sdk.forgejo.api.Label;
import org.jreleaser.sdk.forgejo.api.Milestone;
import org.jreleaser.sdk.forgejo.api.Release;
import org.jreleaser.sdk.forgejo.api.Repository;
import org.jreleaser.sdk.git.ChangelogProvider;
import org.jreleaser.sdk.git.GitSdk;
import org.jreleaser.sdk.git.release.AbstractReleaser;

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
 * @since 0.4.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class CodebergReleaser extends AbstractReleaser<org.jreleaser.model.api.release.CodebergReleaser> {
    private static final long serialVersionUID = -5294232312152902809L;

    private final org.jreleaser.model.internal.release.CodebergReleaser codeberg;

    public CodebergReleaser(JReleaserContext context, Set<org.jreleaser.model.spi.release.Asset> assets) {
        super(context, assets);
        codeberg = context.getModel().getRelease().getCodeberg();
    }

    @Override
    public org.jreleaser.model.api.release.CodebergReleaser getReleaser() {
        return codeberg.asImmutable();
    }

    @Override
    protected void createRelease() throws ReleaseException {
        String pullBranch = codeberg.getBranch();
        String pushBranch = codeberg.getResolvedBranchPush(context);
        boolean mustCheckoutBranch = !pushBranch.equals(pullBranch);
        context.getLogger().info(RB.$("git.releaser.releasing"), codeberg.getResolvedRepoUrl(context), pushBranch);
        String tagName = codeberg.getEffectiveTagName(context);

        try {
            Forgejo api = new Forgejo(context.asImmutable(),
                codeberg.getApiEndpoint(),
                codeberg.getToken(),
                codeberg.getConnectTimeout(),
                codeberg.getReadTimeout());

            if (!context.isDryrun()) {
                List<String> branchNames = api.listBranches(codeberg.getOwner(), codeberg.getName());
                GitSdk.of(context).checkoutBranch(codeberg, pushBranch, mustCheckoutBranch, !branchNames.contains(pushBranch));
            }

            String changelog = context.getChangelog().getResolvedChangelog();

            context.getLogger().debug(RB.$("git.releaser.release.lookup"), tagName, codeberg.getCanonicalRepoName());
            Release release = findReleaseByTag(api, tagName);
            boolean snapshot = context.getModel().getProject().isSnapshot();
            if (null != release) {
                context.getLogger().debug(RB.$("git.releaser.release.exists"), tagName);
                if (codeberg.isOverwrite() || snapshot) {
                    context.getLogger().debug(RB.$("git.releaser.release.delete"), tagName);
                    if (!context.isDryrun()) {
                        api.deleteRelease(codeberg.getOwner(), codeberg.getName(), tagName, release.getId());
                    }
                    context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                    createRelease(api, tagName, changelog, snapshot && codeberg.isMatch());
                } else if (codeberg.getUpdate().isEnabled()) {
                    context.getLogger().debug(RB.$("git.releaser.release.update"), tagName);
                    if (!context.isDryrun()) {
                        Release updater = new Release();
                        if (codeberg.getPrerelease().isEnabledSet()) {
                            updater.setPrerelease(codeberg.getPrerelease().isEnabled());
                        }
                        if (codeberg.isDraftSet()) {
                            updater.setDraft(codeberg.isDraft());
                        }
                        if (codeberg.getUpdate().getSections().contains(UpdateSection.TITLE)) {
                            context.getLogger().info(RB.$("git.releaser.release.update.title"), codeberg.getEffectiveReleaseName());
                            updater.setName(codeberg.getEffectiveReleaseName());
                        }
                        if (codeberg.getUpdate().getSections().contains(UpdateSection.BODY)) {
                            context.getLogger().info(RB.$("git.releaser.release.update.body"));
                            updater.setBody(changelog);
                        }
                        api.updateRelease(codeberg.getOwner(), codeberg.getName(), release.getId(), updater);

                        if (codeberg.getUpdate().getSections().contains(UpdateSection.ASSETS)) {
                            updateAssets(api, release);
                        }
                        updateIssues(codeberg, api);
                    }
                } else {
                    if (context.isDryrun()) {
                        context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                        createRelease(api, tagName, changelog, false);
                        return;
                    }

                    throw new IllegalStateException(RB.$("ERROR_git_releaser_cannot_release",
                        capitalize(codeberg.getServiceName()), tagName));
                }
            } else {
                context.getLogger().debug(RB.$("git.releaser.release.not.found"), tagName);
                context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                createRelease(api, tagName, changelog, snapshot && codeberg.isMatch());
            }
        } catch (RestAPIException | IOException | IllegalStateException e) {
            context.getLogger().trace(e);
            throw new ReleaseException(e);
        }
    }

    private Release findReleaseByTag(Forgejo api, String tagName) {
        if (context.isDryrun()) return null;
        return api.findReleaseByTag(codeberg.getOwner(), codeberg.getName(), tagName);
    }

    protected org.jreleaser.model.spi.release.Repository.Kind resolveRepositoryKind() {
        return org.jreleaser.model.spi.release.Repository.Kind.CODEBERG;
    }

    @Override
    public org.jreleaser.model.spi.release.Repository maybeCreateRepository(String owner, String repo, String password, ExtraProperties extraProperties) throws IOException {
        context.getLogger().debug(RB.$("git.repository.lookup"), owner, repo);

        Forgejo api = new Forgejo(context.asImmutable(),
            codeberg.getApiEndpoint(),
            password,
            codeberg.getConnectTimeout(),
            codeberg.getReadTimeout());
        Repository repository = api.findRepository(owner, repo);
        if (null == repository) {
            repository = api.createRepository(owner, repo);
        }

        return new org.jreleaser.model.spi.release.Repository(
            resolveRepositoryKind(),
            owner,
            repo,
            repository.getHtmlUrl(),
            repository.getCloneUrl());
    }

    @Override
    public Optional<User> findUser(String email, String name) {
        try {
            String host = codeberg.getHost();
            String endpoint = codeberg.getApiEndpoint();
            if (endpoint.startsWith("https")) {
                host = "https://" + host;
            } else {
                host = "http://" + host;
            }
            if (!host.endsWith("/")) {
                host += "/";
            }

            return new Forgejo(context.asImmutable(),
                codeberg.getApiEndpoint(),
                codeberg.getToken(),
                codeberg.getConnectTimeout(),
                codeberg.getReadTimeout())
                .findUser(email, name, host);
        } catch (RestAPIException e) {
            context.getLogger().trace(e);
            context.getLogger().debug(RB.$("git.releaser.user.not.found"), email);
        }

        return Optional.empty();
    }

    @Override
    public List<org.jreleaser.model.spi.release.Release> listReleases(String owner, String repo) throws IOException {
        Forgejo api = new Forgejo(context.asImmutable(),
            codeberg.getApiEndpoint(),
            codeberg.getToken(),
            codeberg.getConnectTimeout(),
            codeberg.getReadTimeout());

        List<org.jreleaser.model.spi.release.Release> releases = api.listReleases(owner, repo);

        VersionUtils.clearUnparseableTags();
        Pattern versionPattern = VersionUtils.resolveVersionPattern(context);
        for (org.jreleaser.model.spi.release.Release release : releases) {
            release.setVersion(VersionUtils.version(context, release.getTagName(), versionPattern));
        }

        releases.sort((r1, r2) -> r2.getVersion().compareTo(r1.getVersion()));

        return releases;
    }

    private void createRelease(Forgejo api, String tagName, String changelog, boolean deleteTags) throws IOException {
        if (context.isDryrun()) {
            for (org.jreleaser.model.spi.release.Asset asset : assets) {
                if (0 == Files.size(asset.getPath()) || !Files.exists(asset.getPath())) {
                    // do not upload empty or non existent files
                    continue;
                }

                context.getLogger().info(" " + RB.$("git.upload.asset"), asset.getFilename());
            }
            updateIssues(codeberg, api);
            return;
        }

        if (deleteTags) {
            deleteTags(api, codeberg.getOwner(), codeberg.getName(), tagName);
        }

        // local tag
        if (deleteTags || !codeberg.isSkipTag()) {
            context.getLogger().debug(RB.$("git.releaser.repository.tag"), tagName, context.getModel().getCommit().getShortHash());
            GitSdk.of(context).tag(tagName, true, context);
        }

        // remote tag/release
        Release release = new Release();
        release.setName(codeberg.getEffectiveReleaseName());
        release.setTagName(tagName);
        release.setTargetCommitish(codeberg.getResolvedBranchPush(context));
        release.setBody(changelog);
        if (codeberg.getPrerelease().isEnabledSet()) {
            release.setPrerelease(codeberg.getPrerelease().isEnabled());
        }
        if (codeberg.isDraftSet()) {
            release.setDraft(codeberg.isDraft());
        }

        release = api.createRelease(codeberg.getOwner(), codeberg.getName(), release);
        api.uploadAssets(codeberg.getOwner(), codeberg.getName(), release, assets);

        if (codeberg.getMilestone().isClose() && !context.getModel().getProject().isSnapshot()) {
            Optional<Milestone> milestone = api.findMilestoneByName(
                codeberg.getOwner(),
                codeberg.getName(),
                codeberg.getMilestone().getEffectiveName());
            milestone.ifPresent(gtMilestone -> api.closeMilestone(codeberg.getOwner(),
                codeberg.getName(),
                gtMilestone));
        }

        updateIssues(codeberg, api);
    }

    private void updateIssues(org.jreleaser.model.internal.release.CodebergReleaser codeberg, Forgejo api) throws IOException {
        if (!codeberg.getIssues().isEnabled()) return;

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

        String tagName = codeberg.getEffectiveTagName(context);
        String labelName = codeberg.getIssues().getLabel().getName();
        String labelColor = codeberg.getIssues().getLabel().getColor();
        TemplateContext props = codeberg.props(context);
        codeberg.fillProps(props, context);
        String comment = resolveTemplate(context.getLogger(), codeberg.getIssues().getComment(), props);

        if (labelColor.startsWith("#")) {
            labelColor = labelColor.substring(1);
        }

        Label gtLabel = null;

        try {
            gtLabel = api.getOrCreateLabel(
                codeberg.getOwner(),
                codeberg.getName(),
                labelName,
                labelColor,
                codeberg.getIssues().getLabel().getDescription());
        } catch (RestAPIException e) {
            throw new IllegalStateException(RB.$("ERROR_git_releaser_fetch_label", tagName, labelName), e);
        }

        Optional<Milestone> milestone = Optional.empty();
        Apply applyMilestone = codeberg.getIssues().getApplyMilestone();
        if (codeberg.getMilestone().isClose() && !context.getModel().getProject().isSnapshot()) {
            milestone = api.findMilestoneByName(
                codeberg.getOwner(),
                codeberg.getName(),
                codeberg.getMilestone().getEffectiveName());

            if (!milestone.isPresent()) {
                milestone = api.findClosedMilestoneByName(
                    codeberg.getOwner(),
                    codeberg.getName(),
                    codeberg.getMilestone().getEffectiveName());
            }
        }

        for (String issueNumber : issueNumbers) {
            Optional<Issue> op = api.findIssue(codeberg.getOwner(), codeberg.getName(), Integer.parseInt(issueNumber));
            if (!op.isPresent()) continue;

            Issue gtIssue = op.get();
            if ("closed".equals(gtIssue.getState()) && gtIssue.getLabels().stream().noneMatch(l -> l.getName().equals(labelName))) {
                context.getLogger().debug(RB.$("git.issue.release", issueNumber));

                try {
                    api.addLabelToIssue(codeberg.getOwner(), codeberg.getName(), gtIssue, gtLabel);
                    api.commentOnIssue(codeberg.getOwner(), codeberg.getName(), gtIssue, comment);

                    milestone.ifPresent(gtMilestone -> applyMilestone(codeberg, api, issueNumber, gtIssue, applyMilestone, gtMilestone));
                } catch (RestAPIException e) {
                    if (e.isForbidden()) {
                        context.getLogger().warn(e.getMessage());
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    private void applyMilestone(org.jreleaser.model.internal.release.CodebergReleaser codeberg, Forgejo api, String issueNumber, Issue gtIssue, Apply applyMilestone, Milestone targetMilestone) {
        Milestone issueMilestone = gtIssue.getMilestone();
        String targetMilestoneTitle = targetMilestone.getTitle();

        if (null == issueMilestone) {
            context.getLogger().debug(RB.$("git.issue.milestone.apply", targetMilestoneTitle, issueNumber));
            api.setMilestoneOnIssue(codeberg.getOwner(), codeberg.getName(), gtIssue, targetMilestone);
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
                    api.setMilestoneOnIssue(codeberg.getOwner(), codeberg.getName(), gtIssue, targetMilestone);
                } else {
                    context.getLogger().debug(uncapitalize(RB.$("git.issue.milestone.warn", issueNumber, milestoneTitle)));
                }
            }
        }
    }

    private void updateAssets(Forgejo api, Release release) throws IOException {
        Set<org.jreleaser.model.spi.release.Asset> assetsToBeUpdated = new TreeSet<>();
        Set<org.jreleaser.model.spi.release.Asset> assetsToBeUploaded = new TreeSet<>();

        Map<String, Asset> existingAssets = api.listAssets(codeberg.getOwner(), codeberg.getName(), release);
        Map<String, org.jreleaser.model.spi.release.Asset> assetsToBePublished = new LinkedHashMap<>();
        assets.forEach(asset -> assetsToBePublished.put(asset.getFilename(), asset));

        assetsToBePublished.keySet().forEach(name -> {
            if (existingAssets.containsKey(name)) {
                assetsToBeUpdated.add(assetsToBePublished.get(name));
            } else {
                assetsToBeUploaded.add(assetsToBePublished.get(name));
            }
        });

        api.updateAssets(codeberg.getOwner(), codeberg.getName(), release, assetsToBeUpdated, existingAssets);
        api.uploadAssets(codeberg.getOwner(), codeberg.getName(), release, assetsToBeUploaded);
    }

    private void deleteTags(Forgejo api, String owner, String repo, String tagName) {
        // delete remote tag
        try {
            api.deleteTag(owner, repo, tagName);
        } catch (RestAPIException ignored) {
            //noop
        }
    }
}
