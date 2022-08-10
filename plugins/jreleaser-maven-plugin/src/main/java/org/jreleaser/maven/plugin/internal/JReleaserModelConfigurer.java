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
package org.jreleaser.maven.plugin.internal;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Developer;
import org.apache.maven.model.License;
import org.apache.maven.project.MavenProject;
import org.jreleaser.model.Environment;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class JReleaserModelConfigurer {
    private JReleaserModelConfigurer() {
        // noop
    }

    public static JReleaserModel configure(JReleaserModel model, MavenProject mavenProject, MavenSession session) {
        Properties properties = new Properties();
        properties.putAll(mavenProject.getProperties());
        properties.putAll(session.getUserProperties());
        Environment.PropertiesSource propertiesSource = new Environment.PropertiesPropertiesSource(properties);
        model.getEnvironment().setPropertiesSource(propertiesSource);

        configureProject(model.getProject(), mavenProject, session);

        return model;
    }

    private static void configureProject(Project project, MavenProject mavenProject, MavenSession session) {
        if (isBlank(project.getName())) {
            project.setName(mavenProject.getArtifactId());
        }
        if (isBlank(project.getVersion())) {
            project.setVersion(mavenProject.getVersion());
        }
        if (isBlank(project.getDescription())) {
            project.setDescription(mavenProject.getDescription());
        }
        if (isBlank(project.getLinks().getHomepage())) {
            project.getLinks().setHomepage(mavenProject.getUrl());
        }
        if (isBlank(project.getLinks().getHomepage()) && (null != mavenProject.getOrganization())) {
            project.getLinks().setHomepage(mavenProject.getOrganization().getUrl());
        }
        if (project.getAuthors().isEmpty()) {
            project.setAuthors(resolveAuthors(mavenProject.getDevelopers()));
        }
        if (isBlank(project.getLicense())) {
            License license = resolveLicense(mavenProject.getLicenses());
            if (null != license) {
                project.setLicense(license.getName());
                project.getLinks().setLicense(license.getUrl());
            }
        }
        if (isBlank(project.getInceptionYear()) && isNotBlank(mavenProject.getInceptionYear())) {
            project.setInceptionYear(mavenProject.getInceptionYear());
        }

        project.getJava().setGroupId(mavenProject.getGroupId());
        project.getJava().setArtifactId(mavenProject.getArtifactId());
        if (isBlank(project.getJava().getVersion())) {
            project.getJava().setVersion(resolveJavaVersion(mavenProject));
        }
        if (!project.getJava().isMultiProjectSet()) {
            project.getJava().setMultiProject(session.getAllProjects().size() > 1);
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

    private static License resolveLicense(List<License> licenses) {
        if (null == licenses || licenses.isEmpty()) return null;
        for (License license : licenses) {
            if (isNotBlank(license.getName())) {
                return license;
            }
        }
        return null;
    }

    private static String resolveJavaVersion(MavenProject mavenProject) {
        String javaVersion = System.getProperty("java.version");

        Properties properties = mavenProject.getProperties();
        if (null != properties && !properties.isEmpty()) {
            if (properties.containsKey("maven.compiler.release")) {
                javaVersion = properties.getProperty("maven.compiler.release");
            } else if (properties.containsKey("maven.compiler.target")) {
                javaVersion = properties.getProperty("maven.compiler.target");
            } else if (properties.containsKey("maven.compiler.source")) {
                javaVersion = properties.getProperty("maven.compiler.source");
            }
        }

        return resolveJavaVersion(javaVersion);
    }

    private static String resolveJavaVersion(String str) {
        if (str.startsWith("1.")) {
            // this can only be Java 8
            return "8";
        }
        return str.split("\\.")[0];
    }
}