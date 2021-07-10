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
import org.jreleaser.maven.plugin.DockerSpec;
import org.jreleaser.maven.plugin.Environment;
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
import org.jreleaser.maven.plugin.Jlink;
import org.jreleaser.maven.plugin.Jreleaser;
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
import org.jreleaser.maven.plugin.Scoop;
import org.jreleaser.maven.plugin.Sdkman;
import org.jreleaser.maven.plugin.Signing;
import org.jreleaser.maven.plugin.Slack;
import org.jreleaser.maven.plugin.Slot;
import org.jreleaser.maven.plugin.Snap;
import org.jreleaser.maven.plugin.Tap;
import org.jreleaser.maven.plugin.Teams;
import org.jreleaser.maven.plugin.Twitter;
import org.jreleaser.maven.plugin.Upload;
import org.jreleaser.maven.plugin.Webhook;
import org.jreleaser.maven.plugin.Zulip;
import org.jreleaser.model.ChocolateyBucket;
import org.jreleaser.model.HomebrewTap;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.JbangCatalog;
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
        p.setSnapshotPattern(project.getSnapshotPattern());
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
        if (github.isPrereleaseSet()) g.setPrerelease(github.isPrerelease());
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
        if (gitea.isPrereleaseSet()) g.setPrerelease(gitea.isPrerelease());
        return g;
    }

    private static org.jreleaser.model.Codeberg convertCodeberg(Codeberg codeberg) {
        if (null == codeberg) return null;
        org.jreleaser.model.Codeberg g = new org.jreleaser.model.Codeberg();
        convertGitService(codeberg, g);
        g.setDraft(codeberg.isDraft());
        if (codeberg.isPrereleaseSet()) g.setPrerelease(codeberg.isPrerelease());
        return g;
    }

    private static org.jreleaser.model.GenericGit convertGeneric(GenericGit generic) {
        if (null == generic) return null;
        org.jreleaser.model.GenericGit g = new org.jreleaser.model.GenericGit();
        convertGitService(generic, g);
        return g;
    }

    private static void convertGitService(GitService service, org.jreleaser.model.GitService s) {
        s.setHost(service.getHost());
        s.setOwner(service.getOwner());
        s.setName(service.getName());
        s.setRepoUrl(service.getRepoUrl());
        s.setRepoCloneUrl(service.getRepoCloneUrl());
        s.setCommitUrl(service.getCommitUrl());
        s.setDownloadUrl(service.getDownloadUrl());
        s.setReleaseNotesUrl(service.getReleaseNotesUrl());
        s.setLatestReleaseUrl(service.getLatestReleaseUrl());
        s.setIssueTrackerUrl(service.getIssueTrackerUrl());
        s.setUsername(service.getUsername());
        s.setToken(service.getToken());
        if (isNotBlank(service.getTagName())) s.setTagName(service.getTagName());
        if (isNotBlank(service.getReleaseName())) s.setReleaseName(service.getReleaseName());
        if (isNotBlank(service.getBranch())) s.setBranch(service.getBranch());
        s.setCommitAuthor(convertCommitAuthor(service.getCommitAuthor()));
        s.setSign(service.isSign());
        if (service.isSkipTagSet()) s.setSkipTag(service.isSkipTag());
        if (service.isOverwriteSet()) s.setOverwrite(service.isOverwrite());
        if (service.isUpdateSet()) {
            s.setUpdate(service.isUpdate());
            s.setUpdateSections(convertUpdateSections(service.getUpdateSections()));
        }
        s.setApiEndpoint(service.getApiEndpoint());
        s.setChangelog(convertChangelog(service.getChangelog()));
        s.setMilestone(convertMilestone(service.getMilestone()));
        s.setConnectTimeout(service.getConnectTimeout());
        s.setReadTimeout(service.getReadTimeout());
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
        c.setChange(changelog.getChange());
        c.setContent(changelog.getContent());
        c.setContentTemplate(changelog.getContentTemplate());
        c.setHiddenCategories(changelog.getHiddenCategories());
        c.setHideUncategorized(changelog.isHideUncategorized());
        c.setCategories(convertCategories(changelog.getCategories()));
        c.setLabelers(convertLabelers(changelog.getLabelers()));
        c.setReplacers(convertReplacers(changelog.getReplacers()));
        return c;
    }

    private static List<org.jreleaser.model.Changelog.Category> convertCategories(List<Changelog.Category> categories) {
        List<org.jreleaser.model.Changelog.Category> list = new ArrayList<>();
        for (Changelog.Category category : categories) {
            org.jreleaser.model.Changelog.Category c = new org.jreleaser.model.Changelog.Category();
            c.setTitle(category.getTitle());
            c.setLabels(category.getLabels());
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

    private static Set<org.jreleaser.model.Changelog.Replacer> convertReplacers(Set<Changelog.Replacer> replacers) {
        Set<org.jreleaser.model.Changelog.Replacer> set = new LinkedHashSet<>();
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
        a.setName(artifactory.getName());
        a.setActive(artifactory.resolveActive());
        a.setExtraProperties(artifactory.getExtraProperties());
        a.setConnectTimeout(artifactory.getConnectTimeout());
        a.setReadTimeout(artifactory.getReadTimeout());
        if (artifactory.isArtifactsSet()) a.setArtifacts(artifactory.isArtifacts());
        if (artifactory.isFilesSet()) a.setFiles(artifactory.isFiles());
        if (artifactory.isSignaturesSet()) a.setSignatures(artifactory.isSignatures());
        a.setTarget(artifactory.getTarget());
        a.setUsername(artifactory.getUsername());
        a.setPassword(artifactory.getPassword());
        a.setAuthorization(artifactory.resolveAuthorization().name());
        return a;
    }

    private static Map<String, org.jreleaser.model.HttpUploader> convertHttp(Map<String, Http> http) {
        Map<String, org.jreleaser.model.HttpUploader> map = new LinkedHashMap<>();
        for (Map.Entry<String, Http> e : http.entrySet()) {
            e.getValue().setName(e.getKey());
            map.put(e.getKey(), convertHttp(e.getValue()));
        }
        return map;
    }

    private static org.jreleaser.model.HttpUploader convertHttp(Http http) {
        org.jreleaser.model.HttpUploader h = new org.jreleaser.model.HttpUploader();
        h.setName(http.getName());
        h.setActive(http.resolveActive());
        h.setExtraProperties(http.getExtraProperties());
        h.setConnectTimeout(http.getConnectTimeout());
        h.setReadTimeout(http.getReadTimeout());
        if (http.isArtifactsSet()) h.setArtifacts(http.isArtifacts());
        if (http.isFilesSet()) h.setFiles(http.isFiles());
        if (http.isSignaturesSet()) h.setSignatures(http.isSignatures());
        h.setTarget(http.getTarget());
        h.setUsername(http.getUsername());
        h.setPassword(http.getPassword());
        h.setAuthorization(http.resolveAuthorization().name());
        h.setMethod(http.resolveMethod().name());
        h.setHeaders(http.getHeaders());
        return h;
    }

    private static org.jreleaser.model.Packagers convertPackagers(Packagers packagers) {
        org.jreleaser.model.Packagers p = new org.jreleaser.model.Packagers();
        if (packagers.getBrew().isSet()) p.setBrew(convertBrew(packagers.getBrew()));
        if (packagers.getChocolatey().isSet()) p.setChocolatey(convertChocolatey(packagers.getChocolatey()));
        if (packagers.getDocker().isSet()) p.setDocker(convertDocker(packagers.getDocker()));
        if (packagers.getJbang().isSet()) p.setJbang(convertJbang(packagers.getJbang()));
        if (packagers.getScoop().isSet()) p.setScoop(convertScoop(packagers.getScoop()));
        if (packagers.getSnap().isSet()) p.setSnap(convertSnap(packagers.getSnap()));
        return p;
    }

    private static org.jreleaser.model.Announce convertAnnounce(Announce announce) {
        org.jreleaser.model.Announce a = new org.jreleaser.model.Announce();
        if (announce.isEnabledSet()) a.setEnabled(announce.isEnabled());
        if (announce.getDiscord().isSet()) a.setDiscord(convertDiscord(announce.getDiscord()));
        if (announce.getDiscussions().isSet()) a.setDiscussions(convertDiscussions(announce.getDiscussions()));
        if (announce.getGitter().isSet()) a.setGitter(convertGitter(announce.getGitter()));
        if (announce.getGoogleChat().isSet()) a.setGoogleChat(convertGoogleChat(announce.getGoogleChat()));
        if (announce.getMail().isSet()) a.setMail(convertMail(announce.getMail()));
        if (announce.getMastodon().isSet()) a.setMastodon(convertMastodon(announce.getMastodon()));
        if (announce.getMattermost().isSet()) a.setMattermost(convertMattermost(announce.getMattermost()));
        if (announce.getSdkman().isSet()) a.setSdkman(convertSdkman(announce.getSdkman()));
        if (announce.getSlack().isSet()) a.setSlack(convertSlack(announce.getSlack()));
        if (announce.getTeams().isSet()) a.setTeams(convertTeams(announce.getTeams()));
        if (announce.getTwitter().isSet()) a.setTwitter(convertTwitter(announce.getTwitter()));
        if (announce.getZulip().isSet()) a.setZulip(convertZulip(announce.getZulip()));
        a.setWebhooks(convertWebhooks(announce.getWebhooks()));
        return a;
    }

    private static org.jreleaser.model.Discord convertDiscord(Discord discord) {
        org.jreleaser.model.Discord a = new org.jreleaser.model.Discord();
        a.setActive(discord.resolveActive());
        a.setWebhook(discord.getWebhook());
        a.setMessage(discord.getMessage());
        a.setMessageTemplate(discord.getMessageTemplate());
        a.setConnectTimeout(discord.getConnectTimeout());
        a.setReadTimeout(discord.getReadTimeout());
        a.setExtraProperties(discord.getExtraProperties());
        return a;
    }

    private static org.jreleaser.model.Discussions convertDiscussions(Discussions discussions) {
        org.jreleaser.model.Discussions a = new org.jreleaser.model.Discussions();
        a.setActive(discussions.resolveActive());
        a.setOrganization(discussions.getOrganization());
        a.setTeam(discussions.getTeam());
        a.setTitle(discussions.getTitle());
        a.setMessage(discussions.getMessage());
        a.setMessageTemplate(discussions.getMessageTemplate());
        a.setConnectTimeout(discussions.getConnectTimeout());
        a.setReadTimeout(discussions.getReadTimeout());
        a.setExtraProperties(discussions.getExtraProperties());
        return a;
    }

    private static org.jreleaser.model.Gitter convertGitter(Gitter gitter) {
        org.jreleaser.model.Gitter a = new org.jreleaser.model.Gitter();
        a.setActive(gitter.resolveActive());
        a.setWebhook(gitter.getWebhook());
        a.setMessage(gitter.getMessage());
        a.setMessageTemplate(gitter.getMessageTemplate());
        a.setConnectTimeout(gitter.getConnectTimeout());
        a.setReadTimeout(gitter.getReadTimeout());
        a.setExtraProperties(gitter.getExtraProperties());
        return a;
    }

    private static org.jreleaser.model.GoogleChat convertGoogleChat(GoogleChat googleChat) {
        org.jreleaser.model.GoogleChat a = new org.jreleaser.model.GoogleChat();
        a.setActive(googleChat.resolveActive());
        a.setWebhook(googleChat.getWebhook());
        a.setMessage(googleChat.getMessage());
        a.setMessageTemplate(googleChat.getMessageTemplate());
        a.setConnectTimeout(googleChat.getConnectTimeout());
        a.setReadTimeout(googleChat.getReadTimeout());
        a.setExtraProperties(googleChat.getExtraProperties());
        return a;
    }

    private static org.jreleaser.model.Mail convertMail(Mail mail) {
        org.jreleaser.model.Mail a = new org.jreleaser.model.Mail();
        a.setActive(mail.resolveActive());
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
        a.setExtraProperties(mail.getExtraProperties());
        return a;
    }

    private static org.jreleaser.model.Mastodon convertMastodon(Mastodon mastodon) {
        org.jreleaser.model.Mastodon a = new org.jreleaser.model.Mastodon();
        a.setActive(mastodon.resolveActive());
        a.setHost(mastodon.getHost());
        a.setAccessToken(mastodon.getAccessToken());
        a.setStatus(mastodon.getStatus());
        a.setConnectTimeout(mastodon.getConnectTimeout());
        a.setReadTimeout(mastodon.getReadTimeout());
        a.setExtraProperties(mastodon.getExtraProperties());
        return a;
    }

    private static org.jreleaser.model.Mattermost convertMattermost(Mattermost mattermost) {
        org.jreleaser.model.Mattermost a = new org.jreleaser.model.Mattermost();
        a.setActive(mattermost.resolveActive());
        a.setWebhook(mattermost.getWebhook());
        a.setMessage(mattermost.getMessage());
        a.setMessageTemplate(mattermost.getMessageTemplate());
        a.setConnectTimeout(mattermost.getConnectTimeout());
        a.setReadTimeout(mattermost.getReadTimeout());
        a.setExtraProperties(mattermost.getExtraProperties());
        return a;
    }

    private static org.jreleaser.model.Sdkman convertSdkman(Sdkman sdkman) {
        org.jreleaser.model.Sdkman a = new org.jreleaser.model.Sdkman();
        a.setActive(sdkman.resolveActive());
        a.setConsumerKey(sdkman.getConsumerKey());
        a.setConsumerToken(sdkman.getConsumerToken());
        a.setCandidate(sdkman.getCandidate());
        a.setMajor(sdkman.isMajor());
        a.setConnectTimeout(sdkman.getConnectTimeout());
        a.setReadTimeout(sdkman.getReadTimeout());
        a.setExtraProperties(sdkman.getExtraProperties());
        return a;
    }

    private static org.jreleaser.model.Slack convertSlack(Slack slack) {
        org.jreleaser.model.Slack a = new org.jreleaser.model.Slack();
        a.setActive(slack.resolveActive());
        a.setToken(slack.getToken());
        a.setWebhook(slack.getWebhook());
        a.setChannel(slack.getChannel());
        a.setMessage(slack.getMessage());
        a.setMessageTemplate(slack.getMessageTemplate());
        a.setConnectTimeout(slack.getConnectTimeout());
        a.setReadTimeout(slack.getReadTimeout());
        a.setExtraProperties(slack.getExtraProperties());
        return a;
    }

    private static org.jreleaser.model.Teams convertTeams(Teams teams) {
        org.jreleaser.model.Teams a = new org.jreleaser.model.Teams();
        a.setActive(teams.resolveActive());
        a.setWebhook(teams.getWebhook());
        a.setMessageTemplate(teams.getMessageTemplate());
        a.setConnectTimeout(teams.getConnectTimeout());
        a.setReadTimeout(teams.getReadTimeout());
        a.setExtraProperties(teams.getExtraProperties());
        return a;
    }

    private static org.jreleaser.model.Twitter convertTwitter(Twitter twitter) {
        org.jreleaser.model.Twitter a = new org.jreleaser.model.Twitter();
        a.setActive(twitter.resolveActive());
        a.setConsumerKey(twitter.getConsumerKey());
        a.setConsumerSecret(twitter.getConsumerSecret());
        a.setAccessToken(twitter.getAccessToken());
        a.setAccessTokenSecret(twitter.getAccessTokenSecret());
        a.setStatus(twitter.getStatus());
        a.setConnectTimeout(twitter.getConnectTimeout());
        a.setReadTimeout(twitter.getReadTimeout());
        a.setExtraProperties(twitter.getExtraProperties());
        return a;
    }

    private static org.jreleaser.model.Zulip convertZulip(Zulip zulip) {
        org.jreleaser.model.Zulip a = new org.jreleaser.model.Zulip();
        a.setActive(zulip.resolveActive());
        a.setAccount(zulip.getAccount());
        a.setApiKey(zulip.getApiKey());
        a.setApiHost(zulip.getApiHost());
        a.setChannel(zulip.getChannel());
        a.setSubject(zulip.getSubject());
        a.setMessage(zulip.getMessage());
        a.setMessageTemplate(zulip.getMessageTemplate());
        a.setConnectTimeout(zulip.getConnectTimeout());
        a.setReadTimeout(zulip.getReadTimeout());
        a.setExtraProperties(zulip.getExtraProperties());
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
        a.setActive(webhook.resolveActive());
        a.setWebhook(webhook.getWebhook());
        a.setMessage(webhook.getMessage());
        a.setMessageProperty(webhook.getMessageProperty());
        a.setMessageTemplate(webhook.getMessageTemplate());
        a.setConnectTimeout(webhook.getConnectTimeout());
        a.setReadTimeout(webhook.getReadTimeout());
        a.setExtraProperties(webhook.getExtraProperties());
        return a;
    }

    private static org.jreleaser.model.Assemble convertAssemble(Assemble assemble) {
        org.jreleaser.model.Assemble a = new org.jreleaser.model.Assemble();
        if (assemble.isEnabledSet()) a.setEnabled(assemble.isEnabled());
        a.setJlink(convertJlink(assemble.getJlink()));
        a.setNativeImage(convertNativeImage(assemble.getNativeImage()));
        return a;
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
        a.setJdk(convertArtifact(jlink.getJdk()));
        a.setMainJar(convertArtifact(jlink.getMainJar()));
        a.setImageName(jlink.getImageName());
        a.setImageNameTransform(jlink.getImageNameTransform());
        a.setModuleName(jlink.getModuleName());
        a.setJars(convertGlobs(jlink.getJars()));
        return a;
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
        a.setArgs(nativeImage.getArgs());
        return a;
    }

    private static org.jreleaser.model.Checksum convertChecksum(Checksum checksum) {
        org.jreleaser.model.Checksum s = new org.jreleaser.model.Checksum();
        s.setName(checksum.getName());
        s.setIndividual(checksum.isIndividual());
        s.setAlgorithms(checksum.getAlgorithms());
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
        g.setDirectory(glob.getDirectory());
        g.setInclude(glob.getInclude());
        g.setExclude(glob.getExclude());
        if (glob.isRecursiveSet()) g.setRecursive(glob.isRecursive());
        return g;
    }

    private static org.jreleaser.model.Brew convertBrew(Brew brew) {
        org.jreleaser.model.Brew t = new org.jreleaser.model.Brew();
        t.setActive(brew.resolveActive());
        t.setTemplateDirectory(brew.getTemplateDirectory());
        t.setExtraProperties(brew.getExtraProperties());
        t.setTap(convertHomebrewTap(brew.getTap()));
        t.setFormulaName(brew.getFormulaName());
        t.setCommitAuthor(convertCommitAuthor(brew.getCommitAuthor()));
        brew.getDependencies().forEach(dependency -> {
            if (isNotBlank(dependency.getValue())) {
                t.addDependency(dependency.getKey(), dependency.getValue());
            } else {
                t.addDependency(dependency.getKey());
            }
        });
        t.setLivecheck(brew.getLivecheck());
        if (brew.getCask().isSet()) {
            t.setCask(convertCask(brew.getCask()));
        }
        return t;
    }

    private static org.jreleaser.model.Cask convertCask(Cask cask) {
        org.jreleaser.model.Cask c = new org.jreleaser.model.Cask();
        c.setName(cask.getName());
        c.setDisplayName(cask.getDisplayName());
        c.setPkgName(cask.getPkgName());
        c.setAppName(cask.getAppName());
        c.setUninstall(cask.getUninstall());
        c.setZap(cask.getZap());
        return c;
    }

    private static HomebrewTap convertHomebrewTap(Tap tap) {
        HomebrewTap t = new HomebrewTap();
        t.setOwner(tap.getOwner());
        t.setName(tap.getName());
        t.setUsername(tap.getUsername());
        t.setToken(tap.getToken());
        return t;
    }

    private static org.jreleaser.model.Chocolatey convertChocolatey(Chocolatey chocolatey) {
        org.jreleaser.model.Chocolatey t = new org.jreleaser.model.Chocolatey();
        t.setActive(chocolatey.resolveActive());
        t.setUsername(chocolatey.getUsername());
        t.setRemoteBuild(chocolatey.isRemoteBuild());
        t.setTemplateDirectory(chocolatey.getTemplateDirectory());
        t.setExtraProperties(chocolatey.getExtraProperties());
        t.setBucket(convertChocolateyBucket(chocolatey.getBucket()));
        t.setCommitAuthor(convertCommitAuthor(chocolatey.getCommitAuthor()));
        return t;
    }

    private static org.jreleaser.model.Docker convertDocker(Docker docker) {
        org.jreleaser.model.Docker t = new org.jreleaser.model.Docker();
        convertDocker(t, docker);
        t.setSpecs(convertDockerSpecs(docker.getSpecs()));
        return t;
    }

    private static void convertDocker(org.jreleaser.model.DockerConfiguration d, DockerConfiguration docker) {
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
        b.setOwner(bucket.getOwner());
        b.setName(bucket.getName());
        b.setUsername(bucket.getUsername());
        b.setToken(bucket.getToken());
        return b;
    }

    private static org.jreleaser.model.Jbang convertJbang(Jbang jbang) {
        org.jreleaser.model.Jbang t = new org.jreleaser.model.Jbang();
        t.setActive(jbang.resolveActive());
        t.setTemplateDirectory(jbang.getTemplateDirectory());
        t.setExtraProperties(jbang.getExtraProperties());
        t.setAlias(jbang.getAlias());
        t.setCatalog(convertJbangCatalog(jbang.getCatalog()));
        t.setCommitAuthor(convertCommitAuthor(jbang.getCommitAuthor()));
        return t;
    }

    private static JbangCatalog convertJbangCatalog(Catalog catalog) {
        JbangCatalog t = new JbangCatalog();
        t.setOwner(catalog.getOwner());
        t.setName(catalog.getName());
        t.setUsername(catalog.getUsername());
        t.setToken(catalog.getToken());
        return t;
    }

    private static org.jreleaser.model.Scoop convertScoop(Scoop scoop) {
        org.jreleaser.model.Scoop t = new org.jreleaser.model.Scoop();
        t.setActive(scoop.resolveActive());
        t.setTemplateDirectory(scoop.getTemplateDirectory());
        t.setExtraProperties(scoop.getExtraProperties());
        t.setCheckverUrl(scoop.getCheckverUrl());
        t.setAutoupdateUrl(scoop.getAutoupdateUrl());
        t.setBucket(convertScoopBucket(scoop.getBucket()));
        t.setCommitAuthor(convertCommitAuthor(scoop.getCommitAuthor()));
        return t;
    }

    private static ScoopBucket convertScoopBucket(Bucket bucket) {
        ScoopBucket b = new ScoopBucket();
        b.setOwner(bucket.getOwner());
        b.setName(bucket.getName());
        b.setUsername(bucket.getUsername());
        b.setToken(bucket.getToken());
        return b;
    }

    private static org.jreleaser.model.Snap convertSnap(Snap snap) {
        org.jreleaser.model.Snap t = new org.jreleaser.model.Snap();
        t.setActive(snap.resolveActive());
        t.setTemplateDirectory(snap.getTemplateDirectory());
        t.setExtraProperties(snap.getExtraProperties());
        if (isNotBlank(snap.getBase())) t.setBase(snap.getBase());
        if (isNotBlank(snap.getGrade())) t.setGrade(snap.getGrade());
        if (isNotBlank(snap.getConfinement())) t.setConfinement(snap.getConfinement());
        if (null != snap.getExportedLogin()) t.setExportedLogin(snap.getExportedLogin().getAbsolutePath());
        t.setRemoteBuild(snap.isRemoteBuild());
        t.setLocalPlugs(snap.getLocalPlugs());
        t.setLocalSlots(snap.getLocalSlots());
        t.setPlugs(convertPlugs(snap.getPlugs()));
        t.setSlots(convertSlots(snap.getSlots()));
        t.setSnap(convertSnapTap(snap.getSnap()));
        t.setCommitAuthor(convertCommitAuthor(snap.getCommitAuthor()));
        return t;
    }

    private static SnapTap convertSnapTap(Tap tap) {
        SnapTap t = new SnapTap();
        t.setOwner(tap.getOwner());
        t.setName(tap.getName());
        t.setUsername(tap.getUsername());
        t.setToken(tap.getToken());
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
