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
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.jreleaser.bundle.RB;
import org.jreleaser.sdk.disco.Disco;
import org.jreleaser.sdk.disco.RestAPIException;
import org.jreleaser.sdk.disco.api.EphemeralId;

import java.util.List;

/**
 * Downloads, verifies, and unpacks JDKs using Foojay's Disco API.
 *
 * @author Andres Almiray
 * @since 0.9.0
 */
@Mojo(threadSafe = true, name = "setup-disco")
public class SetupDiscoMojo extends AbstractDiscoMojo {
    @Component
    private ArchiverManager archiverManager;

    @Parameter(property = "disco.setup.unpack", defaultValue = "true")
    private boolean unpack;

    @Override
    protected void doExecute(Disco disco) throws MojoExecutionException {
        JdkHelper jdkHelper = new JdkHelper(project, getLog(), outputDirectory,
            session, pluginManager, archiverManager);

        for (Pkg pkg : pkgs) {
            Jdk jdk = resolvePkg(pkg, disco);
            if (null != jdk) {
                jdkHelper.setupJdk(jdk, unpack);
            }
        }
    }

    private Jdk resolvePkg(Pkg pkg, Disco disco) throws MojoExecutionException {
        try {
            disco.getLogger().info("Fetching " + pkg);
            List<org.jreleaser.sdk.disco.api.Pkg> packages = disco.packages(pkg.asDiscoPkg());

            if (packages.isEmpty()) return null;

            if (packages.size() > 1) {
                disco.getLogger().warn(RB.$("disco.multiple.packages", packages.size()));
            }

            for (org.jreleaser.sdk.disco.api.Pkg dpkg : packages) {
                if (!dpkg.isDirectlyDownloadable()) {
                    disco.getLogger().warn(RB.$("disco.package.not.downloadable", dpkg.getFilename()));
                    continue;
                }

                return resolvePkg(pkg, dpkg.getId(), disco);
            }

            return null;
        } catch (RestAPIException e) {
            getLog().error(e);
            throw new MojoExecutionException("Could not resolve " + pkg, e);
        }
    }

    private Jdk resolvePkg(Pkg pkg, String id, Disco disco) {
        List<EphemeralId> result = disco.pkg(id);

        if (result.isEmpty()) {
            return null;
        }

        Jdk jdk = new Jdk();
        jdk.setName(pkg.getName());
        jdk.setPlatform(pkg.getPlatform());
        jdk.setUrl(result.get(0).getDirectDownloadUri());
        jdk.setChecksum(result.get(0).getChecksum());
        jdk.setChecksumType(result.get(0).getChecksumType());
        jdk.setFilename(pkg.getFilename());
        return jdk;
    }
}
