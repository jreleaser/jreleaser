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
package org.jreleaser.engine.distribution;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Packager;
import org.jreleaser.model.packager.spi.PackagerProcessingException;
import org.jreleaser.model.packager.spi.PackagerProcessor;
import org.jreleaser.util.Constants;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class DistributionProcessor {
    private final JReleaserContext context;
    private final String distributionName;
    private final String packagerName;

    private DistributionProcessor(JReleaserContext context,
                                  String distributionName,
                                  String packagerName) {
        this.context = context;
        this.distributionName = distributionName;
        this.packagerName = packagerName;
    }

    public String getDistributionName() {
        return distributionName;
    }

    public String getPackagerName() {
        return packagerName;
    }

    public void prepareDistribution() throws PackagerProcessingException {
        Distribution distribution = context.getModel().findDistribution(distributionName);
        Packager packager = distribution.getPackager(packagerName);
        if (!packager.isEnabled()) {
            context.getLogger().debug(RB.$("distributions.skip.distribution"), distributionName);
            return;
        }

        PackagerProcessor<Packager> packagerProcessor = PackagerProcessors.findProcessor(context, packager);
        if (!packagerProcessor.supportsDistribution(distribution)) {
            context.getLogger().info(RB.$("distributions.not.supported.distribution"), distributionName, distribution.getType());
            return;
        }

        context.getLogger().info(RB.$("distributions.apply.action.distribution"), RB.$("distributions.action.preparing"), distributionName);

        try {
            packagerProcessor.prepareDistribution(distribution, initProps());
        } catch (PackagerProcessingException tpe) {
            if (packager.isContinueOnError()) {
                packager.fail();
                context.getLogger().warn(RB.$("distributions.failure"), tpe.getMessage());
                context.getLogger().trace(tpe);
            } else {
                throw tpe;
            }
        }
    }

    public void packageDistribution() throws PackagerProcessingException {
        Distribution distribution = context.getModel().findDistribution(distributionName);
        Packager packager = distribution.getPackager(packagerName);
        if (!packager.isEnabled()) {
            context.getLogger().debug(RB.$("distributions.skip.distribution"), distributionName);
            return;
        }
        if (packager.isFailed()) {
            context.getLogger().warn(RB.$("distributions.previous.failure"));
            return;
        }

        PackagerProcessor<Packager> packagerProcessor = PackagerProcessors.findProcessor(context, packager);
        if (!packagerProcessor.supportsDistribution(distribution)) {
            context.getLogger().info(RB.$("distributions.not.supported.distribution"), distributionName, distribution.getType());
            return;
        }

        context.getLogger().info(RB.$("distributions.apply.action.distribution"), RB.$("distributions.action.packaging"), distributionName);

        try {
            packagerProcessor.packageDistribution(distribution, initProps());
        } catch (PackagerProcessingException tpe) {
            if (packager.isContinueOnError()) {
                packager.fail();
                context.getLogger().warn(RB.$("distributions.failure"), tpe.getMessage());
                context.getLogger().trace(tpe);
            } else {
                throw tpe;
            }
        }
    }

    public void publishDistribution() throws PackagerProcessingException {
        Distribution distribution = context.getModel().findDistribution(distributionName);
        Packager packager = distribution.getPackager(packagerName);
        if (!packager.isEnabled()) {
            context.getLogger().debug(RB.$("distributions.skip.distribution"), distributionName);
            return;
        }
        if (packager.isFailed()) {
            context.getLogger().warn(RB.$("distributions.previous.failure"));
            return;
        }

        PackagerProcessor<Packager> packagerProcessor = PackagerProcessors.findProcessor(context, packager);
        if (!packagerProcessor.supportsDistribution(distribution)) {
            context.getLogger().info(RB.$("distributions.not.supported.distribution"), distributionName, distribution.getType());
            return;
        }

        context.getLogger().info(RB.$("distributions.apply.action.distribution"), RB.$("distributions.action.publishing"), distributionName);

        try {
            packagerProcessor.publishDistribution(distribution, initProps());
        } catch (PackagerProcessingException tpe) {
            if (packager.isContinueOnError()) {
                packager.fail();
                context.getLogger().warn(RB.$("distributions.failure"), tpe.getMessage());
                context.getLogger().trace(tpe);
            } else {
                throw tpe;
            }
        }
    }

    private Map<String, Object> initProps() {
        Map<String, Object> props = context.props();
        props.put(Constants.KEY_PREPARE_DIRECTORY, context.getPrepareDirectory());
        props.put(Constants.KEY_PACKAGE_DIRECTORY, context.getPackageDirectory());
        props.put(Constants.KEY_DISTRIBUTION_PREPARE_DIRECTORY, context.getPrepareDirectory()
            .resolve(distributionName)
            .resolve(packagerName));
        props.put(Constants.KEY_DISTRIBUTION_PACKAGE_DIRECTORY, context.getPackageDirectory()
            .resolve(distributionName)
            .resolve(packagerName));
        return props;
    }

    public static DistributionProcessorBuilder builder() {
        return new DistributionProcessorBuilder();
    }

    public static class DistributionProcessorBuilder {
        private JReleaserContext context;
        private String distributionName;
        private String packagerName;

        public DistributionProcessorBuilder context(JReleaserContext context) {
            this.context = requireNonNull(context, "'context' must not be null");
            return this;
        }

        public DistributionProcessorBuilder distributionName(String distributionName) {
            this.distributionName = requireNonBlank(distributionName, "'distributionName' must not be blank");
            return this;
        }

        public DistributionProcessorBuilder packagerName(String packagerName) {
            this.packagerName = requireNonBlank(packagerName, "'packagerName' must not be blank");
            return this;
        }

        public DistributionProcessor build() {
            requireNonNull(context, "'context' must not be null");
            requireNonBlank(distributionName, "'distributionName' must not be blank");
            requireNonBlank(packagerName, "'packagerName' must not be blank");
            return new DistributionProcessor(context, distributionName, packagerName);
        }
    }
}
