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
package org.jreleaser.gradle.plugin.tasks

import groovy.transform.CompileStatic
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.jreleaser.model.api.JReleaserCommand
import org.jreleaser.model.internal.JReleaserContext
import org.jreleaser.workflow.Workflows

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 1.5.0
 */
@CompileStatic
abstract class JReleaserCatalogTask extends AbstractJReleaserDistributionTask {
    static final String NAME = 'jreleaserCatalog'

    @Input
    @Optional
    final ListProperty<String> catalogers

    @Input
    @Optional
    final ListProperty<String> excludedCatalogers

    @Input
    @Optional
    final ListProperty<String> deployerTypes

    @Input
    @Optional
    final ListProperty<String> excludedDeployerTypes

    @Input
    @Optional
    final ListProperty<String> deployerNames

    @Input
    @Optional
    final ListProperty<String> excludedDeployerNames

    @Inject
    JReleaserCatalogTask(ObjectFactory objects) {
        super(objects)
        catalogers = objects.listProperty(String).convention([])
        excludedCatalogers = objects.listProperty(String).convention([])
        deployerTypes = objects.listProperty(String).convention([])
        excludedDeployerTypes = objects.listProperty(String).convention([])
        deployerNames = objects.listProperty(String).convention([])
        excludedDeployerNames = objects.listProperty(String).convention([])
        command = JReleaserCommand.CATALOG
    }

    @Option(option = 'cataloger', description = 'Include a cataloger (OPTIONAL).')
    void setCataloger(List<String> catalogers) {
        this.catalogers.set(catalogers)
    }

    @Option(option = 'exclude-cataloger', description = 'Exclude a cataloger (OPTIONAL).')
    void setExcludeCataloger(List<String> excludedCatalogers) {
        this.excludedCatalogers.set(excludedCatalogers)
    }

    @Option(option = 'deployer', description = 'Include a deployer by type (OPTIONAL).')
    void setDeployerType(List<String> deployerTypes) {
        this.deployerTypes.set(deployerTypes)
    }

    @Option(option = 'exclude-deployer', description = 'Exclude a deployer by type (OPTIONAL).')
    void setExcludeDeployerType(List<String> excludedDeployerTypes) {
        this.excludedDeployerTypes.set(excludedDeployerTypes)
    }

    @Option(option = 'deployer-name', description = 'Include a deployer by name (OPTIONAL).')
    void setDeployerName(List<String> deployerNames) {
        this.deployerNames.set(deployerNames)
    }

    @Option(option = 'exclude-deployer-name', description = 'Exclude a deployer by name (OPTIONAL).')
    void setExcludeDeployerName(List<String> excludedDeployerNames) {
        this.excludedDeployerNames.set(excludedDeployerNames)
    }

    @TaskAction
    void performAction() {
        JReleaserContext ctx = super.setupContext()
        ctx.includedCatalogers = catalogers.orNull
        ctx.excludedCatalogers = excludedCatalogers.orNull
        ctx.includedDeployerTypes = deployerTypes.orNull
        ctx.excludedDeployerTypes = excludedDeployerTypes.orNull
        ctx.includedDeployerNames = deployerNames.orNull
        ctx.excludedDeployerNames = excludedDeployerNames.orNull
        Workflows.catalog(ctx).execute()
    }
}
