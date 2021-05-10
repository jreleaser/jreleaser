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
package org.jreleaser.tools;

import org.jreleaser.model.Distribution;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.Snap;
import org.jreleaser.model.releaser.spi.Releaser;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import org.jreleaser.util.Constants;
import org.jreleaser.util.MustacheUtils;
import org.jreleaser.util.PlatformUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jreleaser.templates.TemplateUtils.trimTplExtension;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class SnapToolProcessor extends AbstractRepositoryToolProcessor<Snap> {
    public SnapToolProcessor(JReleaserContext context) {
        super(context);
    }

    @Override
    protected boolean doPackageDistribution(Distribution distribution, Map<String, Object> props, Path packageDirectory) throws ToolProcessingException {
        super.doPackageDistribution(distribution, props, packageDirectory);
        copyPreparedFiles(distribution, props);

        if (tool.isRemoteBuild()) {
            return true;
        }

        if (PlatformUtils.isWindows()) {
            context.getLogger().debug("must not run on Windows", getToolName());
            return false;
        }

        return createSnap(distribution, props);
    }

    @Override
    protected boolean doPublishDistribution(Distribution distribution, Releaser releaser, Map<String, Object> props) throws ToolProcessingException {
        if (tool.isRemoteBuild()) {
            return super.doPublishDistribution(distribution, releaser, props);
        }

        if (context.isDryrun()) {
            context.getLogger().error("dryun is set to true. Skipping");
            return true;
        }

        if (PlatformUtils.isWindows()) {
            context.getLogger().debug("must not run on Windows", getToolName());
            return false;
        }

        if (!login(distribution, props)) {
            context.getLogger().error("could not log into snapcraft store");
            return false;
        }

        return push(distribution, props);
    }

    @Override
    protected void fillToolProperties(Map<String, Object> props, Distribution distribution) throws ToolProcessingException {
        GitService gitService = context.getModel().getRelease().getGitService();

        String desc = context.getModel().getProject().getLongDescription();
        desc = Arrays.stream(desc.split(System.lineSeparator()))
            .map(line -> "  " + line)
            .collect(Collectors.joining(System.lineSeparator()));
        props.put(Constants.KEY_PROJECT_LONG_DESCRIPTION,
            MustacheUtils.passThrough("|" + System.lineSeparator() + desc));

        props.put(Constants.KEY_SNAP_REPO_URL,
            gitService.getResolvedRepoUrl(context.getModel(), tool.getSnap().getOwner(), tool.getSnap().getName()));
        props.put(Constants.KEY_SNAP_REPO_CLONE_URL,
            gitService.getResolvedRepoCloneUrl(context.getModel(), tool.getSnap().getOwner(), tool.getSnap().getName()));

        props.put(Constants.KEY_SNAP_BASE, getTool().getBase());
        props.put(Constants.KEY_SNAP_GRADE, getTool().getGrade());
        props.put(Constants.KEY_SNAP_CONFINEMENT, getTool().getConfinement());
        props.put(Constants.KEY_SNAP_HAS_PLUGS, !getTool().getPlugs().isEmpty());
        props.put(Constants.KEY_SNAP_PLUGS, getTool().getPlugs());
        props.put(Constants.KEY_SNAP_HAS_SLOTS, !getTool().getSlots().isEmpty());
        props.put(Constants.KEY_SNAP_SLOTS, getTool().getSlots());
        props.put(Constants.KEY_SNAP_HAS_LOCAL_PLUGS, !getTool().getLocalPlugs().isEmpty());
        props.put(Constants.KEY_SNAP_LOCAL_PLUGS, getTool().getLocalPlugs());
        props.put(Constants.KEY_SNAP_HAS_LOCAL_SLOTS, !getTool().getLocalSlots().isEmpty());
        props.put(Constants.KEY_SNAP_LOCAL_SLOTS, getTool().getLocalSlots());
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

    private boolean login(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        List<String> cmd = new ArrayList<>();
        cmd.add("snapcraft");
        cmd.add("login");
        cmd.add("--with");
        cmd.add(context.getBasedir().resolve(distribution.getSnap().getExportedLogin()).toAbsolutePath().toString());
        return executeCommand(cmd);
    }

    private boolean push(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        Path packageDirectory = (Path) props.get(Constants.KEY_DISTRIBUTION_PACKAGE_DIRECTORY);
        String version = (String) props.get(Constants.KEY_PROJECT_EFFECTIVE_VERSION);
        String snapName = distribution.getName() + "-" + version + ".snap";

        List<String> cmd = new ArrayList<>();
        cmd.add("snapcraft");
        cmd.add("push");
        cmd.add(snapName);
        return executeCommand(packageDirectory, cmd);
    }

    private boolean createSnap(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        Path packageDirectory = (Path) props.get(Constants.KEY_DISTRIBUTION_PACKAGE_DIRECTORY);
        String version = (String) props.get(Constants.KEY_PROJECT_EFFECTIVE_VERSION);
        String snapName = distribution.getName() + "-" + version + ".snap";

        List<String> cmd = new ArrayList<>();
        cmd.add("snapcraft");
        cmd.add("snap");
        cmd.add("--output");
        cmd.add(snapName);
        return executeCommand(packageDirectory, cmd);
    }
}
