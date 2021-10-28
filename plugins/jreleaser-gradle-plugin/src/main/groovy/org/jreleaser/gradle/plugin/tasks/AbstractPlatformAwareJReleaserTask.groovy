/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
package org.jreleaser.gradle.plugin.tasks

import groovy.transform.CompileStatic
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option
import org.jreleaser.util.PlatformUtils

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.6.0
 */
@CompileStatic
abstract class AbstractPlatformAwareJReleaserTask extends AbstractJReleaserTask {
    @Input
    final Property<Boolean> selectCurrentPlatform
    @Input
    @Optional
    final ListProperty<String> selectPlatforms

    @Inject
    AbstractPlatformAwareJReleaserTask(ObjectFactory objects) {
        super(objects)
        selectCurrentPlatform = objects.property(Boolean).convention(false)
        selectPlatforms = objects.listProperty(String).convention([])
    }

    @Option(option = 'select-current-platform', description = 'Activates paths matching the current platform (OPTIONAL).')
    void setSelectCurrentPlatform(boolean selectCurrentPlatform) {
        this.selectCurrentPlatform.set(selectCurrentPlatform)
    }

    @Option(option = 'select-platform', description = 'Activates paths matching the given platform (OPTIONAL).')
    void setSelectPlatform(List<String> selectPlatforms) {
        this.selectPlatforms.addAll(selectPlatforms)
    }

    @Override
    protected List<String> collectSelectedPlatforms() {
        if (selectCurrentPlatform.getOrElse(false)) return Collections.singletonList(PlatformUtils.getCurrentFull());
        return selectPlatforms.get()
    }
}
