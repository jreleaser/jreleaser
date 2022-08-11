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
import org.jreleaser.model.Assembler;
import org.jreleaser.model.CommitAuthor;
import org.jreleaser.model.CommitAuthorAware;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.Environment;
import org.jreleaser.model.FileSet;
import org.jreleaser.model.GitService;
import org.jreleaser.model.Glob;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.OwnerAware;
import org.jreleaser.model.Packager;
import org.jreleaser.model.RepositoryTap;
import org.jreleaser.model.Screenshot;
import org.jreleaser.model.TimeoutAware;
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
class Validator {
    static String checkProperty(JReleaserContext context, String key, String property, String value, Errors errors) {
        if (isNotBlank(value)) return value;
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        return Env.check(key, environment.getVariable(key), property, dsl, configFilePath, errors);
    }

    static String checkProperty(JReleaserContext context, String key, String property, String value, Errors errors, boolean dryrun) {
        if (isNotBlank(value)) return value;
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        return Env.check(key, environment.getVariable(key), property, dsl, configFilePath, dryrun ? new Errors() : errors);
    }

    static Integer checkProperty(JReleaserContext context, String key, String property, Integer value, Errors errors, boolean dryrun) {
        if (null != value) return value;
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        String val = Env.check(key, environment.getVariable(key), property, dsl, configFilePath, dryrun ? new Errors() : errors);
        return isNotBlank(val) ? Integer.parseInt(val) : null;
    }

    static String checkProperty(JReleaserContext context, String key, String property, String value, String defaultValue) {
        if (isNotBlank(value)) return value;
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        Errors errors = new Errors();
        String result = Env.check(key, environment.getVariable(key), property, dsl, configFilePath, errors);
        return !errors.hasErrors() ? result : defaultValue;
    }

    static boolean checkProperty(JReleaserContext context, String key, String property, Boolean value, boolean defaultValue) {
        if (null != value) return value;
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        Errors errors = new Errors();
        String result = Env.check(key, environment.getVariable(key), property, dsl, configFilePath, errors);
        return !errors.hasErrors() ? Boolean.parseBoolean(result) : defaultValue;
    }

    static <T extends Enum<T>> String checkProperty(JReleaserContext context, String key, String property, T value, T defaultValue) {
        if (null != value) return value.name();
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        Errors errors = new Errors();
        String result = Env.check(key, environment.getVariable(key), property, dsl, configFilePath, errors);
        return !errors.hasErrors() ? result : (null != defaultValue ? defaultValue.name() : null);
    }

    static String checkProperty(JReleaserContext context, Collection<String> keys, String property, String value, Errors errors) {
        if (isNotBlank(value)) return value;
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        return Env.check(keys, environment.getVars(), property, dsl, configFilePath, errors);
    }

    static String checkProperty(JReleaserContext context, Collection<String> keys, String property, String value, Errors errors, boolean dryrun) {
        if (isNotBlank(value)) return value;
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        return Env.check(keys, environment.getVars(), property, dsl, configFilePath, dryrun ? new Errors() : errors);
    }

    static Integer checkProperty(JReleaserContext context, Collection<String> keys, String property, Integer value, Errors errors, boolean dryrun) {
        if (null != value) return value;
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        String val = Env.check(keys, environment.getVars(), property, dsl, configFilePath, dryrun ? new Errors() : errors);
        return isNotBlank(val) ? Integer.parseInt(val) : null;
    }

    static String checkProperty(JReleaserContext context, Collection<String> keys, String property, String value, String defaultValue) {
        if (isNotBlank(value)) return value;
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        Errors errors = new Errors();
        String result = Env.check(keys, environment.getVars(), property, dsl, configFilePath, errors);
        return !errors.hasErrors() ? result : defaultValue;
    }

    static boolean checkProperty(JReleaserContext context, Collection<String> keys, String property, Boolean value, boolean defaultValue) {
        if (null != value) return value;
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        Errors errors = new Errors();
        String result = Env.check(keys, environment.getVars(), property, dsl, configFilePath, errors);
        return !errors.hasErrors() ? Boolean.parseBoolean(result) : defaultValue;
    }

    static <T extends Enum<T>> String checkProperty(JReleaserContext context, Collection<String> keys, String property, T value, T defaultValue) {
        if (null != value) return value.name();
        Environment environment = context.getModel().getEnvironment();
        String dsl = context.getConfigurer().toString();
        String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
        Errors errors = new Errors();
        String result = Env.check(keys, environment.getVars(), property, dsl, configFilePath, errors);
        return !errors.hasErrors() ? result : (null != defaultValue ? defaultValue.name() : null);
    }

    static void validateOwner(OwnerAware self, OwnerAware other) {
        if (isBlank(self.getOwner())) self.setOwner(other.getOwner());
    }

    static void validateContinueOnError(Packager self, Packager other) {
        if (!self.isContinueOnErrorSet()) {
            self.setContinueOnError(other.isContinueOnError());
        }
    }

    static void validateCommitAuthor(CommitAuthorAware self, CommitAuthorAware other) {
        CommitAuthor author = new CommitAuthor();
        author.setName(self.getCommitAuthor().getName());
        author.setEmail(self.getCommitAuthor().getEmail());
        if (isBlank(author.getName())) author.setName(other.getCommitAuthor().getName());
        if (isBlank(author.getEmail())) author.setEmail(other.getCommitAuthor().getEmail());
        self.setCommitAuthor(author);
    }

    static void validateTimeout(TimeoutAware self) {
        if (null == self.getConnectTimeout() || (self.getConnectTimeout() <= 0 || self.getConnectTimeout() > 300)) {
            self.setConnectTimeout(20);
        }
        if (null == self.getReadTimeout() || (self.getReadTimeout() <= 0 || self.getReadTimeout() > 300)) {
            self.setReadTimeout(60);
        }
    }

    static void validateTap(JReleaserContext context, Distribution distribution,
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

        GitService service = context.getModel().getRelease().getGitService();

        tap.setUsername(
            checkProperty(context,
                Env.toVar(tap.getBasename() + "_" + service.getServiceName()) + "_USERNAME",
                "distribution." + distribution.getName() + "." + property + ".username",
                tap.getUsername(),
                service.getResolvedUsername()));

        tap.setToken(
            checkProperty(context,
                Env.toVar(tap.getBasename() + "_" + service.getServiceName()) + "_TOKEN",
                "distribution." + distribution.getName() + "." + property + ".token",
                tap.getToken(),
                service.getResolvedToken()));

        tap.setBranch(
            checkProperty(context,
                Env.toVar(tap.getBasename() + "_" + service.getServiceName()) + "_BRANCH",
                "distribution." + distribution.getName() + "." + property + ".branch",
                tap.getBranch(),
                "HEAD"));
    }

    static void validateGlobs(JReleaserContext context, Collection<Glob> globs, String property, Errors errors) {
        int i = 0;
        for (Glob glob : globs) {
            if (isBlank(glob.getPattern())) {
                errors.configuration(RB.$("validation_must_define_pattern", property + "[" + i + "]"));
            }
        }
    }

    static void validateFileSet(JReleaserContext context, JReleaserContext.Mode mode, Assembler assembler, FileSet fileSet, int index, Errors errors) {
        if (mode.validateStandalone() && isBlank(fileSet.getInput())) {
            errors.configuration(RB.$("validation_must_not_be_null", assembler.getType() + "." + assembler.getName() + ".fileSet[" + index + "].input"));
        }
    }

    static void validateScreenshots(JReleaserContext context, JReleaserContext.Mode mode, List<Screenshot> screenshots, Errors errors, String base) {
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

            if (screenshot.getType() == Screenshot.Type.THUMBNAIL) {
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
}
