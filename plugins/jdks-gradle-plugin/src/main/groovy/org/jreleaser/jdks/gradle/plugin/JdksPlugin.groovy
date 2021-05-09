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
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.jreleaser.jdks.gradle.plugin.internal.JdkImpl
import org.jreleaser.jdks.gradle.plugin.tasks.ListJdksTask
import org.jreleaser.util.Errors

import static org.jreleaser.util.StringUtils.getPropertyNameForLowerCaseHyphenSeparatedName

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
        Banner.display(project)

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
                    errors.logErrors(new PrintWriter(System.out, true))
                    throw new IllegalStateException('Jdks are not properly configured')
                }

                configureTasks(p, jdkContainer)
            }
        })
    }

    private void configureTasks(Project project, NamedDomainObjectContainer<JdkImpl> jdkContainer) {
        Provider<Directory> jdksDir = project.layout.buildDirectory.dir('jdks')

        List<JdkImpl> jdks = new ArrayList<>(jdkContainer)

        // register tasks per JDK
        jdks.each { jdk ->
            String normalizedName = getPropertyNameForLowerCaseHyphenSeparatedName(jdk.name).capitalize()
            int p = jdk.url.get().lastIndexOf('/')
            String filename = jdk.url.get().substring(p + 1)

            jdk.downloadTask = project.tasks.register('downloadJdk' + normalizedName,
                Download, new Action<Download>() {
                @Override
                void execute(Download t) {
                    t.group = JDKS_GROUP
                    t.description = "Download JDK ${jdk.name}".toString()

                    t.src(jdk.url)
                    t.dest(jdksDir.get().file(filename).asFile)
                    t.doFirst {
                        jdksDir.get().asFile.mkdirs()
                    }
                    t.onlyIf {
                        !jdksDir.get().file(filename).asFile.exists()
                    }
                }
            })

            jdk.verifyTask = project.tasks.register('verifyJdk' + normalizedName,
                Verify, new Action<Verify>() {
                @Override
                void execute(Verify t) {
                    t.group = JDKS_GROUP
                    t.description = "Verify JDK ${jdk.name}".toString()
                    t.dependsOn(jdk.downloadTask)

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
                }
            })

            jdk.unpackTask = project.tasks.register('unpackJdk' + normalizedName,
                Copy, new Action<Copy>() {
                @Override
                void execute(Copy t) {
                    t.group = JDKS_GROUP
                    t.description = "Unpack JDK ${jdk.name}".toString()
                    t.dependsOn(jdk.verifyTask)

                    File jdkArchive = jdk.downloadTask.get().dest
                    if (jdkArchive.name.endsWith('.zip')) {
                        t.from(t.project.zipTree(jdkArchive))
                    } else if (jdkArchive.name.endsWith('.tar') ||
                        jdkArchive.name.endsWith('.tar.gz') ||
                        jdkArchive.name.endsWith('.tgz')) {
                        t.from(t.project.tarTree(jdkArchive))
                    }
                    t.into(jdksDir)
                    t.onlyIf { jdk.verifyTask.get().didWork }
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

        File cacheDir = project.file("${project.gradle.gradleUserHomeDir}/caches/jdks")

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

        List<File> jdksToBeCopied = []
        if (cacheDir.exists()) {
            cacheDir.listFiles().each { jdkArchive ->
                if (!jdksDir.get().file(jdkArchive.name).asFile.exists()) {
                    jdksToBeCopied << jdkArchive
                }
            }
        }

        TaskProvider<Copy> copyJdksFromCache = null
        if (jdksToBeCopied) {
            copyJdksFromCache = project.tasks.register('copyJdksFromCache',
                Copy, new Action<Copy>() {
                @Override
                void execute(Copy t) {
                    t.group = JDKS_GROUP
                    t.description = 'Copy JDKs from Gradle cache'
                    jdksToBeCopied.each { jdkArchive ->
                        if (jdkArchive.name.endsWith('.zip')) {
                            t.from(t.project.zipTree(jdkArchive))
                        } else if (jdkArchive.name.endsWith('.tar') ||
                            jdkArchive.name.endsWith('.tar.gz') ||
                            jdkArchive.name.endsWith('.tgz')) {
                            t.from(t.project.tarTree(jdkArchive))
                        }
                        t.into(jdksDir)
                    }
                }
            })
        }

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
