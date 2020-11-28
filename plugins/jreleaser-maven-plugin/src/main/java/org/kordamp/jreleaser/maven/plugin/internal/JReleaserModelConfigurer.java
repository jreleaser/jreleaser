/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 Andres Almiray.
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
package org.kordamp.jreleaser.maven.plugin.internal;

import org.apache.maven.model.Developer;
import org.apache.maven.model.License;
import org.apache.maven.project.MavenProject;
import org.kordamp.jreleaser.model.JReleaserModel;
import org.kordamp.jreleaser.model.Project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.kordamp.jreleaser.util.StringUtils.isBlank;
import static org.kordamp.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class JReleaserModelConfigurer {
    private JReleaserModelConfigurer() {
        // noop
    }

    public static void configure(JReleaserModel model, MavenProject mavenProject) {
        configureProject(model.getProject(), mavenProject);
    }

    private static void configureProject(Project project, MavenProject mavenProject) {
        if (isBlank(project.getName())) {
            project.setName(mavenProject.getArtifactId());
        }
        if (isBlank(project.getVersion())) {
            project.setVersion(mavenProject.getVersion());
        }
        if (isBlank(project.getDescription())) {
            project.setDescription(mavenProject.getDescription());
        }
        if (isBlank(project.getWebsite())) {
            project.setWebsite(mavenProject.getUrl());
        }
        if (project.getAuthors().isEmpty()) {
            project.setAuthors(resolveAuthors(mavenProject.getDevelopers()));
        }
        if (isBlank(project.getLicense())) {
            project.setLicense(resolveLicense(mavenProject.getLicenses()));
        }
        if (isBlank(project.getJavaVersion())) {
            project.setJavaVersion(resolveJavaVersion(mavenProject));
        }
    }

    private static List<String> resolveAuthors(List<Developer> developers) {
        if (null == developers || developers.isEmpty()) return Collections.emptyList();

        List<String> authors = new ArrayList<>();
        // 1. find all with role="author"
        for (Developer developer : developers) {
            List<String> roles = developer.getRoles();
            if (null != roles && roles.stream()
                .anyMatch("author"::equalsIgnoreCase)) {
                String name = developer.getName();
                if (isBlank(name)) name = developer.getId();
                if (isNotBlank(name)) authors.add(name);
            }
        }
        // 2. add all if the authors list is empty
        if (authors.isEmpty()) {
            for (Developer developer : developers) {
                String name = developer.getName();
                if (isBlank(name)) name = developer.getId();
                if (isNotBlank(name)) authors.add(name);
            }
        }

        return authors;
    }

    private static String resolveLicense(List<License> licenses) {
        if (null == licenses || licenses.isEmpty()) return "";
        for (License license : licenses) {
            if (isNotBlank(license.getName())) {
                return license.getName();
            }
        }
        return "";
    }

    private static String resolveJavaVersion(MavenProject mavenProject) {
        Properties properties = mavenProject.getProperties();
        if (null == properties || properties.isEmpty()) return "";

        if (properties.containsKey("maven.compiler.release")) {
            return properties.getProperty("maven.compiler.release");
        }
        if (properties.containsKey("maven.compiler.target")) {
            return properties.getProperty("maven.compiler.target");
        }
        if (properties.containsKey("maven.compiler.source")) {
            return properties.getProperty("maven.compiler.source");
        }

        return resolveJavaVersion(System.getProperty("java.version"));
    }

    private static String resolveJavaVersion(String str) {
        if (str.startsWith("1.")) {
            return str.substring(0, 2);
        }
        return str.split("\\.")[0];
    }
}