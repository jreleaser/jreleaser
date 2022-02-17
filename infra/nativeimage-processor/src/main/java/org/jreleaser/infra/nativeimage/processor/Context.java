/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
package org.jreleaser.infra.nativeimage.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
class Context {
    private final ProcessingEnvironment processingEnv;
    private final RoundEnvironment roundEnv;
    private final Set<Element> elements = new LinkedHashSet<>();

    public Context(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv, Set<? extends Element> elements) {
        this.processingEnv = processingEnv;
        this.roundEnv = roundEnv;
        this.elements.addAll(elements);
    }

    public ProcessingEnvironment getProcessingEnv() {
        return processingEnv;
    }

    public RoundEnvironment getRoundEnv() {
        return roundEnv;
    }

    public Set<Element> getElements() {
        return Collections.unmodifiableSet(elements);
    }
}
