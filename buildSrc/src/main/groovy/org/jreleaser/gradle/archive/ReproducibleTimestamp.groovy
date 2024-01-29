package org.jreleaser.gradle.archive

import net.nemerosa.versioning.VersioningExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import org.kordamp.gradle.plugin.buildinfo.internal.ExtGitInfoService

import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.FileTime
import java.time.Instant
import java.time.ZonedDateTime
import java.util.jar.JarFile
import java.util.stream.Stream

import static org.kordamp.gradle.util.PluginUtils.resolveConfig

class ReproducibleTimestamp extends DefaultTask {
    @Input
    Jar target

    @TaskAction
    void updateTimestamp() {
        if (resolveConfig(project.rootProject).buildInfo.useCommitTimestamp) {
            VersioningExtension versioning = project.rootProject.extensions.findByType(VersioningExtension)
            ZonedDateTime timestamp = ExtGitInfoService.getCommitTimestamp(project.rootProject, versioning)

            JarFile jarFile = new JarFile(target.archiveFile.get().asFile)
            try (FileSystem zipfs = FileSystems.newFileSystem(Path.of(jarFile.name),
                this.getClass().getClassLoader())) {
                FileTime lastModifiedTime = FileTime.from(Instant.from(timestamp))
                try (Stream<Path> stream = Files.walk(zipfs.getPath('/'))) {
                    stream.forEach { path ->
                        try {
                            Files.getFileAttributeView(path, BasicFileAttributeView)
                                .setTimes(lastModifiedTime, lastModifiedTime, lastModifiedTime)
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                }
            }
        }
    }
}
