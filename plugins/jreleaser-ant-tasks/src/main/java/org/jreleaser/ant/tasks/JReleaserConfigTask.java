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
package org.jreleaser.ant.tasks;

import org.jreleaser.ant.tasks.internal.JReleaserModelPrinter;
import org.jreleaser.model.JReleaserContext;

import java.io.PrintWriter;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserConfigTask extends AbstractJReleaserTask {
    private boolean full;
    private boolean assembly;

    public void setFull(boolean full) {
        this.full = full;
    }

    public void setAssembly(boolean assembly) {
        this.assembly = assembly;
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        new JReleaserModelPrinter(new PrintWriter(System.out, true))
            .print(context.getModel().asMap(full));
        context.report();
    }

    protected JReleaserContext.Mode getMode() {
        return assembly ? JReleaserContext.Mode.ASSEMBLE : JReleaserContext.Mode.FULL;
    }
}
