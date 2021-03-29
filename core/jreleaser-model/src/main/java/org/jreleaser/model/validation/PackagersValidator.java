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

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Packagers;

import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class PackagersValidator extends Validator {
    public static void validatePackagers(JReleaserContext context, List<String> errors) {
        JReleaserModel model = context.getModel();
        Packagers packagers = model.getPackagers();

        validateCommitAuthor(packagers.getBrew(), model.getRelease().getGitService());
        validateOwner(packagers.getBrew().getTap(), model.getRelease().getGitService());

        validateCommitAuthor(packagers.getChocolatey(), model.getRelease().getGitService());
        validateOwner(packagers.getChocolatey().getBucket(), model.getRelease().getGitService());

        validateCommitAuthor(packagers.getJbang(), model.getRelease().getGitService());
        validateOwner(packagers.getJbang().getCatalog(), model.getRelease().getGitService());

        validateCommitAuthor(packagers.getScoop(), model.getRelease().getGitService());
        validateOwner(packagers.getScoop().getBucket(), model.getRelease().getGitService());

        validateCommitAuthor(packagers.getSnap(), model.getRelease().getGitService());
        validateOwner(packagers.getSnap().getSnap(), model.getRelease().getGitService());
    }
}