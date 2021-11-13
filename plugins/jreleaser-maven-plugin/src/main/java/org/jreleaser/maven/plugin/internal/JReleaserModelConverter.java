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
package org.jreleaser.maven.plugin.internal;

import org.jreleaser.maven.plugin.Announce;
import org.jreleaser.maven.plugin.Announcer;
import org.jreleaser.maven.plugin.Archive;
import org.jreleaser.maven.plugin.Article;
import org.jreleaser.maven.plugin.Artifact;
import org.jreleaser.maven.plugin.Artifactory;
import org.jreleaser.maven.plugin.Assemble;
import org.jreleaser.maven.plugin.Brew;
import org.jreleaser.maven.plugin.Bucket;
import org.jreleaser.maven.plugin.Cask;
import org.jreleaser.maven.plugin.Catalog;
import org.jreleaser.maven.plugin.Changelog;
import org.jreleaser.maven.plugin.Checksum;
import org.jreleaser.maven.plugin.Chocolatey;
import org.jreleaser.maven.plugin.Codeberg;
import org.jreleaser.maven.plugin.CommitAuthor;
import org.jreleaser.maven.plugin.Discord;
import org.jreleaser.maven.plugin.Discussions;
import org.jreleaser.maven.plugin.Distribution;
import org.jreleaser.maven.plugin.Docker;
import org.jreleaser.maven.plugin.DockerConfiguration;
import org.jreleaser.maven.plugin.DockerRepository;
import org.jreleaser.maven.plugin.DockerSpec;
import org.jreleaser.maven.plugin.Environment;
import org.jreleaser.maven.plugin.FileSet;
import org.jreleaser.maven.plugin.Files;
import org.jreleaser.maven.plugin.GenericGit;
import org.jreleaser.maven.plugin.GitService;
import org.jreleaser.maven.plugin.Gitea;
import org.jreleaser.maven.plugin.Github;
import org.jreleaser.maven.plugin.Gitlab;
import org.jreleaser.maven.plugin.Gitter;
import org.jreleaser.maven.plugin.Glob;
import org.jreleaser.maven.plugin.GoogleChat;
import org.jreleaser.maven.plugin.Http;
import org.jreleaser.maven.plugin.Java;
import org.jreleaser.maven.plugin.Jbang;
import org.jreleaser.maven.plugin.Jdeps;
import org.jreleaser.maven.plugin.Jlink;
import org.jreleaser.maven.plugin.Jreleaser;
import org.jreleaser.maven.plugin.Macports;
import org.jreleaser.maven.plugin.Mail;
import org.jreleaser.maven.plugin.Mastodon;
import org.jreleaser.maven.plugin.Mattermost;
import org.jreleaser.maven.plugin.Milestone;
import org.jreleaser.maven.plugin.NativeImage;
import org.jreleaser.maven.plugin.Packagers;
import org.jreleaser.maven.plugin.Plug;
import org.jreleaser.maven.plugin.Project;
import org.jreleaser.maven.plugin.Registry;
import org.jreleaser.maven.plugin.Release;
import org.jreleaser.maven.plugin.S3;
import org.jreleaser.maven.plugin.Scoop;
import org.jreleaser.maven.plugin.Sdkman;
import org.jreleaser.maven.plugin.SdkmanAnnouncer;
import org.jreleaser.maven.plugin.Signing;
import org.jreleaser.maven.plugin.Slack;
import org.jreleaser.maven.plugin.Slot;
import org.jreleaser.maven.plugin.Snap;
import org.jreleaser.maven.plugin.Tap;
import org.jreleaser.maven.plugin.Teams;
import org.jreleaser.maven.plugin.Telegram;
import org.jreleaser.maven.plugin.Twitter;
import org.jreleaser.maven.plugin.Upload;
import org.jreleaser.maven.plugin.Uploader;
import org.jreleaser.maven.plugin.Webhook;
import org.jreleaser.maven.plugin.Zulip;
import org.jreleaser.model.ChocolateyBucket;
import org.jreleaser.model.HomebrewTap;
import org.jreleaser.model.HttpUploader;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.JbangCatalog;
import org.jreleaser.model.MacportsRepository;
import org.jreleaser.model.Repository;
import org.jreleaser.model.RepositoryTap;
import org.jreleaser.model.ScoopBucket;
import org.jreleaser.model.SnapTap;
import org.jreleaser.model.UpdateSection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class JReleaserModelConverter {
    private JReleaserModelConverter() {
        // noop
    }

    public static JReleaserModel convert(Jreleaser jreleaser) {
        JReleaserModel jreleaserModel = new JReleaserModel();
        jreleaserModel.setEnvironment(convertEnvironment(jreleaser.getEnvironment()));
        jreleaserModel.setProject(convertProject(jreleaser.getProject()));
        jreleaserModel.setRelease(convertRelease(jreleaser.getRelease()));
        jreleaserModel.setUpload(convertUpload(jreleaser.getUpload()));
        jreleaserModel.setPackagers(convertPackagers(jreleaser.getPackagers()));
        jreleaserModel.setAnnounce(convertAnnounce(jreleaser.getAnnounce()));
        jreleaserModel.setAssemble(convertAssemble(jreleaser.getAssemble()));
        jreleaserModel.setChecksum(convertChecksum(jreleaser.getChecksum()));
        jreleaserModel.setSigning(convertSigning(jreleaser.getSigning()));
        jreleaserModel.setFiles(convertFiles(jreleaser.getFiles()));
        jreleaserModel.setDistributions(convertDistributions(jreleaser.getDistributions()));
        return jreleaserModel;
    }

    private static org.jreleaser.model.Environment convertEnvironment(Environment environment) {
        org.jreleaser.model.Environment e = new org.jreleaser.model.Environment();
        e.setVariables(environment.getVariables());
        e.setProperties(environment.getProperties());
        return e;
    }

    private static org.jreleaser.model.Project convertProject(Project project) {
        org.jreleaser.model.Project p = new org.jreleaser.model.Project();
        p.setName(project.getName());
        p.setVersion(project.getVersion());
        p.setVersionPattern(project.resolveVersionPattern());
        if (isNotBlank(project.getSnapshotPattern())) p.setSnapshotPattern(project.getSnapshotPattern());
        p.setSnapshot(convertSnapshot(project.getSnapshot()));
        p.setDescription(project.getDescription());
        p.setLongDescription(project.getLongDescription());
        p.setWebsite(project.getWebsite());
        p.setLicense(project.getLicense());
        p.setCopyright(project.getCopyright());
        p.setVendor(project.getVendor());
        p.setDocsUrl(project.getDocsUrl());
        p.setTags(project.getTags());
        p.setAuthors(project.getAuthors());
        p.setExtraProperties(project.getExtraProperties());
        p.setJava(convertJava(project.getJava()));
        return p;
    }

    private static org.jreleaser.model.Project.Snapshot convertSnapshot(Project.Snapshot snapshot) {
        org.jreleaser.model.Project.Snapshot s = new org.jreleaser.model.Project.Snapshot();
        s.setPattern(snapshot.getPattern());
        s.setLabel(snapshot.getLabel());
        if (snapshot.isFullChangelogSet()) s.setFullChangelog(snapshot.isFullChangelog());
        return s;
    }

    private static org.jreleaser.model.Java convertJava(Java java) {
        org.jreleaser.model.Java j = new org.jreleaser.model.Java();
        j.setEnabled(true);
        j.setGroupId(java.getGroupId());
        j.setArtifactId(java.getArtifactId());
        j.setVersion(java.getVersion());
        j.setMainClass(java.getMainClass());
        if (java.isMultiProjectSet()) j.setMultiProject(java.isMultiProject());
        return j;
    }

    private static org.jreleaser.model.Release convertRelease(Release release) {
        org.jreleaser.model.Release r = new org.jreleaser.model.Release();
        r.setGithub(convertGithub(release.getGithub()));
        r.setGitlab(convertGitlab(release.getGitlab()));
        r.setGitea(convertGitea(release.getGitea()));
        r.setCodeberg(convertCodeberg(release.getCodeberg()));
        r.setGeneric(convertGeneric(release.getGeneric()));
        return r;
    }

    private static org.jreleaser.model.Github convertGithub(Github github) {
        if (null == github) return null;
        org.jreleaser.model.Github g = new org.jreleaser.model.Github();
        convertGitService(github, g);
        g.setDraft(github.isDraft());
        if (github.isPrereleaseEnabledSet())
            g.setPrerelease(new org.jreleaser.model.GitService.Prerelease(github.getPrereleaseEnabled()));
        g.setPrerelease(convertPrerelease(github.getPrerelease()));
        g.setDiscussionCategoryName(github.getDiscussionCategoryName());
        return g;
    }

    private static org.jreleaser.model.Gitlab convertGitlab(Gitlab gitlab) {
        if (null == gitlab) return null;
        org.jreleaser.model.Gitlab g = new org.jreleaser.model.Gitlab();
        convertGitService(gitlab, g);
        g.setIdentifier(gitlab.getIdentifier());
        return g;
    }

    private static org.jreleaser.model.Gitea convertGitea(Gitea gitea) {
        if (null == gitea) return null;
        org.jreleaser.model.Gitea g = new org.jreleaser.model.Gitea();
        convertGitService(gitea, g);
        g.setDraft(gitea.isDraft());
        if (gitea.isPrereleaseEnabledSet())
            g.setPrerelease(new org.jreleaser.model.GitService.Prerelease(gitea.getPrereleaseEnabled()));
        g.setPrerelease(convertPrerelease(gitea.getPrerelease()));
        return g;
    }

    private static org.jreleaser.model.Codeberg convertCodeberg(Codeberg codeberg) {
        if (null == codeberg) return null;
        org.jreleaser.model.Codeberg g = new org.jreleaser.model.Codeberg();
        convertGitService(codeberg, g);
        g.setDraft(codeberg.isDraft());
        if (codeberg.isPrereleaseEnabledSet())
            g.setPrerelease(new org.jreleaser.model.GitService.Prerelease(codeberg.getPrereleaseEnabled()));
        g.setPrerelease(convertPrerelease(codeberg.getPrerelease()));
        return g;
    }

    private static org.jreleaser.model.GitService.Prerelease convertPrerelease(GitService.Prerelease prerelease) {
        org.jreleaser.model.GitService.Prerelease s = new org.jreleaser.model.GitService.Prerelease();
        s.setPattern(prerelease.getPattern());
        s.setEnabled(prerelease.getEnabled());
        return s;
    }

    private static org.jreleaser.model.GenericGit convertGeneric(GenericGit generic) {
        if (null == generic) return null;
        org.jreleaser.model.GenericGit g = new org.jreleaser.model.GenericGit();
        convertGitService(generic, g);
        return g;
    }

    private static void convertGitService(GitService service, org.jreleaser.model.GitService s) {
        if (isNotBlank(service.getHost())) s.setHost(service.getHost());
        if (isNotBlank(service.getOwner())) s.setOwner(service.getOwner());
        if (isNotBlank(service.getName())) s.setName(service.getName());
        if (isNotBlank(service.getRepoUrl())) s.setRepoUrl(service.getRepoUrl());
        if (isNotBlank(service.getRepoCloneUrl())) s.setRepoCloneUrl(service.getRepoCloneUrl());
        if (isNotBlank(service.getCommitUrl())) s.setCommitUrl(service.getCommitUrl());
        if (isNotBlank(service.getDownloadUrl())) s.setDownloadUrl(service.getDownloadUrl());
        if (isNotBlank(service.getReleaseNotesUrl())) s.setReleaseNotesUrl(service.getReleaseNotesUrl());
        if (isNotBlank(service.getLatestReleaseUrl())) s.setLatestReleaseUrl(service.getLatestReleaseUrl());
        if (isNotBlank(service.getIssueTrackerUrl())) s.setIssueTrackerUrl(service.getIssueTrackerUrl());
        if (isNotBlank(service.getUsername())) s.setUsername(service.getUsername());
        if (isNotBlank(service.getToken())) s.setToken(service.getToken());
        if (isNotBlank(service.getTagName())) s.setTagName(service.getTagName());
        if (isNotBlank(service.getPreviousTagName())) s.setPreviousTagName(service.getPreviousTagName());
        if (isNotBlank(service.getReleaseName())) s.setReleaseName(service.getReleaseName());
        if (isNotBlank(service.getBranch())) s.setBranch(service.getBranch());
        s.setCommitAuthor(convertCommitAuthor(service.getCommitAuthor()));
        s.setSign(service.isSign());
        if (service.isSkipTagSet()) s.setSkipTag(service.isSkipTag());
        if (service.isSkipReleaseSet()) s.setSkipRelease(service.isSkipRelease());
        if (service.isOverwriteSet()) s.setOverwrite(service.isOverwrite());
        if (service.isUpdateSet()) {
            s.setUpdate(service.isUpdate());
            s.setUpdateSections(convertUpdateSections(service.getUpdateSections()));
        }
        if (isNotBlank(service.getApiEndpoint())) s.setApiEndpoint(service.getApiEndpoint());
        s.setChangelog(convertChangelog(service.getChangelog()));
        s.setMilestone(convertMilestone(service.getMilestone()));
        s.setConnectTimeout(service.getConnectTimeout());
        s.setReadTimeout(service.getReadTimeout());
        if (service.isArtifactsSet()) s.setArtifacts(service.isArtifacts());
        if (service.isFilesSet()) s.setFiles(service.isFiles());
        if (service.isChecksumsSet()) s.setChecksums(service.isChecksums());
        if (service.isSignaturesSet()) s.setSignatures(service.isSignatures());
    }

    private static Set<org.jreleaser.model.UpdateSection> convertUpdateSections(Set<UpdateSection> updateSections) {
        return updateSections.stream()
            .map(s -> org.jreleaser.model.UpdateSection.of(s.name()))
            .collect(Collectors.toSet());
    }

    private static org.jreleaser.model.CommitAuthor convertCommitAuthor(CommitAuthor commitAuthor) {
        org.jreleaser.model.CommitAuthor ca = new org.jreleaser.model.CommitAuthor();
        ca.setName(commitAuthor.getName());
        ca.setEmail(commitAuthor.getEmail());
        return ca;
    }

    private static org.jreleaser.model.Changelog convertChangelog(Changelog changelog) {
        org.jreleaser.model.Changelog c = new org.jreleaser.model.Changelog();
        if (changelog.isEnabledSet()) c.setEnabled(changelog.isEnabled());
        c.setSort(changelog.getSort().name());
        c.setExternal(changelog.getExternal());
        c.setFormatted(changelog.resolveFormatted());
        c.getIncludeLabels().addAll(changelog.getIncludeLabels());
        c.getExcludeLabels().addAll(changelog.getExcludeLabels());
        if (isNotBlank(changelog.getChange())) c.setChange(changelog.getChange());
        c.setFormat(changelog.getFormat());
        c.setContent(changelog.getContent());
        c.setContentTemplate(changelog.getContentTemplate());
        c.setPreset(changelog.getPreset());
        c.setCategories(convertCategories(changelog.getCategories()));
        c.setLabelers(convertLabelers(changelog.getLabelers()));
        c.setReplacers(convertReplacers(changelog.getReplacers()));
        c.setHide(convertHide(changelog.getHide()));
        c.setContributors(convertContributors(changelog.getContributors()));
        return c;
    }

    private static org.jreleaser.model.Changelog.Hide convertHide(Changelog.Hide hide) {
        org.jreleaser.model.Changelog.Hide h = new org.jreleaser.model.Changelog.Hide();
        h.setUncategorized(hide.isUncategorized());
        h.setCategories(hide.getCategories());
        h.setContributors(hide.getContributors());
        return h;
    }

    private static org.jreleaser.model.Changelog.Contributors convertContributors(Changelog.Contributors contributors) {
        org.jreleaser.model.Changelog.Contributors c = new org.jreleaser.model.Changelog.Contributors();
        if (contributors.isEnabledSet()) c.setEnabled(contributors.isEnabled());
        c.setFormat(contributors.getFormat());
        return c;
    }

    private static List<org.jreleaser.model.Changelog.Category> convertCategories(List<Changelog.Category> categories) {
        List<org.jreleaser.model.Changelog.Category> list = new ArrayList<>();
        for (Changelog.Category category : categories) {
            org.jreleaser.model.Changelog.Category c = new org.jreleaser.model.Changelog.Category();
            c.setTitle(category.getTitle());
            c.setLabels(category.getLabels());
            c.setFormat(category.getFormat());
            list.add(c);
        }
        return list;
    }

    private static Set<org.jreleaser.model.Changelog.Labeler> convertLabelers(Set<Changelog.Labeler> labelers) {
        Set<org.jreleaser.model.Changelog.Labeler> set = new LinkedHashSet<>();
        for (Changelog.Labeler labeler : labelers) {
            org.jreleaser.model.Changelog.Labeler l = new org.jreleaser.model.Changelog.Labeler();
            l.setLabel(labeler.getLabel());
            l.setTitle(labeler.getTitle());
            l.setBody(labeler.getBody());
            set.add(l);
        }
        return set;
    }

    private static List<org.jreleaser.model.Changelog.Replacer> convertReplacers(List<Changelog.Replacer> replacers) {
        List<org.jreleaser.model.Changelog.Replacer> set = new ArrayList<>();
        for (Changelog.Replacer replacer : replacers) {
            org.jreleaser.model.Changelog.Replacer r = new org.jreleaser.model.Changelog.Replacer();
            r.setSearch(replacer.getSearch());
            r.setReplace(replacer.getReplace());
            set.add(r);
        }
        return set;
    }

    private static org.jreleaser.model.Milestone convertMilestone(Milestone milestone) {
        org.jreleaser.model.Milestone m = new org.jreleaser.model.Milestone();
        m.setClose(milestone.isClose());
        if (isNotBlank(milestone.getName())) m.setName(milestone.getName());
        return m;
    }

    private static org.jreleaser.model.Upload convertUpload(Upload upload) {
        org.jreleaser.model.Upload u = new org.jreleaser.model.Upload();
        if (upload.isEnabledSet()) u.setEnabled(upload.isEnabled());
        u.setArtifactory(convertArtifactory(upload.getArtifactory()));
        u.setHttp(convertHttp(upload.getHttp()));
        u.setS3(convertS3(upload.getS3()));
        return u;
    }

    private static Map<String, org.jreleaser.model.Artifactory> convertArtifactory(Map<String, Artifactory> artifactory) {
        Map<String, org.jreleaser.model.Artifactory> map = new LinkedHashMap<>();
        for (Map.Entry<String, Artifactory> e : artifactory.entrySet()) {
            e.getValue().setName(e.getKey());
            map.put(e.getKey(), convertArtifactory(e.getValue()));
        }
        return map;
    }

    private static org.jreleaser.model.Artifactory convertArtifactory(Artifactory artifactory) {
        org.jreleaser.model.Artifactory a = new org.jreleaser.model.Artifactory();
        convertUploader(artifactory, a);
        if (isNotBlank(artifactory.getTarget())) a.setTarget(artifactory.getTarget());
        a.setUsername(artifactory.getUsername());
        a.setPassword(artifactory.getPassword());
        a.setAuthorization(artifactory.resolveAuthorization().name());
        return a;
    }

    private static void convertUploader(Uploader from, org.jreleaser.model.Uploader into) {
        into.setName(from.getName());
        into.setActive(from.resolveActive());
        into.setExtraProperties(from.getExtraProperties());
        into.setConnectTimeout(from.getConnectTimeout());
        into.setReadTimeout(from.getReadTimeout());
        if (from.isArtifactsSet()) into.setArtifacts(from.isArtifacts());
        if (from.isFilesSet()) into.setFiles(from.isFiles());
        if (from.isSignaturesSet()) into.setSignatures(from.isSignatures());
        if (from instanceof HttpUploader) {
            convertHttpUploader((HttpUploader) from, (org.jreleaser.model.HttpUploader) into);
        }
    }

    private static void convertHttpUploader(HttpUploader from, org.jreleaser.model.HttpUploader into) {
        into.setUploadUrl(from.getUploadUrl());
        into.setDownloadUrl(from.getDownloadUrl());
    }

    private static Map<String, org.jreleaser.model.Http> convertHttp(Map<String, Http> http) {
        Map<String, org.jreleaser.model.Http> map = new LinkedHashMap<>();
        for (Map.Entry<String, Http> e : http.entrySet()) {
            e.getValue().setName(e.getKey());
            map.put(e.getKey(), convertHttp(e.getValue()));
        }
        return map;
    }

    private static org.jreleaser.model.Http convertHttp(Http http) {
        org.jreleaser.model.Http h = new org.jreleaser.model.Http();
        convertUploader(http, h);
        if (isNotBlank(http.getTarget())) h.setTarget(http.getTarget());
        h.setUsername(http.getUsername());
        h.setPassword(http.getPassword());
        h.setAuthorization(http.resolveAuthorization().name());
        h.setMethod(http.resolveMethod().name());
        h.setHeaders(http.getHeaders());
        return h;
    }

    private static Map<String, org.jreleaser.model.S3> convertS3(Map<String, S3> s3) {
        Map<String, org.jreleaser.model.S3> map = new LinkedHashMap<>();
        for (Map.Entry<String, S3> e : s3.entrySet()) {
            e.getValue().setName(e.getKey());
            map.put(e.getKey(), convertS3(e.getValue()));
        }
        return map;
    }

    private static org.jreleaser.model.S3 convertS3(S3 s3) {
        org.jreleaser.model.S3 s = new org.jreleaser.model.S3();
        convertUploader(s3, s);
        s.setRegion(s3.getRegion());
        s.setBucket(s3.getBucket());
        s.setAccessKeyId(s3.getAccessKeyId());
        s.setSecretKey(s3.getSecretKey());
        s.setSessionToken(s3.getSessionToken());
        s.setEndpoint(s3.getEndpoint());
        s.setPath(s3.getPath());
        s.setDownloadUrl(s3.getDownloadUrl());
        s.setHeaders(s3.getHeaders());
        return s;
    }

    private static org.jreleaser.model.Packagers convertPackagers(Packagers packagers) {
        org.jreleaser.model.Packagers p = new org.jreleaser.model.Packagers();
        if (packagers.getBrew().isSet()) p.setBrew(convertBrew(packagers.getBrew()));
        if (packagers.getChocolatey().isSet()) p.setChocolatey(convertChocolatey(packagers.getChocolatey()));
        if (packagers.getDocker().isSet()) p.setDocker(convertDocker(packagers.getDocker()));
        if (packagers.getJbang().isSet()) p.setJbang(convertJbang(packagers.getJbang()));
        if (packagers.getMacports().isSet()) p.setMacports(convertMacports(packagers.getMacports()));
        if (packagers.getScoop().isSet()) p.setScoop(convertScoop(packagers.getScoop()));
        if (packagers.getSdkman().isSet()) p.setSdkman(convertSdkman(packagers.getSdkman()));
        if (packagers.getSnap().isSet()) p.setSnap(convertSnap(packagers.getSnap()));
        return p;
    }

    private static org.jreleaser.model.Announce convertAnnounce(Announce announce) {
        org.jreleaser.model.Announce a = new org.jreleaser.model.Announce();
        if (announce.isEnabledSet()) a.setEnabled(announce.isEnabled());
        if (announce.getArticle().isSet()) a.setArticle(convertArticle(announce.getArticle()));
        if (announce.getDiscord().isSet()) a.setDiscord(convertDiscord(announce.getDiscord()));
        if (announce.getDiscussions().isSet()) a.setDiscussions(convertDiscussions(announce.getDiscussions()));
        if (announce.getGitter().isSet()) a.setGitter(convertGitter(announce.getGitter()));
        if (announce.getGoogleChat().isSet()) a.setGoogleChat(convertGoogleChat(announce.getGoogleChat()));
        if (announce.getMail().isSet()) a.setMail(convertMail(announce.getMail()));
        if (announce.getMastodon().isSet()) a.setMastodon(convertMastodon(announce.getMastodon()));
        if (announce.getMattermost().isSet()) a.setMattermost(convertMattermost(announce.getMattermost()));
        if (announce.getSdkman().isSet()) a.setSdkman(convertSdkmanAnnouncer(announce.getSdkman()));
        if (announce.getSlack().isSet()) a.setSlack(convertSlack(announce.getSlack()));
        if (announce.getTeams().isSet()) a.setTeams(convertTeams(announce.getTeams()));
        if (announce.getTelegram().isSet()) a.setTelegram(convertTelegram(announce.getTelegram()));
        if (announce.getTwitter().isSet()) a.setTwitter(convertTwitter(announce.getTwitter()));
        if (announce.getZulip().isSet()) a.setZulip(convertZulip(announce.getZulip()));
        a.setWebhooks(convertWebhooks(announce.getWebhooks()));
        return a;
    }

    private static org.jreleaser.model.Article convertArticle(Article article) {
        org.jreleaser.model.Article a = new org.jreleaser.model.Article();
        a.setActive(article.resolveActive());
        a.setExtraProperties(article.getExtraProperties());
        a.setFiles(convertArtifacts(article.getFiles()));
        a.setTemplateDirectory(article.getTemplateDirectory());
        a.setCommitAuthor(convertCommitAuthor(article.getCommitAuthor()));
        a.setRepository(convertRepository(article.getRepository()));
        return a;
    }

    private static Repository convertRepository(Tap tap) {
        Repository t = new Repository();
        convertTap(tap, t);
        return t;
    }

    private static void convertTap(Tap from, RepositoryTap into) {
        into.setOwner(from.getOwner());
        into.setName(from.getName());
        into.setBranch(from.getBranch());
        into.setUsername(from.getUsername());
        into.setToken(from.getToken());
    }

    private static void convertAnnouncer(Announcer from, org.jreleaser.model.Announcer into) {
        into.setActive(from.resolveActive());
        into.setConnectTimeout(from.getConnectTimeout());
        into.setReadTimeout(from.getReadTimeout());
        into.setExtraProperties(from.getExtraProperties());
    }

    private static org.jreleaser.model.Discord convertDiscord(Discord discord) {
        org.jreleaser.model.Discord a = new org.jreleaser.model.Discord();
        convertAnnouncer(discord, a);
        a.setWebhook(discord.getWebhook());
        a.setMessage(discord.getMessage());
        a.setMessageTemplate(discord.getMessageTemplate());
        return a;
    }

    private static org.jreleaser.model.Discussions convertDiscussions(Discussions discussions) {
        org.jreleaser.model.Discussions a = new org.jreleaser.model.Discussions();
        convertAnnouncer(discussions, a);
        a.setOrganization(discussions.getOrganization());
        a.setTeam(discussions.getTeam());
        a.setTitle(discussions.getTitle());
        a.setMessage(discussions.getMessage());
        a.setMessageTemplate(discussions.getMessageTemplate());
        return a;
    }

    private static org.jreleaser.model.Gitter convertGitter(Gitter gitter) {
        org.jreleaser.model.Gitter a = new org.jreleaser.model.Gitter();
        convertAnnouncer(gitter, a);
        a.setWebhook(gitter.getWebhook());
        a.setMessage(gitter.getMessage());
        a.setMessageTemplate(gitter.getMessageTemplate());
        return a;
    }

    private static org.jreleaser.model.GoogleChat convertGoogleChat(GoogleChat googleChat) {
        org.jreleaser.model.GoogleChat a = new org.jreleaser.model.GoogleChat();
        convertAnnouncer(googleChat, a);
        a.setWebhook(googleChat.getWebhook());
        a.setMessage(googleChat.getMessage());
        a.setMessageTemplate(googleChat.getMessageTemplate());
        return a;
    }

    private static org.jreleaser.model.Mail convertMail(Mail mail) {
        org.jreleaser.model.Mail a = new org.jreleaser.model.Mail();
        convertAnnouncer(mail, a);
        if (mail.isAuthSet()) a.setAuth(mail.isAuth());
        if (null != mail.getTransport()) a.setTransport(mail.getTransport().name());
        if (null != mail.getMimeType()) a.setMimeType(mail.getMimeType().name());
        a.setPort(mail.getPort());
        a.setUsername(mail.getUsername());
        a.setPassword(mail.getPassword());
        a.setFrom(mail.getFrom());
        a.setTo(mail.getTo());
        a.setCc(mail.getCc());
        a.setBcc(mail.getBcc());
        a.setSubject(mail.getSubject());
        a.setMessage(mail.getMessage());
        a.setMessageTemplate(mail.getMessageTemplate());
        return a;
    }

    private static org.jreleaser.model.Mastodon convertMastodon(Mastodon mastodon) {
        org.jreleaser.model.Mastodon a = new org.jreleaser.model.Mastodon();
        convertAnnouncer(mastodon, a);
        a.setHost(mastodon.getHost());
        a.setAccessToken(mastodon.getAccessToken());
        a.setStatus(mastodon.getStatus());
        return a;
    }

    private static org.jreleaser.model.Mattermost convertMattermost(Mattermost mattermost) {
        org.jreleaser.model.Mattermost a = new org.jreleaser.model.Mattermost();
        convertAnnouncer(mattermost, a);
        a.setWebhook(mattermost.getWebhook());
        a.setMessage(mattermost.getMessage());
        a.setMessageTemplate(mattermost.getMessageTemplate());
        return a;
    }

    private static org.jreleaser.model.SdkmanAnnouncer convertSdkmanAnnouncer(SdkmanAnnouncer sdkman) {
        org.jreleaser.model.SdkmanAnnouncer a = new org.jreleaser.model.SdkmanAnnouncer();
        convertAnnouncer(sdkman, a);
        a.setConsumerKey(sdkman.getConsumerKey());
        a.setConsumerToken(sdkman.getConsumerToken());
        a.setCandidate(sdkman.getCandidate());
        a.setReleaseNotesUrl(sdkman.getReleaseNotesUrl());
        a.setMajor(sdkman.isMajor());
        a.setCommand(sdkman.resolveCommand());
        return a;
    }

    private static org.jreleaser.model.Slack convertSlack(Slack slack) {
        org.jreleaser.model.Slack a = new org.jreleaser.model.Slack();
        convertAnnouncer(slack, a);
        a.setToken(slack.getToken());
        a.setWebhook(slack.getWebhook());
        a.setChannel(slack.getChannel());
        a.setMessage(slack.getMessage());
        a.setMessageTemplate(slack.getMessageTemplate());
        return a;
    }

    private static org.jreleaser.model.Teams convertTeams(Teams teams) {
        org.jreleaser.model.Teams a = new org.jreleaser.model.Teams();
        convertAnnouncer(teams, a);
        a.setWebhook(teams.getWebhook());
        a.setMessageTemplate(teams.getMessageTemplate());
        return a;
    }

    private static org.jreleaser.model.Telegram convertTelegram(Telegram telegram) {
        org.jreleaser.model.Telegram a = new org.jreleaser.model.Telegram();
        convertAnnouncer(telegram, a);
        a.setToken(telegram.getToken());
        a.setChatId(telegram.getChatId());
        a.setMessage(telegram.getMessage());
        a.setMessageTemplate(telegram.getMessageTemplate());
        return a;
    }

    private static org.jreleaser.model.Twitter convertTwitter(Twitter twitter) {
        org.jreleaser.model.Twitter a = new org.jreleaser.model.Twitter();
        convertAnnouncer(twitter, a);
        a.setConsumerKey(twitter.getConsumerKey());
        a.setConsumerSecret(twitter.getConsumerSecret());
        a.setAccessToken(twitter.getAccessToken());
        a.setAccessTokenSecret(twitter.getAccessTokenSecret());
        a.setStatus(twitter.getStatus());
        return a;
    }

    private static org.jreleaser.model.Zulip convertZulip(Zulip zulip) {
        org.jreleaser.model.Zulip a = new org.jreleaser.model.Zulip();
        convertAnnouncer(zulip, a);
        a.setAccount(zulip.getAccount());
        a.setApiKey(zulip.getApiKey());
        a.setApiHost(zulip.getApiHost());
        a.setChannel(zulip.getChannel());
        a.setSubject(zulip.getSubject());
        a.setMessage(zulip.getMessage());
        a.setMessageTemplate(zulip.getMessageTemplate());
        return a;
    }

    private static Map<String, org.jreleaser.model.Webhook> convertWebhooks(Map<String, Webhook> webhooks) {
        Map<String, org.jreleaser.model.Webhook> ds = new LinkedHashMap<>();
        for (Map.Entry<String, Webhook> e : webhooks.entrySet()) {
            e.getValue().setName(e.getKey());
            ds.put(e.getKey(), convertWebhook(e.getValue()));
        }
        return ds;
    }

    private static org.jreleaser.model.Webhook convertWebhook(Webhook webhook) {
        org.jreleaser.model.Webhook a = new org.jreleaser.model.Webhook();
        convertAnnouncer(webhook, a);
        a.setWebhook(webhook.getWebhook());
        a.setMessage(webhook.getMessage());
        a.setMessageProperty(webhook.getMessageProperty());
        a.setMessageTemplate(webhook.getMessageTemplate());
        return a;
    }

    private static org.jreleaser.model.Assemble convertAssemble(Assemble assemble) {
        org.jreleaser.model.Assemble a = new org.jreleaser.model.Assemble();
        if (assemble.isEnabledSet()) a.setEnabled(assemble.isEnabled());
        a.setArchive(convertArchive(assemble.getArchive()));
        a.setJlink(convertJlink(assemble.getJlink()));
        a.setNativeImage(convertNativeImage(assemble.getNativeImage()));
        return a;
    }


    private static Map<String, org.jreleaser.model.Archive> convertArchive(Map<String, Archive> archive) {
        Map<String, org.jreleaser.model.Archive> map = new LinkedHashMap<>();
        for (Map.Entry<String, Archive> e : archive.entrySet()) {
            e.getValue().setName(e.getKey());
            map.put(e.getKey(), convertArchive(e.getValue()));
        }
        return map;
    }

    private static org.jreleaser.model.Archive convertArchive(Archive archive) {
        org.jreleaser.model.Archive a = new org.jreleaser.model.Archive();
        a.setExported(archive.isExported());
        a.setName(archive.getName());
        a.setActive(archive.resolveActive());
        a.setExtraProperties(archive.getExtraProperties());
        a.setArchiveName(archive.getArchiveName());
        a.setDistributionType(archive.getDistributionType().name());
        if (archive.isAttachPlatformSet()) a.setAttachPlatform(archive.isAttachPlatform());
        a.setFormats(archive.getFormats().stream()
            .map(Object::toString)
            .map(org.jreleaser.model.Archive.Format::valueOf)
            .collect(Collectors.toSet()));
        a.setFileSets(convertFileSets(archive.getFileSets()));
        return a;
    }

    private static List<org.jreleaser.model.FileSet> convertFileSets(List<FileSet> fileSets) {
        return fileSets.stream()
            .map(JReleaserModelConverter::convertFileSet)
            .collect(Collectors.toList());
    }

    private static org.jreleaser.model.FileSet convertFileSet(FileSet fileSet) {
        org.jreleaser.model.FileSet f = new org.jreleaser.model.FileSet();
        f.setInput(fileSet.getInput());
        f.setOutput(fileSet.getOutput());
        f.setIncludes(fileSet.getIncludes());
        f.setExcludes(fileSet.getExcludes());
        f.setExtraProperties(fileSet.getExtraProperties());
        return f;
    }

    private static Map<String, org.jreleaser.model.Jlink> convertJlink(Map<String, Jlink> jlink) {
        Map<String, org.jreleaser.model.Jlink> map = new LinkedHashMap<>();
        for (Map.Entry<String, Jlink> e : jlink.entrySet()) {
            e.getValue().setName(e.getKey());
            map.put(e.getKey(), convertJlink(e.getValue()));
        }
        return map;
    }

    private static org.jreleaser.model.Jlink convertJlink(Jlink jlink) {
        org.jreleaser.model.Jlink a = new org.jreleaser.model.Jlink();
        a.setExported(jlink.isExported());
        a.setName(jlink.getName());
        a.setActive(jlink.resolveActive());
        a.setJava(convertJava(jlink.getJava()));
        a.setExecutable(jlink.getExecutable());
        a.setExtraProperties(jlink.getExtraProperties());
        a.setTemplateDirectory(jlink.getTemplateDirectory());
        a.setTargetJdks(convertArtifacts(jlink.getTargetJdks()));
        a.setModuleNames(jlink.getModuleNames());
        a.setArgs(jlink.getArgs());
        a.setJdeps(convertJdeps(jlink.getJdeps()));
        a.setJdk(convertArtifact(jlink.getJdk()));
        a.setMainJar(convertArtifact(jlink.getMainJar()));
        a.setImageName(jlink.getImageName());
        a.setImageNameTransform(jlink.getImageNameTransform());
        a.setModuleName(jlink.getModuleName());
        if (jlink.isCopyJarsSet()) a.setCopyJars(jlink.isCopyJars());
        a.setJars(convertGlobs(jlink.getJars()));
        a.setFiles(convertGlobs(jlink.getFiles()));
        a.setFileSets(convertFileSets(jlink.getFileSets()));
        return a;
    }

    private static org.jreleaser.model.Jdeps convertJdeps(Jdeps jdeps) {
        org.jreleaser.model.Jdeps j = new org.jreleaser.model.Jdeps();
        j.setMultiRelease(jdeps.getMultiRelease());
        if (jdeps.isIgnoreMissingDepsSet()) j.setIgnoreMissingDeps(jdeps.isIgnoreMissingDeps());
        return j;
    }

    private static Map<String, org.jreleaser.model.NativeImage> convertNativeImage(Map<String, NativeImage> nativeImage) {
        Map<String, org.jreleaser.model.NativeImage> map = new LinkedHashMap<>();
        for (Map.Entry<String, NativeImage> e : nativeImage.entrySet()) {
            e.getValue().setName(e.getKey());
            map.put(e.getKey(), convertNativeImage(e.getValue()));
        }
        return map;
    }

    private static org.jreleaser.model.NativeImage convertNativeImage(NativeImage nativeImage) {
        org.jreleaser.model.NativeImage a = new org.jreleaser.model.NativeImage();
        a.setExported(nativeImage.isExported());
        a.setName(nativeImage.getName());
        a.setActive(nativeImage.resolveActive());
        a.setJava(convertJava(nativeImage.getJava()));
        a.setExecutable(nativeImage.getExecutable());
        a.setExtraProperties(nativeImage.getExtraProperties());
        a.setTemplateDirectory(nativeImage.getTemplateDirectory());
        a.setGraal(convertArtifact(nativeImage.getGraal()));
        a.setMainJar(convertArtifact(nativeImage.getMainJar()));
        a.setImageName(nativeImage.getImageName());
        a.setImageNameTransform(nativeImage.getImageNameTransform());
        a.setJars(convertGlobs(nativeImage.getJars()));
        a.setFiles(convertGlobs(nativeImage.getFiles()));
        a.setFileSets(convertFileSets(nativeImage.getFileSets()));
        a.setArgs(nativeImage.getArgs());
        return a;
    }

    private static org.jreleaser.model.Checksum convertChecksum(Checksum checksum) {
        org.jreleaser.model.Checksum s = new org.jreleaser.model.Checksum();
        s.setName(checksum.getName());
        s.setIndividual(checksum.isIndividual());
        s.setAlgorithms(checksum.getAlgorithms());
        if (checksum.isFilesSet()) s.setFiles(checksum.isFiles());
        return s;
    }

    private static org.jreleaser.model.Signing convertSigning(Signing signing) {
        org.jreleaser.model.Signing s = new org.jreleaser.model.Signing();
        s.setActive(signing.resolveActive());
        s.setArmored(signing.isArmored());
        s.setPublicKey(signing.getPublicKey());
        s.setSecretKey(signing.getSecretKey());
        s.setPassphrase(signing.getPassphrase());
        s.setMode(signing.resolveMode());
        if (signing.isArtifactsSet()) s.setArtifacts(signing.isArtifacts());
        if (signing.isFilesSet()) s.setFiles(signing.isFiles());
        if (signing.isChecksumsSet()) s.setChecksums(signing.isChecksums());
        if (signing.isDefaultKeyringSet()) s.setDefaultKeyring(signing.isDefaultKeyring());
        s.setExecutable(signing.getExecutable());
        s.setKeyName(signing.getKeyName());
        s.setHomeDir(signing.getHomeDir());
        s.setPublicKeyring(signing.getPublicKeyring());
        s.setArgs(signing.getArgs());
        return s;
    }

    private static Map<String, org.jreleaser.model.Distribution> convertDistributions(Map<String, Distribution> distributions) {
        Map<String, org.jreleaser.model.Distribution> ds = new LinkedHashMap<>();
        for (Map.Entry<String, Distribution> e : distributions.entrySet()) {
            e.getValue().setName(e.getKey());
            ds.put(e.getKey(), convertDistribution(e.getValue()));
        }
        return ds;
    }

    private static org.jreleaser.model.Distribution convertDistribution(Distribution distribution) {
        org.jreleaser.model.Distribution d = new org.jreleaser.model.Distribution();
        d.setActive(distribution.resolveActive());
        d.setName(distribution.getName());
        d.setType(distribution.getType().name());
        d.setExecutable(distribution.getExecutable());
        d.setJava(convertJava(distribution.getJava()));
        d.setTags(distribution.getTags());
        d.setExtraProperties(distribution.getExtraProperties());
        d.setArtifacts(convertArtifacts(distribution.getArtifacts()));

        if (distribution.getBrew().isSet()) d.setBrew(convertBrew(distribution.getBrew()));
        if (distribution.getChocolatey().isSet()) d.setChocolatey(convertChocolatey(distribution.getChocolatey()));
        if (distribution.getDocker().isSet()) d.setDocker(convertDocker(distribution.getDocker()));
        if (distribution.getJbang().isSet()) d.setJbang(convertJbang(distribution.getJbang()));
        if (distribution.getMacports().isSet()) d.setMacports(convertMacports(distribution.getMacports()));
        if (distribution.getScoop().isSet()) d.setScoop(convertScoop(distribution.getScoop()));
        if (distribution.getSnap().isSet()) d.setSnap(convertSnap(distribution.getSnap()));

        return d;
    }

    private static org.jreleaser.model.Files convertFiles(Files files) {
        org.jreleaser.model.Files fs = new org.jreleaser.model.Files();
        fs.setArtifacts(convertArtifacts(files.getArtifacts()));
        fs.setGlobs(convertGlobs(files.getGlobs()));
        return fs;
    }

    private static Set<org.jreleaser.model.Artifact> convertArtifacts(Set<Artifact> artifacts) {
        Set<org.jreleaser.model.Artifact> as = new LinkedHashSet<>();
        for (Artifact artifact : artifacts) {
            as.add(convertArtifact(artifact));
        }
        return as;
    }

    private static org.jreleaser.model.Artifact convertArtifact(Artifact artifact) {
        org.jreleaser.model.Artifact a = new org.jreleaser.model.Artifact();
        a.setPath(artifact.getPath());
        a.setTransform(artifact.getTransform());
        a.setPlatform(artifact.getPlatform());
        a.setExtraProperties(artifact.getExtraProperties());
        return a;
    }

    private static List<org.jreleaser.model.Glob> convertGlobs(List<Glob> globs) {
        List<org.jreleaser.model.Glob> gs = new ArrayList<>();
        for (Glob glob : globs) {
            gs.add(convertGlob(glob));
        }
        return gs;
    }

    private static org.jreleaser.model.Glob convertGlob(Glob glob) {
        org.jreleaser.model.Glob g = new org.jreleaser.model.Glob();
        g.setPattern(glob.getPattern());
        if (isNotBlank(glob.getDirectory())) g.setDirectory(glob.getDirectory());
        if (isNotBlank(glob.getInclude())) g.setInclude(glob.getInclude());
        if (isNotBlank(glob.getExclude())) g.setExclude(glob.getExclude());
        if (glob.isRecursiveSet()) g.setRecursive(glob.isRecursive());
        return g;
    }

    private static org.jreleaser.model.Brew convertBrew(Brew tool) {
        org.jreleaser.model.Brew t = new org.jreleaser.model.Brew();
        t.setActive(tool.resolveActive());
        t.setTemplateDirectory(tool.getTemplateDirectory());
        if (tool.isContinueOnErrorSet()) t.setContinueOnError(tool.isContinueOnError());
        t.setExtraProperties(tool.getExtraProperties());
        t.setTap(convertHomebrewTap(tool.getTap()));
        t.setFormulaName(tool.getFormulaName());
        if (tool.isMultiPlatformSet()) t.setMultiPlatform(tool.isMultiPlatform());
        t.setCommitAuthor(convertCommitAuthor(tool.getCommitAuthor()));
        tool.getDependencies().forEach(dependency -> {
            if (isNotBlank(dependency.getValue())) {
                t.addDependency(dependency.getKey(), dependency.getValue());
            } else {
                t.addDependency(dependency.getKey());
            }
        });
        t.setLivecheck(tool.getLivecheck());
        if (tool.getCask().isSet()) {
            t.setCask(convertCask(tool.getCask()));
        }
        return t;
    }

    private static org.jreleaser.model.Cask convertCask(Cask cask) {
        org.jreleaser.model.Cask c = new org.jreleaser.model.Cask();
        c.setName(cask.getName());
        c.setDisplayName(cask.getDisplayName());
        c.setPkgName(cask.getPkgName());
        c.setAppName(cask.getAppName());
        c.setAppcast(cask.getAppcast());
        if (cask.isEnabledSet()) c.setEnabled(cask.isEnabled());
        c.setUninstall(cask.getUninstall());
        c.setZap(cask.getZap());
        return c;
    }

    private static HomebrewTap convertHomebrewTap(Tap tap) {
        HomebrewTap t = new HomebrewTap();
        convertTap(tap, t);
        return t;
    }

    private static org.jreleaser.model.Chocolatey convertChocolatey(Chocolatey tool) {
        org.jreleaser.model.Chocolatey t = new org.jreleaser.model.Chocolatey();
        t.setActive(tool.resolveActive());
        if (tool.isContinueOnErrorSet()) t.setContinueOnError(tool.isContinueOnError());
        t.setUsername(tool.getUsername());
        t.setRemoteBuild(tool.isRemoteBuild());
        t.setTemplateDirectory(tool.getTemplateDirectory());
        t.setExtraProperties(tool.getExtraProperties());
        t.setBucket(convertChocolateyBucket(tool.getBucket()));
        t.setCommitAuthor(convertCommitAuthor(tool.getCommitAuthor()));
        return t;
    }

    private static org.jreleaser.model.Docker convertDocker(Docker docker) {
        org.jreleaser.model.Docker t = new org.jreleaser.model.Docker();
        convertDocker(t, docker);
        t.setSpecs(convertDockerSpecs(docker.getSpecs()));
        return t;
    }

    private static void convertDocker(org.jreleaser.model.DockerConfiguration d, DockerConfiguration docker) {
        if (d instanceof org.jreleaser.model.Docker && docker instanceof Docker) {
            org.jreleaser.model.Docker dd = (org.jreleaser.model.Docker) d;
            Docker kk = (Docker) docker;
            if (kk.isContinueOnErrorSet()) dd.setContinueOnError(kk.isContinueOnError());

            dd.setRepository(convertDockerRepository(kk.getRepository()));
            dd.setCommitAuthor(convertCommitAuthor(kk.getCommitAuthor()));
        }
        d.setActive(docker.resolveActive());
        d.setTemplateDirectory(docker.getTemplateDirectory());
        d.setExtraProperties(docker.getExtraProperties());
        d.setBaseImage(docker.getBaseImage());
        d.setImageNames(docker.getImageNames());
        d.setBuildArgs(docker.getBuildArgs());
        d.setPreCommands(docker.getPreCommands());
        d.setPostCommands(docker.getPostCommands());
        d.setLabels(docker.getLabels());
        d.setRegistries(convertRegistries(docker.getRegistries()));
        if (docker.isUseLocalArtifactSet()) d.setUseLocalArtifact(docker.isUseLocalArtifact());
    }

    private static org.jreleaser.model.DockerRepository convertDockerRepository(DockerRepository tap) {
        org.jreleaser.model.DockerRepository t = new org.jreleaser.model.DockerRepository();
        convertTap(tap, t);
        if (tap.isVersionedSubfoldersSet()) t.setVersionedSubfolders(tap.isVersionedSubfolders());
        return t;
    }

    private static Map<String, org.jreleaser.model.DockerSpec> convertDockerSpecs(List<DockerSpec> specs) {
        Map<String, org.jreleaser.model.DockerSpec> ds = new LinkedHashMap<>();
        for (DockerSpec spec : specs) {
            ds.put(spec.getName(), convertDockerSpec(spec));
        }
        return ds;
    }

    private static org.jreleaser.model.DockerSpec convertDockerSpec(DockerSpec spec) {
        org.jreleaser.model.DockerSpec d = new org.jreleaser.model.DockerSpec();
        convertDocker(d, spec);
        d.setMatchers(spec.getMatchers());
        return d;
    }

    private static Set<org.jreleaser.model.Registry> convertRegistries(Set<Registry> repositories) {
        Set<org.jreleaser.model.Registry> set = new LinkedHashSet<>();
        for (Registry registry : repositories) {
            set.add(convertRegistry(registry));
        }
        return set;
    }

    private static org.jreleaser.model.Registry convertRegistry(Registry registry) {
        org.jreleaser.model.Registry r = new org.jreleaser.model.Registry();
        if (isNotBlank(registry.getServerName())) r.setServerName(registry.getServerName());
        r.setServer(registry.getServer());
        r.setRepositoryName(registry.getRepositoryName());
        r.setUsername(registry.getUsername());
        r.setPassword(registry.getPassword());
        return r;
    }

    private static ChocolateyBucket convertChocolateyBucket(Bucket bucket) {
        ChocolateyBucket b = new ChocolateyBucket();
        convertTap(bucket, b);
        return b;
    }

    private static org.jreleaser.model.Jbang convertJbang(Jbang tool) {
        org.jreleaser.model.Jbang t = new org.jreleaser.model.Jbang();
        t.setActive(tool.resolveActive());
        if (tool.isContinueOnErrorSet()) t.setContinueOnError(tool.isContinueOnError());
        t.setTemplateDirectory(tool.getTemplateDirectory());
        t.setExtraProperties(tool.getExtraProperties());
        t.setAlias(tool.getAlias());
        t.setCatalog(convertJbangCatalog(tool.getCatalog()));
        t.setCommitAuthor(convertCommitAuthor(tool.getCommitAuthor()));
        return t;
    }

    private static JbangCatalog convertJbangCatalog(Catalog catalog) {
        JbangCatalog t = new JbangCatalog();
        convertTap(catalog, t);
        return t;
    }

    private static org.jreleaser.model.Macports convertMacports(Macports tool) {
        org.jreleaser.model.Macports t = new org.jreleaser.model.Macports();
        t.setActive(tool.resolveActive());
        if (tool.isContinueOnErrorSet()) t.setContinueOnError(tool.isContinueOnError());
        t.setTemplateDirectory(tool.getTemplateDirectory());
        t.setExtraProperties(tool.getExtraProperties());
        t.setRevision(tool.getRevision());
        t.setCategories(tool.getCategories());
        t.setMaintainers(tool.getMaintainers());
        t.setRepository(convertMacportsRepository(tool.getRepository()));
        t.setCommitAuthor(convertCommitAuthor(tool.getCommitAuthor()));
        return t;
    }

    private static MacportsRepository convertMacportsRepository(Tap tap) {
        MacportsRepository r = new MacportsRepository();
        convertTap(tap, r);
        return r;
    }

    private static org.jreleaser.model.Scoop convertScoop(Scoop tool) {
        org.jreleaser.model.Scoop t = new org.jreleaser.model.Scoop();
        t.setActive(tool.resolveActive());
        if (tool.isContinueOnErrorSet()) t.setContinueOnError(tool.isContinueOnError());
        t.setTemplateDirectory(tool.getTemplateDirectory());
        t.setExtraProperties(tool.getExtraProperties());
        t.setCheckverUrl(tool.getCheckverUrl());
        t.setAutoupdateUrl(tool.getAutoupdateUrl());
        t.setBucket(convertScoopBucket(tool.getBucket()));
        t.setCommitAuthor(convertCommitAuthor(tool.getCommitAuthor()));
        return t;
    }

    private static ScoopBucket convertScoopBucket(Bucket bucket) {
        ScoopBucket b = new ScoopBucket();
        convertTap(bucket, b);
        return b;
    }

    private static org.jreleaser.model.Sdkman convertSdkman(Sdkman tool) {
        org.jreleaser.model.Sdkman t = new org.jreleaser.model.Sdkman();
        t.setActive(tool.resolveActive());
        if (tool.isContinueOnErrorSet()) t.setContinueOnError(tool.isContinueOnError());
        t.setExtraProperties(tool.getExtraProperties());
        t.setConsumerKey(tool.getConsumerKey());
        t.setConsumerToken(tool.getConsumerToken());
        t.setCandidate(tool.getCandidate());
        t.setCommand(tool.resolveCommand());
        t.setConnectTimeout(tool.getConnectTimeout());
        t.setReadTimeout(tool.getReadTimeout());
        return t;
    }

    private static org.jreleaser.model.Snap convertSnap(Snap tool) {
        org.jreleaser.model.Snap t = new org.jreleaser.model.Snap();
        t.setActive(tool.resolveActive());
        if (tool.isContinueOnErrorSet()) t.setContinueOnError(tool.isContinueOnError());
        t.setTemplateDirectory(tool.getTemplateDirectory());
        t.setExtraProperties(tool.getExtraProperties());
        if (isNotBlank(tool.getBase())) t.setBase(tool.getBase());
        if (isNotBlank(tool.getGrade())) t.setGrade(tool.getGrade());
        if (isNotBlank(tool.getConfinement())) t.setConfinement(tool.getConfinement());
        if (null != tool.getExportedLogin()) t.setExportedLogin(tool.getExportedLogin().getAbsolutePath());
        t.setRemoteBuild(tool.isRemoteBuild());
        t.setLocalPlugs(tool.getLocalPlugs());
        t.setLocalSlots(tool.getLocalSlots());
        t.setPlugs(convertPlugs(tool.getPlugs()));
        t.setSlots(convertSlots(tool.getSlots()));
        t.setSnap(convertSnapTap(tool.getSnap()));
        t.setCommitAuthor(convertCommitAuthor(tool.getCommitAuthor()));
        return t;
    }

    private static SnapTap convertSnapTap(Tap tap) {
        SnapTap t = new SnapTap();
        convertTap(tap, t);
        return t;
    }

    private static List<org.jreleaser.model.Plug> convertPlugs(List<Plug> plugs) {
        List<org.jreleaser.model.Plug> ps = new ArrayList<>();
        for (Plug plug : plugs) {
            ps.add(convertArtifact(plug));
        }
        return ps;
    }

    private static org.jreleaser.model.Plug convertArtifact(Plug plug) {
        org.jreleaser.model.Plug p = new org.jreleaser.model.Plug();
        p.setName(plug.getName());
        p.setAttributes(plug.getAttributes());
        return p;
    }

    private static List<org.jreleaser.model.Slot> convertSlots(List<Slot> slots) {
        List<org.jreleaser.model.Slot> ps = new ArrayList<>();
        for (Slot slot : slots) {
            ps.add(convertSlot(slot));
        }
        return ps;
    }

    private static org.jreleaser.model.Slot convertSlot(Slot slot) {
        org.jreleaser.model.Slot p = new org.jreleaser.model.Slot();
        p.setName(slot.getName());
        p.setAttributes(slot.getAttributes());
        p.setReads(slot.getReads());
        p.setWrites(slot.getWrites());
        return p;
    }
}
