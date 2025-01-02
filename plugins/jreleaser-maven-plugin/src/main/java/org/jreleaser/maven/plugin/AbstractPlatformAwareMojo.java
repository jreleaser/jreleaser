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
package org.jreleaser.maven.plugin;

import org.apache.maven.plugins.annotations.Parameter;
import org.jreleaser.util.PlatformUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.6.0
 */
abstract class AbstractPlatformAwareMojo extends AbstractJReleaserMojo {
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

    @Override
    protected List<String> collectSelectedPlatforms() {
        boolean resolvedSelectCurrentPlatform = resolveBoolean(org.jreleaser.model.api.JReleaserContext.SELECT_CURRENT_PLATFORM, selectCurrentPlatform);
        if (resolvedSelectCurrentPlatform) return Collections.singletonList(PlatformUtils.getCurrentFull());

        List<String> list = new ArrayList<>();
        if (null != selectPlatforms && selectPlatforms.length > 0) {
            Collections.addAll(list, selectPlatforms);
        }
        return resolveCollection(org.jreleaser.model.api.JReleaserContext.SELECT_PLATFORMS, list);
    }

    @Override
    protected List<String> collectRejectedPlatforms() {
        List<String> list = new ArrayList<>();
        if (null != rejectedPlatforms && rejectedPlatforms.length > 0) {
            Collections.addAll(list, rejectedPlatforms);
        }
        return resolveCollection(org.jreleaser.model.api.JReleaserContext.REJECT_PLATFORMS, list);
    }
}
