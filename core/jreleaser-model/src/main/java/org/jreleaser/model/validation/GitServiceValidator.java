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
package org.jreleaser.model.validation;

import org.jreleaser.bundle.RB;
import org.jreleaser.config.JReleaserConfigLoader;
import org.jreleaser.model.Active;
import org.jreleaser.model.Changelog;
import org.jreleaser.model.GenericGit;
import org.jreleaser.model.GitService;
import org.jreleaser.model.Github;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Project;
import org.jreleaser.model.Signing;
import org.jreleaser.model.UpdateSection;
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
import static org.jreleaser.model.GitService.BRANCH;
import static org.jreleaser.model.GitService.Milestone.MILESTONE_NAME;
import static org.jreleaser.model.GitService.OVERWRITE;
import static org.jreleaser.model.GitService.RELEASE_NAME;
import static org.jreleaser.model.GitService.SKIP_RELEASE;
import static org.jreleaser.model.GitService.SKIP_TAG;
import static org.jreleaser.model.GitService.TAG_NAME;
import static org.jreleaser.model.GitService.UPDATE;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class GitServiceValidator extends Validator {
    private static final String DEFAULT_CHANGELOG_TPL = "src/jreleaser/templates/changelog.tpl";

    public static void validateGitService(JReleaserContext context, JReleaserContext.Mode mode, GitService service, Errors errors) {
        JReleaserModel model = context.getModel();
        Project project = model.getProject();

        if (!service.isEnabledSet()) {
            service.setEnabled(true);
        }

        if (!service.isEnabled()) {
            return;
        }

        if (!mode.validateStandalone()) {
            if (isBlank(service.getOwner()) && !(service instanceof GenericGit)) {
                errors.configuration(RB.$("validation_must_not_be_blank", service.getServiceName() + ".owner"));
            }
        }

        if (isBlank(service.getName())) {
            service.setName(project.getName());
        }

        service.setUsername(
            checkProperty(context,
                service.getServiceName().toUpperCase(Locale.ENGLISH) + "_USERNAME",
                service.getServiceName() + ".username",
                service.getUsername(),
                service.getOwner()));

        service.setToken(
            checkProperty(context,
                service.getServiceName().toUpperCase(Locale.ENGLISH) + "_TOKEN",
                service.getServiceName() + ".token",
                service.getToken(),
                !mode.validateStandalone() ? errors : new Errors()));

        service.setTagName(
            checkProperty(context,
                TAG_NAME,
                service.getServiceName() + ".tagName",
                service.getTagName(),
                "v{{projectVersion}}"));

        if (service.isReleaseSupported()) {
            service.setReleaseName(
                checkProperty(context,
                    RELEASE_NAME,
                    service.getServiceName() + ".releaseName",
                    service.getReleaseName(),
                    "Release {{tagName}}"));
        }

        service.setBranch(
            checkProperty(context,
                BRANCH,
                service.getServiceName() + ".branch",
                service.getBranch(),
                "main"));

        if (!service.isOverwriteSet()) {
            service.setOverwrite(
                checkProperty(context,
                    OVERWRITE,
                    service.getServiceName() + ".overwrite",
                    null,
                    false));
        }

        if (service.isReleaseSupported()) {
            if (!service.getUpdate().isEnabledSet()) {
                service.getUpdate().setEnabled(
                    checkProperty(context,
                        UPDATE,
                        service.getServiceName() + ".update",
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
                    service.getServiceName() + ".skipTag",
                    null,
                    false));
        }

        if (!service.isSkipReleaseSet()) {
            service.setSkipRelease(
                checkProperty(context,
                    SKIP_RELEASE,
                    service.getServiceName() + ".skipRelease",
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
        }
        if (isBlank(service.getCommitAuthor().getName())) {
            service.getCommitAuthor().setName("jreleaserbot");
        }
        if (isBlank(service.getCommitAuthor().getEmail())) {
            service.getCommitAuthor().setEmail("jreleaser@kordamp.org");
        }

        validateTimeout(service);

        if (service.isReleaseSupported()) {
            // milestone
            service.getMilestone().setName(
                checkProperty(context,
                    MILESTONE_NAME,
                    service.getServiceName() + ".milestone.name",
                    service.getMilestone().getName(),
                    "{{tagName}}"));
        }

        // eager resolve
        service.getResolvedTagName(context.getModel());
        if (service.isReleaseSupported()) {
            service.getResolvedReleaseName(context.getModel());
            service.getMilestone().getResolvedName(service.props(context.getModel()));
        }

        if (project.isSnapshot()) {
            boolean generate = false;
            if (service instanceof Github) {
                Github gh = (Github) service;
                generate = gh.getReleaseNotes().isEnabled();
            }

            if (!generate) {
                service.getChangelog().setEnabled(true);
                service.getChangelog().setExternal(null);
                service.getChangelog().setSort(Changelog.Sort.DESC);
            }
            if (service.isReleaseSupported()) {
                service.setOverwrite(true);
            }
        }

        if (!service.getChangelog().isEnabledSet()) {
            boolean generate = false;
            if (service instanceof Github) {
                Github gh = (Github) service;
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
            if (service.isSign()) {
                if (model.getSigning().getMode() == Signing.Mode.COSIGN) {
                    service.setSign(false);
                    errors.warning(RB.$("validation_git_signing_cosign", service.getServiceName()));
                    return;
                }
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

    private static void validateChangelog(JReleaserContext context, GitService service, Errors errors) {
        Changelog changelog = service.getChangelog();

        if (isNotBlank(changelog.getExternal())) {
            changelog.setEnabled(true);
            changelog.setFormatted(Active.NEVER);
        }

        if(!changelog.isEnabledSet() && changelog.isSet()) {
            changelog.setEnabled(true);
        }

        // Special case for GitHub
        if (service instanceof Github) {
            Github gh = (Github) service;
            boolean generate = gh.getReleaseNotes().isEnabled();

            if (generate && changelog.isEnabled()) {
                errors.configuration(RB.$("validation_github_releasenotes_changelog"));
                return;
            }
        }

        if (!changelog.resolveFormatted(context.getModel().getProject())) return;

        if (null == changelog.getSort()) {
            changelog.setSort(Changelog.Sort.DESC);
        }

        if (isBlank(changelog.getFormat())) {
            changelog.setFormat("- {{commitShortHash}} {{commitTitle}} ({{commitAuthor}})");
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
                if (isBlank(labeler.getTitle()) && isBlank(labeler.getBody())) {
                    errors.configuration(RB.$("validation_git_required", service.getServiceName() + ".changelog.labelers[" + i + "] title", "body"));
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
    }

    private static void loadPreset(JReleaserContext context, Changelog changelog, Errors errors) {
        try {
            String preset = changelog.getPreset().toLowerCase(Locale.ENGLISH).trim();
            String presetFileName = "META-INF/jreleaser/changelog/preset-" + preset + ".yml";

            InputStream inputStream = GitServiceValidator.class.getClassLoader()
                .getResourceAsStream(presetFileName);

            if (null != inputStream) {
                Changelog loaded = JReleaserConfigLoader.load(Changelog.class, presetFileName, inputStream);

                Set<Changelog.Labeler> labelersCopy = new TreeSet<>(Changelog.Labeler.ORDER);
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
