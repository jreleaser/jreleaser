/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.jdks.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jreleaser.logging.SimpleJReleaserLoggerAdapter;
import org.jreleaser.util.Env;
import org.jreleaser.util.PlatformUtils;
import org.jreleaser.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
abstract class AbstractSetupMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    /**
     * Activates paths matching the current platform.
     */
    @Parameter(property = "jreleaser.select.current.platform")
    private Boolean selectCurrentPlatform;

    /**
     * Activates paths matching the given platform.
     */
    @Parameter(property = "jreleaser.select.platforms")
    private String[] selectPlatforms;

    /**
     * Activates paths not matching the given platform.
     */
    @Parameter(property = "jreleaser.reject.platforms")
    private String[] rejectedPlatforms;

    protected boolean isPlatformSelected(String platform) {
        if (isBlank(platform)) return true;

        List<String> selectedPlatforms = collectSelectedPlatforms();
        if (!selectedPlatforms.isEmpty()) {
            return selectedPlatforms.stream()
                .anyMatch(selected -> matchPlatform(selected, platform));
        }

        List<String> rejectedPlatforms = collectRejectedPlatforms();
        if (!rejectedPlatforms.isEmpty()) {
            return rejectedPlatforms.stream()
                .noneMatch(selected -> matchPlatform(selected, platform));
        }

        return true;
    }

    private List<String> collectSelectedPlatforms() {
        boolean resolvedSelectCurrentPlatform = resolveBoolean("SELECT_CURRENT_PLATFORM", selectCurrentPlatform);
        if (resolvedSelectCurrentPlatform) {
            PlatformUtils.resolveCurrentPlatform(new SimpleJReleaserLoggerAdapter());
            return Collections.singletonList(PlatformUtils.getCurrentFull());
        }

        List<String> list = new ArrayList<>();
        if (null != selectPlatforms && selectPlatforms.length > 0) {
            Collections.addAll(list, selectPlatforms);
        }
        return resolveCollection("SELECT_PLATFORMS", list);
    }

    private List<String> collectRejectedPlatforms() {
        List<String> list = new ArrayList<>();
        if (null != rejectedPlatforms && rejectedPlatforms.length > 0) {
            Collections.addAll(list, rejectedPlatforms);
        }
        return resolveCollection("REJECT_PLATFORMS", list);
    }

    private boolean resolveBoolean(String key, Boolean value) {
        if (null != value) return value;
        String resolvedValue = Env.resolve(key, "");
        return isNotBlank(resolvedValue) && Boolean.parseBoolean(resolvedValue);
    }

    private List<String> resolveCollection(String key, List<String> values) {
        if (!values.isEmpty()) return values;
        String resolvedValue = Env.resolve(key, "");
        if (isBlank(resolvedValue)) return Collections.emptyList();
        return Arrays.stream(resolvedValue.trim().split(","))
            .map(String::trim)
            .filter(StringUtils::isNotBlank)
            .collect(toList());
    }

    private boolean matchPlatform(String input, String target) {
        input = input.replace("aarch_64", "aarch64");
        return PlatformUtils.isCompatible(input, target);
    }
}
