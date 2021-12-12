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
package org.jreleaser.tools;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.Snap;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import org.jreleaser.util.MustacheUtils;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.command.Command;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jreleaser.model.Snap.SKIP_SNAP;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.Constants.KEY_DISTRIBUTION_PACKAGE_DIRECTORY;
import static org.jreleaser.util.Constants.KEY_PROJECT_EFFECTIVE_VERSION;
import static org.jreleaser.util.Constants.KEY_PROJECT_LONG_DESCRIPTION;
import static org.jreleaser.util.Constants.KEY_SNAP_BASE;
import static org.jreleaser.util.Constants.KEY_SNAP_CONFINEMENT;
import static org.jreleaser.util.Constants.KEY_SNAP_GRADE;
import static org.jreleaser.util.Constants.KEY_SNAP_HAS_LOCAL_PLUGS;
import static org.jreleaser.util.Constants.KEY_SNAP_HAS_LOCAL_SLOTS;
import static org.jreleaser.util.Constants.KEY_SNAP_HAS_PLUGS;
import static org.jreleaser.util.Constants.KEY_SNAP_HAS_SLOTS;
import static org.jreleaser.util.Constants.KEY_SNAP_LOCAL_PLUGS;
import static org.jreleaser.util.Constants.KEY_SNAP_LOCAL_SLOTS;
import static org.jreleaser.util.Constants.KEY_SNAP_PACKAGE_NAME;
import static org.jreleaser.util.Constants.KEY_SNAP_PLUGS;
import static org.jreleaser.util.Constants.KEY_SNAP_REPO_CLONE_URL;
import static org.jreleaser.util.Constants.KEY_SNAP_REPO_URL;
import static org.jreleaser.util.Constants.KEY_SNAP_SLOTS;
import static org.jreleaser.util.StringUtils.isTrue;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class SnapToolProcessor extends AbstractRepositoryToolProcessor<Snap> {
    public SnapToolProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected void doPackageDistribution(Distribution distribution, Map<String, Object> props, Path packageDirectory) throws ToolProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);
        copyPreparedFiles(distribution, props);

        if (tool.isRemoteBuild()) {
            return;
        }

        if (PlatformUtils.isWindows()) {
            context.getLogger().debug(RB.$("ERROR_tool_excludes_platform", "Windows"));
            return;
        }

        createSnap(distribution, props);
    }

    @Override
    protected void doPublishDistribution(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        if (tool.isRemoteBuild()) {
            super.doPublishDistribution(distribution, props);
            return;
        }

        if (context.isDryrun()) {
            context.getLogger().error(RB.$("dryrun.set"));
            return;
        }

        if (PlatformUtils.isWindows()) {
            context.getLogger().debug(RB.$("ERROR_tool_excludes_platform", "Windows"));
            return;
        }

        login(distribution, props);
        push(distribution, props);
    }

    @Override
    protected void fillToolProperties(Map<String, Object> props, Distribution distribution) throws ToolProcessingException {
        GitService gitService = context.getModel().getRelease().getGitService();

        String desc = context.getModel().getProject().getLongDescription();
        desc = Arrays.stream(desc.split(System.lineSeparator()))
            .map(line -> "  " + line)
            .collect(Collectors.joining(System.lineSeparator()));
        props.put(KEY_PROJECT_LONG_DESCRIPTION,
            MustacheUtils.passThrough("|" + System.lineSeparator() + desc));

        props.put(KEY_SNAP_REPO_URL,
            gitService.getResolvedRepoUrl(context.getModel(), tool.getSnap().getOwner(), tool.getSnap().getResolvedName()));
        props.put(KEY_SNAP_REPO_CLONE_URL,
            gitService.getResolvedRepoCloneUrl(context.getModel(), tool.getSnap().getOwner(), tool.getSnap().getResolvedName()));

        props.put(KEY_SNAP_PACKAGE_NAME, tool.getPackageName());
        props.put(KEY_SNAP_BASE, tool.getBase());
        props.put(KEY_SNAP_GRADE, tool.getGrade());
        props.put(KEY_SNAP_CONFINEMENT, tool.getConfinement());
        props.put(KEY_SNAP_HAS_PLUGS, !tool.getPlugs().isEmpty());
        props.put(KEY_SNAP_PLUGS, tool.getPlugs());
        props.put(KEY_SNAP_HAS_SLOTS, !tool.getSlots().isEmpty());
        props.put(KEY_SNAP_SLOTS, tool.getSlots());
        props.put(KEY_SNAP_HAS_LOCAL_PLUGS, !tool.getLocalPlugs().isEmpty());
        props.put(KEY_SNAP_LOCAL_PLUGS, tool.getLocalPlugs());
        props.put(KEY_SNAP_HAS_LOCAL_SLOTS, !tool.getLocalSlots().isEmpty());
        props.put(KEY_SNAP_LOCAL_SLOTS, tool.getLocalSlots());
    }

    @Override
    protected void writeFile(Project project,
                             Distribution distribution,
                             String content,
                             Map<String, Object> props,
                             Path outputDirectory,
                             String fileName)
        throws ToolProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputFile = outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }

    private void login(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        Command cmd = new Command("snapcraft")
            .arg("login")
            .arg("--with")
            .arg(context.getBasedir().resolve(distribution.getSnap().getExportedLogin()).toAbsolutePath().toString());
        executeCommand(cmd);
    }

    private void push(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        Path packageDirectory = (Path) props.get(KEY_DISTRIBUTION_PACKAGE_DIRECTORY);
        String version = (String) props.get(KEY_PROJECT_EFFECTIVE_VERSION);
        String snapName = tool.getPackageName() + "-" + version + ".snap";

        Command cmd = new Command("snapcraft")
            .arg("push")
            .arg(snapName);
        executeCommand(packageDirectory, cmd);
    }

    private void createSnap(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        Path packageDirectory = (Path) props.get(KEY_DISTRIBUTION_PACKAGE_DIRECTORY);
        String version = (String) props.get(KEY_PROJECT_EFFECTIVE_VERSION);
        String snapName = tool.getPackageName() + "-" + version + ".snap";

        Command cmd = new Command("snapcraft")
            .arg("snap")
            .arg("--output")
            .arg(snapName);
        executeCommand(packageDirectory, cmd);
    }

    @Override
    protected boolean isSkipped(Artifact artifact) {
        return isTrue(artifact.getExtraProperties().get(SKIP_SNAP));
    }
}
