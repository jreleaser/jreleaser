/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.sdk.twist;

import org.jreleaser.model.spi.announce.AnnouncerBuilderFactory;
import org.kordamp.jipsy.annotations.ServiceProviderFor;

/**
 * @author Usman Shaikh
 * @since 1.23.0
 */
@ServiceProviderFor(AnnouncerBuilderFactory.class)
public class TwistAnnouncerBuilderFactory implements AnnouncerBuilderFactory<TwistAnnouncer, TwistAnnouncerBuilder> {
    @Override
    public String getName() {
        return org.jreleaser.model.api.announce.TwistAnnouncer.TYPE;
    }

    @Override
    public TwistAnnouncerBuilder getBuilder() {
        return new TwistAnnouncerBuilder();
    }
}
