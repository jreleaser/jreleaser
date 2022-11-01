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

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor8;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.util.Map;

import static org.jreleaser.infra.nativeimage.processor.ProcessorUtil.stacktrace;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
abstract class AbstractGenerator implements Generator {
    public static final String OPTION_VERBOSE = "verbose";

    private final String fileName;
    private final String disableKey;

    protected AbstractGenerator(String fileName, String disableKey) {
        this.fileName = fileName;
        this.disableKey = disableKey;
    }

    @Override
    public void generate(Context context) {
        if (!enabled(context)) {
            logInfo(context, "is not enabled");
            return;
        }

        try {
            String path = createRelativePath(context, fileName());
            String text = generateConfig(context);
            if (text != null && !text.isEmpty()) {
                logInfo(context, "writing to: " + StandardLocation.CLASS_OUTPUT + "/" + path);
                ProcessorUtil.generate(StandardLocation.CLASS_OUTPUT, path, text, context);
            }
        } catch (Exception e) {
            fatalError(context, stacktrace(e));
        }
    }

    protected abstract String generateConfig(Context context);

    protected boolean enabled(Context context) {
        Map<String, String> options = context.getProcessingEnv().getOptions();
        return !options.containsKey(disableKey);
    }

    protected String fileName() {
        return fileName;
    }

    protected String createRelativePath(Context context, String fileName) {
        Map<String, String> options = context.getProcessingEnv().getOptions();
        String id = options.get(Constants.OPTION_PROJECT_PATH);
        String relativeName = Constants.BASE_PATH;
        if (id == null) {
            id = "jreleaser-generated";
        }
        relativeName += id + "/";
        return relativeName + fileName;
    }

    protected void logInfo(Context context, String msg) {
        if (context.getProcessingEnv().getOptions().containsKey(OPTION_VERBOSE)) {
            context.getProcessingEnv().getMessager().printMessage(Diagnostic.Kind.NOTE, getClass().getSimpleName() + " " + msg);
        }
    }

    protected void fatalError(Context context, String msg) {
        context.getProcessingEnv().getMessager().printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: " + msg);
    }

    protected String elementTypeName(TypeMirror typeMirror) {
        String result = typeMirror.accept(new SimpleTypeVisitor8<String, Void>() {
            @Override
            public String visitDeclared(DeclaredType declaredType, Void aVoid) {
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                if (typeElement.getNestingKind().isNested()) {
                    return elementTypeName(typeElement.getEnclosingElement().asType()) + "$" + typeElement.getSimpleName();
                }
                return typeElement.getQualifiedName().toString();
            }

            @Override
            public String visitArray(ArrayType arrayType, Void aVoid) {
                return elementTypeName(arrayType.getComponentType()) + "[]";
            }
        }, null);
        if (result == null) {
            return typeMirror.toString();
        }
        return result;
    }
}
