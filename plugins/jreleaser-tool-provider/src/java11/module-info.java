/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020".minisig" The JReleaser authors.
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

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
module org.jreleaser.tool {
    exports org.jreleaser.tool;

    requires java.logging;
    requires java.naming;
    requires java.sql;
    requires java.desktop;
    requires java.scripting;
    requires java.xml;
    // requires java.management; // twitter4j

    provides java.util.spi.ToolProvider
        with org.jreleaser.tool.JReleaser;

    provides java.security.Provider
        with jreleaser.shadow.org.bouncycastle.jce.provider.BouncyCastleProvider,
             jreleaser.shadow.org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
    provides javax.xml.stream.XMLEventFactory
        with jreleaser.shadow.com.ctc.wstx.stax.WstxEventFactory;
    provides javax.xml.stream.XMLInputFactory
        with jreleaser.shadow.com.ctc.wstx.stax.WstxInputFactory;
    provides javax.xml.stream.XMLOutputFactory
        with jreleaser.shadow.com.ctc.wstx.stax.WstxOutputFactory;
    provides javax.script.ScriptEngineFactory
        with jreleaser.shadow.org.apache.commons.jexl3.scripting.JexlScriptEngineFactory;
    provides org.jreleaser.extensions.api.Extension
        with jreleaser.shadow.org.jreleaser.extensions.internal.mustache.DefaultMustacheExtension;
    provides org.jreleaser.extensions.api.ExtensionManager
        with jreleaser.shadow.org.jreleaser.extensions.internal.DefaultExtensionManager;
    provides jreleaser.shadow.software.amazon.awssdk.http.SdkHttpService
        with jreleaser.shadow.software.amazon.awssdk.http.apache5.Apache5SdkHttpService;
    provides jreleaser.shadow.com.fasterxml.jackson.core.ObjectCodec
        with jreleaser.shadow.com.fasterxml.jackson.databind.ObjectMapper,
             jreleaser.shadow.com.fasterxml.jackson.dataformat.xml.XmlMapper,
             jreleaser.shadow.com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
    provides jreleaser.shadow.com.fasterxml.jackson.core.TokenStreamFactory
        with jreleaser.shadow.com.fasterxml.jackson.dataformat.toml.TomlFactory;
    provides jreleaser.shadow.com.fasterxml.jackson.databind.ObjectMapper
        with jreleaser.shadow.com.fasterxml.jackson.dataformat.toml.TomlMapper;
    provides jreleaser.shadow.com.fasterxml.jackson.core.JsonFactory
        with jreleaser.shadow.com.fasterxml.jackson.core.JsonFactory,
             jreleaser.shadow.com.fasterxml.jackson.dataformat.xml.XmlFactory,
             jreleaser.shadow.com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
    provides jreleaser.shadow.com.fasterxml.jackson.databind.Module
        with jreleaser.shadow.com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
    provides jreleaser.shadow.org.jreleaser.model.spi.deploy.maven.MavenDeployerFactory
        with jreleaser.shadow.org.jreleaser.sdk.artifactory.ArtifactoryMavenDeployerFactory,
             jreleaser.shadow.org.jreleaser.sdk.azure.AzureMavenDeployerFactory,
             jreleaser.shadow.org.jreleaser.sdk.forgejo.ForgejoMavenDeployerFactory,
             jreleaser.shadow.org.jreleaser.sdk.gitea.GiteaMavenDeployerFactory,
             jreleaser.shadow.org.jreleaser.sdk.github.GithubMavenDeployerFactory,
             jreleaser.shadow.org.jreleaser.sdk.gitlab.GitlabMavenDeployerFactory,
             jreleaser.shadow.org.jreleaser.sdk.mavencentral.MavenCentralMavenDeployerFactory,
             jreleaser.shadow.org.jreleaser.sdk.nexus2.Nexus2MavenDeployerFactory,
             jreleaser.shadow.org.jreleaser.sdk.nexus3.Nexus3MavenDeployerFactory;
    provides jreleaser.shadow.org.jreleaser.model.spi.announce.AnnouncerBuilderFactory
        with jreleaser.shadow.org.jreleaser.sdk.bluesky.BlueskyAnnouncerBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.discord.DiscordAnnouncerBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.discourse.DiscourseAnnouncerBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.github.DiscussionsAnnouncerBuilderFactory,
             jreleaser.shadow.org.jreleaser.announcers.ArticleAnnouncerBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.gitter.GitterAnnouncerBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.googlechat.GoogleChatAnnouncerBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.http.HttpAnnouncerBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.linkedin.LinkedinAnnouncerBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.mastodon.MastodonAnnouncerBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.mattermost.MattermostAnnouncerBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.opencollective.OpenCollectiveAnnouncerBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.reddit.RedditAnnouncerBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.sdkman.SdkmanAnnouncerBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.slack.SlackAnnouncerBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.smtp.SmtpAnnouncerBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.teams.TeamsAnnouncerBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.telegram.TelegramAnnouncerBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.twitter.TwitterAnnouncerBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.webhooks.WebhooksAnnouncerBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.zernio.ZernioAnnouncerBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.zulip.ZulipAnnouncerBuilderFactory;
    provides jreleaser.shadow.org.jreleaser.model.spi.release.ReleaserBuilderFactory
        with jreleaser.shadow.org.jreleaser.sdk.codeberg.CodebergReleaserBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.forgejo.ForgejoReleaserBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.generic.git.GenericGitReleaserBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.gitea.GiteaReleaserBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.github.GithubReleaserBuilderFactory,
             jreleaser.shadow.org.jreleaser.sdk.gitlab.GitlabReleaserBuilderFactory;
    provides jreleaser.shadow.org.jreleaser.config.JReleaserConfigParser
        with jreleaser.shadow.org.jreleaser.config.json.JsonJReleaserConfigParser,
             jreleaser.shadow.org.jreleaser.config.toml.TomlJReleaserConfigParser,
             jreleaser.shadow.org.jreleaser.config.yaml.YamlJReleaserConfigParser;
    provides jreleaser.shadow.org.jreleaser.model.spi.assemble.AssemblerProcessorFactory
        with jreleaser.shadow.org.jreleaser.assemblers.ArchiveAssemblerProcessorFactory,
             jreleaser.shadow.org.jreleaser.assemblers.DebAssemblerProcessorFactory,
             jreleaser.shadow.org.jreleaser.assemblers.JavaArchiveAssemblerProcessorFactory,
             jreleaser.shadow.org.jreleaser.assemblers.JlinkAssemblerProcessorFactory,
             jreleaser.shadow.org.jreleaser.assemblers.JpackageAssemblerProcessorFactory,
             jreleaser.shadow.org.jreleaser.assemblers.NativeImageAssemblerProcessorFactory;
    provides jreleaser.shadow.org.jreleaser.model.spi.catalog.sbom.SbomCatalogerProcessorFactory
        with jreleaser.shadow.org.jreleaser.catalogers.CyclonedxSbomCatalogerProcessorFactory,
             jreleaser.shadow.org.jreleaser.catalogers.SyftSbomCatalogerProcessorFactory;
    provides jreleaser.shadow.org.jreleaser.model.spi.packagers.PackagerProcessorFactory
        with jreleaser.shadow.org.jreleaser.packagers.AppImagePackagerProcessorFactory,
             jreleaser.shadow.org.jreleaser.packagers.AsdfPackagerProcessorFactory,
             jreleaser.shadow.org.jreleaser.packagers.BrewPackagerProcessorFactory,
             jreleaser.shadow.org.jreleaser.packagers.ChocolateyPackagerProcessorFactory,
             jreleaser.shadow.org.jreleaser.packagers.DockerPackagerProcessorFactory,
             jreleaser.shadow.org.jreleaser.packagers.FlatpakPackagerProcessorFactory,
             jreleaser.shadow.org.jreleaser.packagers.GofishPackagerProcessorFactory,
             jreleaser.shadow.org.jreleaser.packagers.JbangPackagerProcessorFactory,
             jreleaser.shadow.org.jreleaser.packagers.JibPackagerProcessorFactory,
             jreleaser.shadow.org.jreleaser.packagers.MacportsPackagerProcessorFactory,
             jreleaser.shadow.org.jreleaser.packagers.ScoopPackagerProcessorFactory,
             jreleaser.shadow.org.jreleaser.packagers.SdkmanPackagerProcessorFactory,
             jreleaser.shadow.org.jreleaser.packagers.SnapPackagerProcessorFactory,
             jreleaser.shadow.org.jreleaser.packagers.SpecPackagerProcessorFactory,
             jreleaser.shadow.org.jreleaser.packagers.WingetPackagerProcessorFactory;
    provides jreleaser.shadow.org.jreleaser.model.spi.upload.ArtifactUploaderFactory
        with jreleaser.shadow.org.jreleaser.sdk.artifactory.ArtifactoryArtifactUploaderFactory,
             jreleaser.shadow.org.jreleaser.sdk.forgejo.ForgejoArtifactUploaderFactory,
             jreleaser.shadow.org.jreleaser.sdk.ftp.FtpArtifactUploaderFactory,
             jreleaser.shadow.org.jreleaser.sdk.gitea.GiteaArtifactUploaderFactory,
             jreleaser.shadow.org.jreleaser.sdk.gitlab.GitlabArtifactUploaderFactory,
             jreleaser.shadow.org.jreleaser.sdk.http.HttpArtifactUploaderFactory,
             jreleaser.shadow.org.jreleaser.sdk.s3.S3ArtifactUploaderFactory,
             jreleaser.shadow.org.jreleaser.sdk.ssh.ScpArtifactUploaderFactory,
             jreleaser.shadow.org.jreleaser.sdk.ssh.SftpArtifactUploaderFactory;
    provides jreleaser.shadow.org.jreleaser.model.spi.download.ArtifactDownloaderFactory
        with jreleaser.shadow.org.jreleaser.sdk.ftp.FtpArtifactDownloaderFactory,
             jreleaser.shadow.org.jreleaser.sdk.http.HttpArtifactDownloaderFactory,
             jreleaser.shadow.org.jreleaser.sdk.ssh.ScpArtifactDownloaderFactory,
             jreleaser.shadow.org.jreleaser.sdk.ssh.SftpArtifactDownloaderFactory;
    provides jreleaser.shadow.software.amazon.awssdk.core.warmup.SdkWarmUpProvider
        with jreleaser.shadow.software.amazon.awssdk.services.s3.internal.warmup.S3WarmUpProvider;
    provides jreleaser.shadow.org.slf4j.spi.SLF4JServiceProvider
        with jreleaser.shadow.org.slf4j.simple.SimpleServiceProvider;
    provides jreleaser.shadow.software.amazon.awssdk.thirdparty.jackson.core.JsonFactory
        with jreleaser.shadow.software.amazon.awssdk.thirdparty.jackson.core.JsonFactory;
    provides jreleaser.shadow.org.apache.tika.metadata.filter.MetadataFilter
        with jreleaser.shadow.org.apache.tika.metadata.filter.NoOpFilter;
    provides jreleaser.shadow.org.codehaus.stax2.validation.XMLValidationSchemaFactory$dtd
        with jreleaser.shadow.com.ctc.wstx.dtd.DTDSchemaFactory;
    provides jreleaser.shadow.org.codehaus.stax2.validation.XMLValidationSchemaFactory$relaxng
        with jreleaser.shadow.com.ctc.wstx.msv.RelaxNGSchemaFactory;
    provides jreleaser.shadow.org.codehaus.stax2.validation.XMLValidationSchemaFactory$w3c
        with jreleaser.shadow.com.ctc.wstx.msv.W3CSchemaFactory;
    provides jreleaser.shadow.com.github.sbaudoin.yamllint.rules.Rule
        with jreleaser.shadow.com.github.sbaudoin.yamllint.rules.Anchors,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.Braces,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.Brackets,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.Colons,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.Commas,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.Comments,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.CommentsIndentation,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.DocumentEnd,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.DocumentStart,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.EmptyLines,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.EmptyValues,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.FloatValues,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.Hyphens,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.Indentation,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.KeyDuplicates,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.KeyOrdering,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.LineLength,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.NewLineAtEndOfFile,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.NewLines,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.OctalValues,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.QuotedStrings,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.TrailingSpaces,
             jreleaser.shadow.com.github.sbaudoin.yamllint.rules.Truthy;

    // JReleaser
    uses org.jreleaser.extensions.api.Extension;
    uses org.jreleaser.extensions.api.ExtensionManager;
    uses jreleaser.shadow.org.jreleaser.config.JReleaserConfigParser;
    uses jreleaser.shadow.org.jreleaser.model.spi.announce.AnnouncerBuilderFactory;
    uses jreleaser.shadow.org.jreleaser.model.spi.assemble.AssemblerProcessorFactory;
    uses jreleaser.shadow.org.jreleaser.model.spi.catalog.sbom.SbomCatalogerProcessorFactory;
    uses jreleaser.shadow.org.jreleaser.model.spi.deploy.maven.MavenDeployerFactory;
    uses jreleaser.shadow.org.jreleaser.model.spi.download.ArtifactDownloaderFactory;
    uses jreleaser.shadow.org.jreleaser.model.spi.packagers.PackagerProcessorFactory;
    uses jreleaser.shadow.org.jreleaser.model.spi.release.ReleaserBuilderFactory;
    uses jreleaser.shadow.org.jreleaser.model.spi.upload.ArtifactUploaderFactory;

    // 3rd party
    uses java.security.Provider;
    uses javax.script.ScriptEngineFactory;
    uses javax.xml.stream.XMLEventFactory;
    uses javax.xml.stream.XMLInputFactory;
    uses javax.xml.stream.XMLOutputFactory;
    uses jreleaser.shadow.com.ctc.wstx.shaded.msv.relaxng_datatype.DatatypeLibraryFactory;
    uses jreleaser.shadow.com.fasterxml.jackson.core.JsonFactory;
    uses jreleaser.shadow.com.fasterxml.jackson.core.ObjectCodec;
    uses jreleaser.shadow.com.fasterxml.jackson.databind.Module;
    uses jreleaser.shadow.com.github.sbaudoin.yamllint.rules.Rule;
    uses jreleaser.shadow.org.apache.commons.logging.LogFactory;
    uses jreleaser.shadow.org.apache.tika.metadata.filter.MetadataFilter;
    uses jreleaser.shadow.org.codehaus.stax2.validation.XMLValidationSchemaFactory;
    uses jreleaser.shadow.org.codehaus.stax2.validation.XMLValidationSchemaFactory.dtd;
    uses jreleaser.shadow.org.codehaus.stax2.validation.XMLValidationSchemaFactory.relaxng;
    uses jreleaser.shadow.org.codehaus.stax2.validation.XMLValidationSchemaFactory.w3c;
    uses jreleaser.shadow.org.eclipse.jgit.lib.GpgSigner;
    uses jreleaser.shadow.org.slf4j.spi.SLF4JServiceProvider;
    uses jreleaser.shadow.software.amazon.awssdk.core.warmup.SdkWarmUpProvider;
    uses jreleaser.shadow.software.amazon.awssdk.http.SdkHttpService;
    uses jreleaser.shadow.software.amazon.awssdk.thirdparty.jackson.core.JsonFactory;

    opens jreleaser.shadow.org.bouncycastle.jcajce.provider.asymmetric.cmce to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.jcajce.provider.asymmetric.edec to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.jcajce.provider.asymmetric.frodokem to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.jcajce.provider.asymmetric.mldsa to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.jcajce.provider.asymmetric.mlkem to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.jcajce.provider.asymmetric.rsa to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.jcajce.provider.asymmetric.slhdsa to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.jcajce.provider.digest to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.jcajce.provider.symmetric to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.pqc.jcajce.provider.aimer to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.pqc.jcajce.provider.dilithium to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.pqc.jcajce.provider.faest to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.pqc.jcajce.provider.falcon to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.pqc.jcajce.provider.haetae to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.pqc.jcajce.provider.hawk to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.pqc.jcajce.provider.lms to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.pqc.jcajce.provider.mayo to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.pqc.jcajce.provider.mqom to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.pqc.jcajce.provider.ntruplus to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.pqc.jcajce.provider.qruov to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.pqc.jcajce.provider.sdith to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.pqc.jcajce.provider.snova to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.pqc.jcajce.provider.sqisign to
        java.base;
    opens jreleaser.shadow.org.bouncycastle.pqc.jcajce.provider.uov to
        java.base;
    opens jreleaser.shadow.com.fasterxml.jackson.datatype.jsr310.deser;
    opens jreleaser.shadow.com.fasterxml.jackson.datatype.jsr310.deser.key;
    opens jreleaser.shadow.com.fasterxml.jackson.datatype.jsr310.ser;
    opens jreleaser.shadow.com.fasterxml.jackson.datatype.jsr310.ser.key;
    opens jreleaser.shadow.org.slf4j.simple;
    opens jreleaser.shadow.org.apache.tika.mime;
}