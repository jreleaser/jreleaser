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
import org.jreleaser.engine.announce.Announcers;
import org.jreleaser.model.JReleaserCommand;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.util.JReleaserException;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
class AnnounceWorkflowItem extends AbstractWorkflowItem {
    protected AnnounceWorkflowItem() {
        super(JReleaserCommand.ANNOUNCE);
    }

    @Override
    protected void doInvoke(JReleaserContext context) {
        try {
            Announcers.announce(context);
        } catch (AnnounceException e) {
            throw new JReleaserException(RB.$("ERROR_unexpected_release_announce"), e);
        }
    }
}
