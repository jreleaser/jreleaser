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
package org.jreleaser.jdks.gradle.plugin.internal

import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify
import groovy.transform.CompileStatic
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider
import org.jreleaser.jdks.gradle.plugin.Jdk
import org.jreleaser.jdks.gradle.plugin.tasks.UnpackTask
import org.jreleaser.util.Errors

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.getFilename
import static org.jreleaser.util.StringUtils.getPropertyNameForLowerCaseHyphenSeparatedName

/**
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@CompileStatic
class JdkImpl implements Jdk {
    private final ObjectFactory objects
    String name

    final Property<String> platform
    final Property<String> url
    final Property<String> checksum
    final RegularFileProperty archive

    TaskProvider<Download> downloadTask
    TaskProvider<Verify> verifyTask
    TaskProvider<UnpackTask> unpackTask

    @Inject
    JdkImpl(ObjectFactory objects) {
        this.objects = objects
        platform = objects.property(String).convention(Providers.notDefined())
        url = objects.property(String).convention(Providers.notDefined())
        checksum = objects.property(String).convention(Providers.notDefined())
        archive = objects.fileProperty().convention(Providers.notDefined())
    }

    String toString() {
        "${name} ${url.orNull}".toString()
    }

    JdkImpl copyOf() {
        JdkImpl jdk = new JdkImpl(objects)
        jdk.name = name
        jdk.platform.set(platform.orNull)
        jdk.url.set(url.orNull)
        jdk.checksum.set(checksum.orNull)
        jdk.archive.set(archive.orNull)
        jdk
    }

    String getArchiveName() {
        if (archive.present) {
            return getFilename(archive.get().asFile.name)
        }

        getFilename(getArchiveFileName())
    }

    String getArchiveFileName() {
        if (archive.present) {
            return archive.get().asFile.name
        }

        int p = url.get().lastIndexOf('/')
        url.get().substring(p + 1)
    }

    String getNormalizedName() {
        getPropertyNameForLowerCaseHyphenSeparatedName(name)
    }

    void validate(Errors errors) {
        if (!platform.present) {
            errors.configuration("jdk.${name}.platform is missing".toString())
        }
        if (!url.present) {
            errors.configuration("jdk.${name}.url is missing".toString())
        }
        if (!checksum.present) {
            errors.configuration("jdk.${name}.checksum is missing".toString())
        }
    }
}
