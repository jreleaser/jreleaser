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
package org.jreleaser.model.internal.validation.release;

import org.jreleaser.bundle.RB;
import org.jreleaser.config.JReleaserConfigLoader;
import org.jreleaser.model.Active;
import org.jreleaser.model.UpdateSection;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.model.internal.project.Project;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.release.Changelog;
import org.jreleaser.model.internal.release.CodebergReleaser;
import org.jreleaser.model.internal.release.GenericGitReleaser;
import org.jreleaser.model.internal.release.GiteaReleaser;
import org.jreleaser.model.internal.release.GithubReleaser;
import org.jreleaser.util.Errors;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.groupingBy;
import static org.jreleaser.model.api.release.Releaser.BRANCH;
import static org.jreleaser.model.api.release.Releaser.BRANCH_PUSH;
import static org.jreleaser.model.api.release.Releaser.MILESTONE_NAME;
import static org.jreleaser.model.api.release.Releaser.OVERWRITE;
import static org.jreleaser.model.api.release.Releaser.PREVIOUS_TAG_NAME;
import static org.jreleaser.model.api.release.Releaser.RELEASE_NAME;
import static org.jreleaser.model.api.release.Releaser.SKIP_RELEASE;
import static org.jreleaser.model.api.release.Releaser.SKIP_TAG;
import static org.jreleaser.model.api.release.Releaser.TAG_NAME;
import static org.jreleaser.model.api.release.Releaser.UPDATE;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class BaseReleaserValidator {
    private static final String DEFAULT_CHANGELOG_TPL = "src/jreleaser/templates/changelog.tpl";
    private static final String DEFAULT_APPEND_CHANGELOG_TPL = "src/jreleaser/templates/append-changelog.tpl";
    private BaseReleaserValidator() {
        // noop
    }

    public static void validateGitService(JReleaserContext context, Mode mode, BaseReleaser<?, ?> service, Errors errors) {
        JReleaserModel model = context.getModel();
        Project project = model.getProject();

        if (!service.isEnabledSet()) {
            service.setEnabled(true);
        }

        if (!service.isEnabled()) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (!mode.validateStandalone() && isBlank(service.getOwner()) && !(service instanceof GenericGitReleaser)) {
            errors.configuration(RB.$("validation_must_not_be_blank", service.getServiceName() + ".owner"));
        }

        if (isBlank(service.getName())) {
            service.setName(project.getName());
        }

        String baseKey = "release." + service.getServiceName() + ".";
        service.setUsername(
            checkProperty(context,
                service.getServiceName().toUpperCase(Locale.ENGLISH) + "_USERNAME",
                baseKey + "username",
                service.getUsername(),
                service.getOwner()));

        service.setToken(
            checkProperty(context,
                service.getServiceName().toUpperCase(Locale.ENGLISH) + "_TOKEN",
                baseKey + "token",
                service.getToken(),
                !mode.validateStandalone() ? errors : new Errors()));

        service.setTagName(
            checkProperty(context,
                TAG_NAME,
                baseKey + "tagName",
                service.getTagName(),
                "v{{projectVersion}}"));

        service.setPreviousTagName(
            checkProperty(context,
                PREVIOUS_TAG_NAME,
                baseKey + "previousTagName",
                service.getPreviousTagName(),
                ""));

        if (service.isReleaseSupported()) {
            service.setReleaseName(
                checkProperty(context,
                    RELEASE_NAME,
                    baseKey + "releaseName",
                    service.getReleaseName(),
                    "Release {{tagName}}"));
        }

        service.setBranch(
            checkProperty(context,
                BRANCH,
                baseKey + "branch",
                service.getBranch(),
                "main"));

        service.setBranchPush(
            checkProperty(context,
                BRANCH_PUSH,
                baseKey + "branch.push",
                service.getBranchPush(),
                service.getBranch()));

        if (!service.isOverwriteSet()) {
            service.setOverwrite(
                checkProperty(context,
                    OVERWRITE,
                    baseKey + "overwrite",
                    null,
                    false));
        }

        if (service.isReleaseSupported()) {
            if (!service.getUpdate().isEnabledSet()) {
                service.getUpdate().setEnabled(
                    checkProperty(context,
                        UPDATE,
                        baseKey + "update",
                        null,
                        false));
            }

            if (service.getUpdate().isEnabled() && service.getUpdate().getSections().isEmpty()) {
                service.getUpdate().getSections().add(UpdateSection.ASSETS);
            }
        }

        if (!service.isSkipTagSet()) {
            service.setSkipTag(
                checkProperty(context,
                    SKIP_TAG,
                    baseKey + "skipTag",
                    null,
                    false));
        }

        if (!service.isSkipReleaseSet()) {
            service.setSkipRelease(
                checkProperty(context,
                    SKIP_RELEASE,
                    baseKey + "skipRelease",
                    null,
                    false));
        }

        if (isBlank(service.getTagName())) {
            service.setTagName("v" + project.getVersion());
        }

        if (service.isReleaseSupported()) {
            if (isBlank(service.getReleaseName())) {
                service.setReleaseName("Release {{ tagName }}");
            }

            service.getMilestone().setName(
                checkProperty(context,
                    MILESTONE_NAME,
                    baseKey + "milestone.name",
                    service.getMilestone().getName(),
                    "{{tagName}}"));

            BaseReleaser.Issues issues = service.getIssues();
            if (isBlank(issues.getComment())) {
                issues.setComment(RB.$("default.release.issue.message"));
            }
            if (isBlank(issues.getLabel().getName())) {
                issues.getLabel().setName(RB.$("default.release.issue.label.text"));
            }
            if (isBlank(issues.getLabel().getColor())) {
                issues.getLabel().setColor(RB.$("default.release.issue.label.color"));
            }
            if (isBlank(issues.getLabel().getDescription())) {
                issues.getLabel().setDescription(RB.$("default.release.issue.label.desc"));
            }
        }

        if (isBlank(service.getCommitAuthor().getName())) {
            service.getCommitAuthor().setName("jreleaserbot");
        }
        if (isBlank(service.getCommitAuthor().getEmail())) {
            service.getCommitAuthor().setEmail("jreleaser@kordamp.org");
        }

        validateTimeout(service);

        // FIXME: extension
        // eager resolve
        service.getResolvedTagName(context.getModel());
        service.getResolvedBranchPush(context.getModel());
        if (service.isReleaseSupported()) {
            service.getResolvedReleaseName(context.getModel());
            service.getMilestone().getResolvedName(service.props(context.getModel()));
        }

        if (project.isSnapshot()) {
            boolean generate = false;
            if (service instanceof GithubReleaser) {
                GithubReleaser gh = (GithubReleaser) service;
                generate = gh.getReleaseNotes().isEnabled();
                if (!gh.isDraftSet()) gh.setDraft(false);
            }

            if (service instanceof GiteaReleaser) {
                GiteaReleaser g = (GiteaReleaser) service;
                if (!g.isDraftSet()) g.setDraft(false);
            }

            if (service instanceof CodebergReleaser) {
                CodebergReleaser g = (CodebergReleaser) service;
                if (!g.isDraftSet()) g.setDraft(false);
            }

            if (!generate && !service.getChangelog().isEnabledSet()) {
                service.getChangelog().setEnabled(true);
                service.getChangelog().setSort(org.jreleaser.model.Changelog.Sort.DESC);
            }
            if (service.isReleaseSupported()) {
                service.setOverwrite(true);
            }
            service.getIssues().setEnabled(false);
        }

        if (!service.getChangelog().isEnabledSet()) {
            boolean generate = false;
            if (service instanceof GithubReleaser) {
                GithubReleaser gh = (GithubReleaser) service;
                generate = gh.getReleaseNotes().isEnabled();
            }

            if (!generate) {
                service.getChangelog().setEnabled(true);
            }
        }

        if (!mode.validateStandalone()) {
            validateChangelog(context, service, errors);
        }

        if (mode.validateConfig()) {
            if (service.isSign() && model.getSigning().getMode() == org.jreleaser.model.Signing.Mode.COSIGN) {
                service.setSign(false);
                errors.warning(RB.$("validation_git_signing_cosign", service.getServiceName()));
                return;
            }
            if (service.isSign() && !model.getSigning().isEnabled()) {
                if (context.isDryrun()) {
                    service.setSign(false);
                } else {
                    errors.configuration(RB.$("validation_git_signing", service.getServiceName()));
                }
            }
        }
    }

    private static void validateChangelog(JReleaserContext context, BaseReleaser<?, ?> service, Errors errors) {
        Changelog changelog = service.getChangelog();

        if (isNotBlank(changelog.getExternal())) {
            changelog.setEnabled(true);
            changelog.setFormatted(Active.NEVER);
        }

        if (!changelog.isEnabledSet() && changelog.isSet()) {
            changelog.setEnabled(true);
        }

        // Special case for GitHub
        if (service instanceof GithubReleaser) {
            GithubReleaser gh = (GithubReleaser) service;
            boolean generate = gh.getReleaseNotes().isEnabled();

            if (generate && changelog.isEnabled()) {
                errors.configuration(RB.$("validation_github_releasenotes_changelog"));
                return;
            }
        }

        if (!changelog.resolveFormatted(context.getModel().getProject())) return;

        if (null == changelog.getSort()) {
            changelog.setSort(org.jreleaser.model.Changelog.Sort.DESC);
        }

        if (isBlank(changelog.getCategoryTitleFormat())) {
            changelog.setCategoryTitleFormat("## {{categoryTitle}}");
        }

        if (isBlank(changelog.getContributorsTitleFormat())) {
            changelog.setContributorsTitleFormat("## Contributors");
        }

        if (isBlank(changelog.getContent()) && isBlank(changelog.getContentTemplate())) {
            if (Files.exists(context.getBasedir().resolve(DEFAULT_CHANGELOG_TPL))) {
                changelog.setContentTemplate(DEFAULT_CHANGELOG_TPL);
            } else {
                changelog.setContent(lineSeparator() + "## Changelog" +
                    lineSeparator() + lineSeparator() + "{{changelogChanges}}" +
                    lineSeparator() + "    {{changelogContributors}}");
            }
        }

        if (isNotBlank(changelog.getContentTemplate()) &&
            !Files.exists(context.getBasedir().resolve(changelog.getContentTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "changelog.contentTemplate", changelog.getContentTemplate()));
        }

        if (isNotBlank(changelog.getPreset())) {
            loadPreset(context, changelog, errors);
        }

        // set the default format after the preset, as preset can contain a default format too
        if (isBlank(changelog.getFormat())) {
            changelog.setFormat("- {{commitShortHash}} {{commitTitle}} ({{commitAuthor}})");
        }

        if (changelog.getCategories().isEmpty()) {
            changelog.getCategories().add(Changelog.Category.of("feature", RB.$("default.category.feature"), "", "feature", "enhancement"));
            changelog.getCategories().add(Changelog.Category.of("fix", RB.$("default.category.bug.fix"), "", "bug", "fix"));
        } else {
            int i = 0;
            for (Changelog.Category category : changelog.getCategories()) {
                if (isBlank(category.getTitle())) {
                    errors.configuration(RB.$("validation_is_missing", service.getServiceName() + ".changelog.categories[" + i + "].title"));
                }
                if (category.getLabels().isEmpty()) {
                    errors.configuration(RB.$("validation_are_missing", service.getServiceName() + ".changelog.categories[" + i + "].labels"));
                }

                i++;
            }

            // validate category.title is unique
            Map<String, List<Changelog.Category>> byTitle = changelog.getCategories().stream()
                .collect(groupingBy(Changelog.Category::getTitle));
            byTitle.forEach((title, categories) -> {
                if (categories.size() > 1) {
                    errors.configuration(RB.$("validation_changelog_multiple_categories", service.getServiceName(), title));
                }
            });
        }

        if (!changelog.getLabelers().isEmpty()) {
            int i = 0;
            for (Changelog.Labeler labeler : changelog.getLabelers()) {
                if (isBlank(labeler.getLabel())) {
                    errors.configuration(RB.$("validation_is_missing", service.getServiceName() + ".changelog.labelers[" + i + "].label"));
                }
                if (isBlank(labeler.getTitle()) && isBlank(labeler.getBody()) && isBlank(labeler.getContributor())) {
                    errors.configuration(RB.$("validation_git_required", service.getServiceName() + ".changelog.labelers[" + i + "] title", "body", "contributor"));
                }

                if (isNotBlank(labeler.getContributor()) && changelog.getExcludeLabels().contains(labeler.getLabel())) {
                    changelog.getHide().getContributors().add(labeler.getContributor());
                }

                i++;
            }
        }

        if (!changelog.getReplacers().isEmpty()) {
            int i = 0;
            for (Changelog.Replacer replacer : changelog.getReplacers()) {
                if (isBlank(replacer.getSearch())) {
                    errors.configuration(RB.$("validation_is_missing", service.getServiceName() + ".changelog.replacers[" + i + "].search"));
                }
                if (null == replacer.getReplace()) {
                    errors.configuration(RB.$("validation_is_missing", service.getServiceName() + ".changelog.replacers[" + i + "].replace"));
                }

                i++;
            }
        }

        if (!changelog.getContributors().isEnabledSet()) {
            changelog.getContributors().setEnabled(true);
        }

        // append changelog
        if (!changelog.getAppend().isEnabled()) return;

        if (isBlank(changelog.getAppend().getTitle())) {
            changelog.getAppend().setTitle("## [{{tagName}}]");
        }

        if (isBlank(changelog.getAppend().getTarget())) {
            changelog.getAppend().setTarget("CHANGELOG.md");
        }

        if (isBlank(changelog.getAppend().getTarget())) {
            errors.configuration(RB.$("validation_is_missing", service.getServiceName() + ".changelog.append.target"));
        }

        if (isBlank(changelog.getAppend().getContent()) && isBlank(changelog.getAppend().getContentTemplate())) {
            if (Files.exists(context.getBasedir().resolve(DEFAULT_APPEND_CHANGELOG_TPL))) {
                changelog.getAppend().setContentTemplate(DEFAULT_APPEND_CHANGELOG_TPL);
            } else {
                changelog.getAppend().setContent(lineSeparator() + "{{changelogTitle}}" +
                    lineSeparator() + lineSeparator() + "{{changelogContent}}");
            }
        }

        if (isNotBlank(changelog.getAppend().getContentTemplate()) &&
            !Files.exists(context.getBasedir().resolve(changelog.getAppend().getContentTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "changelog.append.contentTemplate", changelog.getAppend().getContentTemplate()));
        }
    }

    private static void loadPreset(JReleaserContext context, Changelog changelog, Errors errors) {
        try {
            String preset = changelog.getPreset().toLowerCase(Locale.ENGLISH).trim();
            String presetFileName = "META-INF/jreleaser/changelog/preset-" + preset + ".yml";

            InputStream inputStream = BaseReleaserValidator.class.getClassLoader()
                .getResourceAsStream(presetFileName);

            if (null != inputStream) {
                Changelog loaded = JReleaserConfigLoader.load(Changelog.class, presetFileName, inputStream);

                if (isBlank(changelog.getFormat())) {
                    changelog.setFormat(loaded.getFormat());
                }

                Set<Changelog.Labeler> labelersCopy = new TreeSet<>(Changelog.Labeler.ORDER_COMPARATOR);
                labelersCopy.addAll(changelog.getLabelers());
                labelersCopy.addAll(loaded.getLabelers());
                changelog.setLabelers(labelersCopy);

                List<Changelog.Replacer> replacersCopy = new ArrayList<>(changelog.getReplacers());
                replacersCopy.addAll(loaded.getReplacers());
                changelog.setReplacers(replacersCopy);

                Map<String, List<Changelog.Category>> categoriesByKey = changelog.getCategories().stream()
                    .collect(groupingBy(Changelog.Category::getKey));
                Map<String, List<Changelog.Category>> loadedCategoriesByKey = loaded.getCategories().stream()
                    .collect(groupingBy(Changelog.Category::getKey));
                categoriesByKey.forEach((categoryKey, categories) -> {
                    if (loadedCategoriesByKey.containsKey(categoryKey)) {
                        Changelog.Category loadedCategory = loadedCategoriesByKey.remove(categoryKey).get(0);
                        Changelog.Category category = categories.get(0);
                        category.addLabels(loadedCategory.getLabels());
                    }
                });

                loadedCategoriesByKey.values().forEach(list -> changelog.getCategories().add(list.get(0)));
                // sort categories once again as order might have changed
                changelog.setCategories(Changelog.Category.sort(changelog.getCategories()));

                changelog.getHide().addCategories(loaded.getHide().getCategories());
                changelog.getHide().addContributors(loaded.getHide().getContributors());
            } else {
                context.getLogger().warn(RB.$("changelog.preset.not.found"), preset);
            }
        } catch (IOException e) {
            context.getLogger().warn(RB.$("ERROR_classpath_template_resolve"));
        }
    }
}
