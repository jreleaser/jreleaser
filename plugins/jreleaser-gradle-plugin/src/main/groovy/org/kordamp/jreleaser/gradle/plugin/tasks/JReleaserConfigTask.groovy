/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 Andres Almiray.
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
package org.kordamp.jreleaser.gradle.plugin.tasks

import groovy.transform.CompileStatic
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.kordamp.gradle.plugin.base.tasks.AbstractSettingsTask
import org.kordamp.jreleaser.model.JReleaserModel

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class JReleaserConfigTask extends AbstractSettingsTask {
    @Internal
    final Property<JReleaserModel> jreleaserModel

    @Inject
    JReleaserConfigTask(ObjectFactory objects) {
        jreleaserModel = objects.property(JReleaserModel)
    }

    @TaskAction
    void createOutput() {
        println '== JReleaser =='
        doPrintMap(jreleaserModel.get().asMap(), 0)
    }
}
