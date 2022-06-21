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
package org.jreleaser.ant.tasks;

import org.jreleaser.ant.tasks.internal.JReleaserModelPrinter;
import org.jreleaser.engine.context.ModelValidator;
import org.jreleaser.model.JReleaserContext;

import java.io.PrintWriter;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserConfigTask extends AbstractPlatformAwareJReleaserTask {
    private boolean full;
    private boolean assembly;
    private boolean download;

    public void setFull(boolean full) {
        this.full = full;
    }

    public void setAssembly(boolean assembly) {
        this.assembly = assembly;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        ModelValidator.validate(context);
        new JReleaserModelPrinter(new PrintWriter(System.out, true))
            .print(context.getModel().asMap(full));
        context.report();
    }

    protected JReleaserContext.Mode getMode() {
        if (download) return JReleaserContext.Mode.DOWNLOAD;
        if (assembly) return JReleaserContext.Mode.ASSEMBLE;
        return JReleaserContext.Mode.CONFIG;
    }
}
