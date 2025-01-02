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
package org.jreleaser.gradle.plugin.internal.dsl.deploy

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.deploy.Deploy
import org.jreleaser.gradle.plugin.dsl.deploy.maven.Maven
import org.jreleaser.gradle.plugin.internal.dsl.deploy.maven.MavenImpl
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.2.0
 */
@CompileStatic
class DeployImpl implements Deploy {
    final Property<Active> active
    final MavenImpl maven

    @Inject
    DeployImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        maven = objects.newInstance(MavenImpl, objects)
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void maven(Action<? super Maven> action) {
        action.execute(maven)
    }

    @Override
    @CompileDynamic
    void maven(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Maven) Closure<Void> action) {
        ConfigureUtil.configure(action, maven)
    }

    org.jreleaser.model.internal.deploy.Deploy toModel() {
        org.jreleaser.model.internal.deploy.Deploy deploy = new org.jreleaser.model.internal.deploy.Deploy()
        if (active.present) deploy.active = active.get()
        deploy.maven = maven.toModel()
        deploy
    }
}
