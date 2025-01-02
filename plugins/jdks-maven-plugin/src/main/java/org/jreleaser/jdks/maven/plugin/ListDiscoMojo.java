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
import org.jreleaser.bundle.RB;
import org.jreleaser.sdk.disco.Disco;
import org.jreleaser.sdk.disco.RestAPIException;

import java.util.List;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * Lists all configured JDKs with the Foojay's Disco API.
 *
 * @author Andres Almiray
 * @since 0.9.0
 */
@Mojo(threadSafe = true, name = "list-disco")
public class ListDiscoMojo extends AbstractDiscoMojo {
    @Override
    protected void doExecute(Disco disco) throws MojoExecutionException {
        for (Pkg pkg : pkgs) {
            printPkg(pkg, disco);
        }
    }

    private void printPkg(Pkg pkg, Disco disco) throws MojoExecutionException {
        try {
            List<org.jreleaser.sdk.disco.api.Pkg> packages = disco.packages(pkg.asDiscoPkg());

            if (packages.isEmpty()) return;

            getLog().info("== Pkg " + pkg.getName() + " ==");
            getLog().info("version:       " + pkg.getVersion());
            getLog().info("archiveType:   " + pkg.getArchiveType());
            getLog().info("platform:      " + pkg.getPlatform());
            getLog().info("distribution:  " + pkg.getDistribution());
            getLog().info("javafxBundled: " + pkg.isJavafxBundled());
            getLog().info("packageType:   " + pkg.getPackageType());
            if (isNotBlank(pkg.getReleaseStatus())) getLog().info("releaseStatus: " + pkg.getReleaseStatus());
            if (isNotBlank(pkg.getTermOfSupport())) getLog().info("termOfSupport: " + pkg.getTermOfSupport());
            if (isNotBlank(pkg.getBitness())) getLog().info("bitness:       " + pkg.getBitness());
            if (isNotBlank(pkg.getLibcType())) getLog().info("libcType:      " + pkg.getLibcType());
            getLog().info("package(s):    " + packages.size());

            int count = 0;
            for (org.jreleaser.sdk.disco.api.Pkg dpkg : packages) {
                if (!dpkg.isDirectlyDownloadable()) {
                    disco.getLogger().warn(RB.$("disco.package.not.downloadable", dpkg.getFilename()));
                    continue;
                }

                if (isNotBlank(pkg.getFilename()) && ++count == 1) {
                    getLog().info("filename:      " + pkg.getFilename());
                } else {
                    getLog().info("filename:      " + dpkg.getFilename());
                }
            }

        } catch (RestAPIException e) {
            getLog().error(e);
            throw new MojoExecutionException("Could not resolve " + pkg, e);
        }
    }
}
