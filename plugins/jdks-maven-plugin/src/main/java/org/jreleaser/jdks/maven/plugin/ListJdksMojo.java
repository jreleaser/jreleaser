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
package org.jreleaser.jdks.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Lists all configured JDKs.
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@Mojo(threadSafe = true, name = "list-jdks")
public class ListJdksMojo extends AbstractJdksMojo {
    @Override
    protected void doExecute() throws MojoExecutionException {
        for (Jdk jdk : jdks) {
            printJdk(jdk);
        }
    }

    private void printJdk(Jdk jdk) {
        getLog().info("== JDK " + jdk.getName() + " ==");
        getLog().info("url: " + jdk.getUrl());
        getLog().info("checksum: " + jdk.getChecksum());
        getLog().info("platform: " + jdk.getPlatform());
        getLog().info("");
    }
}
