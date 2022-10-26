/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
package org.jreleaser.model.internal;

import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class JReleaserSupport {
    private JReleaserSupport() {
        // noop
    }

    public static Set<String> supportedAssemblers() {
        Set<String> set = new LinkedHashSet<>();
        set.add(org.jreleaser.model.api.assemble.ArchiveAssembler.TYPE);
        set.add(org.jreleaser.model.api.assemble.JlinkAssembler.TYPE);
        set.add(org.jreleaser.model.api.assemble.JpackageAssembler.TYPE);
        set.add(org.jreleaser.model.api.assemble.NativeImageAssembler.TYPE);
        return unmodifiableSet(set);
    }

    public static Set<String> supportedAnnouncers() {
        Set<String> set = new LinkedHashSet<>();
        set.add(org.jreleaser.model.api.announce.ArticleAnnouncer.TYPE);
        set.add(org.jreleaser.model.api.announce.DiscordAnnouncer.TYPE);
        set.add(org.jreleaser.model.api.announce.DiscourseAnnouncer.TYPE);
        set.add(org.jreleaser.model.api.announce.DiscussionsAnnouncer.TYPE);
        set.add(org.jreleaser.model.api.announce.GitterAnnouncer.TYPE);
        set.add(org.jreleaser.model.api.announce.GoogleChatAnnouncer.TYPE);
        set.add(org.jreleaser.model.api.announce.HttpAnnouncers.TYPE);
        set.add(org.jreleaser.model.api.announce.SmtpAnnouncer.TYPE);
        set.add(org.jreleaser.model.api.announce.MastodonAnnouncer.TYPE);
        set.add(org.jreleaser.model.api.announce.MattermostAnnouncer.TYPE);
        set.add(org.jreleaser.model.api.announce.SdkmanAnnouncer.TYPE);
        set.add(org.jreleaser.model.api.announce.SlackAnnouncer.TYPE);
        set.add(org.jreleaser.model.api.announce.TeamsAnnouncer.TYPE);
        set.add(org.jreleaser.model.api.announce.TelegramAnnouncer.TYPE);
        set.add(org.jreleaser.model.api.announce.TwitterAnnouncer.TYPE);
        set.add(org.jreleaser.model.api.announce.WebhooksAnnouncer.TYPE);
        set.add(org.jreleaser.model.api.announce.ZulipAnnouncer.TYPE);
        return unmodifiableSet(set);
    }

    public static Set<String> supportedPackagers() {
        Set<String> set = new LinkedHashSet<>();
        set.add(org.jreleaser.model.api.packagers.AppImagePackager.TYPE);
        set.add(org.jreleaser.model.api.packagers.AsdfPackager.TYPE);
        set.add(org.jreleaser.model.api.packagers.BrewPackager.TYPE);
        set.add(org.jreleaser.model.api.packagers.ChocolateyPackager.TYPE);
        set.add(org.jreleaser.model.api.packagers.DockerPackager.TYPE);
        set.add(org.jreleaser.model.api.packagers.FlatpakPackager.TYPE);
        set.add(org.jreleaser.model.api.packagers.GofishPackager.TYPE);
        set.add(org.jreleaser.model.api.packagers.JbangPackager.TYPE);
        set.add(org.jreleaser.model.api.packagers.MacportsPackager.TYPE);
        set.add(org.jreleaser.model.api.packagers.ScoopPackager.TYPE);
        set.add(org.jreleaser.model.api.packagers.SdkmanPackager.TYPE);
        set.add(org.jreleaser.model.api.packagers.SnapPackager.TYPE);
        set.add(org.jreleaser.model.api.packagers.SpecPackager.TYPE);
        return unmodifiableSet(set);
    }

    public static Set<String> supportedDownloaders() {
        Set<String> set = new LinkedHashSet<>();
        set.add(org.jreleaser.model.api.download.FtpDownloader.TYPE);
        set.add(org.jreleaser.model.api.download.HttpDownloader.TYPE);
        set.add(org.jreleaser.model.api.download.ScpDownloader.TYPE);
        set.add(org.jreleaser.model.api.download.SftpDownloader.TYPE);
        return unmodifiableSet(set);
    }

    public static Set<String> supportedUploaders() {
        Set<String> set = new LinkedHashSet<>();
        set.add(org.jreleaser.model.api.upload.ArtifactoryUploader.TYPE);
        set.add(org.jreleaser.model.api.upload.FtpUploader.TYPE);
        set.add(org.jreleaser.model.api.upload.GiteaUploader.TYPE);
        set.add(org.jreleaser.model.api.upload.GitlabUploader.TYPE);
        set.add(org.jreleaser.model.api.upload.HttpUploader.TYPE);
        set.add(org.jreleaser.model.api.upload.S3Uploader.TYPE);
        set.add(org.jreleaser.model.api.upload.ScpUploader.TYPE);
        set.add(org.jreleaser.model.api.upload.SftpUploader.TYPE);
        return unmodifiableSet(set);
    }

    public static Set<String> supportedMavenDeployers() {
        Set<String> set = new LinkedHashSet<>();
        set.add(org.jreleaser.model.api.deploy.maven.ArtifactoryMavenDeployer.TYPE);
        set.add(org.jreleaser.model.api.deploy.maven.GiteaMavenDeployer.TYPE);
        set.add(org.jreleaser.model.api.deploy.maven.GithubMavenDeployer.TYPE);
        set.add(org.jreleaser.model.api.deploy.maven.GitlabMavenDeployer.TYPE);
        set.add(org.jreleaser.model.api.deploy.maven.Nexus2MavenDeployer.TYPE);
        return unmodifiableSet(set);
    }
}
