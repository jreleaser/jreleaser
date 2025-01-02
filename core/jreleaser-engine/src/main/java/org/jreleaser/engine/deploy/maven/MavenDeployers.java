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
package org.jreleaser.engine.deploy.maven;

import org.jreleaser.bundle.RB;
import org.jreleaser.extensions.api.workflow.WorkflowListenerException;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.hooks.ExecutionEvent;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.deploy.maven.Maven;
import org.jreleaser.model.internal.deploy.maven.MavenDeployer;
import org.jreleaser.model.spi.deploy.DeployException;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.model.internal.JReleaserSupport.supportedMavenDeployers;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public final class MavenDeployers {
    private MavenDeployers() {
        // noop
    }

    public static void deploy(JReleaserContext context) {
        context.getLogger().info(RB.$("deployers.maven.header"));
        context.getLogger().increaseIndent();
        context.getLogger().setPrefix("maven");

        Maven maven = context.getModel().getDeploy().getMaven();
        if (!maven.isEnabled()) {
            context.getLogger().info(RB.$("deployers.not.enabled"));
            context.getLogger().decreaseIndent();
            context.getLogger().restorePrefix();
            return;
        }

        try {
            doDeploy(context, maven);
        } finally {
            context.getLogger().decreaseIndent();
            context.getLogger().restorePrefix();
        }
    }

    private static void doDeploy(JReleaserContext context, Maven maven) {
        if (!context.getIncludedDeployerTypes().isEmpty()) {
            for (String deployerType : context.getIncludedDeployerTypes()) {
                // check if the deployerType is valid
                if (!supportedMavenDeployers().contains(deployerType)) {
                    context.getLogger().warn(RB.$("ERROR_unsupported_deployer", deployerType));
                    continue;
                }

                Map<String, MavenDeployer<?>> deployers = maven.findMavenDeployersByType(deployerType);

                if (deployers.isEmpty()) {
                    context.getLogger().info(RB.$("deployers.no.match"), deployerType);
                    return;
                }

                if (!context.getIncludedDeployerNames().isEmpty()) {
                    boolean deployed = false;
                    for (String deployerName : context.getIncludedDeployerNames()) {
                        if (!deployers.containsKey(deployerName)) {
                            context.getLogger().warn(RB.$("deployers.deployer.not.configured"), deployerType, deployerName);
                            continue;
                        }

                        MavenDeployer<?> deployer = deployers.get(deployerName);
                        if (!deployer.isEnabled()) {
                            context.getLogger().info(RB.$("deployers.deployer.disabled"), deployerType, deployerName);
                            continue;
                        }

                        context.getLogger().info(RB.$("deployers.deploy.with"),
                            deployerType,
                            deployerName);
                        if (deploy(context, deployer)) deployed = true;
                    }

                    if (!deployed) {
                        context.getLogger().info(RB.$("deployers.not.triggered"));
                    }
                } else {
                    context.getLogger().info(RB.$("deployers.deploy.all.artifacts.with"), deployerType);
                    boolean[] deployed = new boolean[]{false};
                    deployers.values().forEach(deployer -> {
                        if (deploy(context, deployer)) deployed[0] = true;
                    });

                    if (!deployed[0]) {
                        context.getLogger().info(RB.$("deployers.not.triggered"));
                    }
                }
            }
        } else if (!context.getIncludedDeployerNames().isEmpty()) {
            boolean[] deployed = new boolean[]{false};
            for (String deployerName : context.getIncludedDeployerNames()) {
                List<MavenDeployer<?>> filteredDeployers = maven.findAllActiveMavenDeployers().stream()
                    .filter(a -> deployerName.equals(a.getName()))
                    .collect(toList());

                if (!filteredDeployers.isEmpty()) {
                    context.getLogger().info(RB.$("deployers.deploy.all.artifacts.to"), deployerName);
                    filteredDeployers.forEach(deployer -> {
                        if (deploy(context, deployer)) deployed[0] = true;
                    });
                } else {
                    context.getLogger().warn(RB.$("deployers.deployer.not.configured2"), deployerName);
                }
            }

            if (!deployed[0]) {
                context.getLogger().info(RB.$("deployers.not.triggered"));
            }
        } else {
            context.getLogger().info(RB.$("deployers.deploy.all.artifacts"));
            boolean deployed = false;
            for (MavenDeployer<?> deployer : maven.findAllActiveMavenDeployers()) {
                String deployerType = deployer.getType();
                String deployerName = deployer.getName();

                if (context.getExcludedDeployerTypes().contains(deployerType) ||
                    context.getExcludedDeployerNames().contains(deployerName)) {
                    context.getLogger().info(RB.$("deployers.deployer.excluded"), deployerType, deployerName);
                    continue;
                }

                if (deploy(context, deployer)) deployed = true;
            }

            if (!deployed) {
                context.getLogger().info(RB.$("deployers.not.triggered"));
            }
        }
    }

    private static boolean deploy(JReleaserContext context, MavenDeployer<?> deployer) {
        try {
            context.getLogger().increaseIndent();
            context.getLogger().setPrefix(deployer.getType());

            fireDeployEvent(ExecutionEvent.before(JReleaserCommand.DEPLOY.toStep()), context, deployer);

            ProjectMavenDeployer projectDeployer = createProjectDeployer(context, deployer);
            boolean deployed = projectDeployer.deploy();
            fireDeployEvent(ExecutionEvent.success(JReleaserCommand.DEPLOY.toStep()), context, deployer);
            return deployed;
        } catch (DeployException e) {
            fireDeployEvent(ExecutionEvent.failure(JReleaserCommand.DEPLOY.toStep(), e), context, deployer);
            throw new JReleaserException(RB.$("ERROR_unexpected_error"), e);
        } finally {
            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
        }
    }

    private static ProjectMavenDeployer createProjectDeployer(JReleaserContext context,
                                                              MavenDeployer<?> deployer) {
        return ProjectMavenDeployer.builder()
            .context(context)
            .deployer(deployer)
            .build();
    }

    private static void fireDeployEvent(ExecutionEvent event, JReleaserContext context, MavenDeployer<?> deployer) {
        if (!deployer.isEnabled()) return;

        try {
            context.fireDeployStepEvent(event, deployer.asImmutable());
        } catch (WorkflowListenerException e) {
            context.getLogger().error(RB.$("listener.failure", e.getListener().getClass().getName()));
            context.getLogger().trace(e);
            if (event.getType() != ExecutionEvent.Type.FAILURE && !e.getListener().isContinueOnError()) {
                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                } else {
                    throw new JReleaserException(RB.$("ERROR_unexpected_error"), e.getCause());
                }
            }
        }
    }
}
