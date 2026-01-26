/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.sdk.mavencentral.api;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Stepan Romankov
 * @since 1.23.0
 */
class DeploymentTest {

    @Test
    void isTransitioning_withPendingState_returnsTrue() {
        Deployment deployment = new Deployment();
        deployment.setDeploymentState(State.PENDING);
        assertTrue(deployment.isTransitioning());
    }

    @Test
    void isTransitioning_withValidatingState_returnsTrue() {
        Deployment deployment = new Deployment();
        deployment.setDeploymentState(State.VALIDATING);
        assertTrue(deployment.isTransitioning());
    }

    @Test
    void isTransitioning_withPublishingState_returnsTrue() {
        Deployment deployment = new Deployment();
        deployment.setDeploymentState(State.PUBLISHING);
        assertTrue(deployment.isTransitioning());
    }

    @Test
    void isTransitioning_withPublishedState_returnsFalse() {
        Deployment deployment = new Deployment();
        deployment.setDeploymentState(State.PUBLISHED);
        assertFalse(deployment.isTransitioning());
    }

    @Test
    void isTransitioning_withFailedState_returnsFalse() {
        Deployment deployment = new Deployment();
        deployment.setDeploymentState(State.FAILED);
        assertFalse(deployment.isTransitioning());
    }

    @Test
    void isTransitioningWithAcceptableStates_whenPublishingIsAcceptable_returnsFalse() {
        Deployment deployment = new Deployment();
        deployment.setDeploymentState(State.PUBLISHING);

        // When skipPublicationCheck is true, PUBLISHING should be acceptable
        Set<State> acceptableStates = Set.of(State.PUBLISHING, State.PUBLISHED, State.FAILED);

        assertFalse(deployment.isTransitioning(acceptableStates));
    }

    @Test
    void isTransitioningWithAcceptableStates_whenPublishingIsNotAcceptable_returnsTrue() {
        Deployment deployment = new Deployment();
        deployment.setDeploymentState(State.PUBLISHING);

        // When skipPublicationCheck is false, only PUBLISHED and FAILED are acceptable
        Set<State> acceptableStates = Set.of(State.PUBLISHED, State.FAILED);

        assertTrue(deployment.isTransitioning(acceptableStates));
    }

    @Test
    void isTransitioningWithAcceptableStates_withValidatedState_returnsFalse() {
        Deployment deployment = new Deployment();
        deployment.setDeploymentState(State.VALIDATED);

        // During upload, VALIDATED is an acceptable terminal state
        Set<State> acceptableStates = Set.of(State.VALIDATED, State.FAILED);

        assertFalse(deployment.isTransitioning(acceptableStates));
    }

    @Test
    void isTransitioningWithAcceptableStates_withPendingState_returnsTrue() {
        Deployment deployment = new Deployment();
        deployment.setDeploymentState(State.PENDING);

        Set<State> acceptableStates = Set.of(State.PUBLISHING, State.PUBLISHED, State.FAILED);

        assertTrue(deployment.isTransitioning(acceptableStates));
    }
}
