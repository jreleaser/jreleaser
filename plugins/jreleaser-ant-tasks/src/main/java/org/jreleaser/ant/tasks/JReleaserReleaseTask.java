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
package org.jreleaser.ant.tasks;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.releaser.spi.ReleaseException;
import org.jreleaser.release.Releasers;

import static org.jreleaser.ant.tasks.JReleaserChecksumTask.checksum;
import static org.jreleaser.ant.tasks.JReleaserSignTask.sign;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserReleaseTask extends AbstractJReleaserTask {
    @Override
    protected void consumeModel(JReleaserModel jreleaserModel) {
        JReleaserContext context = createContext(jreleaserModel);
        checksum(context);
        sign(context);
        release(context);
    }

    static void release(JReleaserContext context) {
        try {
            Releasers.release(context);
        } catch (ReleaseException e) {
            throw new JReleaserException("Unexpected error when creating release.", e);
        }
    }
}
