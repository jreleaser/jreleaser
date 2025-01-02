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
package org.jreleaser.sdk.github;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserException;
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
import org.jreleaser.sdk.git.ChangelogGenerator;
import org.jreleaser.sdk.git.ChangelogProvider;
import org.jreleaser.sdk.git.GitSdk;
import org.jreleaser.sdk.git.release.AbstractReleaser;
import org.jreleaser.sdk.github.api.GhAsset;
import org.jreleaser.sdk.github.api.GhIssue;
import org.jreleaser.sdk.github.api.GhLabel;
import org.jreleaser.sdk.github.api.GhMilestone;
import org.jreleaser.sdk.github.api.GhRelease;
import org.jreleaser.sdk.github.api.GhReleaseNotes;
import org.jreleaser.sdk.github.api.GhReleaseNotesParams;
import org.jreleaser.sdk.github.api.GhRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.sdk.git.ChangelogProvider.extractIssues;
import static org.jreleaser.sdk.git.ChangelogProvider.storeIssues;
import static org.jreleaser.sdk.git.GitSdk.extractTagName;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.uncapitalize;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class GithubReleaser extends AbstractReleaser<org.jreleaser.model.api.release.GithubReleaser> {
    private static final long serialVersionUID = -825713164586669508L;

    private static final String NOREPLY_GITHUB_COM_EMAIL = "noreply@github.com";

    private final org.jreleaser.model.internal.release.GithubReleaser github;

    public GithubReleaser(JReleaserContext context, Set<Asset> assets) {
        super(context, assets);
        github = context.getModel().getRelease().getGithub();
    }

    @Override
    public org.jreleaser.model.api.release.GithubReleaser getReleaser() {
        return github.asImmutable();
    }

    @Override
    public String generateReleaseNotes() throws IOException {
        if (github.getReleaseNotes().isEnabled()) {
            String content = generateReleaseNotesByAPI();

            if (github.getIssues().isEnabled()) {
                context.getLogger().info(RB.$("issues.generator.extract"));
                Set<Integer> issues = extractIssues(context, content);
                storeIssues(context, issues);
            }

            return ChangelogProvider.storeChangelog(context, content);
        }

        try {
            return ChangelogProvider.getChangelog(context).trim();
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_changelog"), e);
        }
    }

    private String generateReleaseNotesByAPI() throws JReleaserException {
        org.jreleaser.model.internal.release.GithubReleaser github = context.getModel().getRelease().getGithub();
        String tagName = github.getEffectiveTagName(context.getModel());

        try {
            Git git = GitSdk.of(context).open();
            ChangelogGenerator.Tags tags = new ChangelogGenerator().resolveTags(git, context);
            GhReleaseNotesParams params = new GhReleaseNotesParams();
            params.setTagName(tagName);
            if (!isTagInRemote(context, tagName)) {
                params.setTagName("HEAD");
            }
            if (tags.getPrevious().isPresent()) {
                params.setPreviousTagName(extractTagName(tags.getPrevious().get()));
            }
            params.setTargetCommitish(github.getBranch());
            GhReleaseNotes releaseNotes = new Github(context.asImmutable(),
                github.getApiEndpoint(),
                github.getToken(),
                github.getConnectTimeout(),
                github.getReadTimeout())
                .generateReleaseNotes(github.getOwner(), github.getName(), params);
            return releaseNotes.getBody().replace("...HEAD", "..." + tagName);
        } catch (IOException | GitAPIException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_changelog"), e);
        }
    }

    protected boolean isTagInRemote(JReleaserContext context, String tagName) {
        org.jreleaser.model.internal.release.GithubReleaser github = context.getModel().getRelease().getGithub();

        Github api = new Github(context.asImmutable(),
            github.getApiEndpoint(),
            github.getToken(),
            github.getConnectTimeout(),
            github.getReadTimeout());
        GhRepository repository = api.findRepository(github.getOwner(), github.getName());
        if (null == repository) {
            // remote does not exist!
            throw new IllegalStateException(RB.$("ERROR_git_repository_not_exists", github.getCanonicalRepoName()));
        }

        return api.listTags(github.getOwner(), github.getName()).stream()
            .anyMatch(tag -> tag.getName().equals(tagName));
    }

    @Override
    protected void createRelease() throws ReleaseException {
        String pullBranch = github.getBranch();
        String pushBranch = github.getResolvedBranchPush(context.getModel());
        boolean mustCheckoutBranch = !pushBranch.equals(pullBranch);

        context.getLogger().info(RB.$("git.releaser.releasing"), github.getResolvedRepoUrl(context.getModel()), pushBranch);
        String tagName = github.getEffectiveTagName(context.getModel());

        try {
            Github api = new Github(context.asImmutable(),
                github.getApiEndpoint(),
                github.getToken(),
                github.getConnectTimeout(),
                github.getReadTimeout());

            if (!context.isDryrun()) {
                List<String> branchNames = api.listBranches(github.getOwner(), github.getName());
                GitSdk.of(context).checkoutBranch(github, pushBranch, mustCheckoutBranch, !branchNames.contains(pushBranch));
            }

            String changelog = normalizeChangelog(context.getChangelog().getResolvedChangelog());

            context.getLogger().debug(RB.$("git.releaser.release.lookup"), tagName, github.getCanonicalRepoName());
            GhRelease release = findReleaseByTag(api, tagName);
            boolean snapshot = context.getModel().getProject().isSnapshot();
            if (null != release) {
                context.getLogger().debug(RB.$("git.releaser.release.exists"), tagName);
                if (github.isOverwrite() || snapshot) {
                    context.getLogger().debug(RB.$("git.releaser.release.delete"), tagName);
                    if (!context.isDryrun()) {
                        api.deleteRelease(github.getOwner(), github.getName(), tagName, release.getId());
                    }
                    context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                    createRelease(api, tagName, changelog, github.isMatch());
                } else if (github.getUpdate().isEnabled()) {
                    context.getLogger().debug(RB.$("git.releaser.release.update"), tagName);
                    if (!context.isDryrun()) {
                        GhRelease updater = new GhRelease();
                        if (github.getPrerelease().isEnabledSet()) {
                            updater.setPrerelease(github.getPrerelease().isEnabled());
                        }
                        if (github.isDraftSet()) {
                            updater.setDraft(github.isDraft());
                        }
                        if (github.getUpdate().getSections().contains(UpdateSection.TITLE)) {
                            context.getLogger().info(RB.$("git.releaser.release.update.title"), github.getEffectiveReleaseName());
                            updater.setName(github.getEffectiveReleaseName());
                        }
                        if (github.getUpdate().getSections().contains(UpdateSection.BODY)) {
                            context.getLogger().info(RB.$("git.releaser.release.update.body"));
                            updater.setBody(changelog);
                        }
                        api.updateRelease(github.getOwner(), github.getName(), release.getId(), updater);

                        if (github.getUpdate().getSections().contains(UpdateSection.ASSETS)) {
                            updateAssets(api, release);
                        }
                        linkDiscussion(tagName, release, api);
                        updateIssues(github, api);
                    }
                } else {
                    if (context.isDryrun()) {
                        context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                        createRelease(api, tagName, changelog, false);
                        return;
                    }

                    throw new IllegalStateException(RB.$("ERROR_git_releaser_cannot_release",
                        "GitHub", tagName));
                }
            } else {
                context.getLogger().debug(RB.$("git.releaser.release.not.found"), tagName);
                context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                createRelease(api, tagName, changelog, snapshot && github.isMatch());
            }
        } catch (RestAPIException | IOException | IllegalStateException e) {
            context.getLogger().trace(e);
            throw new ReleaseException(e);
        }
    }

    private String normalizeChangelog(String changelog) throws ReleaseException {
        if (changelog.length() > 10_000) {
            try {
                Path tmp = Files.createTempDirectory("jreleaser");
                Path releaseMd = tmp.resolve("RELEASE.md");
                Files.write(releaseMd, changelog.getBytes(UTF_8));
                assets.add(Asset.file(releaseMd));
            } catch (IOException e) {
                throw new ReleaseException(e);
            }
            context.getLogger().warn(RB.$("github.release.changelog.trimmed"));
            return changelog.substring(0, 9995) + " ...";
        }
        return changelog;
    }

    private GhRelease findReleaseByTag(Github api, String tagName) {
        if (context.isDryrun()) return null;
        return api.findReleaseByTag(github.getOwner(), github.getName(), tagName);
    }

    @Override
    public Repository maybeCreateRepository(String owner, String repo, String password, ExtraProperties extraProperties) throws IOException {
        context.getLogger().debug(RB.$("git.repository.lookup"), owner, repo);

        Github api = new Github(context.asImmutable(),
            github.getApiEndpoint(),
            password,
            github.getConnectTimeout(),
            github.getReadTimeout());
        GhRepository repository = api.findRepository(owner, repo);
        if (null == repository) {
            repository = api.createRepository(owner, repo);
        }

        return new Repository(
            Repository.Kind.GITHUB,
            owner,
            repo,
            repository.getHtmlUrl(),
            repository.getCloneUrl());
    }

    @Override
    public Optional<User> findUser(String email, String name) {
        if (NOREPLY_GITHUB_COM_EMAIL.equals(email)) return Optional.empty();

        try {
            return new Github(context.asImmutable(),
                github.getApiEndpoint(),
                github.getToken(),
                github.getConnectTimeout(),
                github.getReadTimeout())
                .findUser(email, name);
        } catch (RestAPIException e) {
            context.getLogger().trace(e);
            context.getLogger().debug(RB.$("git.releaser.user.not.found"), email);
        }

        return Optional.empty();
    }

    @Override
    public List<Release> listReleases(String owner, String repo) throws IOException {
        Github api = new Github(context.asImmutable(),
            github.getApiEndpoint(),
            github.getToken(),
            github.getConnectTimeout(),
            github.getReadTimeout());

        List<Release> releases = api.listReleases(owner, repo);

        VersionUtils.clearUnparseableTags();
        Pattern versionPattern = VersionUtils.resolveVersionPattern(context);
        for (Release release : releases) {
            release.setVersion(VersionUtils.version(context, release.getTagName(), versionPattern));
        }

        releases.sort((r1, r2) -> r2.getVersion().compareTo(r1.getVersion()));

        return releases;
    }

    private void createRelease(Github api, String tagName, String changelog, boolean deleteTags) throws IOException {
        if (context.isDryrun()) {
            for (Asset asset : assets) {
                if (0 == Files.size(asset.getPath()) || !Files.exists(asset.getPath())) {
                    // do not upload empty or non existent files
                    continue;
                }

                context.getLogger().info(" " + RB.$("git.upload.asset"), asset.getFilename());
            }
            updateIssues(github, api);
            return;
        }

        if (deleteTags) {
            deleteTags(api, github.getOwner(), github.getName(), tagName);
        }

        // local tag
        if (deleteTags || !github.isSkipTag()) {
            context.getLogger().debug(RB.$("git.releaser.repository.tag"), tagName, context.getModel().getCommit().getShortHash());
            GitSdk.of(context).tag(tagName, true, context);
        }

        // remote tag/release
        GhRelease release = new GhRelease();
        release.setName(github.getEffectiveReleaseName());
        release.setTagName(tagName);
        release.setTargetCommitish(github.getResolvedBranchPush(context.getModel()));
        release.setBody(changelog);
        if (github.getPrerelease().isEnabledSet()) {
            release.setPrerelease(github.getPrerelease().isEnabled());
        }
        if (github.isDraftSet()) {
            release.setDraft(github.isDraft());
        }

        boolean isDraftBefore = release.isDraft();
        release = api.createRelease(github.getOwner(), github.getName(), release);
        api.uploadAssets(release, assets);

        if (github.getMilestone().isClose() && !context.getModel().getProject().isSnapshot()) {
            Optional<GhMilestone> milestone = api.findMilestoneByName(
                github.getOwner(),
                github.getName(),
                github.getMilestone().getEffectiveName());
            milestone.ifPresent(gtMilestone -> api.closeMilestone(github.getOwner(),
                github.getName(),
                gtMilestone));
        }

        linkDiscussion(tagName, release, api);
        updateIssues(github, api);

        context.getLogger().debug(RB.$("git.check.release.draft", github.getOwner(), github.getName(), release.getTagName()));
        GhRelease newRelease = api.findReleaseById(github.getOwner(), github.getName(), release.getId());
        boolean isDraftAfter = newRelease.isDraft();

        if (!isDraftBefore && isDraftAfter) {
            GhRelease updater = new GhRelease();
            updater.setPrerelease(release.isPrerelease());
            updater.setDraft(false);
            updater.setTagName(release.getTagName());
            api.updateRelease(github.getOwner(), github.getName(), release.getId(), updater);
        }
    }

    private void updateIssues(org.jreleaser.model.internal.release.GithubReleaser github, Github api) throws IOException {
        if (!github.getIssues().isEnabled()) return;

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

        String tagName = github.getEffectiveTagName(context.getModel());
        String labelName = github.getIssues().getLabel().getName();
        String labelColor = github.getIssues().getLabel().getColor();
        TemplateContext props = github.props(context.getModel());
        github.fillProps(props, context.getModel());
        String comment = resolveTemplate(github.getIssues().getComment(), props);
        if (labelColor.startsWith("#")) {
            labelColor = labelColor.substring(1);
        }

        GhLabel ghLabel = null;

        try {
            ghLabel = api.getOrCreateLabel(
                github.getOwner(),
                github.getName(),
                labelName,
                labelColor,
                github.getIssues().getLabel().getDescription());
        } catch (RestAPIException e) {
            throw new IllegalStateException(RB.$("ERROR_git_releaser_fetch_label", tagName, labelName), e);
        }

        Optional<GhMilestone> milestone = Optional.empty();
        Apply applyMilestone = github.getIssues().getApplyMilestone();
        if (github.getMilestone().isClose() && !context.getModel().getProject().isSnapshot()) {
            milestone = api.findMilestoneByName(
                github.getOwner(),
                github.getName(),
                github.getMilestone().getEffectiveName());

            if (!milestone.isPresent()) {
                milestone = api.findClosedMilestoneByName(
                    github.getOwner(),
                    github.getName(),
                    github.getMilestone().getEffectiveName());
            }
        }

        for (String issueNumber : issueNumbers) {
            Optional<GhIssue> op = api.findIssue(github.getOwner(), github.getName(), Integer.parseInt(issueNumber));
            if (!op.isPresent()) continue;

            GhIssue ghIssue = op.get();
            if ("closed".equals(ghIssue.getState()) && ghIssue.getLabels().stream().noneMatch(l -> l.getName().equals(labelName))) {
                context.getLogger().debug(RB.$("git.issue.release", issueNumber));
                api.addLabelToIssue(github.getOwner(), github.getName(), ghIssue, ghLabel);
                api.commentOnIssue(github.getOwner(), github.getName(), ghIssue, comment);

                milestone.ifPresent(ghMilestone -> applyMilestone(github, api, issueNumber, ghIssue, applyMilestone, ghMilestone));
            }
        }
    }

    private void applyMilestone(org.jreleaser.model.internal.release.GithubReleaser github, Github api, String issueNumber, GhIssue ghIssue, Apply applyMilestone, GhMilestone targetMilestone) {
        GhMilestone issueMilestone = ghIssue.getMilestone();
        String targetMilestoneTitle = targetMilestone.getTitle();

        if (null == issueMilestone) {
            context.getLogger().debug(RB.$("git.issue.milestone.apply", targetMilestoneTitle, issueNumber));
            api.setMilestoneOnIssue(github.getOwner(), github.getName(), ghIssue, targetMilestone);
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
                    api.setMilestoneOnIssue(github.getOwner(), github.getName(), ghIssue, targetMilestone);
                } else {
                    context.getLogger().debug(uncapitalize(RB.$("git.issue.milestone.warn", issueNumber, milestoneTitle)));
                }
            }
        }
    }

    private void updateAssets(Github api, GhRelease release) throws IOException {
        Set<Asset> assetsToBeUpdated = new TreeSet<>();
        Set<Asset> assetsToBeUploaded = new TreeSet<>();

        Map<String, GhAsset> existingAssets = api.listAssets(github.getOwner(), github.getName(), release);
        Map<String, Asset> assetsToBePublished = new LinkedHashMap<>();
        assets.forEach(asset -> assetsToBePublished.put(asset.getFilename(), asset));

        assetsToBePublished.keySet().forEach(name -> {
            if (existingAssets.containsKey(name)) {
                assetsToBeUpdated.add(assetsToBePublished.get(name));
            } else {
                assetsToBeUploaded.add(assetsToBePublished.get(name));
            }
        });

        api.updateAssets(github.getOwner(), github.getName(), release, assetsToBeUpdated, existingAssets);
        api.uploadAssets(release, assetsToBeUploaded);
    }

    private void linkDiscussion(String tagName, GhRelease release, Github api) {
        String discussionCategoryName = github.getDiscussionCategoryName();
        if (context.getModel().getProject().isSnapshot() ||
            isBlank(discussionCategoryName) ||
            github.isDraft()) return;

        context.getLogger().debug(RB.$("git.releaser.link.discussion"), tagName, discussionCategoryName);

        if (context.isDryrun()) return;

        try {
            GhRelease ghRelease = new GhRelease();
            ghRelease.setDiscussionCategoryName(discussionCategoryName);
            api.updateRelease(github.getOwner(),
                github.getName(),
                tagName,
                release.getId(),
                ghRelease);
        } catch (RestAPIException e) {
            context.getLogger().trace(e);
            context.getLogger().warn(RB.$("git.releaser.link.discussion.error"),
                tagName, discussionCategoryName);
        }
    }

    private void deleteTags(Github api, String owner, String repo, String tagName) {
        // delete remote tag
        try {
            api.deleteTag(owner, repo, tagName);
        } catch (RestAPIException e) {
            if (e.isUnprocessableEntity()) {
                // OK, tag does not exist on remote
                return;
            }
            throw e;
        }
    }
}
