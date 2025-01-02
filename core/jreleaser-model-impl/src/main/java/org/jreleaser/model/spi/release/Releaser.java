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
package org.jreleaser.model.spi.release;

import org.jreleaser.model.api.common.ExtraProperties;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public interface Releaser<A extends org.jreleaser.model.api.release.Releaser> extends Serializable {
    void release() throws ReleaseException;

    Repository maybeCreateRepository(String owner, String repo, String password, ExtraProperties extraProperties) throws IOException;

    Optional<User> findUser(String email, String name);

    String generateReleaseNotes() throws IOException;

    List<Release> listReleases(String owner, String repo) throws IOException;

    A getReleaser();
}
