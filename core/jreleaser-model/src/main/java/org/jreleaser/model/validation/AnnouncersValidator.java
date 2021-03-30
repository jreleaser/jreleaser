/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import org.jreleaser.model.Announce;
import org.jreleaser.model.JReleaserContext;

import java.util.List;

import static org.jreleaser.model.validation.MailValidator.validateMail;
import static org.jreleaser.model.validation.SdkmanValidator.validateSdkman;
import static org.jreleaser.model.validation.TwitterValidator.validateTwitter;
import static org.jreleaser.model.validation.ZulipValidator.validateZulip;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AnnouncersValidator extends Validator {
    public static void validateAnnouncers(JReleaserContext context, List<String> errors) {
        Announce announce = context.getModel().getAnnounce();
        validateMail(context, announce.getMail(), errors);
        validateSdkman(context, announce.getSdkman(), errors);
        validateTwitter(context, announce.getTwitter(), errors);
        validateZulip(context, announce.getZulip(), errors);

        boolean enabled = announce.getMail().isEnabled() ||
            announce.getSdkman().isEnabled() ||
            announce.getTwitter().isEnabled() ||
            announce.getZulip().isEnabled();
        if (!announce.isEnabledSet()) {
            announce.setEnabled(enabled);
        }
    }
}