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
package org.jreleaser.maven.plugin.internal;

import org.jreleaser.maven.plugin.Announce;
import org.jreleaser.maven.plugin.Artifact;
import org.jreleaser.maven.plugin.Assemble;
import org.jreleaser.maven.plugin.Brew;
import org.jreleaser.maven.plugin.Bucket;
import org.jreleaser.maven.plugin.Catalog;
import org.jreleaser.maven.plugin.Changelog;
import org.jreleaser.maven.plugin.Chocolatey;
import org.jreleaser.maven.plugin.CommitAuthor;
import org.jreleaser.maven.plugin.Discussions;
import org.jreleaser.maven.plugin.Distribution;
import org.jreleaser.maven.plugin.Docker;
import org.jreleaser.maven.plugin.Environment;
import org.jreleaser.maven.plugin.Files;
import org.jreleaser.maven.plugin.GitService;
import org.jreleaser.maven.plugin.Gitea;
import org.jreleaser.maven.plugin.Github;
import org.jreleaser.maven.plugin.Gitlab;
import org.jreleaser.maven.plugin.Glob;
import org.jreleaser.maven.plugin.Java;
import org.jreleaser.maven.plugin.Jbang;
import org.jreleaser.maven.plugin.Jlink;
import org.jreleaser.maven.plugin.Jreleaser;
import org.jreleaser.maven.plugin.Mail;
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
import org.jreleaser.maven.plugin.Twitter;
import org.jreleaser.maven.plugin.Zulip;
import org.jreleaser.model.ChocolateyBucket;
import org.jreleaser.model.HomebrewTap;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.JbangCatalog;
import org.jreleaser.model.ScoopBucket;
import org.jreleaser.model.SnapTap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        jreleaserModel.setPackagers(convertPackagers(jreleaser.getPackagers()));
        jreleaserModel.setAnnounce(convertAnnounce(jreleaser.getAnnounce()));
        jreleaserModel.setAssemble(convertAssemble(jreleaser.getAssemble()));
        jreleaserModel.setSigning(convertSigning(jreleaser.getSigning()));
        jreleaserModel.setFiles(convertFiles(jreleaser.getFiles()));
        jreleaserModel.setDistributions(convertDistributions(jreleaserModel, jreleaser.getDistributions()));
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
        p.setSnapshotPattern(project.getSnapshotPattern());
        p.setDescription(project.getDescription());
        p.setLongDescription(project.getLongDescription());
        p.setWebsite(project.getWebsite());
        p.setLicense(project.getLicense());
        p.setTags(project.getTags());
        p.setAuthors(project.getAuthors());
        p.setExtraProperties(project.getExtraProperties());
        p.setJava(convertJava(project.getJava()));
        return p;
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
        return r;
    }

    private static org.jreleaser.model.Github convertGithub(Github github) {
        if (null == github) return null;
        org.jreleaser.model.Github g = new org.jreleaser.model.Github();
        convertGitService(github, g);
        if (isNotBlank(github.getTargetCommitish())) g.setTargetCommitish(github.getTargetCommitish());
        g.setDraft(github.isDraft());
        if (github.isPrereleaseSet()) g.setPrerelease(github.isPrerelease());
        return g;
    }

    private static org.jreleaser.model.Gitlab convertGitlab(Gitlab gitlab) {
        if (null == gitlab) return null;
        org.jreleaser.model.Gitlab g = new org.jreleaser.model.Gitlab();
        convertGitService(gitlab, g);
        if (isNotBlank(gitlab.getRef())) g.setRef(gitlab.getRef());
        return g;
    }

    private static org.jreleaser.model.Gitea convertGitea(Gitea gitea) {
        if (null == gitea) return null;
        org.jreleaser.model.Gitea g = new org.jreleaser.model.Gitea();
        convertGitService(gitea, g);
        if (isNotBlank(gitea.getTargetCommitish())) g.setTargetCommitish(gitea.getTargetCommitish());
        g.setDraft(gitea.isDraft());
        if (gitea.isPrereleaseSet()) g.setPrerelease(gitea.isPrerelease());
        return g;
    }

    private static void convertGitService(GitService service, org.jreleaser.model.GitService s) {
        s.setOwner(service.getOwner());
        s.setName(service.getName());
        s.setRepoUrlFormat(service.getRepoUrlFormat());
        s.setRepoCloneUrlFormat(service.getRepoCloneUrlFormat());
        s.setCommitUrlFormat(service.getCommitUrlFormat());
        s.setDownloadUrlFormat(service.getDownloadUrlFormat());
        s.setReleaseNotesUrlFormat(service.getReleaseNotesUrlFormat());
        s.setLatestReleaseUrlFormat(service.getLatestReleaseUrlFormat());
        s.setIssueTrackerUrlFormat(service.getIssueTrackerUrlFormat());
        s.setUsername(service.getUsername());
        s.setToken(service.getToken());
        if (isNotBlank(service.getTagName())) s.setTagName(service.getTagName());
        if (isNotBlank(service.getReleaseName())) s.setReleaseName(service.getReleaseName());
        s.setCommitAuthor(convertCommitAuthor(service.getCommitAuthor()));
        s.setSign(service.isSign());
        if (service.isSkipTagSet()) s.setSkipTag(service.isSkipTag());
        if (service.isOverwriteSet()) s.setOverwrite(service.isOverwrite());
        if (service.isUpdateSet()) s.setUpdate(service.isUpdate());
        s.setApiEndpoint(service.getApiEndpoint());
        s.setChangelog(convertChangelog(service.getChangelog()));
        s.setMilestone(convertMilestone(service.getMilestone()));
    }

    private static org.jreleaser.model.CommitAuthor convertCommitAuthor(CommitAuthor commitAuthor) {
        org.jreleaser.model.CommitAuthor ca = new org.jreleaser.model.CommitAuthor();
        ca.setName(commitAuthor.getName());
        ca.setEmail(commitAuthor.getEmail());
        return ca;
    }

    private static org.jreleaser.model.Changelog convertChangelog(Changelog changelog) {
        org.jreleaser.model.Changelog c = new org.jreleaser.model.Changelog();
        c.setEnabled(changelog.isEnabled());
        c.setSort(changelog.getSort().name());
        c.setExternal(changelog.getExternal());
        return c;
    }

    private static org.jreleaser.model.Milestone convertMilestone(Milestone milestone) {
        org.jreleaser.model.Milestone m = new org.jreleaser.model.Milestone();
        m.setClose(milestone.isClose());
        if (isNotBlank(milestone.getName())) m.setName(milestone.getName());
        return m;
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
        if (announce.getDiscussions().isSet()) a.setDiscussions(convertDiscussions(announce.getDiscussions()));
        if (announce.getMail().isSet()) a.setMail(convertMail(announce.getMail()));
        if (announce.getSdkman().isSet()) a.setSdkman(convertSdkman(announce.getSdkman()));
        if (announce.getSlack().isSet()) a.setSlack(convertSlack(announce.getSlack()));
        if (announce.getTwitter().isSet()) a.setTwitter(convertTwitter(announce.getTwitter()));
        if (announce.getZulip().isSet()) a.setZulip(convertZulip(announce.getZulip()));
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
        return a;
    }

    private static org.jreleaser.model.Sdkman convertSdkman(Sdkman sdkman) {
        org.jreleaser.model.Sdkman a = new org.jreleaser.model.Sdkman();
        a.setActive(sdkman.resolveActive());
        a.setConsumerKey(sdkman.getConsumerKey());
        a.setConsumerToken(sdkman.getConsumerToken());
        a.setCandidate(sdkman.getCandidate());
        a.setMajor(sdkman.isMajor());
        return a;
    }

    private static org.jreleaser.model.Slack convertSlack(Slack slack) {
        org.jreleaser.model.Slack a = new org.jreleaser.model.Slack();
        a.setActive(slack.resolveActive());
        a.setToken(slack.getToken());
        a.setChannel(slack.getChannel());
        a.setMessage(slack.getMessage());
        a.setMessageTemplate(slack.getMessageTemplate());
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
        return a;
    }

    private static org.jreleaser.model.Assemble convertAssemble(Assemble assemble) {
        org.jreleaser.model.Assemble a = new org.jreleaser.model.Assemble();
        if (assemble.isEnabledSet()) a.setEnabled(assemble.isEnabled());
        a.setJlinks(convertJlinks(assemble.getJlinks()));
        a.setNativeImages(convertNativeImages(assemble.getNativeImages()));
        return a;
    }

    private static Map<String, org.jreleaser.model.Jlink> convertJlinks(Map<String, Jlink> jlink) {
        Map<String, org.jreleaser.model.Jlink> map = new LinkedHashMap<>();
        for (Jlink jl : jlink.values()) {
            map.put(jl.getName(), convertJlinks(jl));
        }
        return map;
    }

    private static org.jreleaser.model.Jlink convertJlinks(Jlink jlink) {
        org.jreleaser.model.Jlink a = new org.jreleaser.model.Jlink();
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
        a.setModuleName(jlink.getModuleName());
        a.setJars(convertGlobs(jlink.getJars()));
        return a;
    }

    private static Map<String, org.jreleaser.model.NativeImage> convertNativeImages(Map<String, NativeImage> nativeImage) {
        Map<String, org.jreleaser.model.NativeImage> map = new LinkedHashMap<>();
        for (NativeImage ni : nativeImage.values()) {
            map.put(ni.getName(), convertNativeImages(ni));
        }
        return map;
    }

    private static org.jreleaser.model.NativeImage convertNativeImages(NativeImage nativeImage) {
        org.jreleaser.model.NativeImage a = new org.jreleaser.model.NativeImage();
        a.setName(nativeImage.getName());
        a.setActive(nativeImage.resolveActive());
        a.setJava(convertJava(nativeImage.getJava()));
        a.setExecutable(nativeImage.getExecutable());
        a.setExtraProperties(nativeImage.getExtraProperties());
        a.setTemplateDirectory(nativeImage.getTemplateDirectory());
        a.setGraal(convertArtifact(nativeImage.getGraal()));
        a.setMainJar(convertArtifact(nativeImage.getMainJar()));
        a.setArgs(nativeImage.getArgs());
        return a;
    }

    private static org.jreleaser.model.Signing convertSigning(Signing signing) {
        org.jreleaser.model.Signing s = new org.jreleaser.model.Signing();
        s.setActive(signing.resolveActive());
        s.setArmored(signing.isArmored());
        s.setPublicKey(signing.getPublicKey());
        s.setSecretKey(signing.getSecretKey());
        s.setPassphrase(signing.getPassphrase());
        return s;
    }

    private static Map<String, org.jreleaser.model.Distribution> convertDistributions(JReleaserModel model, List<Distribution> distributions) {
        Map<String, org.jreleaser.model.Distribution> ds = new LinkedHashMap<>();
        for (Distribution distribution : distributions) {
            ds.put(distribution.getName(), convertDistribution(model, distribution));
        }
        return ds;
    }

    private static org.jreleaser.model.Distribution convertDistribution(JReleaserModel model, Distribution distribution) {
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
        a.setHash(artifact.getHash());
        a.setPlatform(artifact.getPlatform());
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
        return t;
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
        t.setActive(docker.resolveActive());
        t.setTemplateDirectory(docker.getTemplateDirectory());
        t.setExtraProperties(docker.getExtraProperties());
        t.setBaseImage(docker.getBaseImage());
        t.setImageNames(docker.getImageNames());
        t.setBuildArgs(docker.getBuildArgs());
        t.setPreCommands(docker.getPreCommands());
        t.setPostCommands(docker.getPostCommnands());
        t.setLabels(docker.getLabels());
        t.setRegistries(convertRegistries(docker.getRegistries()));
        return t;
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