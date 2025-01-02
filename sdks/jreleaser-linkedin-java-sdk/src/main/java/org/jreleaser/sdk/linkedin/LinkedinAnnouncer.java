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
package org.jreleaser.sdk.linkedin;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.announce.Announcer;
import org.jreleaser.mustache.MustacheUtils;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.sdk.linkedin.api.Message;

import static org.jreleaser.model.Constants.KEY_LINKEDIN_OWNER;
import static org.jreleaser.model.Constants.KEY_LINKEDIN_SUBJECT;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class LinkedinAnnouncer implements Announcer<org.jreleaser.model.api.announce.LinkedinAnnouncer> {
    private final JReleaserContext context;
    private final org.jreleaser.model.internal.announce.LinkedinAnnouncer linkedin;

    public LinkedinAnnouncer(JReleaserContext context) {
        this.context = context;
        this.linkedin = context.getModel().getAnnounce().getLinkedin();
    }

    @Override
    public org.jreleaser.model.api.announce.LinkedinAnnouncer getAnnouncer() {
        return linkedin.asImmutable();
    }

    @Override
    public String getName() {
        return org.jreleaser.model.api.announce.LinkedinAnnouncer.TYPE;
    }

    @Override
    public boolean isEnabled() {
        return linkedin.isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        String owner = linkedin.getOwner();
        String subject = linkedin.getResolvedTitle(context);

        Message message = null;
        String text = "";
        if (isNotBlank(linkedin.getMessage())) {
            text = linkedin.getResolvedMessage(context);
            message = Message.of(subject, text);
        } else {
            TemplateContext props = new TemplateContext();
            context.getModel().getRelease().getReleaser().fillProps(props, context.getModel());
            text = linkedin.getResolvedMessageTemplate(context, props);
        }

        try {
            LinkedinSdk sdk = LinkedinSdk.builder(context.asImmutable())
                .accessToken(linkedin.getAccessToken())
                .connectTimeout(linkedin.getConnectTimeout())
                .readTimeout(linkedin.getReadTimeout())
                .dryrun(context.isDryrun())
                .build();
            if (null != message) {
                sdk.share(owner, message);
            } else {
                TemplateContext props = context.fullProps();
                props.set(KEY_LINKEDIN_SUBJECT, subject);
                props.set(KEY_LINKEDIN_OWNER, MustacheUtils.passThrough("{{" + KEY_LINKEDIN_OWNER + "}}"));
                applyTemplates(props, linkedin.resolvedExtraProperties());
                text = MustacheUtils.applyTemplate(text, props);
                sdk.share(owner, subject, text);
            }
        } catch (LinkedinException e) {
            throw new AnnounceException(e);
        }
    }
}
