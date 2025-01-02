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
package org.jreleaser.jdks.gradle.plugin

import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskProvider
import org.jreleaser.jdks.gradle.plugin.internal.JdkImpl
import org.jreleaser.jdks.gradle.plugin.tasks.ListJdksTask
import org.jreleaser.jdks.gradle.plugin.tasks.UnpackTask
import org.jreleaser.util.Errors

import static org.jreleaser.util.IoUtils.newPrintWriter

/**
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@CompileStatic
class JdksPlugin implements Plugin<Project> {
    private static final String JDKS_GROUP = 'Jdks'

    @Override
    void apply(Project project) {
        if (project.gradle.startParameter.logLevel != LogLevel.QUIET) {
            project.gradle.sharedServices
                .registerIfAbsent('jdks-banner', Banner, { spec -> })
                .get().display(project)
        }

        NamedDomainObjectContainer<JdkImpl> jdkContainer = project.objects.domainObjectContainer(JdkImpl,
            new NamedDomainObjectFactory<JdkImpl>() {
                @Override
                JdkImpl create(String name) {
                    JdkImpl jdk = project.objects.newInstance(JdkImpl, project.objects)
                    jdk.name = name
                    jdk
                }
            })

        project.extensions.add('jdks', jdkContainer)

        project.afterEvaluate(new Action<Project>() {
            @Override
            void execute(Project p) {
                if (jdkContainer.isEmpty()) return

                Errors errors = new Errors()
                jdkContainer.each { jdk -> jdk.validate(errors) }
                if (errors.hasErrors()) {
                    errors.logErrors(newPrintWriter(System.out))
                    throw new IllegalStateException('Jdks are not properly configured')
                }

                configureTasks(p, jdkContainer)
            }
        })
    }

    private void configureTasks(Project project, NamedDomainObjectContainer<JdkImpl> jdkContainer) {
        Provider<Directory> jdksDir = project.layout.buildDirectory.dir('jdks')

        List<JdkImpl> jdks = new ArrayList<>(jdkContainer)

        File cacheDir = project.file("${project.gradle.gradleUserHomeDir}/caches/jdks")

        List<JdkImpl> jdksToBeCopied = []
        if (cacheDir.exists()) {
            for (File jdkArchive : cacheDir.listFiles()) {
                JdkImpl candidateJdk = jdks.find { it.archiveFileName == jdkArchive.name }
                if (!candidateJdk || jdksDir.get().file(jdkArchive.name).asFile.exists()) continue

                JdkImpl copy = candidateJdk.copyOf()
                copy.archive.set(jdkArchive)
                jdksToBeCopied << copy
            }
        }

        // register tasks per JDK
        jdks.each { jdk ->
            String normalizedName = jdk.normalizedName
            String jdkArchiveFileName = jdk.archiveFileName
            String jdkArchiveName = jdk.archiveName
            Provider<Directory> jdkDirectory = jdksDir.map({ d -> d.dir(normalizedName) })

            jdk.downloadTask = project.tasks.register('downloadJdk' + normalizedName.capitalize(),
                Download, new Action<Download>() {
                @Override
                void execute(Download t) {
                    t.group = JDKS_GROUP
                    t.description = "Download JDK ${jdk.name}".toString()
                    t.enabled = !jdksToBeCopied.find { it.archiveFileName == jdkArchiveFileName }

                    t.src(jdk.url)
                    t.dest(jdksDir.get().file(jdkArchiveFileName).asFile)
                    t.doFirst {
                        jdksDir.get().asFile.mkdirs()
                    }
                    t.onlyIf {
                        !jdksDir.get().file(jdkArchiveFileName).asFile.exists() &&
                            !jdkDirectory.get().file(jdkArchiveName).asFile.exists()
                    }
                }
            })

            jdk.verifyTask = project.tasks.register('verifyJdk' + normalizedName.capitalize(),
                Verify, new Action<Verify>() {
                @Override
                void execute(Verify t) {
                    t.group = JDKS_GROUP
                    t.description = "Verify JDK ${jdk.name}".toString()
                    t.enabled = !jdksToBeCopied.find { it.archiveFileName == jdkArchiveFileName }
                    t.dependsOn(jdk.downloadTask)
                    t.inputs.file(jdk.downloadTask.get().dest)

                    String algorithm = 'SHA-256'
                    String checksum = jdk.checksum.get()
                    if (checksum.contains('/')) {
                        String[] parts = checksum.split('/')
                        algorithm = parts[0]
                        checksum = parts[1]
                    }

                    t.src(jdk.downloadTask.get().dest)
                    t.algorithm(algorithm)
                    t.checksum(checksum)
                    t.onlyIf {
                        jdksDir.get().file(jdkArchiveFileName).asFile.exists()
                    }
                }
            })

            jdk.unpackTask = project.tasks.register('unpackJdk' + normalizedName.capitalize(),
                UnpackTask, new Action<UnpackTask>() {
                @Override
                void execute(UnpackTask t) {
                    t.group = JDKS_GROUP
                    t.description = "Unpack JDK ${jdk.name}".toString()
                    t.enabled = !jdksToBeCopied.find { it.archiveFileName == jdkArchiveFileName }
                    t.dependsOn(jdk.verifyTask)
                    t.inputFile.set(jdk.downloadTask.get().dest)
                    t.outputDirectory.set(jdkDirectory)
                    t.onlyIf { !jdkDirectory.get().file(jdkArchiveName).asFile.exists() && jdk.verifyTask.get().didWork }
                }
            })
        }

        // register aggregating tasks

        TaskProvider<DefaultTask> downloadJdks = project.tasks.register('downloadJdks',
            DefaultTask, new Action<DefaultTask>() {
            @Override
            @CompileDynamic
            void execute(DefaultTask t) {
                t.group = JDKS_GROUP
                t.description = 'Download all JDKs'
                t.dependsOn(jdks.downloadTask)
            }
        })

        TaskProvider<DefaultTask> verifyJdks = project.tasks.register('verifyJdks',
            DefaultTask, new Action<DefaultTask>() {
            @Override
            @CompileDynamic
            void execute(DefaultTask t) {
                t.group = JDKS_GROUP
                t.description = 'Verify all JDKs'
                t.dependsOn(jdks.verifyTask)
            }
        })

        TaskProvider<DefaultTask> unpackJdks = project.tasks.register('unpackJdks',
            DefaultTask, new Action<DefaultTask>() {
            @Override
            @CompileDynamic
            void execute(DefaultTask t) {
                t.group = JDKS_GROUP
                t.description = 'Unpack all JDKs'
                t.dependsOn(jdks.unpackTask)
            }
        })

        TaskProvider<Copy> copyJdksToCache = project.tasks.register('copyJdksToCache',
            Copy, new Action<Copy>() {
            @Override
            @CompileDynamic
            void execute(Copy t) {
                t.group = JDKS_GROUP
                t.description = 'Copy JDKs to Gradle cache'
                t.dependsOn downloadJdks
                t.from(jdks.downloadTask*.get().dest)
                t.into(cacheDir)
            }
        })

        List<TaskProvider<UnpackTask>> copyFromCacheTasks = []
        if (jdksToBeCopied) {
            jdksToBeCopied.each { candidateJdk ->
                String normalizedName = candidateJdk.normalizedName
                Provider<Directory> jdkDirectory = jdksDir.map({ d -> d.dir(normalizedName) })

                copyFromCacheTasks << project.tasks.register('copyJdkFromCache' + normalizedName.capitalize(),
                    UnpackTask, new Action<UnpackTask>() {
                    @Override
                    void execute(UnpackTask t) {
                        t.group = JDKS_GROUP
                        t.description = "Copy JDK ${candidateJdk.name} from cache".toString()
                        t.inputFile.set(candidateJdk.archive)
                        t.outputDirectory.set(jdkDirectory)
                        // Otherwise Gradle 8+ complains about task dependencies
                        t.dependsOn(copyJdksToCache)
                        // Not ideal but must nuke the directory to avoid copy errors
                        t.doFirst { jdkDirectory.get().asFile.deleteDir() }
                    }
                })
            }
        }

        TaskProvider<DefaultTask> copyJdksFromCache = project.tasks.register('copyJdksFromCache',
            DefaultTask, new Action<DefaultTask>() {
            @Override
            @CompileDynamic
            void execute(DefaultTask t) {
                t.group = JDKS_GROUP
                t.description = 'Copy JDKs from Gradle cache'
                t.dependsOn(copyFromCacheTasks)
            }
        })

        List<TaskProvider<Delete>> deleteFromCacheTasks = []
        if (jdksToBeCopied) {
            jdksToBeCopied.each { candidateJdk ->
                String normalizedName = candidateJdk.normalizedName

                deleteFromCacheTasks << project.tasks.register('deleteJdkFromCache' + normalizedName.capitalize(),
                    Delete, new Action<Delete>() {
                    @Override
                    void execute(Delete t) {
                        t.group = JDKS_GROUP
                        t.description = "Delete JDK ${candidateJdk.name} from cache".toString()
                        t.delete(candidateJdk.archive)
                    }
                })
            }
        }

        project.tasks.register('deleteJdksFromCache',
            DefaultTask, new Action<DefaultTask>() {
            @Override
            @CompileDynamic
            void execute(DefaultTask t) {
                t.group = JDKS_GROUP
                t.description = 'Delete JDKs from Gradle cache'
                t.dependsOn(deleteFromCacheTasks)
            }
        })

        TaskProvider<DefaultTask> setupJdks = project.tasks.register('setupJdks',
            DefaultTask, new Action<DefaultTask>() {
            @Override
            @CompileDynamic
            void execute(DefaultTask t) {
                t.group = JDKS_GROUP
                t.description = 'Setups all JDKs'
                t.dependsOn downloadJdks, verifyJdks, unpackJdks
                t.finalizedBy(copyJdksToCache)
            }
        })

        if (cacheDir.exists() && jdksToBeCopied) {
            project.tasks.named('assemble').get().dependsOn(copyJdksFromCache)
        }
        project.tasks.named('assemble').get().dependsOn(setupJdks)

        project.tasks.register('listJdks',
            ListJdksTask, new Action<ListJdksTask>() {
            @Override
            @CompileDynamic
            void execute(ListJdksTask t) {
                t.group = JDKS_GROUP
                t.description = 'Lists all JDKs'
                t.jdkContainer = jdkContainer
            }
        })
    }
}
