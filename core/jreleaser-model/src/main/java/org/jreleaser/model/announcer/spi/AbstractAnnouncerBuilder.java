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
package org.jreleaser.model.announcer.spi;

import org.jreleaser.model.JReleaserModel;
import org.jreleaser.util.Logger;

import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AbstractAnnouncerBuilder<R extends Announcer, B extends AnnouncerBuilder<R, B>> implements AnnouncerBuilder<R, B> {
    protected Logger logger;
    protected Path basedir;
    protected JReleaserModel model;

    protected final B self() {
        return (B) this;
    }

    @Override
    public B basedir(Path basedir) {
        this.basedir = requireNonNull(basedir, "'basedir' must not be null");
        return self();
    }

    @Override
    public B logger(Logger logger) {
        this.logger = requireNonNull(logger, "'logger' must not be null");
        return self();
    }

    @Override
    public B model(JReleaserModel model) {
        this.model = requireNonNull(model, "'model' must not be null");
        return self();
    }

    protected void validate() {
        requireNonNull(basedir, "'basedir' must not be null");
        requireNonNull(logger, "'logger' must not be null");
        requireNonNull(model, "'model' must not be null");
    }

    @Override
    public B configureWith(Path basedir, JReleaserModel model) {
        basedir(basedir);
        model(model);
        return self();
    }
}