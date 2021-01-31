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
package org.kordamp.jreleaser.sdk.git;

import org.kordamp.jreleaser.model.Changelog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ChangelogProvider {
    public static String getChangelog(Path basedir, Changelog changelog) throws IOException {
        File externalChangelog = changelog.getExternal();

        if(null != externalChangelog) {
            if(!externalChangelog.exists()) {
                throw new IllegalStateException("Changelog "+basedir.resolve(externalChangelog.toPath())+" does not exist");
            }

            return new String(Files.readAllBytes(basedir.resolve(externalChangelog.toPath())));
        }

        return ChangelogGenerator.generate(basedir, changelog);
    }
}
