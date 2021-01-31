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
package org.kordamp.jreleaser.tools;

import org.kordamp.jreleaser.model.Distribution;
import org.kordamp.jreleaser.model.JReleaserModel;
import org.kordamp.jreleaser.model.Project;
import org.kordamp.jreleaser.model.Snap;
import org.kordamp.jreleaser.util.FileUtils;
import org.kordamp.jreleaser.util.Logger;
import org.kordamp.jreleaser.util.OsUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.kordamp.jreleaser.templates.TemplateUtils.trimTplExtension;
import static org.kordamp.jreleaser.util.FileUtils.createDirectoriesWithFullAccess;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class SnapToolProcessor extends AbstractToolProcessor<Snap> {
    public SnapToolProcessor(Logger logger, JReleaserModel model, Snap snap) {
        super(logger, model, snap);
    }

    @Override
    protected boolean doPackageDistribution(Distribution distribution, Map<String, Object> context) throws ToolProcessingException {
        if (OsUtils.isWindows()) {
            getLogger().debug("Tool {} must not run on Windows", getToolName());
            return false;
        }

        Path primeDirectory = createPackage(context);

        if (!login(distribution, context)) {
            getLogger().error("Could not log into snapcraft store");
            return false;
        }

        return createSnap(distribution, context, primeDirectory);
    }

    @Override
    protected Set<String> resolveByExtensionsFor(Distribution.DistributionType type) {
        Set<String> set = new LinkedHashSet<>();
        if (type == Distribution.DistributionType.BINARY) {
            set.add(".tar.gz");
            set.add(".tar");
        } else if (type == Distribution.DistributionType.SINGLE_JAR) {
            set.add(".jar");
        }
        return set;
    }

    @Override
    protected void fillToolProperties(Map<String, Object> context, Distribution distribution) throws ToolProcessingException {
        context.put(Constants.KEY_SNAP_BASE, getTool().getBase());
        context.put(Constants.KEY_SNAP_GRADE, getTool().getGrade());
        context.put(Constants.KEY_SNAP_CONFINEMENT, getTool().getConfinement());
        context.put(Constants.KEY_SNAP_HAS_PLUGS, !getTool().getPlugs().isEmpty());
        context.put(Constants.KEY_SNAP_PLUGS, getTool().getPlugs());
        context.put(Constants.KEY_SNAP_HAS_SLOTS, !getTool().getSlots().isEmpty());
        context.put(Constants.KEY_SNAP_SLOTS, getTool().getSlots());
        context.put(Constants.KEY_SNAP_HAS_LOCAL_PLUGS, !getTool().getLocalPlugs().isEmpty());
        context.put(Constants.KEY_SNAP_LOCAL_PLUGS, getTool().getLocalPlugs());
    }

    @Override
    protected void writeFile(Project project, Distribution distribution, String content, Map<String, Object> context, String fileName)
        throws ToolProcessingException {
        fileName = trimTplExtension(fileName);

        Path outputDirectory = (Path) context.get(Constants.KEY_PREPARE_DIRECTORY);
        Path outputFile = outputDirectory.resolve(fileName);

        writeFile(content, outputFile);
    }

    private Path createPackage(Map<String, Object> context) throws ToolProcessingException {
        try {
            Path prepareDirectory = (Path) context.get(Constants.KEY_PREPARE_DIRECTORY);
            Path snapDirectory = prepareDirectory.resolve("snap");
            Path packageDirectory = (Path) context.get(Constants.KEY_PACKAGE_DIRECTORY);
            Path primeDirectory = packageDirectory.resolve("prime");
            Path metaDirectory = primeDirectory.resolve("meta");
            createDirectoriesWithFullAccess(metaDirectory);
            if (FileUtils.copyFiles(getLogger(), snapDirectory, metaDirectory)) {
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

    private boolean login(Distribution distribution, Map<String, Object> context) throws ToolProcessingException {
        List<String> cmd = new ArrayList<>();
        cmd.add("snapcraft");
        cmd.add("login");
        cmd.add("--with");
        cmd.add(distribution.getSnap().getExportedLogin());
        return executeCommand(cmd);
    }

    private boolean createSnap(Distribution distribution, Map<String, Object> context, Path primeDirectory) throws ToolProcessingException {
        Path packageDirectory = (Path) context.get(Constants.KEY_PACKAGE_DIRECTORY);
        String version = (String) context.get(Constants.KEY_PROJECT_VERSION);
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
