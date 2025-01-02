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
package org.jreleaser.model.internal.validation.assemble;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.ArchiveAssembler;
import org.jreleaser.model.internal.assemble.Assembler;
import org.jreleaser.model.internal.assemble.DebAssembler;
import org.jreleaser.model.internal.assemble.JavaArchiveAssembler;
import org.jreleaser.model.internal.assemble.JlinkAssembler;
import org.jreleaser.model.internal.assemble.NativeImageAssembler;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.util.Errors;
import org.jreleaser.util.PlatformUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.jreleaser.model.internal.validation.assemble.ArchiveAssemblerResolver.resolveArchiveOutputs;
import static org.jreleaser.model.internal.validation.assemble.JavaArchiveAssemblerResolver.resolveJavaArchiveOutputs;
import static org.jreleaser.model.internal.validation.assemble.JlinkAssemblerResolver.resolveJlinkOutputs;
import static org.jreleaser.model.internal.validation.assemble.NativeImageAssemblerResolver.resolveNativeImageOutputs;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.16.0
 */
public final class DebAssemblerResolver {
    private DebAssemblerResolver() {
        // noop
    }

    public static void resolveDebOutputs(JReleaserContext context, Errors errors) {
        List<DebAssembler> activeDebs = context.getModel().getAssemble().getActiveDebs();
        if (!activeDebs.isEmpty()) context.getLogger().debug("assemble.deb");

        for (DebAssembler deb : activeDebs) {
            if (deb.isExported()) resolveDebOutputs(context, deb, errors);
        }
    }

    private static void resolveDebOutputs(JReleaserContext context, DebAssembler assembler, Errors errors) {
        Path assembleDirectory = context.getAssembleDirectory()
            .resolve(assembler.getName())
            .resolve(assembler.getType());

        String assemblerRef = assembler.getAssemblerRef();

        if (isNotBlank(assemblerRef)) {
            Assembler<?> assemblerReference = resolveAssemblerReference(context, assemblerRef.trim(), errors);
            for (Artifact artifact : assemblerReference.getOutputs()) {
                resolveDebianArtifact(context, assembleDirectory, assembler, artifact, errors);
            }
        } else {
            resolveDebianArtifact(context, assembleDirectory, assembler, null, errors);
        }
    }

    private static Assembler<?> resolveAssemblerReference(JReleaserContext context, String assemblerName, Errors errors) {
        Assembler<?> assemblerReference = context.getModel().getAssemble().findAssembler(assemblerName);

        if (assemblerReference instanceof ArchiveAssembler) {
            resolveArchiveOutputs(context, (ArchiveAssembler) assemblerReference, errors);
        } else if (assemblerReference instanceof JavaArchiveAssembler) {
            resolveJavaArchiveOutputs(context, (JavaArchiveAssembler) assemblerReference, errors);
        } else if (assemblerReference instanceof JlinkAssembler) {
            resolveJlinkOutputs(context, (JlinkAssembler) assemblerReference, errors);
        } else if (assemblerReference instanceof NativeImageAssembler) {
            resolveNativeImageOutputs(context, (NativeImageAssembler) assemblerReference, errors);
        }

        return assemblerReference;
    }

    private static void resolveDebianArtifact(JReleaserContext context, Path assembleDirectory, DebAssembler assembler, Artifact artifact, Errors errors) {
        String architecture = isNotBlank(assembler.getArchitecture()) ? assembler.getArchitecture().trim() : "all";
        String platform = "";

        if (null != artifact) {
            platform = artifact.getPlatform();

            if (isBlank(platform)) {
                architecture = "all";
            } else if (PlatformUtils.isLinux(platform) && PlatformUtils.isIntel64(platform)) {
                architecture = "amd64";
            } else if (PlatformUtils.isLinux(platform) && PlatformUtils.isArm64(platform)) {
                architecture = "arm64";
            } else {
                return;
            }
        }

        Path path = assembleDirectory.resolve(
            assembler.getControl().getPackageName() + "-" +
                assembler.getControl().getPackageVersion() + "-" +
                assembler.getControl().getPackageRevision() + "_" +
                architecture + ".deb");

        if (!Files.exists(path)) {
            errors.assembly(RB.$("validation_missing_assembly",
                assembler.getType(), assembler.getName(), assembler.getName()));
        } else {
            Artifact a = Artifact.of(path, platform);
            a.resolveActiveAndSelected(context);
            a.setExtraProperties(assembler.getExtraProperties());
            assembler.addOutput(a);
        }
    }
}
