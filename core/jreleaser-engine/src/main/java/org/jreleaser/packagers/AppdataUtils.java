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
package org.jreleaser.packagers;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Icon;
import org.jreleaser.model.internal.distributions.Distribution;
import org.jreleaser.model.internal.packagers.TemplatePackager;
import org.jreleaser.model.spi.packagers.PackagerProcessingException;
import org.jreleaser.mustache.TemplateContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.getFilenameExtension;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public final class AppdataUtils {
    private AppdataUtils() {
        // noop
    }

    public static void resolveIcons(JReleaserContext context, TemplatePackager<?> packager,
                                    Distribution distribution, TemplateContext props, List<Icon> icons) throws PackagerProcessingException {
        for (Icon icon : icons) {
            // check if exists
            String iconUrl = resolveTemplate(icon.getUrl(), props);
            String iconExt = getFilenameExtension(iconUrl);
            Path iconPath = Paths.get(packager.getTemplateDirectory(), "icons",
                icon.getWidth() + "x" + icon.getHeight(),
                distribution.getExecutable().getName() + "." + iconExt);
            iconPath = context.getBasedir().resolve(iconPath);

            if (!Files.exists(iconPath)) {
                // download
                context.getLogger().debug("{} -> {}", iconUrl, context.relativizeToBasedir(iconPath));
                try {
                    org.apache.commons.io.FileUtils.copyURLToFile(
                        new URI(iconUrl).toURL(),
                        iconPath.toFile(),
                        20000,
                        60000);
                } catch (URISyntaxException | IOException e) {
                    throw new PackagerProcessingException(RB.$("ERROR_unexpected_download", iconUrl), e);
                }
            }
        }
    }

    public static boolean isReleaseIncluded(Set<String> skipReleases, String version) {
        if (null == skipReleases || skipReleases.isEmpty()) {
            return true;
        }

        // 1. exact match
        if (skipReleases.contains(version)) {
            return false;
        }

        // 2. regex match
        for (String regex : skipReleases) {
            Pattern p = Pattern.compile(regex);
            if (p.matcher(version).matches()) return false;
        }

        return true;
    }

    public static class Release {
        private final String url;
        private final String version;
        private final String date;

        private Release(String url, String version, String date) {
            this.url = url;
            this.version = version;
            this.date = date;
        }

        public String getUrl() {
            return url;
        }

        public String getVersion() {
            return version;
        }

        public String getDate() {
            return date;
        }

        public static Release of(String url, String version, Date date) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            return new Release(url, version, format.format(date));
        }
    }
}
