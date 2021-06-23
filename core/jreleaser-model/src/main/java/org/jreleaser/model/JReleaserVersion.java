/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
package org.jreleaser.model;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserVersion {
    private static final ResourceBundle bundle = ResourceBundle.getBundle(JReleaserVersion.class.getName());
    private static final String JRELEASER_VERSION = bundle.getString("jreleaser_version");

    public static String getPlainVersion() {
        return JRELEASER_VERSION;
    }

    public static void banner(PrintStream out) {
        banner(out, true);
    }

    public static void banner(PrintStream out, boolean full) {
        Manifest manifest = findMyManifest();
        if (null != manifest) {
            String version = manifest.getMainAttributes().getValue(Attributes.Name.SPECIFICATION_VERSION);
            String buildDate = manifest.getMainAttributes().getValue("Build-Date");
            String buildTime = manifest.getMainAttributes().getValue("Build-Time");
            String buildRevision = manifest.getMainAttributes().getValue("Build-Revision");
            boolean additionalInfo = isNotBlank(buildDate) || isNotBlank(buildTime) || isNotBlank(buildRevision);

            if (full) {
                out.printf("------------------------------------------------------------%n");
                out.printf("jreleaser %s%n", version);
            }
            out.printf("------------------------------------------------------------%n");
            if (additionalInfo) {
                if (isNotBlank(buildDate) && isNotBlank(buildTime)) {
                    out.printf("Build time:   %s %s%n", buildDate, buildTime);
                }
                if (isNotBlank(buildRevision)) out.println("Revision:     " + buildRevision);
                out.printf("------------------------------------------------------------%n");
            }
        } else {
            out.printf("jreleaser %s%n", JRELEASER_VERSION);
        }
    }

    public static void banner(PrintWriter out) {
        banner(out, true);
    }

    public static void banner(PrintWriter out, boolean full) {
        Manifest manifest = findMyManifest();
        if (null != manifest) {
            String version = manifest.getMainAttributes().getValue(Attributes.Name.SPECIFICATION_VERSION);
            String buildDate = manifest.getMainAttributes().getValue("Build-Date");
            String buildTime = manifest.getMainAttributes().getValue("Build-Time");
            String buildRevision = manifest.getMainAttributes().getValue("Build-Revision");
            boolean additionalInfo = isNotBlank(buildDate) || isNotBlank(buildTime) || isNotBlank(buildRevision);

            if (full) {
                out.printf("------------------------------------------------------------%n");
                out.printf("jreleaser %s%n", version);
            }
            out.printf("------------------------------------------------------------%n");
            if (additionalInfo) {
                if (isNotBlank(buildDate) && isNotBlank(buildTime)) {
                    out.printf("Build time:   %s %s%n", buildDate, buildTime);
                }
                if (isNotBlank(buildRevision)) out.println("Revision:     " + buildRevision);
                out.printf("------------------------------------------------------------%n");
            }
        } else {
            out.printf("jreleaser %s%n", JRELEASER_VERSION);
        }
    }

    private static Manifest findMyManifest() {
        try {
            Enumeration<URL> urls = JReleaserVersion.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                Manifest manifest = new Manifest(url.openStream());
                if (manifest.getMainAttributes().containsKey(Attributes.Name.SPECIFICATION_TITLE)) {
                    String specificationTitle = manifest.getMainAttributes().getValue(Attributes.Name.SPECIFICATION_TITLE);
                    if ("jreleaser".equals(specificationTitle)) {
                        return manifest;
                    }
                }
            }
        } catch (IOException e) {
            // well, this sucks
        }

        return null;
    }
}
