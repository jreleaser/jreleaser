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
package org.jreleaser.model.validation;

import org.jreleaser.bundle.RB;
import org.jreleaser.config.JReleaserConfigLoader;
import org.jreleaser.model.Active;
import org.jreleaser.model.Changelog;
import org.jreleaser.model.GenericGit;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Project;
import org.jreleaser.model.UpdateSection;
import org.jreleaser.util.Errors;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.groupingBy;
import static org.jreleaser.model.GitService.BRANCH;
import static org.jreleaser.model.GitService.OVERWRITE;
import static org.jreleaser.model.GitService.RELEASE_NAME;
import static org.jreleaser.model.GitService.SKIP_RELEASE;
import static org.jreleaser.model.GitService.SKIP_TAG;
import static org.jreleaser.model.GitService.TAG_NAME;
import static org.jreleaser.model.GitService.UPDATE;
import static org.jreleaser.model.Milestone.MILESTONE_NAME;
import static org.jreleaser.util.ResourceUtils.resolveLocation;
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

        if (mode != JReleaserContext.Mode.ASSEMBLE) {
            if (isBlank(service.getOwner()) && !(service instanceof GenericGit)) {
                errors.configuration(RB.$("validation_must_not_be_blank", service.getServiceName() + ".owner"));
            }
        }

        if (isBlank(service.getName())) {
            service.setName(project.getName());
        }

        service.setUsername(
            checkProperty(context,
                service.getServiceName().toUpperCase() + "_USERNAME",
                service.getServiceName() + ".username",
                service.getUsername(),
                service.getOwner()));

        service.setToken(
            checkProperty(context,
                service.getServiceName().toUpperCase() + "_TOKEN",
                service.getServiceName() + ".token",
                service.getToken(),
                mode != JReleaserContext.Mode.ASSEMBLE? errors: new Errors()));

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
            if (!service.isUpdateSet()) {
                service.setUpdate(
                    checkProperty(context,
                        UPDATE,
                        service.getServiceName() + ".update",
                        null,
                        false));
            }

            if (service.isUpdate() && service.getUpdateSections().isEmpty()) {
                service.getUpdateSections().add(UpdateSection.ASSETS);
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
        if (!service.getChangelog().isEnabledSet()) {
            service.getChangelog().setEnabled(true);
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
            service.getChangelog().setEnabled(true);
            service.getChangelog().setExternal(null);
            service.getChangelog().setSort(Changelog.Sort.DESC);
            if (service.isReleaseSupported()) {
                service.setOverwrite(true);
            }
        }

        if (mode != JReleaserContext.Mode.ASSEMBLE) {
            if (service.isSign() && !model.getSigning().isEnabled()) {
                if (context.isDryrun()) {
                    service.setSign(false);
                } else {
                    errors.configuration(RB.$("validation_git_signing", service.getServiceName()));
                }
            }

            validateChangelog(context, service, errors);
        }
    }

    private static void validateChangelog(JReleaserContext context, GitService service, Errors errors) {
        Changelog changelog = service.getChangelog();

        if (isNotBlank(changelog.getExternal())) {
            changelog.setFormatted(Active.NEVER);
        }

        if (!changelog.resolveFormatted(context.getModel().getProject())) return;

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
            changelog.getCategories().add(Changelog.Category.of(RB.$("default_category_feature"), "", "feature", "enhancement"));
            changelog.getCategories().add(Changelog.Category.of(RB.$("default_category_bug_fix"), "", "bug", "fix"));
        } else {
            int i = 0;
            for (Changelog.Category category : changelog.getCategories()) {
                if (isBlank(category.getTitle())) {
                    errors.configuration(RB.$("validation_is_missing", service.getServiceName() + ".changelog.categories[" + i + "].title"));
                }
                if (category.getLabels().isEmpty()) {
                    errors.configuration(RB.$("validation_ares_missing", service.getServiceName() + ".changelog.categories[" + i + "].labels"));
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
        URL location = resolveLocation(Changelog.class);

        if (null == location) {
            context.getLogger().warn(RB.$("ERROR_classpath_template_resolve"));
            return;
        }

        try {
            if ("file".equals(location.getProtocol())) {
                String preset = changelog.getPreset().toLowerCase().trim();
                String presetFileName = "META-INF/jreleaser/changelog/preset-" + preset + ".yml";

                boolean found = false;
                JarFile jarFile = new JarFile(new File(location.toURI()));
                for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); ) {
                    JarEntry entry = e.nextElement();
                    if (entry.isDirectory() || !entry.getName().equals(presetFileName)) {
                        continue;
                    }

                    Changelog loaded = JReleaserConfigLoader.load(Changelog.class, presetFileName, jarFile.getInputStream(entry));

                    LinkedHashSet<Changelog.Labeler> labelersCopy = new LinkedHashSet<>(changelog.getLabelers());
                    labelersCopy.addAll(loaded.getLabelers());
                    changelog.setLabelers(labelersCopy);

                    List<Changelog.Replacer> replacersCopy = new ArrayList<>(changelog.getReplacers());
                    replacersCopy.addAll(loaded.getReplacers());
                    changelog.setReplacers(replacersCopy);

                    Map<String, List<Changelog.Category>> categoriesByTitle = changelog.getCategories().stream()
                        .collect(groupingBy(Changelog.Category::getTitle));
                    Map<String, List<Changelog.Category>> loadedCategoriesByTitle = loaded.getCategories().stream()
                        .collect(groupingBy(Changelog.Category::getTitle));
                    categoriesByTitle.forEach((categoryTitle, categories) -> {
                        if (loadedCategoriesByTitle.containsKey(categoryTitle)) {
                            Changelog.Category loadedCategory = loadedCategoriesByTitle.remove(categoryTitle).get(0);
                            categories.get(0).addLabels(loadedCategory.getLabels());
                        }
                    });

                    loaded.getCategories().forEach(category -> {
                        if (loadedCategoriesByTitle.containsKey(category.getTitle())) {
                            changelog.getCategories().add(category);
                        }
                    });

                    changelog.getHide().addCategories(loaded.getHide().getCategories());
                    changelog.getHide().addContributors(loaded.getHide().getContributors());

                    found = true;
                    break;
                }

                if (!found) {
                    context.getLogger().warn(RB.$("changelog.preset.not.found"), preset);
                }
            } else {
                context.getLogger().warn(RB.$("ERROR_classpath_template_resolve"));
            }
        } catch (URISyntaxException | IOException e) {
            context.getLogger().warn(RB.$("ERROR_classpath_template_resolve"));
        }
    }
}
