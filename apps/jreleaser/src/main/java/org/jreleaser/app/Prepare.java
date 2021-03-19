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
package org.jreleaser.app;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import picocli.CommandLine;

import static org.jreleaser.app.Checksum.checksum;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "prepare",
    description = "Prepares all distributions")
public class Prepare extends AbstractProcessorCommand {
    @Override
    protected void consumeModel(JReleaserModel jreleaserModel) {
        JReleaserContext context = createContext(jreleaserModel);
        checksum(context);
        prepare(context, failFast);
    }

    static void prepare(JReleaserContext context, boolean failFast) {
        processContext(context, failFast, "Preparing", processor -> {
            if (processor.prepareDistribution()) {
                context.getLogger().info("Prepared " + processor.getDistributionName() +
                    " distribution with " + processor.getToolName());
            }
        });
    }
}
