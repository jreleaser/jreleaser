/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.assemblers;

import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.Constants;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.model.internal.assemble.Assembler;
import org.jreleaser.model.internal.assemble.NativeImageAssembler;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.common.Glob;
import org.jreleaser.model.internal.common.Java;
import org.jreleaser.model.internal.platform.Platform;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.release.Release;
import org.jreleaser.model.spi.assemble.AssemblerProcessingException;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.sdk.command.Command;
import org.jreleaser.util.PlatformUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link NativeImageAssemblerProcessor}
 */
class NativeImageAssemblerProcessorTest {

    private static final String DETECTED_OS_ARCH = PlatformUtils.getDetectedOs() + "-" + PlatformUtils.getDetectedArch();

    private static final String IMAGE_NAME = "jreleaser-1.24.0";

    private static final String MAIN_CLASS = "org.jreleaser.cli.Main";

    private static final String ERROR_ASSEMBLER_NO_MAIN_CLASS = "Could not resolve main class: value not configured in the DSL and no Main-Class attribute found in the manifest of %s";

    private static final Path JARS_UNIVERSAL_DIR = Path.of("jars/universal");

    enum Mode {
        DEFAULT, AUTO_DETECT_MAIN_CLASS, AUTO_DETECT_MISSING_MAIN_CLASS, MODULAR_JAR, MODULAR_JAR_WITH_DEPS,
        MODULAR_JAR_AUTO_DETECT_MAIN_CLASS, MODULAR_JAR_AUTO_DETECT_MISSING_MAIN_CLASS
    }

    @TempDir
    Path tempDir;

    Path graalVmJdkDir;

    Path nativeImageFile;

    TemplateContext props;

    NativeImageAssembler assembler;

    NativeImageAssemblerProcessor nativeImageAssemblerProcessor;

    @BeforeEach
    void setup() throws Exception {
        setupTestData();
        setupMocks();
    }

    @ParameterizedTest
    @EnumSource(value = Mode.class, names = {"DEFAULT", "AUTO_DETECT_MAIN_CLASS", "AUTO_DETECT_MISSING_MAIN_CLASS"})
    void testAssembleBinary_UberJar(Mode mode) throws Exception {
        Artifact mainJar = mockMainJar(mode);
        when(assembler.getMainJar()).thenReturn(mainJar);
        if (mode.name().startsWith("AUTO_DETECT")) {
            Java java = mock(Java.class);
            when(assembler.getJava()).thenReturn(java);
        }

        Command expectedCommand = new Command(nativeImageFile.toString(), true);
        expectedCommand.arg("-cp")
            .arg(JARS_UNIVERSAL_DIR.resolve(IMAGE_NAME + ".jar").toString())
            .arg("-H:Name=" + IMAGE_NAME + "-" + DETECTED_OS_ARCH)
            .arg(MAIN_CLASS);
        Throwable exception = catchThrowable(() -> testAssembleBinary(expectedCommand));
        if (Mode.AUTO_DETECT_MISSING_MAIN_CLASS.equals(mode)) {
            assertThat(exception)
                .isInstanceOf(AssemblerProcessingException.class)
                .hasMessageContaining(ERROR_ASSEMBLER_NO_MAIN_CLASS.formatted(tempDir.resolve(IMAGE_NAME + ".jar")));
        } else {
            assertThat(exception).isNull();
        }
    }

    @ParameterizedTest
    @EnumSource(value = Mode.class, names = {"DEFAULT", "AUTO_DETECT_MAIN_CLASS", "AUTO_DETECT_MISSING_MAIN_CLASS"})
    void testAssembleBinary_JarWithDependencies(Mode mode) throws Exception {
        Artifact mainJar = mockMainJar(mode);
        List<Glob> jars = mockJars();
        when(assembler.getMainJar()).thenReturn(mainJar);
        when(assembler.getJars()).thenReturn(jars);
        if (mode.name().startsWith("AUTO_DETECT")) {
            Java java = mock(Java.class);
            when(assembler.getJava()).thenReturn(java);
        }

        List<Path> classpath = List.of(JARS_UNIVERSAL_DIR.resolve(IMAGE_NAME + ".jar"),
            JARS_UNIVERSAL_DIR.resolve("dependency1-1.0.0.jar"),
            JARS_UNIVERSAL_DIR.resolve("dependency2-2.0.0.jar"),
            JARS_UNIVERSAL_DIR.resolve("dependency3-3.0.0.jar"));
        Command expectedCommand = new Command(nativeImageFile.toString(), true);
        expectedCommand.arg("-cp")
            .arg(classpath.stream().map(Path::toString).collect(Collectors.joining(File.pathSeparator)))
            .arg("-H:Name=" + IMAGE_NAME + "-" + DETECTED_OS_ARCH)
            .arg(MAIN_CLASS);
        Throwable exception = catchThrowable(() -> testAssembleBinary(expectedCommand));
        if (Mode.AUTO_DETECT_MISSING_MAIN_CLASS.equals(mode)) {
            assertThat(exception)
                .isInstanceOf(AssemblerProcessingException.class)
                .hasMessageContaining(ERROR_ASSEMBLER_NO_MAIN_CLASS.formatted(tempDir.resolve(IMAGE_NAME + ".jar")));
        } else {
            assertThat(exception).isNull();
        }
    }

    @ParameterizedTest
    @EnumSource(value = Mode.class, names = {"MODULAR_JAR", "MODULAR_JAR_WITH_DEPS", "MODULAR_JAR_AUTO_DETECT_MAIN_CLASS",
        "MODULAR_JAR_AUTO_DETECT_MISSING_MAIN_CLASS"})
    void testAssembleBinary_ModularJar(Mode mode) throws Exception {
        Artifact mainJar = mockMainJar(mode);
        String mainModule = MAIN_CLASS.substring(0, MAIN_CLASS.lastIndexOf('.'));
        when(assembler.getMainJar()).thenReturn(mainJar);
        when(assembler.getJava().getMainModule()).thenReturn(mainModule);
        if (Mode.MODULAR_JAR_WITH_DEPS.equals(mode)) {
            List<Glob> jars = mockJars(mode);
            when(assembler.getJars()).thenReturn(jars);
        } else if (mode.name().contains("AUTO_DETECT")) {
            when(assembler.getJava().getMainClass()).thenReturn("");
        }

        Path distributionAssembleDir = props.get(Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY);
        Command expectedCommand = new Command(nativeImageFile.toString(), true);
        expectedCommand.arg("--module-path")
            .arg(distributionAssembleDir.resolve(JARS_UNIVERSAL_DIR).toString())
            .arg("-H:Name=" + IMAGE_NAME + "-" + DETECTED_OS_ARCH)
            .arg("--module")
            .arg(mainModule + "/" + MAIN_CLASS);
        Throwable exception = catchThrowable(() -> testAssembleBinary(expectedCommand));
        if (Mode.MODULAR_JAR_AUTO_DETECT_MISSING_MAIN_CLASS.equals(mode)) {
            assertThat(exception)
                .isInstanceOf(AssemblerProcessingException.class)
                .hasMessageContaining(ERROR_ASSEMBLER_NO_MAIN_CLASS.formatted(tempDir.resolve(IMAGE_NAME + ".jar")));
        } else {
            assertThat(exception).isNull();
        }
    }

    private void testAssembleBinary(Command expectedCommand) throws Exception {
        nativeImageAssemblerProcessor.assemble(props);

        ArgumentCaptor<Command> commandCaptor = ArgumentCaptor.forClass(Command.class);
        verify(nativeImageAssemblerProcessor).executeCommand(eq(props.get(Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY)),
            commandCaptor.capture());
        Command actualCommand = commandCaptor.getValue();
        assertThat(actualCommand).isNotNull();
        assertCommandArgs(actualCommand.getArgs(), expectedCommand.getArgs());
    }

    private void assertCommandArgs(List<String> actualCommandArgs, List<String> expectedCommandArgs) {
        int cpIndex = expectedCommandArgs.indexOf("-cp");
        if (cpIndex < 0) {
            assertThat(actualCommandArgs).isEqualTo(expectedCommandArgs);
            return;
        }
        assertThat(actualCommandArgs).hasSameSizeAs(expectedCommandArgs);
        int cpJarsIndex = cpIndex + 1;
        assertThat(actualCommandArgs.subList(0, cpJarsIndex)).isEqualTo(expectedCommandArgs.subList(0, cpJarsIndex));
        assertClasspath(actualCommandArgs.get(cpJarsIndex), expectedCommandArgs.get(cpJarsIndex));
        if (expectedCommandArgs.size() > cpJarsIndex + 1) {
            assertThat(actualCommandArgs.subList(cpJarsIndex + 1, actualCommandArgs.size()))
                .isEqualTo(expectedCommandArgs.subList(cpJarsIndex + 1, expectedCommandArgs.size()));
        }
    }

    private void assertClasspath(String actualClasspath, String expectedClasspath) {
        List<String> actualJarsOnClasspath = Arrays.asList(actualClasspath.split(File.pathSeparator));
        List<String> expectedJarsOnClasspath = Arrays.asList(expectedClasspath.split(File.pathSeparator));

        assertThat(actualJarsOnClasspath).isNotEmpty();
        assertThat(expectedJarsOnClasspath).isNotEmpty();
        assertThat(actualJarsOnClasspath).hasSameSizeAs(expectedJarsOnClasspath);
        assertThat(actualJarsOnClasspath.get(0)).isEqualTo(expectedJarsOnClasspath.get(0));
        if (expectedJarsOnClasspath.size() > 1) {
            assertThat(actualJarsOnClasspath.subList(1, actualJarsOnClasspath.size()))
                .containsExactlyInAnyOrderElementsOf(expectedJarsOnClasspath.subList(1, expectedJarsOnClasspath.size()));
        }
    }

    private Artifact mockMainJar(Mode mode) throws Exception {
        Artifact mainJar = mock(Artifact.class);

        Path mainJarFile = tempDir.resolve(IMAGE_NAME + ".jar");
        createDummyJar(mainJarFile, mode);

        when(mainJar.getPath()).thenReturn(mainJarFile.toString());
        when(mainJar.getResolvedPath()).thenReturn(mainJarFile);
        when(mainJar.getEffectivePath(any(JReleaserContext.class), any(Assembler.class))).thenReturn(mainJarFile);

        return mainJar;
    }

    private List<Glob> mockJars() throws Exception {
        return mockJars(Mode.DEFAULT);
    }

    private List<Glob> mockJars(Mode mode) throws Exception {
        Glob glob = new Glob();

        Path thirdPartyLibDir = tempDir.resolve("third-party/lib");
        Files.createDirectories(thirdPartyLibDir);
        glob.setPattern(thirdPartyLibDir + "/*.jar");
        Path dependency1JarFile = thirdPartyLibDir.resolve("dependency1-1.0.0.jar");
        Path dependency2JarFile = thirdPartyLibDir.resolve("dependency2-2.0.0.jar");
        Path dependency3JarFile = thirdPartyLibDir.resolve("dependency3-3.0.0.jar");
        createDummyJar(dependency1JarFile, mode);
        createDummyJar(dependency2JarFile, mode);
        createDummyJar(dependency3JarFile, mode);

        return List.of(glob);
    }

    private void createDummyJar(Path jarFile, Mode mode) throws Exception {
        String jarFileName = jarFile.getFileName().toString();
        String jarPackage = jarFileName.substring(0, jarFileName.lastIndexOf('-'));
        String imagePackage = IMAGE_NAME.substring(0, IMAGE_NAME.lastIndexOf('-'));
        if (imagePackage.equals(jarPackage)) {
            jarPackage += "/cli";
        }

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        if (!mode.name().endsWith("AUTO_DETECT_MISSING_MAIN_CLASS")) {
            manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, MAIN_CLASS);
        }

        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            JarEntry mainClassEntry = new JarEntry("org/" + jarPackage + "/Main.class");
            jos.putNextEntry(mainClassEntry);
            jos.closeEntry();
            if (mode.name().startsWith("MODULAR_JAR")) {
                JarEntry moduleEntry = new JarEntry("module-info.class");
                jos.putNextEntry(moduleEntry);
                jos.closeEntry();
            }
        }

        assertThat(jarFile).exists();
    }

    private void setupTestData() throws Exception {
        graalVmJdkDir = tempDir.resolve("graalvm-jdk");
        Path graalVmJdkBinDir = graalVmJdkDir.resolve("bin");
        Files.createDirectories(graalVmJdkBinDir);
        Path graalVmJdkReleaseFile = graalVmJdkDir.resolve("release");
        Files.writeString(graalVmJdkReleaseFile, "JAVA_VERSION=\"21.0.10\"\nGRAALVM_VERSION=\"21.0.10\"");
        String nativeImageFileName = PlatformUtils.isWindows() ? "native-image.cmd" : "native-image";
        nativeImageFile = graalVmJdkBinDir.resolve(nativeImageFileName);
        Files.createFile(nativeImageFile);
    }

    private void setupMocks() throws Exception {
        props = TemplateContext.from(Map.of(
            Constants.KEY_DISTRIBUTION_ASSEMBLE_DIRECTORY, tempDir.resolve("jreleaser/assemble/jreleaser-native/native-image"))
        );
        nativeImageAssemblerProcessor = spy(new NativeImageAssemblerProcessor(mockContext()));
        mockExecuteCommand();
        mockAssembler();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private JReleaserContext mockContext() {
        JReleaserContext context = mock(JReleaserContext.class);
        JReleaserLogger logger = mock(JReleaserLogger.class);
        JReleaserModel model = mock(JReleaserModel.class);
        Project project = mock(Project.class);
        Release release = mock(Release.class);
        BaseReleaser releaser = mock(BaseReleaser.class);

        when(context.getLogger()).thenReturn(logger);
        when(context.getModel()).thenReturn(model);
        when(model.getProject()).thenReturn(project);
        when(model.getRelease()).thenReturn(release);
        when(release.getReleaser()).thenReturn(releaser);
        when(context.getBasedir()).thenReturn(tempDir);
        when(context.fullProps()).thenReturn(props);
        when(context.isPlatformSelected(nullable(String.class))).thenReturn(true);

        return context;
    }

    private void mockExecuteCommand() throws Exception {
        Command.Result commandResult = mock(Command.Result.class);
        when(commandResult.getExitValue()).thenReturn(0);
        doReturn(commandResult)
            .when(nativeImageAssemblerProcessor)
            .executeCommand(any(Path.class), any(Command.class));
    }

    private void mockAssembler() {
        assembler = mock(NativeImageAssembler.class);
        Artifact graal = mockGraal();
        Platform platform = new Platform();
        NativeImageAssembler.Archiving archiving = mock(NativeImageAssembler.Archiving.class);
        NativeImageAssembler.PlatformCustomizer platformCustomizer = mock(NativeImageAssembler.PlatformCustomizer.class);
        Java java = mockJava();
        NativeImageAssembler.Upx upx = mock(NativeImageAssembler.Upx.class);

        when(assembler.getGraal()).thenReturn(graal);
        when(assembler.getPlatform()).thenReturn(platform);
        when(assembler.getArchiving()).thenReturn(archiving);
        when(assembler.getResolvedPlatformCustomizer()).thenReturn(platformCustomizer);
        when(assembler.getJava()).thenReturn(java);
        when(assembler.getUpx()).thenReturn(upx);
        when(assembler.getResolvedImageName(any(JReleaserContext.class))).thenReturn(IMAGE_NAME);
        when(assembler.getExecutable()).thenReturn(IMAGE_NAME);

        nativeImageAssemblerProcessor.setAssembler(assembler);
    }

    private Artifact mockGraal() {
        Artifact graal = mock(Artifact.class);

        when(graal.isActiveAndSelected()).thenReturn(true);
        when(graal.getEffectivePath(any(JReleaserContext.class), any(Assembler.class))).thenReturn(graalVmJdkDir);
        when(graal.getPlatform()).thenReturn(DETECTED_OS_ARCH);

        return graal;
    }

    private Java mockJava() {
        Java java = mock(Java.class);

        when(java.getMainClass()).thenReturn(MAIN_CLASS);

        return java;
    }

}
