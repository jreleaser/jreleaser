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
package org.jreleaser.gradle.plugin.internal.dsl.release

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.jreleaser.gradle.plugin.dsl.release.CodebergReleaser
import org.jreleaser.gradle.plugin.dsl.release.GenericGitReleaser
import org.jreleaser.gradle.plugin.dsl.release.GiteaReleaser
import org.jreleaser.gradle.plugin.dsl.release.GithubReleaser
import org.jreleaser.gradle.plugin.dsl.release.GitlabReleaser
import org.jreleaser.gradle.plugin.dsl.release.Release
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ReleaseImpl implements Release {
    final GithubReleaserImpl github
    final GitlabReleaserImpl gitlab
    final GiteaReleaserImpl gitea
    final CodebergReleaserImpl codeberg
    final GenericGitReleaserImpl generic

    @Inject
    ReleaseImpl(ObjectFactory objects) {
        github = objects.newInstance(GithubReleaserImpl, objects)
        gitlab = objects.newInstance(GitlabReleaserImpl, objects)
        gitea = objects.newInstance(GiteaReleaserImpl, objects)
        codeberg = objects.newInstance(CodebergReleaserImpl, objects)
        generic = objects.newInstance(GenericGitReleaserImpl, objects)
    }

    @Override
    void github(Action<? super GithubReleaser> action) {
        action.execute(github)
    }

    @Override
    void gitlab(Action<? super GitlabReleaser> action) {
        action.execute(gitlab)
    }

    @Override
    void gitea(Action<? super GiteaReleaser> action) {
        action.execute(gitea)
    }

    @Override
    void codeberg(Action<? super CodebergReleaser> action) {
        action.execute(codeberg)
    }

    @Override
    void generic(Action<? super GenericGitReleaser> action) {
        action.execute(generic)
    }

    @Override
    @CompileDynamic
    void github(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GithubReleaser) Closure<Void> action) {
        ConfigureUtil.configure(action, github)
    }

    @Override
    @CompileDynamic
    void gitlab(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GitlabReleaser) Closure<Void> action) {
        ConfigureUtil.configure(action, gitlab)
    }

    @Override
    @CompileDynamic
    void gitea(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GiteaReleaser) Closure<Void> action) {
        ConfigureUtil.configure(action, gitea)
    }

    @Override
    @CompileDynamic
    void codeberg(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CodebergReleaser) Closure<Void> action) {
        ConfigureUtil.configure(action, codeberg)
    }

    @Override
    @CompileDynamic
    void generic(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GenericGitReleaser) Closure<Void> action) {
        ConfigureUtil.configure(action, generic)
    }

    org.jreleaser.model.internal.release.Release toModel() {
        org.jreleaser.model.internal.release.Release release = new org.jreleaser.model.internal.release.Release()
        if (github.isSet()) release.github = github.toModel()
        if (gitlab.isSet()) release.gitlab = gitlab.toModel()
        if (gitea.isSet()) release.gitea = gitea.toModel()
        if (codeberg.isSet()) release.codeberg = codeberg.toModel()
        if (generic.isSet()) release.generic = generic.toModel()
        release
    }
}
