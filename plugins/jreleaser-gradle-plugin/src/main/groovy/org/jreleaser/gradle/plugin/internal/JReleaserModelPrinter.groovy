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
package org.jreleaser.gradle.plugin.internal

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.kordamp.gradle.util.AnsiConsole

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class JReleaserModelPrinter extends org.jreleaser.model.JReleaserModelPrinter {
    private final AnsiConsole console

    JReleaserModelPrinter(Project project) {
        this(project, new PrintWriter(System.out, true))
    }

    JReleaserModelPrinter(Project project, PrintWriter out) {
        super(out)
        this.console = new AnsiConsole(project)
    }

    @Override
    protected String color(String color, String input) {
        switch (color) {
            case 'cyan':
                return console.cyan(input)
            case 'blue':
                return console.blue(input)
            case 'yellow':
                return console.yellow(input)
            case 'red':
                return console.red(input)
            case 'green':
                return console.green(input)
            case 'magenta':
                return console.magenta(input)
            case 'black':
                return console.black(input)
            case 'white':
                return console.white(input)
        }
    }
}