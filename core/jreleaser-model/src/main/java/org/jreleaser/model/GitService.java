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
import org.jreleaser.util.Env;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.getClassNameForLowerCaseHyphenSeparatedName;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class GitService implements Releaser, CommitAuthorProvider, OwnerProvider {
    public static final String TAG_NAME = "tagName";

    private static final String TAG_EARLY_ACCESS = "early-access";

    private final String serviceName;
    protected Boolean enabled;
    private String host;
    private String owner;
    private String name;
    private String repoUrlFormat;
    private String commitUrlFormat;
    private String downloadUrlFormat;
    private String releaseNotesUrlFormat;
    private String latestReleaseUrlFormat;
    private String issueTrackerUrlFormat;
    private String username;
    private String token;
    private String tagName = "v{{projectVersion}}";
    private String releaseName = "Release {{tagName}}";
    private CommitAuthor commitAuthor = new CommitAuthor();
    private boolean sign;
    private Changelog changelog = new Changelog();
    private boolean overwrite;
    private boolean allowUploadToExisting;
    private String apiEndpoint;

    private String cachedTagName;

    protected GitService(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    void setAll(GitService service) {
        this.enabled = service.enabled;
        this.host = service.host;
        this.owner = service.owner;
        this.name = service.name;
        this.repoUrlFormat = service.repoUrlFormat;
        this.commitUrlFormat = service.commitUrlFormat;
        this.downloadUrlFormat = service.downloadUrlFormat;
        this.releaseNotesUrlFormat = service.releaseNotesUrlFormat;
        this.latestReleaseUrlFormat = service.latestReleaseUrlFormat;
        this.issueTrackerUrlFormat = service.issueTrackerUrlFormat;
        this.username = service.username;
        this.token = service.token;
        this.tagName = service.tagName;
        this.releaseName = service.releaseName;
        this.sign = service.sign;
        this.overwrite = service.overwrite;
        this.allowUploadToExisting = service.allowUploadToExisting;
        this.apiEndpoint = service.apiEndpoint;
        this.commitAuthor.setAll(service.commitAuthor);
        this.changelog.setAll(service.changelog);
    }

    public void setCachedTagName(String cachedTagName) {
        this.cachedTagName = cachedTagName;
    }

    public String getCanonicalRepoName() {
        return owner + "/" + name;
    }

    public abstract String getBranch();

    public abstract String getReverseRepoHost();

    public String getResolvedTagName(Project project) {
        if (isBlank(cachedTagName)) {
            cachedTagName = Env.resolve(TAG_NAME, cachedTagName);
        }

        if (isBlank(cachedTagName)) {
            cachedTagName = applyTemplate(new StringReader(tagName), props(project));
        }

        return cachedTagName;
    }

    public String getEffectiveTagName(Project project) {
        if (project.isSnapshot()) {
            return TAG_EARLY_ACCESS;
        }

        return getResolvedTagName(project);
    }

    public String getResolvedReleaseName(Project project) {
        if (isNotBlank(releaseName)) {
            return applyTemplate(new StringReader(releaseName), props(project));
        }
        return "Release " + getResolvedTagName(project);
    }

    public String getResolvedRepoUrl(Project project) {
        return applyTemplate(new StringReader(repoUrlFormat), props(project));
    }

    public String getResolvedCommitUrl(Project project) {
        return applyTemplate(new StringReader(commitUrlFormat), props(project));
    }

    public String getResolvedDownloadUrl(Project project) {
        return applyTemplate(new StringReader(downloadUrlFormat), props(project));
    }

    public String getResolvedReleaseNotesUrl(Project project) {
        return applyTemplate(new StringReader(releaseNotesUrlFormat), props(project));
    }

    public String getResolvedLatestReleaseUrl(Project project) {
        return applyTemplate(new StringReader(latestReleaseUrlFormat), props(project));
    }

    public String getResolvedIssueTrackerUrl(Project project) {
        return applyTemplate(new StringReader(issueTrackerUrlFormat), props(project));
    }

    @Override
    public Boolean isEnabled() {
        return enabled != null && enabled;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabledSet() {
        return enabled != null;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepoUrlFormat() {
        return repoUrlFormat;
    }

    public void setRepoUrlFormat(String repoUrlFormat) {
        this.repoUrlFormat = repoUrlFormat;
    }

    public String getCommitUrlFormat() {
        return commitUrlFormat;
    }

    public void setCommitUrlFormat(String commitUrlFormat) {
        this.commitUrlFormat = commitUrlFormat;
    }

    public String getDownloadUrlFormat() {
        return downloadUrlFormat;
    }

    public void setDownloadUrlFormat(String downloadUrlFormat) {
        this.downloadUrlFormat = downloadUrlFormat;
    }

    public String getReleaseNotesUrlFormat() {
        return releaseNotesUrlFormat;
    }

    public void setReleaseNotesUrlFormat(String releaseNotesUrlFormat) {
        this.releaseNotesUrlFormat = releaseNotesUrlFormat;
    }

    public String getLatestReleaseUrlFormat() {
        return latestReleaseUrlFormat;
    }

    public void setLatestReleaseUrlFormat(String latestReleaseUrlFormat) {
        this.latestReleaseUrlFormat = latestReleaseUrlFormat;
    }

    public String getIssueTrackerUrlFormat() {
        return issueTrackerUrlFormat;
    }

    public void setIssueTrackerUrlFormat(String issueTrackerUrlFormat) {
        this.issueTrackerUrlFormat = issueTrackerUrlFormat;
    }

    public String getResolvedToken() {
        return Env.resolve(getClass().getSimpleName().toUpperCase() + "_TOKEN", token);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    @Override
    public CommitAuthor getCommitAuthor() {
        return commitAuthor;
    }

    @Override
    public void setCommitAuthor(CommitAuthor commitAuthor) {
        this.commitAuthor = commitAuthor;
    }

    public boolean isSign() {
        return sign;
    }

    public void setSign(boolean sign) {
        this.sign = sign;
    }

    public Changelog getChangelog() {
        return changelog;
    }

    public void setChangelog(Changelog changelog) {
        this.changelog = changelog;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public boolean isAllowUploadToExisting() {
        return allowUploadToExisting;
    }

    public void setAllowUploadToExisting(boolean allowUploadToExisting) {
        this.allowUploadToExisting = allowUploadToExisting;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("enabled", isEnabled());
        map.put("host", host);
        map.put("owner", owner);
        map.put("name", name);
        map.put("username", username);
        map.put("token", isNotBlank(getResolvedToken()) ? "************" : "**unset**");
        map.put("repoUrlFormat", repoUrlFormat);
        map.put("commitUrlFormat", commitUrlFormat);
        map.put("downloadUrlFormat", downloadUrlFormat);
        map.put("releaseNotesUrlFormat", releaseNotesUrlFormat);
        map.put("latestReleaseUrlFormat", latestReleaseUrlFormat);
        map.put("issueTrackerUrlFormat", issueTrackerUrlFormat);
        map.put("tagName", tagName);
        map.put("releaseName", releaseName);
        map.put("commitAuthor", commitAuthor.asMap());
        map.put("sign", sign);
        map.put("overwrite", overwrite);
        map.put("allowUploadToExisting", allowUploadToExisting);
        map.put("apiEndpoint", apiEndpoint);
        map.put("changelog", changelog.asMap());
        return map;
    }


    private Map<String, Object> props(Project project) {
        // duplicate from JReleaserModel to avoid endless recursion
        Map<String, Object> props = new LinkedHashMap<>();
        props.put(Constants.KEY_PROJECT_NAME, project.getName());
        props.put(Constants.KEY_PROJECT_NAME_CAPITALIZED, getClassNameForLowerCaseHyphenSeparatedName(project.getName()));
        props.put(Constants.KEY_PROJECT_VERSION, project.getResolvedVersion());
        props.put(Constants.KEY_PROJECT_DESCRIPTION, project.getDescription());
        props.put(Constants.KEY_PROJECT_LONG_DESCRIPTION, project.getLongDescription());
        props.put(Constants.KEY_PROJECT_WEBSITE, project.getWebsite());
        props.put(Constants.KEY_PROJECT_LICENSE, project.getLicense());

        if (project.getJava().isEnabled()) {
            props.put(Constants.KEY_PROJECT_JAVA_GROUP_ID, project.getJava().getGroupId());
            props.put(Constants.KEY_PROJECT_JAVA_ARTIFACT_ID, project.getJava().getArtifactId());
            props.put(Constants.KEY_PROJECT_JAVA_VERSION, project.getJava().getVersion());
            props.put(Constants.KEY_PROJECT_JAVA_MAIN_CLASS, project.getJava().getMainClass());
        }

        props.putAll(project.getResolvedExtraProperties());
        props.put(Constants.KEY_REPO_HOST, host);
        props.put(Constants.KEY_REPO_OWNER, owner);
        props.put(Constants.KEY_REPO_NAME, name);
        props.put(Constants.KEY_REPO_BRANCH, getBranch());
        props.put(Constants.KEY_REVERSE_REPO_HOST, getReverseRepoHost());
        props.put(Constants.KEY_CANONICAL_REPO_NAME, getCanonicalRepoName());
        props.put(Constants.KEY_TAG_NAME, project.isSnapshot() ? TAG_EARLY_ACCESS : cachedTagName);
        return props;
    }

    public void fillProps(Map<String, Object> props, Project project) {
        props.put(Constants.KEY_REPO_HOST, host);
        props.put(Constants.KEY_REPO_OWNER, owner);
        props.put(Constants.KEY_REPO_NAME, name);
        props.put(Constants.KEY_REPO_BRANCH, getBranch());
        props.put(Constants.KEY_REVERSE_REPO_HOST, getReverseRepoHost());
        props.put(Constants.KEY_CANONICAL_REPO_NAME, getCanonicalRepoName());
        props.put(Constants.KEY_TAG_NAME, getEffectiveTagName(project));
        props.put(Constants.KEY_RELEASE_NAME, getResolvedReleaseName(project));
        props.put(Constants.KEY_REPO_URL, getResolvedRepoUrl(project));
        props.put(Constants.KEY_COMMIT_URL, getResolvedCommitUrl(project));
        props.put(Constants.KEY_RELEASE_NOTES_URL, getResolvedReleaseNotesUrl(project));
        props.put(Constants.KEY_LATEST_RELEASE_URL, getResolvedLatestReleaseUrl(project));
        props.put(Constants.KEY_ISSUE_TRACKER_URL, getResolvedIssueTrackerUrl(project));
    }
}
