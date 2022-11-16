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
package org.jreleaser.workflow;

import org.jreleaser.bundle.RB;
import org.jreleaser.engine.changelog.Changelog;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.release.Changelog.Append;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static java.lang.System.lineSeparator;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.jreleaser.mustache.MustacheUtils.applyTemplate;
import static org.jreleaser.mustache.MustacheUtils.passThrough;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.stripMargin;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
class ChangelogWorkflowItem extends AbstractWorkflowItem {

    private static final String JRELEASER_CHANGELOG_ANCHOR = "<!-- JRELEASER_CHANGELOG_APPEND - Do not remove or modify this section -->";

    protected ChangelogWorkflowItem() {
        super(JReleaserCommand.CHANGELOG);
    }

    @Override
    protected void doInvoke(JReleaserContext context) {
        String resolvedChangelog = Changelog.createChangelog(context);
        context.setChangelog(resolvedChangelog);

        if (isNotBlank(resolvedChangelog) &&
            context.getModel().getProject().isRelease() &&
            context.getModel().getRelease().getReleaser().getChangelog().getAppend().isEnabled()) {
            appendChangelog(context, resolvedChangelog);
        }
    }

    private void appendChangelog(JReleaserContext context, String resolvedChangelog) {
        BaseReleaser<?, ?> releaser = context.getModel().getRelease().getReleaser();
        Append append = releaser.getChangelog().getAppend();

        Map<String, Object> props = context.fullProps();
        String resolvedTitle = applyTemplate(append.getTitle(), props);

        props = context.fullProps();
        props.put("changelogTitle", passThrough(resolvedTitle));
        props.put("changelogContent", passThrough(resolvedChangelog));
        String appendableChangelog = stripMargin(applyTemplate(append.getResolvedContentTemplate(context), props));

        Path target = context.getBasedir().resolve(append.getTarget());
        String fullChangelog = null;
        try {
            fullChangelog = new String(Files.readAllBytes(target));
        } catch (IOException e) {
            context.getLogger().warn(RB.$("ERROR_cannot_read_changelog"),
                context.relativizeToBasedir(append.getTarget()));
        }

        String separator = separator(releaser);

        if (fullChangelog.contains(JRELEASER_CHANGELOG_ANCHOR)) {
            fullChangelog = fullChangelog.replaceFirst(JRELEASER_CHANGELOG_ANCHOR,
                JRELEASER_CHANGELOG_ANCHOR + separator + appendableChangelog + separator + separator);
        } else {
            fullChangelog = appendableChangelog + separator + separator + fullChangelog;
        }

        context.getLogger().info(RB.$("changelog.generator.store"), context.getBasedir().relativize(target));

        try {
            Files.write(target, fullChangelog.getBytes(), WRITE);
        } catch (IOException e) {
            context.getLogger().error(RB.$("ERROR_unexpected_error_changelog_append"));
        }
    }

    private String separator(BaseReleaser<?, ?> releaser) {
        String separator = lineSeparator();
        if (org.jreleaser.model.api.release.GitlabReleaser.TYPE.equals(releaser.getServiceName())) {
            separator += lineSeparator();
        }
        return separator;
    }
}
