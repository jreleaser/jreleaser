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

import org.jreleaser.model.JReleaserModel;
import org.jreleaser.tools.Checksums;
import org.jreleaser.tools.DistributionProcessor;
import org.jreleaser.model.tool.spi.ToolProcessingException;
import picocli.CommandLine;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command(name = "prepare",
    description = "Prepares all distributions")
public class Prepare extends AbstractProcessorCommand {
    @Override
    protected void consumeModel(JReleaserModel jreleaserModel) {
        Checksums.collectAndWriteChecksums(createContext(jreleaserModel));
        super.consumeModel(jreleaserModel);
    }

    @Override
    protected void consumeProcessor(DistributionProcessor processor) throws ToolProcessingException {
        if (processor.prepareDistribution()) {
            logger.info("Prepared " + processor.getDistributionName() +
                " distribution with " + processor.getToolName());
        }
    }
}
