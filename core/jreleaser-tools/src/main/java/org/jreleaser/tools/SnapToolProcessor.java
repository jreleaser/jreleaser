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
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Project;
import org.jreleaser.model.Snap;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import org.jreleaser.util.Constants;
import org.jreleaser.util.FileUtils;
import org.jreleaser.util.PlatformUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.jreleaser.util.FileUtils.createDirectoriesWithFullAccess;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class SnapToolProcessor extends AbstractToolProcessor<Snap> {
    public SnapToolProcessor(JReleaserContext context, Snap snap) {
        super(context, snap);
    }

    @Override
    protected boolean doPackageDistribution(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        if (tool.isRemoteBuild()) {
            // copy from prepare to package
            copyPreparedFiles(distribution, props);
            return true;
        }

        if (PlatformUtils.isWindows()) {
            context.getLogger().debug("Tool {} must not run on Windows", getToolName());
            return false;
        }

        Path primeDirectory = createPackage(props);

        if (!login(distribution, props)) {
            context.getLogger().error("Could not log into snapcraft store");
            return false;
        }

        return createSnap(distribution, props, primeDirectory);
    }

    @Override
    protected boolean doUploadDistribution(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        if (tool.isRemoteBuild()) {
            super.doUploadDistribution(distribution, props);
        }
        return false;
    }

    @Override
    protected Set<String> resolveByExtensionsFor(Distribution.DistributionType type) {
        Set<String> set = new LinkedHashSet<>();
        if (type == Distribution.DistributionType.BINARY || type == Distribution.DistributionType.JLINK) {
            set.add(".tar.gz");
            set.add(".tar");
        }
        return set;
    }

    @Override
    protected void fillToolProperties(Map<String, Object> props, Distribution distribution) throws ToolProcessingException {
        props.put(Constants.KEY_SNAP_BASE, getTool().getBase());
        props.put(Constants.KEY_SNAP_GRADE, getTool().getGrade());
        props.put(Constants.KEY_SNAP_CONFINEMENT, getTool().getConfinement());
        props.put(Constants.KEY_SNAP_HAS_PLUGS, !getTool().getPlugs().isEmpty());
        props.put(Constants.KEY_SNAP_PLUGS, getTool().getPlugs());
        props.put(Constants.KEY_SNAP_HAS_SLOTS, !getTool().getSlots().isEmpty());
        props.put(Constants.KEY_SNAP_SLOTS, getTool().getSlots());
        props.put(Constants.KEY_SNAP_HAS_LOCAL_PLUGS, !getTool().getLocalPlugs().isEmpty());
        props.put(Constants.KEY_SNAP_LOCAL_PLUGS, getTool().getLocalPlugs());
    }

    @Override
    protected void writeFile(Project project, Distribution distribution, String content, Map<String, Object> props, String fileName)
        throws ToolProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputDirectory = (Path) props.get(Constants.KEY_PREPARE_DIRECTORY);
        Path outputFile = outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }

    private Path createPackage(Map<String, Object> props) throws ToolProcessingException {
        try {
            Path prepareDirectory = (Path) props.get(Constants.KEY_PREPARE_DIRECTORY);
            Path snapDirectory = prepareDirectory.resolve("snap");
            Path packageDirectory = (Path) props.get(Constants.KEY_PACKAGE_DIRECTORY);
            Path primeDirectory = packageDirectory.resolve("prime");
            Path metaDirectory = primeDirectory.resolve("meta");
            createDirectoriesWithFullAccess(metaDirectory);
            if (FileUtils.copyFiles(context.getLogger(), snapDirectory, metaDirectory)) {
                Files.move(metaDirectory.resolve("snapcraft.yaml"),
                    metaDirectory.resolve("snap.yaml"),
                    REPLACE_EXISTING);
                return primeDirectory;
            } else {
                throw new ToolProcessingException("Could not copy files from " +
                    prepareDirectory.toAbsolutePath().toString() + " to " +
                    metaDirectory.toAbsolutePath().toString());
            }
        } catch (IOException e) {
            throw new ToolProcessingException("Unexpected error when creating package", e);
        }
    }

    private boolean login(Distribution distribution, Map<String, Object> props) throws ToolProcessingException {
        List<String> cmd = new ArrayList<>();
        cmd.add("snapcraft");
        cmd.add("login");
        cmd.add("--with");
        cmd.add(distribution.getSnap().getExportedLogin());
        return executeCommand(cmd);
    }

    private boolean createSnap(Distribution distribution, Map<String, Object> props, Path primeDirectory) throws ToolProcessingException {
        Path packageDirectory = (Path) props.get(Constants.KEY_PACKAGE_DIRECTORY);
        String version = (String) props.get(Constants.KEY_PROJECT_VERSION);
        String snapName = distribution.getName() + "-" + version + ".snap";

        List<String> cmd = new ArrayList<>();
        cmd.add("snapcraft");
        cmd.add("snap");
        cmd.add(primeDirectory.toAbsolutePath().toString());
        cmd.add("--output");
        cmd.add(packageDirectory.resolve(snapName).toAbsolutePath().toString());
        return executeCommand(cmd);
    }
}
