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
package org.jreleaser.cli;

import org.jreleaser.util.PlatformUtils;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
@CommandLine.Command
public abstract class AbstractPlatformAwareModelCommand<C extends IO> extends AbstractModelCommand<C> {
    @CommandLine.Option(names = {"-scp", "--select-current-platform"})
    Boolean selectCurrentPlatform;

    @CommandLine.Option(names = {"-sp", "--select-platform"},
        paramLabel = "<platform>")
    String[] selectPlatforms;

    @CommandLine.Option(names = {"-rp", "--reject-platform"},
        paramLabel = "<platform>")
    String[] rejectedPlatforms;

    @Override
    protected List<String> collectSelectedPlatforms() {
        boolean resolvedSelectCurrentPlatform = resolveBoolean(org.jreleaser.model.api.JReleaserContext.SELECT_CURRENT_PLATFORM, selectCurrentPlatform);
        if (resolvedSelectCurrentPlatform) return Collections.singletonList(PlatformUtils.getCurrentFull());

        List<String> list = new ArrayList<>();
        if (selectPlatforms != null && selectPlatforms.length > 0) {
            Collections.addAll(list, selectPlatforms);
        }
        return resolveCollection(org.jreleaser.model.api.JReleaserContext.SELECT_PLATFORMS, list);
    }

    @Override
    protected List<String> collectRejectedPlatforms() {
        List<String> list = new ArrayList<>();
        if (rejectedPlatforms != null && rejectedPlatforms.length > 0) {
            Collections.addAll(list, rejectedPlatforms);
        }
        return resolveCollection(org.jreleaser.model.api.JReleaserContext.REJECT_PLATFORMS, list);
    }
}
