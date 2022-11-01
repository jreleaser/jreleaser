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

import java.util.ArrayList;
import java.util.List;

import static org.jreleaser.infra.nativeimage.processor.ProcessorUtil.stacktrace;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
abstract class AbstractCompositeGeneratorProcessor extends AbstractNativeImageProcessor {
    protected final List<Generator> generators = new ArrayList<>();

    @Override
    protected void process(Context context) {
        try {
            for (Generator generator : generators) {
                generator.generate(context);
            }
        } catch (Exception e) {
            fatalError(stacktrace(e));
        }
    }
}
