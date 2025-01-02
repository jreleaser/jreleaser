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
package org.jreleaser.jdks.gradle.plugin.tasks

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.jreleaser.jdks.gradle.plugin.internal.JdkImpl

/**
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@CompileStatic
abstract class ListJdksTask extends DefaultTask {
    @Input
    NamedDomainObjectContainer<JdkImpl> jdkContainer

    @TaskAction

    void displayConfig() {
        jdkContainer.forEach({ JdkImpl jdk ->
            println "== JDK ${jdk.name} =="
            println "url: ${jdk.url.get()}"
            println "checksum: ${jdk.checksum.get()}"
            println "platform: ${jdk.platform.get()}"
            println ""
        })
    }
}
