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
package org.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Docker
import org.jreleaser.model.DockerSpec

import javax.inject.Inject
import java.util.stream.Collectors

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class DockerImpl extends AbstractDockerConfiguration implements Docker {
    final NamedDomainObjectContainer<DockerSpecImpl> specs

    @Inject
    DockerImpl(ObjectFactory objects) {
        super(objects)

        specs = objects.domainObjectContainer(DockerSpecImpl, new NamedDomainObjectFactory<DockerSpecImpl>() {
            @Override
            DockerSpecImpl create(String name) {
                DockerSpecImpl spec = objects.newInstance(DockerSpecImpl, objects)
                spec.name = name
                return spec
            }
        })
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            !specs.isEmpty()
    }

    @CompileDynamic
    org.jreleaser.model.Docker toModel() {
        org.jreleaser.model.Docker tool = new org.jreleaser.model.Docker()
        toModel(tool)
        tool.specs = (specs.toList().stream()
            .collect(Collectors.toMap(
                { DockerSpecImpl d -> d.name },
                { DockerSpecImpl d -> d.toModel() })) as Map<String, DockerSpec>)
        tool
    }
}
