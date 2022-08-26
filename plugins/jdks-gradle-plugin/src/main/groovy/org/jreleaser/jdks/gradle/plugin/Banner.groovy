/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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

import groovy.transform.CompileStatic
import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.Project

import java.text.MessageFormat

/**
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@CompileStatic
final class Banner {
    private final ResourceBundle bundle = ResourceBundle.getBundle(Banner.name)
    private final String productVersion = bundle.getString('product.version')
    private final String productId = bundle.getString('product.id')
    private final String productName = bundle.getString('product.name')
    private final String banner = MessageFormat.format(bundle.getString('product.banner'), productName, productVersion)
    private final List<String> visited = []

    private static final Banner BANNER = new Banner()

    private Banner() {
        // noop
    }

    static void display(Project project) {
        if (BANNER.visited.contains(project.rootProject.name)) {
            return
        }
        BANNER.visited.add(project.rootProject.name)
        project.gradle.addBuildListener(new BuildAdapter() {
            @Override
            void buildFinished(BuildResult result) {
                BANNER.visited.clear()
            }
        })

        File parent = new File(project.gradle.gradleUserHomeDir, 'caches')
        File markerFile = BANNER.getMarkerFile(parent)
        if (!markerFile.exists()) {
            markerFile.parentFile.mkdirs()
            markerFile.text = '1'
            println(BANNER.banner)
        } else {
            try {
                int count = Integer.parseInt(markerFile.text)
                if (count < 3) {
                    println(BANNER.banner)
                }
                markerFile.text = (count + 1) + ''
            } catch (NumberFormatException e) {
                markerFile.text = '1'
                println(BANNER.banner)
            }
        }
    }

    private File getMarkerFile(File parent) {
        new File(parent,
            'jreleaser' +
                File.separator +
                productId +
                File.separator +
                productVersion +
                File.separator +
                'marker.txt')
    }
}
