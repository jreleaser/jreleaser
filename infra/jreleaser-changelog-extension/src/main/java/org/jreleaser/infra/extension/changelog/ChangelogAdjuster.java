/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
package org.jreleaser.infra.extension.changelog;

import org.jreleaser.extensions.api.workflow.WorkflowAdapter;
import org.jreleaser.model.api.JReleaserContext;
import org.jreleaser.model.api.hooks.ExecutionEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.stream.Collectors.toList;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public class ChangelogAdjuster extends WorkflowAdapter {
    private static final Pattern PATTERN_SHA256 = Pattern.compile("sha256:([\\w\\.\\-]+)");

    @Override
    public void onWorkflowStep(ExecutionEvent event, JReleaserContext context) {
        if (event.getType() == ExecutionEvent.Type.SUCCESS && "checksum".equals(event.getName())) {
            context.getLogger().increaseIndent();
            context.getLogger().setPrefix("changelog-adjuster");
            try {
                adjustChangelog(context);
            } finally {
                context.getLogger().restorePrefix();
                context.getLogger().decreaseIndent();
            }
        }
    }

    private void adjustChangelog(JReleaserContext context) {
        Map<String, String> checksums = new LinkedHashMap<>();

        Path checksumsFile = context.getChecksumsDirectory().resolve("checksums_sha256.txt");
        Path changelogFile = context.getOutputDirectory().resolve("release/CHANGELOG.md");

        try {
            readAllLines(checksumsFile).forEach(line -> {
                String checksum = line.substring(0, 65).trim();
                String filename = line.substring(66).trim();
                checksums.put(filename, checksum);
            });
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected error reading checksums. " + e.getMessage());
        }

        context.getLogger().info("{} checksums read", checksums.size());

        int[] replaceCount = new int[]{0};
        try {
            List<String> lines = readAllLines(changelogFile).stream()
                .map(line -> replaceChecksums(line, checksums, replaceCount))
                .collect(toList());
            String adjustedChangelog = join(lineSeparator(), lines);
            write(changelogFile, adjustedChangelog.getBytes(), WRITE, TRUNCATE_EXISTING);
            context.getChangelog().setResolvedChangelog(adjustedChangelog);
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected error replacing checksums. " + e.getMessage());
        }

        context.getLogger().info("{} checksums replaced", replaceCount[0]);
    }

    private String replaceChecksums(String line, Map<String, String> checksums, int[] replaceCount) {
        Matcher matcher = PATTERN_SHA256.matcher(line);

        while (matcher.find()) {
            String filename = matcher.group(1);
            if (checksums.containsKey(filename)) {
                line = line.replace("sha256:" + filename, "sha256:`" + checksums.get(filename) + "`");
                replaceCount[0]++;
            } else {
                line = line.replace("sha256:" + filename, "");
            }
        }

        return line;
    }
}
