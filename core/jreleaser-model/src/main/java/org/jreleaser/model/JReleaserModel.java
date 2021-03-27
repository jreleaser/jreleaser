/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import org.jreleaser.util.Constants;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jreleaser.util.StringUtils.getClassNameForLowerCaseHyphenSeparatedName;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserModel implements Domain {
    private final Environment environment = new Environment();
    private final Project project = new Project();
    private final Release release = new Release();
    private final Packagers packagers = new Packagers();
    private final Announce announce = new Announce();
    private final Signing signing = new Signing();
    private final Set<Artifact> files = new LinkedHashSet<>();
    private final Map<String, Distribution> distributions = new LinkedHashMap<>();

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment.setAll(environment);
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project.setAll(project);
    }

    public Release getRelease() {
        return release;
    }

    public void setRelease(Release release) {
        this.release.setAll(release);
    }

    public Packagers getPackagers() {
        return packagers;
    }

    public void setPackagers(Packagers packagers) {
        this.packagers.setAll(packagers);
    }

    public Announce getAnnounce() {
        return announce;
    }

    public void setAnnounce(Announce announce) {
        this.announce.setAll(announce);
    }

    public Signing getSign() {
        return signing;
    }

    public void setSigning(Signing signing) {
        this.signing.setAll(signing);
    }

    public Set<Artifact> getFiles() {
        return files;
    }

    public void setFiles(Set<Artifact> files) {
        this.files.clear();
        this.files.addAll(files);
    }

    public void addFiles(Set<Artifact> files) {
        this.files.addAll(files);
    }

    public void addFiles(Artifact artifact) {
        if (null != artifact) {
            this.files.add(artifact);
        }
    }

    public Map<String, Distribution> getDistributions() {
        return distributions;
    }

    public void setDistributions(Map<String, Distribution> distributions) {
        this.distributions.clear();
        this.distributions.putAll(distributions);
    }

    public void addDistributions(Map<String, Distribution> distributions) {
        this.distributions.putAll(distributions);
    }

    public void addDistribution(Distribution distribution) {
        this.distributions.put(distribution.getName(), distribution);
    }

    public Distribution findDistribution(String name) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("Distribution name must not be blank");
        }

        if (distributions.isEmpty()) {
            throw new IllegalArgumentException("No distributions have been configured");
        }

        if (distributions.containsKey(name)) {
            return distributions.get(name);
        }

        throw new IllegalArgumentException("Distribution '" + name + "' not found");
    }

    public Map<String, Object> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        if (isNotBlank(environment.getVariables())) map.put("environment", environment.asMap());
        map.put("project", project.asMap());
        map.put("release", release.asMap());
        map.put("packagers", packagers.asMap());
        if (announce.isEnabled()) map.put("announce", announce.asMap());
        if (signing.isEnabled()) map.put("signing", signing.asMap());
        if (files.size() > 0) {
            map.put("files", files.stream()
                .map(Artifact::asMap)
                .collect(Collectors.toList()));
        }
        map.put("distributions", distributions.values()
            .stream()
            .map(Distribution::asMap)
            .collect(Collectors.toList()));
        return map;
    }

    public Map<String, Object> props() {
        Map<String, Object> props = new LinkedHashMap<>();
        fillProjectProperties(props, project);
        fillReleaseProperties(props, release);
        return props;
    }

    private void fillProjectProperties(Map<String, Object> props, Project project) {
        props.put(Constants.KEY_PROJECT_NAME, project.getName());
        props.put(Constants.KEY_PROJECT_NAME_CAPITALIZED, getClassNameForLowerCaseHyphenSeparatedName(project.getName()));
        props.put(Constants.KEY_PROJECT_VERSION, project.getResolvedVersion());
        props.put(Constants.KEY_PROJECT_DESCRIPTION, project.getDescription());
        props.put(Constants.KEY_PROJECT_LONG_DESCRIPTION, project.getLongDescription());
        props.put(Constants.KEY_PROJECT_WEBSITE, project.getWebsite());
        props.put(Constants.KEY_PROJECT_LICENSE, project.getLicense());
        props.put(Constants.KEY_PROJECT_AUTHORS_BY_SPACE, String.join(" ", project.getAuthors()));
        props.put(Constants.KEY_PROJECT_AUTHORS_BY_COMMA, String.join(",", project.getAuthors()));
        props.put(Constants.KEY_PROJECT_TAGS_BY_SPACE, String.join(" ", project.getTags()));
        props.put(Constants.KEY_PROJECT_TAGS_BY_COMMA, String.join(",", project.getTags()));

        if (project.getJava().isEnabled()) {
            props.put(Constants.KEY_PROJECT_JAVA_GROUP_ID, project.getJava().getGroupId());
            props.put(Constants.KEY_PROJECT_JAVA_ARTIFACT_ID, project.getJava().getArtifactId());
            props.put(Constants.KEY_PROJECT_JAVA_VERSION, project.getJava().getVersion());
            props.put(Constants.KEY_PROJECT_JAVA_MAIN_CLASS, project.getJava().getMainClass());
        }

        props.putAll(project.getResolvedExtraProperties());
    }

    private void fillReleaseProperties(Map<String, Object> props, Release release) {
        GitService service = release.getGitService();
        props.put(Constants.KEY_REPO_HOST, service.getHost());
        props.put(Constants.KEY_REPO_OWNER, service.getOwner());
        props.put(Constants.KEY_REPO_NAME, service.getName());
        props.put(Constants.KEY_REPO_BRANCH, service.getBranch());
        props.put(Constants.KEY_RELEASE_NAME, service.getResolvedReleaseName(project));
        props.put(Constants.KEY_TAG_NAME, service.getEffectiveTagName(project));
        props.put(Constants.KEY_REVERSE_REPO_HOST, service.getReverseRepoHost());
        props.put(Constants.KEY_CANONICAL_REPO_NAME, service.getCanonicalRepoName());
        props.put(Constants.KEY_REPO_URL, service.getResolvedRepoUrl(project));
        props.put(Constants.KEY_COMMIT_URL, service.getResolvedCommitUrl(project));
        props.put(Constants.KEY_RELEASE_NOTES_URL, service.getResolvedReleaseNotesUrl(project));
        props.put(Constants.KEY_LATEST_RELEASE_URL, service.getResolvedLatestReleaseUrl(project));
        props.put(Constants.KEY_ISSUE_TRACKER_URL, service.getResolvedIssueTrackerUrl(project));
    }
}
