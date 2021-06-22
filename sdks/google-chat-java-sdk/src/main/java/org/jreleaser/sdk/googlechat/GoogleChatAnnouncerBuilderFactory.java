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
package org.jreleaser.sdk.googlechat;

import org.jreleaser.model.announcer.spi.AnnouncerBuilderFactory;
import org.kordamp.jipsy.annotations.ServiceProviderFor;

/**
 * @author Anyul Rivas
 * @since 0.5.0
 */
@ServiceProviderFor(AnnouncerBuilderFactory.class)
public class GoogleChatAnnouncerBuilderFactory implements AnnouncerBuilderFactory<GoogleChatAnnouncer, GoogleChatAnnouncerBuilder> {
    @Override
    public String getName() {
        return org.jreleaser.model.GoogleChat.NAME;
    }

    @Override
    public GoogleChatAnnouncerBuilder getBuilder() {
        return new GoogleChatAnnouncerBuilder();
    }
}