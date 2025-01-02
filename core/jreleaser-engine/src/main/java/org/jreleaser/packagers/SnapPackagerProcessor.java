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
package org.jreleaser.packagers;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.SnapPackager;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.spi.packagers.PackagerProcessingException;
import org.jreleaser.mustache.MustacheUtils;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.sdk.command.Command;
import org.jreleaser.util.PlatformUtils;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_CLASS;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_JAVA_MAIN_MODULE;
import static org.jreleaser.model.Constants.KEY_DISTRIBUTION_PACKAGE_DIRECTORY;
import static org.jreleaser.model.Constants.KEY_PROJECT_EFFECTIVE_VERSION;
import static org.jreleaser.model.Constants.KEY_PROJECT_LONG_DESCRIPTION;
import static org.jreleaser.model.Constants.KEY_SNAP_ARCHITECTURES;
import static org.jreleaser.model.Constants.KEY_SNAP_BASE;
import static org.jreleaser.model.Constants.KEY_SNAP_CONFINEMENT;
import static org.jreleaser.model.Constants.KEY_SNAP_GRADE;
import static org.jreleaser.model.Constants.KEY_SNAP_HAS_ARCHITECTURES;
import static org.jreleaser.model.Constants.KEY_SNAP_HAS_LOCAL_PLUGS;
import static org.jreleaser.model.Constants.KEY_SNAP_HAS_LOCAL_SLOTS;
import static org.jreleaser.model.Constants.KEY_SNAP_HAS_PLUGS;
import static org.jreleaser.model.Constants.KEY_SNAP_HAS_SLOTS;
import static org.jreleaser.model.Constants.KEY_SNAP_LOCAL_PLUGS;
import static org.jreleaser.model.Constants.KEY_SNAP_LOCAL_SLOTS;
import static org.jreleaser.model.Constants.KEY_SNAP_PACKAGE_NAME;
import static org.jreleaser.model.Constants.KEY_SNAP_PLUGS;
import static org.jreleaser.model.Constants.KEY_SNAP_REPOSITORY_CLONE_URL;
import static org.jreleaser.model.Constants.KEY_SNAP_REPOSITORY_URL;
import static org.jreleaser.model.Constants.KEY_SNAP_REPO_CLONE_URL;
import static org.jreleaser.model.Constants.KEY_SNAP_REPO_URL;
import static org.jreleaser.model.Constants.KEY_SNAP_SLOTS;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class SnapPackagerProcessor extends AbstractRepositoryPackagerProcessor<SnapPackager> {
    public SnapPackagerProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPackageDistribution(Distribution distribution, TemplateContext props, Path packageDirectory) throws PackagerProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);
        copyPreparedFiles(props);

        if (packager.isRemoteBuild()) {
            return;
        }

        if (PlatformUtils.isWindows()) {
            context.getLogger().warn(RB.$("ERROR_packager_excludes_platform", "Windows"));
            return;
        }

        createSnap(props);
    }

    @Override
    protected void doPublishDistribution(Distribution distribution, TemplateContext props) throws PackagerProcessingException {
        if (packager.isRemoteBuild()) {
            super.doPublishDistribution(distribution, props);
            return;
        }

        if (context.isDryrun()) {
            context.getLogger().error(RB.$("dryrun.set"));
            return;
        }

        if (PlatformUtils.isWindows()) {
            context.getLogger().warn(RB.$("ERROR_packager_excludes_platform", "Windows"));
            return;
        }

        login(distribution);
        push(props);
    }

    @Override
    protected void fillPackagerProperties(TemplateContext props, Distribution distribution) {
        props.set(KEY_DISTRIBUTION_JAVA_MAIN_CLASS, distribution.getJava().getMainClass());
        props.set(KEY_DISTRIBUTION_JAVA_MAIN_MODULE, distribution.getJava().getMainModule());
        BaseReleaser<?, ?> releaser = context.getModel().getRelease().getReleaser();

        String desc = context.getModel().getProject().getLongDescription();
        desc = Arrays.stream(desc.split(System.lineSeparator()))
            .map(line -> "  " + line)
            .collect(Collectors.joining(System.lineSeparator()));
        props.set(KEY_PROJECT_LONG_DESCRIPTION,
            MustacheUtils.passThrough("|" + System.lineSeparator() + desc));

        props.set(KEY_SNAP_REPO_URL,
            releaser.getResolvedRepoUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName()));
        props.set(KEY_SNAP_REPO_CLONE_URL,
            releaser.getResolvedRepoCloneUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName()));

        props.set(KEY_SNAP_REPOSITORY_URL,
            releaser.getResolvedRepoUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName()));
        props.set(KEY_SNAP_REPOSITORY_CLONE_URL,
            releaser.getResolvedRepoCloneUrl(context.getModel(), packager.getRepository().getOwner(), packager.getRepository().getResolvedName()));

        props.set(KEY_SNAP_PACKAGE_NAME, packager.getPackageName());
        props.set(KEY_SNAP_BASE, packager.getBase());
        props.set(KEY_SNAP_GRADE, packager.getGrade());
        props.set(KEY_SNAP_CONFINEMENT, packager.getConfinement());
        props.set(KEY_SNAP_HAS_PLUGS, !packager.getPlugs().isEmpty());
        props.set(KEY_SNAP_PLUGS, packager.getPlugs());
        props.set(KEY_SNAP_HAS_SLOTS, !packager.getSlots().isEmpty());
        props.set(KEY_SNAP_SLOTS, packager.getSlots());
        props.set(KEY_SNAP_HAS_LOCAL_PLUGS, !packager.getLocalPlugs().isEmpty());
        props.set(KEY_SNAP_LOCAL_PLUGS, packager.getLocalPlugs());
        props.set(KEY_SNAP_HAS_LOCAL_SLOTS, !packager.getLocalSlots().isEmpty());
        props.set(KEY_SNAP_LOCAL_SLOTS, packager.getLocalSlots());
        props.set(KEY_SNAP_HAS_ARCHITECTURES, !packager.getArchitectures().isEmpty());
        props.set(KEY_SNAP_ARCHITECTURES, packager.getArchitectures());
    }

    @Override
    protected void writeFile(Distribution distribution,
                             String content,
                             TemplateContext props,
                             Path outputDirectory,
                             String fileName) throws PackagerProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputFile = outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }

    private void login(Distribution distribution) throws PackagerProcessingException {
        Command cmd = new Command("snapcraft")
            .arg("login")
            .arg("--with")
            .arg(context.getBasedir().resolve(distribution.getSnap().getExportedLogin()).toAbsolutePath().toString());
        executeCommand(cmd);
    }

    private void push(TemplateContext props) throws PackagerProcessingException {
        Path packageDirectory = props.get(KEY_DISTRIBUTION_PACKAGE_DIRECTORY);
        String version = props.get(KEY_PROJECT_EFFECTIVE_VERSION);
        String snapName = packager.getPackageName() + "-" + version + ".snap";

        Command cmd = new Command("snapcraft")
            .arg("push")
            .arg(snapName);
        executeCommand(packageDirectory, cmd);
    }

    private void createSnap(TemplateContext props) throws PackagerProcessingException {
        Path packageDirectory = props.get(KEY_DISTRIBUTION_PACKAGE_DIRECTORY);
        String version = props.get(KEY_PROJECT_EFFECTIVE_VERSION);
        String snapName = packager.getPackageName() + "-" + version + ".snap";

        Command cmd = new Command("snapcraft")
            .arg("snap")
            .arg("--output")
            .arg(snapName);
        executeCommand(packageDirectory, cmd);
    }
}
