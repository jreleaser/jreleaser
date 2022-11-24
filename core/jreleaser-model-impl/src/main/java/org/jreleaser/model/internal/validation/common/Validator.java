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
package org.jreleaser.model.internal.validation.common;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.assemble.Assembler;
import org.jreleaser.model.internal.common.CommitAuthor;
import org.jreleaser.model.internal.common.CommitAuthorAware;
import org.jreleaser.model.internal.common.FileSet;
import org.jreleaser.model.internal.common.Glob;
import org.jreleaser.model.internal.common.Icon;
import org.jreleaser.model.internal.common.OwnerAware;
import org.jreleaser.model.internal.common.Screenshot;
import org.jreleaser.model.internal.common.TimeoutAware;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.environment.Environment;
import org.jreleaser.model.internal.packagers.Packager;
import org.jreleaser.model.internal.packagers.RepositoryTap;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import java.util.Collection;
import java.util.List;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Validator {
    public static String checkProperty(JReleaserContext context, String key, String property, String value, Errors errors) {
        if (isNotBlank(value)) return value;
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        return Env.check(key, environment.getVariable(key), property, dsl, configFilePath, errors);
    }

    public static String checkProperty(JReleaserContext context, String key, String property, String value, Errors errors, boolean dryrun) {
        if (isNotBlank(value)) return value;
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        return Env.check(key, environment.getVariable(key), property, dsl, configFilePath, dryrun ? new Errors() : errors);
    }

    public static Integer checkProperty(JReleaserContext context, String key, String property, Integer value, Errors errors, boolean dryrun) {
        if (null != value) return value;
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        String val = Env.check(key, environment.getVariable(key), property, dsl, configFilePath, dryrun ? new Errors() : errors);
        return isNotBlank(val) ? Integer.parseInt(val) : null;
    }

    public static String checkProperty(JReleaserContext context, String key, String property, String value, String defaultValue) {
        if (isNotBlank(value)) return value;
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        Errors errors = new Errors();
        String result = Env.check(key, environment.getVariable(key), property, dsl, configFilePath, errors);
        return !errors.hasErrors() ? result : defaultValue;
    }

    public static boolean checkProperty(JReleaserContext context, String key, String property, Boolean value, boolean defaultValue) {
        if (null != value) return value;
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        Errors errors = new Errors();
        String result = Env.check(key, environment.getVariable(key), property, dsl, configFilePath, errors);
        return !errors.hasErrors() ? Boolean.parseBoolean(result) : defaultValue;
    }

    public static <T extends Enum<T>> String checkProperty(JReleaserContext context, String key, String property, T value, T defaultValue) {
        if (null != value) return value.name();
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        Errors errors = new Errors();
        String result = Env.check(key, environment.getVariable(key), property, dsl, configFilePath, errors);
        return !errors.hasErrors() ? result : (null != defaultValue ? defaultValue.name() : null);
    }

    public static String checkProperty(JReleaserContext context, Collection<String> keys, String property, String value, Errors errors) {
        if (isNotBlank(value)) return value;
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        return Env.check(keys, environment.getVars(), property, dsl, configFilePath, errors);
    }

    public static String checkProperty(JReleaserContext context, Collection<String> keys, String property, String value, Errors errors, boolean dryrun) {
        if (isNotBlank(value)) return value;
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        return Env.check(keys, environment.getVars(), property, dsl, configFilePath, dryrun ? new Errors() : errors);
    }

    public static Integer checkProperty(JReleaserContext context, Collection<String> keys, String property, Integer value, Errors errors, boolean dryrun) {
        if (null != value) return value;
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        String val = Env.check(keys, environment.getVars(), property, dsl, configFilePath, dryrun ? new Errors() : errors);
        return isNotBlank(val) ? Integer.parseInt(val) : null;
    }

    public static String checkProperty(JReleaserContext context, Collection<String> keys, String property, String value, String defaultValue) {
        if (isNotBlank(value)) return value;
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        Errors errors = new Errors();
        String result = Env.check(keys, environment.getVars(), property, dsl, configFilePath, errors);
        return !errors.hasErrors() ? result : defaultValue;
    }

    public static boolean checkProperty(JReleaserContext context, Collection<String> keys, String property, Boolean value, boolean defaultValue) {
        if (null != value) return value;
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        Errors errors = new Errors();
        String result = Env.check(keys, environment.getVars(), property, dsl, configFilePath, errors);
        return !errors.hasErrors() ? Boolean.parseBoolean(result) : defaultValue;
    }

    public static <T extends Enum<T>> String checkProperty(JReleaserContext context, Collection<String> keys, String property, T value, T defaultValue) {
        if (null != value) return value.name();
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        Errors errors = new Errors();
        String result = Env.check(keys, environment.getVars(), property, dsl, configFilePath, errors);
        return !errors.hasErrors() ? result : (null != defaultValue ? defaultValue.name() : null);
    }

    public static void validateOwner(OwnerAware self, OwnerAware other) {
        if (isBlank(self.getOwner())) self.setOwner(other.getOwner());
    }

    public static void validateContinueOnError(Packager self, Packager other) {
        if (!self.isContinueOnErrorSet()) {
            self.setContinueOnError(other.isContinueOnError());
        }
    }

    public static void validateCommitAuthor(CommitAuthorAware self, CommitAuthorAware other) {
        CommitAuthor author = new CommitAuthor();
        author.setName(self.getCommitAuthor().getName());
        author.setEmail(self.getCommitAuthor().getEmail());
        if (isBlank(author.getName())) author.setName(other.getCommitAuthor().getName());
        if (isBlank(author.getEmail())) author.setEmail(other.getCommitAuthor().getEmail());
        self.setCommitAuthor(author);
    }

    public static void validateTimeout(TimeoutAware self) {
        if (null == self.getConnectTimeout() || (self.getConnectTimeout() <= 0 || self.getConnectTimeout() > 300)) {
            self.setConnectTimeout(20);
        }
        if (null == self.getReadTimeout() || (self.getReadTimeout() <= 0 || self.getReadTimeout() > 300)) {
            self.setReadTimeout(60);
        }
    }

    public static void validateTap(JReleaserContext context, Distribution distribution,
                            RepositoryTap tap, RepositoryTap parentTap, String property) {
        validateOwner(tap, parentTap);

        if (isBlank(tap.getCommitMessage()) && isNotBlank(parentTap.getCommitMessage())) {
            tap.setCommitMessage(parentTap.getCommitMessage());
        }
        if (isBlank(tap.getCommitMessage())) {
            tap.setCommitMessage("{{distributionName}} {{tagName}}");
        }
        if (isBlank(tap.getTagName()) && isNotBlank(parentTap.getTagName())) {
            tap.setTagName(parentTap.getTagName());
        }
        if (isBlank(tap.getBranch()) && isNotBlank(parentTap.getBranch())) {
            tap.setBranch(parentTap.getBranch());
        }
        if (isBlank(tap.getName()) && isNotBlank(parentTap.getName())) {
            tap.setName(parentTap.getName());
        }
        if (isBlank(tap.getUsername()) && isNotBlank(parentTap.getUsername())) {
            tap.setUsername(parentTap.getUsername());
        }
        if (isBlank(tap.getToken()) && isNotBlank(parentTap.getToken())) {
            tap.setToken(parentTap.getToken());
        }

        BaseReleaser service = context.getModel().getRelease().getReleaser();

        tap.setUsername(
            checkProperty(context,
                Env.toVar(tap.getBasename() + "_" + service.getServiceName()) + "_USERNAME",
                "distributions." + distribution.getName() + "." + property + ".username",
                tap.getUsername(),
                service.getUsername()));

        tap.setToken(
            checkProperty(context,
                Env.toVar(tap.getBasename() + "_" + service.getServiceName()) + "_TOKEN",
                "distributions." + distribution.getName() + "." + property + ".token",
                tap.getToken(),
                service.getToken()));

        tap.setBranch(
            checkProperty(context,
                Env.toVar(tap.getBasename() + "_" + service.getServiceName()) + "_BRANCH",
                "distributions." + distribution.getName() + "." + property + ".branch",
                tap.getBranch(),
                "HEAD"));
    }

    public static void validateGlobs(JReleaserContext context, Collection<Glob> globs, String property, Errors errors) {
        int i = 0;
        for (Glob glob : globs) {
            if (isBlank(glob.getPattern())) {
                errors.configuration(RB.$("validation_must_define_pattern", property + "[" + i + "]"));
            }
        }
    }

    public static void validateFileSet(JReleaserContext context, Mode mode, Assembler assembler, FileSet fileSet, int index, Errors errors) {
        if (mode.validateStandalone() && isBlank(fileSet.getInput())) {
            errors.configuration(RB.$("validation_must_not_be_null", assembler.getType() + "." + assembler.getName() + ".fileSet[" + index + "].input"));
        }
    }

    public static void validateScreenshots(JReleaserContext context, Mode mode, List<Screenshot> screenshots, Errors errors, String base) {
        if (screenshots.size() == 1) {
            screenshots.get(0).setPrimary(true);
        }

        if (screenshots.stream()
            .mapToInt(s -> s.isPrimary() ? 1 : 0)
            .sum() > 1) {
            errors.configuration(RB.$("validation_multiple_primary_screenshots", base));
        }

        for (int i = 0; i < screenshots.size(); i++) {
            Screenshot screenshot = screenshots.get(i);
            if (isBlank(screenshot.getUrl())) {
                errors.configuration(RB.$("validation_must_not_be_blank", base + ".screenshots[" + i + "].url"));
            }

            if (screenshot.getType() == org.jreleaser.model.Screenshot.Type.THUMBNAIL) {
                if (null == screenshot.getWidth()) {
                    errors.configuration(RB.$("validation_must_not_be_null", base + ".screenshots[" + i + "].width"));
                }
                if (null == screenshot.getHeight()) {
                    errors.configuration(RB.$("validation_must_not_be_null", base + ".screenshots[" + i + "].height"));
                }
            } else {
                if (null == screenshot.getWidth() && null != screenshot.getHeight()) {
                    errors.configuration(RB.$("validation_must_not_be_null", base + ".screenshots[" + i + "].width"));
                } else if (null != screenshot.getWidth() && null == screenshot.getHeight()) {
                    errors.configuration(RB.$("validation_must_not_be_null", base + ".screenshots[" + i + "].height"));
                }
            }
        }
    }

    public static void validateIcons(JReleaserContext context, Mode mode, List<Icon> icons, Errors errors, String base) {
        validateIcons(context, mode, icons, errors, base, true);
    }

    public static void validateIcons(JReleaserContext context, Mode mode, List<Icon> icons, Errors errors, String base, boolean validatePrimary) {
        if (validatePrimary) {
            if (icons.size() == 1) {
                icons.get(0).setPrimary(true);
            }

            if (icons.stream()
                .mapToInt(s -> s.isPrimary() ? 1 : 0)
                .sum() > 1) {
                errors.configuration(RB.$("validation_multiple_primary_icons", base));
            }
        }

        for (int i = 0; i < icons.size(); i++) {
            Icon icon = icons.get(i);
            if (isBlank(icon.getUrl())) {
                errors.configuration(RB.$("validation_must_not_be_blank", base + ".icons[" + i + "].url"));
            }
            if (null == icon.getWidth()) {
                errors.configuration(RB.$("validation_must_not_be_null", base + ".icons[" + i + "].width"));
            }
            if (null == icon.getHeight()) {
                errors.configuration(RB.$("validation_must_not_be_null", base + ".icons[" + i + "].height"));
            }
        }
    }
}
