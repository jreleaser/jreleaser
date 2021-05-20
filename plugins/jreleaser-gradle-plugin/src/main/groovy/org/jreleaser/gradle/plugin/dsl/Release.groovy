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
package org.jreleaser.gradle.plugin.dsl

import groovy.transform.CompileStatic
import org.gradle.api.Action

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface Release {
    Github getGithub()

    Gitlab getGitlab()

    Gitea getGitea()

    Codeberg getCodeberg()

    GenericGit getGeneric()

    void github(Action<? super Github> action)

    void gitlab(Action<? super Gitlab> action)

    void gitea(Action<? super Gitea> action)

    void codeberg(Action<? super Codeberg> action)

    void generic(Action<? super GenericGit> action)

    void github(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Github) Closure<Void> action)

    void gitlab(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Gitlab) Closure<Void> action)

    void gitea(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Gitea) Closure<Void> action)

    void codeberg(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Codeberg) Closure<Void> action)

    void generic(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = GenericGit) Closure<Void> action)
}