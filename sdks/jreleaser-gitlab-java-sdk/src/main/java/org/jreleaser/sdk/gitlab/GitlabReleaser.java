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
package org.jreleaser.sdk.gitlab;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.UpdateSection;
import org.jreleaser.model.api.common.Apply;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.ExtraProperties;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.upload.Uploader;
import org.jreleaser.model.internal.util.Artifacts;
import org.jreleaser.model.internal.util.VersionUtils;
import org.jreleaser.model.spi.release.AbstractReleaser;
import org.jreleaser.model.spi.release.Asset;
import org.jreleaser.model.spi.release.Release;
import org.jreleaser.model.spi.release.ReleaseException;
import org.jreleaser.model.spi.release.Repository;
import org.jreleaser.model.spi.release.User;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.sdk.commons.RestAPIException;
import org.jreleaser.sdk.git.ChangelogProvider;
import org.jreleaser.sdk.git.GitSdk;
import org.jreleaser.sdk.git.ReleaseUtils;
import org.jreleaser.sdk.gitlab.api.GlFileUpload;
import org.jreleaser.sdk.gitlab.api.GlIssue;
import org.jreleaser.sdk.gitlab.api.GlLabel;
import org.jreleaser.sdk.gitlab.api.GlLink;
import org.jreleaser.sdk.gitlab.api.GlLinkRequest;
import org.jreleaser.sdk.gitlab.api.GlMilestone;
import org.jreleaser.sdk.gitlab.api.GlProject;
import org.jreleaser.sdk.gitlab.api.GlRelease;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.jreleaser.model.Constants.KEY_PLATFORM_REPLACED;
import static org.jreleaser.model.api.signing.Signing.KEY_SKIP_SIGNING;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.uncapitalize;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class GitlabReleaser extends AbstractReleaser<org.jreleaser.model.api.release.GitlabReleaser> {
    private final org.jreleaser.model.internal.release.GitlabReleaser gitlab;

    public GitlabReleaser(JReleaserContext context, List<Asset> assets) {
        super(context, assets);
        gitlab = context.getModel().getRelease().getGitlab();
    }

    @Override
    public org.jreleaser.model.api.release.GitlabReleaser getReleaser() {
        return gitlab.asImmutable();
    }

    @Override
    public String generateReleaseNotes() throws IOException {
        try {
            return ChangelogProvider.getChangelog(context).trim();
        } catch (IOException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_error_changelog"), e);
        }
    }

    @Override
    protected void createTag() throws ReleaseException {
        ReleaseUtils.createTag(context);
    }

    @Override
    protected void createRelease() throws ReleaseException {
        context.getLogger().info(RB.$("git.releaser.releasing"), gitlab.getResolvedRepoUrl(context.getModel()));
        String tagName = gitlab.getEffectiveTagName(context.getModel());

        try {
            Gitlab api = new Gitlab(context.getLogger(),
                gitlab.getApiEndpoint(),
                gitlab.getToken(),
                gitlab.getConnectTimeout(),
                gitlab.getReadTimeout());

            String branch = gitlab.getBranch();

            List<String> branchNames = api.listBranches(gitlab.getOwner(), gitlab.getName(), gitlab.getProjectIdentifier());
            if (!branchNames.contains(branch)) {
                throw new ReleaseException(RB.$("ERROR_git_release_branch_not_exists", branch, branchNames));
            }

            String changelog = context.getChangelog().getResolvedChangelog();

            context.getLogger().debug(RB.$("git.releaser.release.lookup"), tagName, gitlab.getCanonicalRepoName());
            GlRelease release = api.findReleaseByTag(gitlab.getOwner(), gitlab.getName(), gitlab.getProjectIdentifier(), tagName);
            boolean snapshot = context.getModel().getProject().isSnapshot();
            if (null != release) {
                context.getLogger().debug(RB.$("git.releaser.release.exists"), tagName);
                if (gitlab.isOverwrite() || snapshot) {
                    context.getLogger().debug(RB.$("git.releaser.release.delete"), tagName);
                    if (!context.isDryrun()) {
                        api.deleteRelease(gitlab.getOwner(), gitlab.getName(), gitlab.getProjectIdentifier(), tagName);
                    }
                    context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                    createRelease(api, tagName, changelog, gitlab.isMatch());
                } else if (gitlab.getUpdate().isEnabled()) {
                    context.getLogger().debug(RB.$("git.releaser.release.update"), tagName);
                    if (!context.isDryrun()) {
                        boolean update = false;
                        GlRelease updater = new GlRelease();
                        if (gitlab.getUpdate().getSections().contains(UpdateSection.TITLE)) {
                            update = true;
                            context.getLogger().info(RB.$("git.releaser.release.update.title"), gitlab.getEffectiveReleaseName());
                            updater.setName(gitlab.getEffectiveReleaseName());
                        }
                        if (gitlab.getUpdate().getSections().contains(UpdateSection.BODY)) {
                            update = true;
                            context.getLogger().info(RB.$("git.releaser.release.update.body"));
                            updater.setDescription(changelog);
                        }
                        if (update) {
                            api.updateRelease(gitlab.getOwner(), gitlab.getName(), gitlab.getProjectIdentifier(), updater);
                        }

                        if (gitlab.getUpdate().getSections().contains(UpdateSection.ASSETS)) {
                            updateAssets(api, release);
                        }
                        updateIssues(gitlab, api);
                    }
                } else {
                    if (context.isDryrun()) {
                        context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                        createRelease(api, tagName, changelog, false);
                        return;
                    }

                    throw new IllegalStateException(RB.$("ERROR_git_releaser_cannot_release",
                        "GitLab", tagName));
                }
            } else {
                context.getLogger().debug(RB.$("git.releaser.release.not.found"), tagName);
                context.getLogger().debug(RB.$("git.releaser.release.create"), tagName);
                createRelease(api, tagName, changelog, snapshot && gitlab.isMatch());
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
        org.jreleaser.model.internal.release.GitlabReleaser gitlab = context.getModel().getRelease().getGitlab();
        context.getLogger().debug(RB.$("git.repository.lookup"), owner, repo);

        Gitlab api = new Gitlab(context.getLogger(),
            gitlab.getApiEndpoint(),
            password,
            gitlab.getConnectTimeout(),
            gitlab.getReadTimeout());
        GlProject project = null;

        try {
            project = api.findProject(repo, gitlab.getProjectIdentifier());
        } catch (RestAPIException e) {
            if (!e.isNotFound()) {
                throw e;
            }
        }

        if (null == project) {
            project = api.createProject(owner, repo);
        }

        return new Repository(
            Repository.Kind.GITLAB,
            owner,
            repo,
            project.getWebUrl(),
            project.getHttpUrlToRepo());
    }

    @Override
    public Optional<User> findUser(String email, String name) {
        org.jreleaser.model.internal.release.GitlabReleaser gitlab = context.getModel().getRelease().getGitlab();

        try {
            return new Gitlab(context.getLogger(),
                gitlab.getApiEndpoint(),
                gitlab.getToken(),
                gitlab.getConnectTimeout(),
                gitlab.getReadTimeout())
                .findUser(email, name);
        } catch (RestAPIException e) {
            context.getLogger().trace(e);
            context.getLogger().debug(RB.$("git.releaser.user.not.found"), email);
        }

        return Optional.empty();
    }

    @Override
    public List<Release> listReleases(String owner, String repo) throws IOException {
        org.jreleaser.model.internal.release.GitlabReleaser gitlab = context.getModel().getRelease().getGitlab();

        Gitlab api = new Gitlab(context.getLogger(),
            gitlab.getApiEndpoint(),
            gitlab.getToken(),
            gitlab.getConnectTimeout(),
            gitlab.getReadTimeout());

        List<Release> releases = api.listReleases(owner, repo, gitlab.getProjectIdentifier());

        VersionUtils.clearUnparseableTags();
        Pattern versionPattern = VersionUtils.resolveVersionPattern(context);
        for (Release release : releases) {
            release.setVersion(VersionUtils.version(context, release.getTagName(), versionPattern));
        }

        releases.sort((r1, r2) -> r2.getVersion().compareTo(r1.getVersion()));

        return releases;
    }

    private void createRelease(Gitlab api, String tagName, String changelog, boolean deleteTags) throws IOException {
        org.jreleaser.model.internal.release.GitlabReleaser gitlab = context.getModel().getRelease().getGitlab();

        Collection<GlLinkRequest> links = collectUploadLinks(gitlab);

        if (context.isDryrun()) {
            if (!assets.isEmpty()) {
                for (Asset asset : assets) {
                    if (0 == Files.size(asset.getPath()) || !Files.exists(asset.getPath())) {
                        // do not upload empty or non existent files
                        continue;
                    }

                    context.getLogger().info(" " + RB.$("git.upload.asset"), asset.getFilename());
                }
            }
            if (!links.isEmpty()) {
                for (GlLinkRequest link : links) {
                    context.getLogger().info(" " + RB.$("git.upload.asset"), link.getName());
                }
            }
            updateIssues(gitlab, api);

            return;
        }

        Integer projectIdentifier = api.findProject(gitlab.getName(), gitlab.getProjectIdentifier()).getId();

        if (deleteTags) {
            deleteTags(api, gitlab.getOwner(), gitlab.getName(), projectIdentifier, tagName);
        }

        // local tag
        if (deleteTags || !gitlab.isSkipTag()) {
            context.getLogger().debug(RB.$("git.releaser.repository.tag"), tagName);
            GitSdk.of(context).tag(tagName, true, context);
        }

        GlRelease release = new GlRelease();
        release.setName(gitlab.getEffectiveReleaseName());
        release.setTagName(tagName);
        release.setRef(gitlab.getBranch());
        release.setDescription(changelog);

        // remote tag/release
        api.createRelease(gitlab.getOwner(), gitlab.getName(), projectIdentifier, release);

        if (!assets.isEmpty()) {
            Collection<GlFileUpload> uploads = api.uploadAssets(gitlab.getOwner(), gitlab.getName(), projectIdentifier, assets);
            api.linkReleaseAssets(gitlab.getOwner(), gitlab.getName(), release, projectIdentifier, uploads);
        }
        if (!links.isEmpty()) {
            api.linkAssets(gitlab.getOwner(), gitlab.getName(), release, projectIdentifier, links);
        }

        if (gitlab.getMilestone().isClose() && !context.getModel().getProject().isSnapshot()) {
            Optional<GlMilestone> milestone = api.findMilestoneByName(
                gitlab.getOwner(),
                gitlab.getName(),
                projectIdentifier,
                gitlab.getMilestone().getEffectiveName());
            if (milestone.isPresent()) {
                api.closeMilestone(gitlab.getOwner(),
                    gitlab.getName(),
                    projectIdentifier,
                    milestone.get());
            }
        }
        updateIssues(gitlab, api);
    }

    private void updateAssets(Gitlab api, GlRelease release) throws IOException {
        List<Asset> assetsToBeUpdated = new ArrayList<>();
        List<Asset> assetsToBeUploaded = new ArrayList<>();

        Integer projectIdentifier = api.findProject(gitlab.getName(), gitlab.getProjectIdentifier()).getId();
        String tagName = gitlab.getEffectiveTagName(context.getModel());
        Map<String, GlLink> existingAssets = api.listLinks(projectIdentifier, tagName);

        Map<String, Asset> assetsToBePublished = new LinkedHashMap<>();
        assets.forEach(asset -> assetsToBePublished.put(asset.getFilename(), asset));

        assetsToBePublished.keySet().forEach(name -> {
            if (existingAssets.containsKey(name)) {
                assetsToBeUpdated.add(assetsToBePublished.get(name));
            } else {
                assetsToBeUploaded.add(assetsToBePublished.get(name));
            }
        });

        updateAssets(api, release, assetsToBeUpdated, projectIdentifier, tagName, existingAssets);
        uploadAssets(api, release, assetsToBeUploaded, projectIdentifier);
        if (!gitlab.getUploadLinks().isEmpty()) {
            Collection<GlLinkRequest> links = collectUploadLinks(gitlab);
            api.linkAssets(gitlab.getOwner(), gitlab.getName(), release, projectIdentifier, links);
        }
    }

    private void updateAssets(Gitlab api, GlRelease release, List<Asset> assetsToBeUpdated, Integer projectIdentifier, String tagName, Map<String, GlLink> existingLinks) throws IOException {
        if (!assetsToBeUpdated.isEmpty()) {
            for (Asset asset : assetsToBeUpdated) {
                GlLink existingLink = existingLinks.get(asset.getFilename());
                api.deleteLinkedAsset(gitlab.getToken(), projectIdentifier, tagName, existingLink);
            }

            Collection<GlFileUpload> uploads = api.uploadAssets(gitlab.getOwner(), gitlab.getName(), projectIdentifier, assetsToBeUpdated);
            api.linkReleaseAssets(gitlab.getOwner(), gitlab.getName(), release, projectIdentifier, uploads);
        }
    }

    private void uploadAssets(Gitlab api, GlRelease release, List<Asset> assetsToBeUploaded, Integer projectIdentifier) throws IOException {
        if (!assetsToBeUploaded.isEmpty()) {
            Collection<GlFileUpload> uploads = api.uploadAssets(gitlab.getOwner(), gitlab.getName(), projectIdentifier, assetsToBeUploaded);
            api.linkReleaseAssets(gitlab.getOwner(), gitlab.getName(), release, projectIdentifier, uploads);
        }
    }

    private void updateIssues(org.jreleaser.model.internal.release.GitlabReleaser gitlab, Gitlab api) throws IOException {
        if (!gitlab.getIssues().isEnabled()) return;

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

        Integer projectIdentifier = api.findProject(gitlab.getName(), gitlab.getProjectIdentifier()).getId();
        String tagName = gitlab.getEffectiveTagName(context.getModel());
        String labelName = gitlab.getIssues().getLabel().getName();
        String labelColor = gitlab.getIssues().getLabel().getColor();
        TemplateContext props = gitlab.props(context.getModel());
        gitlab.fillProps(props, context.getModel());
        String comment = resolveTemplate(gitlab.getIssues().getComment(), props);

        if (!labelColor.startsWith("#")) {
            try {
                Integer.parseInt(labelColor, 16);
                labelColor = "#" + labelColor;
            } catch (NumberFormatException ok) {
                // ignored
            }
        }

        GlLabel glLabel = null;

        try {
            glLabel = api.getOrCreateLabel(
                projectIdentifier,
                labelName,
                labelColor,
                gitlab.getIssues().getLabel().getDescription());
        } catch (IOException e) {
            throw new IllegalStateException(RB.$("ERROR_git_releaser_fetch_label", tagName, labelName), e);
        }

        Optional<GlMilestone> milestone = Optional.empty();
        Apply applyMilestone = gitlab.getIssues().getApplyMilestone();
        if (gitlab.getMilestone().isClose() && !context.getModel().getProject().isSnapshot()) {
            milestone = api.findMilestoneByName(
                gitlab.getOwner(),
                gitlab.getName(),
                projectIdentifier,
                gitlab.getMilestone().getEffectiveName());

            if (!milestone.isPresent()) {
                milestone = api.findClosedMilestoneByName(
                    gitlab.getOwner(),
                    gitlab.getName(),
                    projectIdentifier,
                    gitlab.getMilestone().getEffectiveName());
            }
        }

        List<GlIssue> issues = api.listIssues(projectIdentifier);

        for (String issueNumber : issueNumbers) {
            Integer in = Integer.parseInt(issueNumber);
            Optional<GlIssue> op = issues.stream().filter(i -> i.getIid().equals(in)).findFirst();
            if (!op.isPresent()) continue;

            GlIssue glIssue = op.get();
            if ("closed".equals(glIssue.getState()) && glIssue.getLabels().stream().noneMatch(l -> l.equals(labelName))) {
                context.getLogger().debug(RB.$("git.issue.release", issueNumber));
                api.addLabelToIssue(projectIdentifier, glIssue, glLabel);
                api.commentOnIssue(projectIdentifier, glIssue, comment);

                milestone.ifPresent(glMilestone -> applyMilestone(api, projectIdentifier, issueNumber, glIssue, applyMilestone, glMilestone));
            }
        }
    }

    private void applyMilestone(Gitlab api, Integer projectIdentifier,
                                String issueNumber, GlIssue glIssue, Apply applyMilestone, GlMilestone targetMilestone) {
        GlMilestone issueMilestone = glIssue.getMilestone();
        String targetMilestoneTitle = targetMilestone.getTitle();

        if (null == issueMilestone) {
            context.getLogger().debug(RB.$("git.issue.milestone.apply", targetMilestoneTitle, issueNumber));
            api.setMilestoneOnIssue(projectIdentifier, glIssue, targetMilestone);
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
                    api.setMilestoneOnIssue(projectIdentifier, glIssue, targetMilestone);
                } else {
                    context.getLogger().debug(uncapitalize(RB.$("git.issue.milestone.warn", issueNumber, milestoneTitle)));
                }
            }
        }
    }

    private Collection<GlLinkRequest> collectUploadLinks(org.jreleaser.model.internal.release.GitlabReleaser gitlab) {
        List<GlLinkRequest> links = new ArrayList<>();

        for (Map.Entry<String, String> e : gitlab.getUploadLinks().entrySet()) {
            Optional<? extends Uploader> uploader = context.getModel().getUpload().getActiveUploader(e.getKey(), e.getValue());
            if (uploader.isPresent()) {
                collectUploadLinks(uploader.get(), links);
            }
        }

        return links;
    }

    private void collectUploadLinks(Uploader<?> uploader, List<GlLinkRequest> links) {
        List<String> keys = uploader.resolveSkipKeys();
        keys.add(org.jreleaser.model.api.release.GitlabReleaser.SKIP_GITLAB_LINKS);

        List<Artifact> artifacts = new ArrayList<>();

        if (uploader.isFiles()) {
            for (Artifact artifact : Artifacts.resolveFiles(context)) {
                if (!artifact.isActive()) continue;
                Path path = artifact.getEffectivePath(context);
                if (isSkip(artifact, keys)) continue;
                if (Files.exists(path) && 0 != path.toFile().length()) {
                    artifacts.add(artifact);
                }
            }
        }

        if (uploader.isArtifacts()) {
            for (Distribution distribution : context.getModel().getActiveDistributions()) {
                if (isSkip(distribution, keys)) continue;
                for (Artifact artifact : distribution.getArtifacts()) {
                    if (!artifact.isActive()) continue;
                    Path path = artifact.getEffectivePath(context, distribution);
                    if (isSkip(artifact, keys)) continue;
                    if (Files.exists(path) && 0 != path.toFile().length()) {
                        String platform = artifact.getPlatform();
                        String platformReplaced = distribution.getPlatform().applyReplacements(platform);
                        if (isNotBlank(platformReplaced)) {
                            artifact.getExtraProperties().put(KEY_PLATFORM_REPLACED, platformReplaced);
                        }
                        artifacts.add(artifact);
                    }
                }
            }
        }

        if (uploader.isSignatures() && context.getModel().getSigning().isEnabled()) {
            String extension = context.getModel().getSigning().isArmored() ? ".asc" : ".sig";

            List<Artifact> signatures = new ArrayList<>();
            for (Artifact artifact : artifacts) {
                if (artifact.extraPropertyIsTrue(KEY_SKIP_SIGNING)) continue;
                Path signaturePath = context.getSignaturesDirectory()
                    .resolve(artifact.getEffectivePath(context).getFileName() + extension);
                if (Files.exists(signaturePath) && 0 != signaturePath.toFile().length()) {
                    signatures.add(Artifact.of(signaturePath));
                }
            }

            artifacts.addAll(signatures);
        }

        for (Artifact artifact : artifacts) {
            links.add(toLinkRequest(artifact.getEffectivePath(), uploader.getResolvedDownloadUrl(context, artifact)));
        }
    }

    private boolean isSkip(ExtraProperties props, List<String> keys) {
        for (String key : keys) {
            if (props.extraPropertyIsTrue(key)) {
                return true;
            }
        }
        return false;
    }

    private void deleteTags(Gitlab api, String owner, String repo, Integer projectIdentifier, String tagName) {
        // delete remote tag
        try {
            api.deleteTag(owner, repo, projectIdentifier, tagName);
        } catch (RestAPIException ignored) {
            //noop
        }
    }

    private static GlLinkRequest toLinkRequest(Path path, String url) {
        GlLinkRequest link = new GlLinkRequest();
        link.setName(path.getFileName().toString());
        link.setUrl(url);
        link.setFilepath("/" + link.getName());
        return link;
    }
}
