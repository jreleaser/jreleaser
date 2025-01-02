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
package org.jreleaser.infra.nativeimage.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.jreleaser.infra.nativeimage.processor.ProcessorUtil.stacktrace;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
abstract class AbstractNativeImageProcessor extends AbstractProcessor {
    @Override
    public SourceVersion getSupportedSourceVersion() {
        SupportedSourceVersion ssv = this.getClass().getAnnotation(SupportedSourceVersion.class);
        if (null == ssv) {
            return SourceVersion.latest();
        } else {
            return ssv.value();
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            Set<Element> elements = new LinkedHashSet<>();

            for (Element rootElement : roundEnv.getRootElements()) {
                elements.add(rootElement);
                for (Element element : rootElement.getEnclosedElements()) {
                    switch (element.getKind()) {
                        case ENUM:
                        case INTERFACE:
                        case CLASS:
                            elements.add(element);
                            break;
                        default:
                            // noop
                            break;
                    }
                }
            }
            if (!elements.isEmpty()) {
                process(new Context(processingEnv, roundEnv, elements));
            }
            return false;
        } catch (Exception e) {
            fatalError(stacktrace(e));
        }
        return false;
    }

    protected abstract void process(Context context);

    protected void fatalError(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: " + msg);
    }
}
