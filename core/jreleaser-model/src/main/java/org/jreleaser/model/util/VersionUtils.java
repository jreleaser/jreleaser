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
package org.jreleaser.model.util;

import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.CalVer;
import org.jreleaser.util.ChronVer;
import org.jreleaser.util.CustomVersion;
import org.jreleaser.util.JavaModuleVersion;
import org.jreleaser.util.JavaRuntimeVersion;
import org.jreleaser.util.SemVer;
import org.jreleaser.util.Version;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public class VersionUtils {
    private final static UnparseableTags unparseableTags = new UnparseableTags();

    public static Pattern resolveVersionPattern(JReleaserContext context) {
        GitService gitService = context.getModel().getRelease().getGitService();
        String tagName = gitService.getConfiguredTagName();
        Pattern vp = Pattern.compile(tagName.replaceAll("\\{\\{.*}}", "\\(\\.\\*\\)"));
        if (!tagName.contains("{{")) {
            vp = Pattern.compile("(.*)");
        }

        return vp;
    }

    public static void clearUnparseableTags() {
        unparseableTags.clear();
    }

    public static Version version(JReleaserContext context, String tagName, Pattern versionPattern) {
        return version(context, tagName, versionPattern, false);
    }

    public static Version version(JReleaserContext context, String tagName, Pattern versionPattern, boolean strict) {
        switch (context.getModel().getProject().versionPattern().getType()) {
            case SEMVER:
                return semverOf(context, tagName, versionPattern, strict);
            case JAVA_RUNTIME:
                return javaRuntimeVersionOf(context, tagName, versionPattern, strict);
            case JAVA_MODULE:
                return javaModuleVersionOf(context, tagName, versionPattern, strict);
            case CALVER:
                return calverOf(context, tagName, versionPattern, strict);
            case CHRONVER:
                return chronVer(context, tagName, versionPattern, strict);
            case CUSTOM:
            default:
                return versionOf(tagName, versionPattern, strict);
        }
    }

    public static Version defaultVersion(JReleaserContext context) {
        switch (context.getModel().getProject().versionPattern().getType()) {
            case SEMVER:
                return SemVer.defaultOf();
            case JAVA_RUNTIME:
                return JavaRuntimeVersion.defaultOf();
            case JAVA_MODULE:
                return JavaModuleVersion.defaultOf();
            case CALVER:
                String format = context.getModel().getProject().versionPattern().getFormat();
                return CalVer.defaultOf(format);
            case CHRONVER:
                return ChronVer.defaultOf();
            case CUSTOM:
            default:
                return CustomVersion.defaultOf();
        }
    }

    private static SemVer semverOf(JReleaserContext context, String tagName, Pattern versionPattern, boolean strict) {
        Matcher matcher = versionPattern.matcher(tagName);
        if (matcher.matches()) {
            String tag = matcher.group(1);
            try {
                return SemVer.of(tag);
            } catch (IllegalArgumentException e) {
                unparseableTags.unparseable(context, tag, e);
            }
        }

        if (!strict && tagName.startsWith("v")) {
            String tag = tagName.substring(1);
            try {
                return SemVer.of(tag);
            } catch (IllegalArgumentException e) {
                unparseableTags.unparseable(context, tag, e);
            }
        }

        return SemVer.defaultOf();
    }

    private static JavaRuntimeVersion javaRuntimeVersionOf(JReleaserContext context, String tagName, Pattern versionPattern, boolean strict) {
        Matcher matcher = versionPattern.matcher(tagName);
        if (matcher.matches()) {
            String tag = matcher.group(1);
            try {
                return JavaRuntimeVersion.of(tag);
            } catch (IllegalArgumentException e) {
                unparseableTags.unparseable(context, tag, e);
            }
        }

        if (!strict && tagName.startsWith("v")) {
            String tag = tagName.substring(1);
            try {
                return JavaRuntimeVersion.of(tag);
            } catch (IllegalArgumentException e) {
                unparseableTags.unparseable(context, tag, e);
            }
        }

        return JavaRuntimeVersion.defaultOf();
    }

    private static JavaModuleVersion javaModuleVersionOf(JReleaserContext context, String tagName, Pattern versionPattern, boolean strict) {
        Matcher matcher = versionPattern.matcher(tagName);
        if (matcher.matches()) {
            String tag = matcher.group(1);
            try {
                return JavaModuleVersion.of(tag);
            } catch (IllegalArgumentException e) {
                unparseableTags.unparseable(context, tag, e);
            }
        }

        if (!strict && tagName.startsWith("v")) {
            String tag = tagName.substring(1);
            try {
                return JavaModuleVersion.of(tag);
            } catch (IllegalArgumentException e) {
                unparseableTags.unparseable(context, tag, e);
            }
        }

        return JavaModuleVersion.defaultOf();
    }

    private static CalVer calverOf(JReleaserContext context, String tagName, Pattern versionPattern, boolean strict) {
        String format = context.getModel().getProject().versionPattern().getFormat();
        Matcher matcher = versionPattern.matcher(tagName);
        if (matcher.matches()) {
            String tag = matcher.group(1);
            try {
                return CalVer.of(format, tag);
            } catch (IllegalArgumentException e) {
                unparseableTags.unparseable(context, tag, e);
            }
        }

        if (!strict && tagName.startsWith("v")) {
            String tag = tagName.substring(1);
            try {
                return CalVer.of(format, tag);
            } catch (IllegalArgumentException e) {
                unparseableTags.unparseable(context, tag, e);
            }
        }

        return CalVer.defaultOf(format);
    }

    private static ChronVer chronVer(JReleaserContext context, String tagName, Pattern versionPattern, boolean strict) {
        Matcher matcher = versionPattern.matcher(tagName);
        if (matcher.matches()) {
            String tag = matcher.group(1);
            try {
                return ChronVer.of(tag);
            } catch (IllegalArgumentException e) {
                unparseableTags.unparseable(context, tag, e);
            }
        }

        if (!strict && tagName.startsWith("v")) {
            String tag = tagName.substring(1);
            try {
                return ChronVer.of(tag);
            } catch (IllegalArgumentException e) {
                unparseableTags.unparseable(context, tag, e);
            }
        }

        return ChronVer.defaultOf();
    }

    private static CustomVersion versionOf(String tagName, Pattern versionPattern, boolean strict) {
        Matcher matcher = versionPattern.matcher(tagName);
        if (matcher.matches()) {
            return CustomVersion.of(matcher.group(1));
        }
        return CustomVersion.defaultOf();
    }

    private static class UnparseableTags extends ThreadLocal<Set<String>> {
        @Override
        protected Set<String> initialValue() {
            return new LinkedHashSet<>();
        }

        public void clear() {
            get().clear();
        }

        public void unparseable(JReleaserContext context, String tag, Exception exception) {
            if (!get().contains(tag)) {
                get().add(tag);
                context.getLogger().warn(exception.getMessage());
            }
        }
    }
}
