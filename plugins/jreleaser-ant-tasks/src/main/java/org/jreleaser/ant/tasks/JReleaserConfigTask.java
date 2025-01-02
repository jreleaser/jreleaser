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
package org.jreleaser.ant.tasks;

import org.jreleaser.ant.tasks.internal.AntJReleaserModelPrinter;
import org.jreleaser.engine.context.ModelValidator;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;

import static org.jreleaser.util.IoUtils.newPrintWriter;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaserConfigTask extends AbstractPlatformAwareJReleaserTask {
    private boolean full;
    private boolean assembly;
    private boolean download;
    private boolean changelog;
    private boolean announce;

    public void setFull(boolean full) {
        this.full = full;
    }

    public void setAssembly(boolean assembly) {
        this.assembly = assembly;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    public void setChangelog(boolean changelog) {
        this.changelog = changelog;
    }

    public void setAnnounce(boolean announce) {
        this.announce = announce;
    }

    @Override
    protected void doExecute(JReleaserContext context) {
        ModelValidator.validate(context);
        new AntJReleaserModelPrinter(newPrintWriter(System.out))
            .print(context.getModel().asMap(full));
        context.report();
    }

    @Override
    protected Mode getMode() {
        if (download) return Mode.DOWNLOAD;
        if (assembly) return Mode.ASSEMBLE;
        if (changelog) return Mode.CHANGELOG;
        if (announce) return Mode.ANNOUNCE;
        return Mode.CONFIG;
    }

    @Override
    protected JReleaserCommand getCommand() {
        return JReleaserCommand.CONFIG;
    }
}
