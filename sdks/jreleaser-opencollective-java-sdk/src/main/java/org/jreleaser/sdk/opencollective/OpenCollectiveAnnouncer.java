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
package org.jreleaser.sdk.opencollective;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.announce.Announcer;
import org.jreleaser.mustache.TemplateContext;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.6.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class OpenCollectiveAnnouncer implements Announcer<org.jreleaser.model.api.announce.OpenCollectiveAnnouncer> {
    private final JReleaserContext context;
    private final org.jreleaser.model.internal.announce.OpenCollectiveAnnouncer openCollective;

    public OpenCollectiveAnnouncer(JReleaserContext context) {
        this.context = context;
        this.openCollective = context.getModel().getAnnounce().getOpenCollective();
    }

    @Override
    public org.jreleaser.model.api.announce.OpenCollectiveAnnouncer getAnnouncer() {
        return openCollective.asImmutable();
    }

    @Override
    public String getName() {
        return org.jreleaser.model.api.announce.OpenCollectiveAnnouncer.TYPE;
    }

    @Override
    public boolean isEnabled() {
        return openCollective.isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        String message = "";
        if (isNotBlank(openCollective.getMessage())) {
            message = openCollective.getResolvedMessage(context);
        } else {
            TemplateContext props = new TemplateContext();
            context.getModel().getRelease().getReleaser().fillProps(props, context.getModel());
            message = openCollective.getResolvedMessageTemplate(context, props);
        }

        String title = openCollective.getResolvedTitle(context);
        String slug = openCollective.getSlug();

        context.getLogger().debug("slug: {}", slug);
        context.getLogger().info("title: {}", title);
        context.getLogger().info("message: {}", message);

        try {
            OpenCollectiveSdk sdk = OpenCollectiveSdk.builder(context.asImmutable())
                .host(openCollective.getHost())
                .token(openCollective.getToken())
                .connectTimeout(openCollective.getConnectTimeout())
                .readTimeout(openCollective.getReadTimeout())
                .dryrun(context.isDryrun())
                .build();
            sdk.postUpdate(slug, title, message);
        } catch (OpenCollectiveException e) {
            throw new AnnounceException(e);
        }
    }
}
