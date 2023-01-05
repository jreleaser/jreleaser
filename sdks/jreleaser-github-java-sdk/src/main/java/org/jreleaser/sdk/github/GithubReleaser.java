/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.util.VersionUtils;
import org.jreleaser.model.spi.release.AbstractReleaser;
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
import org.jreleaser.sdk.git.ReleaseUtils;
import org.jreleaser.sdk.github.api.GhRelease;
import org.jreleaser.sdk.github.api.GhReleaseNotes;
import org.jreleaser.sdk.github.api.GhReleaseNotesParams;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHMilestone;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHReleaseUpdater;
import org.kohsuke.github.GHRepository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

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

    private final org.jreleaser.model.internal.release.GithubReleaser github;

    public GithubReleaser(JReleaserContext context, List<Asset> assets) {
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
            GhReleaseNotes releaseNotes = new XGithub(context.getLogger(),
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

        try {
            Github api = new Github(context.getLogger(),
                github.getApiEndpoint(),
                github.getToken(),
                github.getConnectTimeout(),
                github.getReadTimeout());
            GHRepository repository = api.findRepository(github.getOwner(), github.getName());
            if (null == repository) {
                // remote does not exist!
                throw new IllegalStateException(RB.$("ERROR_git_repository_not_exists", github.getCanonicalRepoName()));
            }

            return null != repository.getRef("tags/" + tagName);
        } catch (FileNotFoundException e) {
            // OK, it means tag does not exist
            return false;
        } catch (IOException e) {
            context.getLogger().trace(e);
            throw new JReleaserException(e);
        }
    }

    @Override
    protected void createTag() throws ReleaseException {
        ReleaseUtils.createTag(context);
    }

    @Override
    protected void createRelease() throws ReleaseException {
        org.jreleaser.model.internal.release.GithubReleaser github = context.getModel().getRelease().getGithub();
        context.getLogger().info(RB.$("git.releaser.releasing"), github.getResolvedRepoUrl(context.getModel()));
        String tagName = github.getEffectiveTagName(context.getModel());

        try {
            Github api = new Github(context.getLogger(),
                github.getApiEndpoint(),
                github.getToken(),
                github.getConnectTimeout(),
                github.getReadTimeout());

            String branch = github.getBranch();
            Map<String, GHBranch> branches = api.listBranches(github.getOwner(), github.getName());
            if (!branches.containsKey(branch)) {
                throw new ReleaseException(RB.$("ERROR_git_release_branch_not_exists", branch, branches.keySet()));
            }

            String changelog = context.getChangelog();

            context.getLogger().debug(RB.$("git.releaser.release.lookup"), tagName, github.getCanonicalRepoName());
            GHRelease release = api.findReleaseByTag(github.getCanonicalRepoName(), tagName);
            boolean snapshot = context.getModel().getProject().isSnapshot();
            if (null != release) {
                context.getLogger().debug(RB.$("git.releaser.release.exists"), tagName);
                if (github.isOverwrite() || snapshot) {
                    context.getLogger().debug(RB.$("git.releaser.release.delete"), tagName);
                    if (!context.isDryrun()) {
                        release.delete();
                    }
                    context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                    createRelease(api, tagName, changelog, github.isMatch());
                } else if (github.getUpdate().isEnabled()) {
                    context.getLogger().debug(RB.$("git.releaser.release.update"), tagName);
                    if (!context.isDryrun()) {
                        GHReleaseUpdater updater = release.update();
                        updater.prerelease(github.getPrerelease().isEnabled());
                        updater.draft(github.isDraft());
                        if (github.getUpdate().getSections().contains(UpdateSection.TITLE)) {
                            context.getLogger().info(RB.$("git.releaser.release.update.title"), github.getEffectiveReleaseName());
                            updater.name(github.getEffectiveReleaseName());
                        }
                        if (github.getUpdate().getSections().contains(UpdateSection.BODY)) {
                            context.getLogger().info(RB.$("git.releaser.release.update.body"));
                            updater.body(changelog);
                        }
                        release = updater.update();

                        if (github.getUpdate().getSections().contains(UpdateSection.ASSETS)) {
                            updateAssets(api, release);
                        }
                        linkDiscussion(tagName, release);
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
        } catch (RestAPIException e) {
            context.getLogger().trace(e.getStatus() + " " + e.getReason());
            context.getLogger().trace(e);
            throw new ReleaseException(e);
        } catch (IOException | IllegalStateException e) {
            context.getLogger().trace(e);
            throw new ReleaseException(e);
        }
    }

    @Override
    public Repository maybeCreateRepository(String owner, String repo, String password) throws IOException {
        org.jreleaser.model.internal.release.GithubReleaser github = context.getModel().getRelease().getGithub();
        context.getLogger().debug(RB.$("git.repository.lookup"), owner, repo);

        Github api = new Github(context.getLogger(),
            github.getApiEndpoint(),
            password,
            github.getConnectTimeout(),
            github.getReadTimeout());
        GHRepository repository = api.findRepository(owner, repo);
        if (null == repository) {
            repository = api.createRepository(owner, repo);
        }

        return new Repository(
            Repository.Kind.GITHUB,
            owner,
            repo,
            repository.getUrl().toExternalForm(),
            repository.getHttpTransportUrl());
    }

    @Override
    public Optional<User> findUser(String email, String name) {
        org.jreleaser.model.internal.release.GithubReleaser github = context.getModel().getRelease().getGithub();

        try {
            return new XGithub(context.getLogger(),
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
        org.jreleaser.model.internal.release.GithubReleaser github = context.getModel().getRelease().getGithub();

        Github api = new Github(context.getLogger(),
            github.getApiEndpoint(),
            github.getToken(),
            github.getConnectTimeout(),
            github.getReadTimeout());

        List<Release> releases = new ArrayList<>();

        for (GHRelease ghRelease : api.listReleases(owner, repo).toList()) {
            if (ghRelease.isDraft() || ghRelease.isPrerelease()) continue;
            releases.add(new Release(
                ghRelease.getName(),
                ghRelease.getTagName(),
                ghRelease.getHtmlUrl().toExternalForm(),
                ghRelease.getPublished_at()
            ));
        }

        VersionUtils.clearUnparseableTags();
        Pattern versionPattern = VersionUtils.resolveVersionPattern(context);
        for (Release release : releases) {
            release.setVersion(VersionUtils.version(context, release.getTagName(), versionPattern));
        }

        releases.sort((r1, r2) -> r2.getVersion().compareTo(r1.getVersion()));

        return releases;
    }

    private void createRelease(Github api, String tagName, String changelog, boolean deleteTags) throws IOException {
        org.jreleaser.model.internal.release.GithubReleaser github = context.getModel().getRelease().getGithub();

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
            deleteTags(api, github.getCanonicalRepoName(), tagName);
        }

        // local tag
        if (deleteTags || !github.isSkipTag()) {
            context.getLogger().debug(RB.$("git.releaser.repository.tag"), tagName);
            GitSdk.of(context).tag(tagName, true, context);
        }

        // remote tag/release
        GHRelease release = api.createRelease(github.getCanonicalRepoName(), tagName)
            .commitish(github.getBranch())
            .name(github.getEffectiveReleaseName())
            .draft(github.isDraft())
            .prerelease(github.getPrerelease().isEnabled())
            .body(changelog)
            .create();
        api.uploadAssets(release, assets);

        if (github.getMilestone().isClose() && !context.getModel().getProject().isSnapshot()) {
            Optional<GHMilestone> milestone = api.findMilestoneByName(
                github.getOwner(),
                github.getName(),
                github.getMilestone().getEffectiveName());
            if (milestone.isPresent()) {
                api.closeMilestone(github.getOwner(),
                    github.getName(),
                    milestone.get());
            }
        }

        linkDiscussion(tagName, release);
        updateIssues(github, api);
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

        GHRepository ghRepository = api.findRepository(github.getOwner(), github.getName());
        GHLabel ghLabel = null;

        try {
            ghLabel = api.getOrCreateLabel(
                ghRepository,
                labelName,
                labelColor,
                github.getIssues().getLabel().getDescription());
        } catch (IOException e) {
            throw new IllegalStateException(RB.$("ERROR_git_releaser_fetch_label", tagName, labelName), e);
        }

        Optional<GHMilestone> milestone = Optional.empty();
        Apply applyMilestone = github.getIssues().getApplyMilestone();
        if (applyMilestone != Apply.NEVER) {
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
            try {
                Optional<GHIssue> op = api.findIssue(ghRepository, Integer.parseInt(issueNumber));
                if (!op.isPresent()) continue;

                GHIssue ghIssue = op.get();
                if (ghIssue.getState() == GHIssueState.CLOSED && ghIssue.getLabels().stream().noneMatch(l -> l.getName().equals(labelName))) {
                    context.getLogger().debug(RB.$("git.issue.release", issueNumber));
                    ghIssue.addLabels(ghLabel);
                    ghIssue.comment(comment);

                    if (milestone.isPresent()) {
                        applyMilestone(issueNumber, ghIssue, applyMilestone, milestone.get());
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException(RB.$("ERROR_git_releaser_cannot_release", tagName, issueNumber), e);
            }
        }
    }

    private void applyMilestone(String issueNumber, GHIssue ghIssue, Apply applyMilestone, GHMilestone targetMilestone) throws IOException {
        GHMilestone issueMilestone = ghIssue.getMilestone();
        String targetMilestoneTitle = targetMilestone.getTitle();

        if (null == issueMilestone) {
            context.getLogger().debug(RB.$("git.issue.milestone.apply", targetMilestoneTitle, issueNumber));
            ghIssue.setMilestone(targetMilestone);
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
                    ghIssue.setMilestone(targetMilestone);
                } else {
                    context.getLogger().debug(uncapitalize(RB.$("git.issue.milestone.warn", issueNumber, milestoneTitle)));
                }
            }
        }
    }

    private void updateAssets(Github api, GHRelease release) throws IOException {
        org.jreleaser.model.internal.release.GithubReleaser github = context.getModel().getRelease().getGithub();

        List<Asset> assetsToBeUpdated = new ArrayList<>();
        List<Asset> assetsToBeUploaded = new ArrayList<>();

        Map<String, GHAsset> existingAssets = api.listAssets(github.getOwner(), github.getName(), release);
        Map<String, Asset> assetsToBePublished = new LinkedHashMap<>();
        assets.forEach(asset -> assetsToBePublished.put(asset.getFilename(), asset));

        assetsToBePublished.keySet().forEach(name -> {
            if (existingAssets.containsKey(name)) {
                assetsToBeUpdated.add(assetsToBePublished.get(name));
            } else {
                assetsToBeUploaded.add(assetsToBePublished.get(name));
            }
        });

        api.updateAssets(release, assetsToBeUpdated, existingAssets);
        api.uploadAssets(release, assetsToBeUploaded);
    }

    private void linkDiscussion(String tagName, GHRelease release) {
        org.jreleaser.model.internal.release.GithubReleaser github = context.getModel().getRelease().getGithub();

        String discussionCategoryName = github.getDiscussionCategoryName();
        if (context.getModel().getProject().isSnapshot() ||
            isBlank(discussionCategoryName) ||
            github.isDraft()) return;

        context.getLogger().debug(RB.$("git.releaser.link.discussion"), tagName, discussionCategoryName);

        if (context.isDryrun()) return;

        try {
            XGithub xapi = new XGithub(context.getLogger(),
                github.getApiEndpoint(),
                github.getToken(),
                github.getConnectTimeout(),
                github.getReadTimeout());

            GhRelease ghRelease = new GhRelease();
            ghRelease.setDiscussionCategoryName(discussionCategoryName);
            xapi.updateRelease(github.getOwner(),
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

    private void deleteTags(Github api, String repo, String tagName) {
        // delete remote tag
        try {
            api.deleteTag(repo, tagName);
        } catch (IOException ignored) {
            //noop
        }
    }
}
