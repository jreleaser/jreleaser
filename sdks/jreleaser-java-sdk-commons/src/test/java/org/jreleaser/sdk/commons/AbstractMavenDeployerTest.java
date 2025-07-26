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
package org.jreleaser.sdk.commons;


import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.sdk.signing.SigningUtils;
import org.jreleaser.logging.JReleaserLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AbstractMavenDeployerTest {
    private JReleaserContext context;

    @BeforeEach
    void setup() {
        context = mock(JReleaserContext.class, RETURNS_DEEP_STUBS);
        when(context.getLogger()).thenReturn(mock(JReleaserLogger.class));
    }


    static class TestMavenDeployer extends AbstractMavenDeployer {
        private final org.jreleaser.model.internal.deploy.maven.MavenDeployer mavenDeployer;

        TestMavenDeployer(JReleaserContext context, org.jreleaser.model.internal.deploy.maven.MavenDeployer mavenDeployer) {
            super(context);
            this.mavenDeployer = mavenDeployer;
        }

        @Override
        public org.jreleaser.model.internal.deploy.maven.MavenDeployer getDeployer() {
            return mavenDeployer;
        }

        @Override
        public String getType() {
            return "test";
        }

        public void setDeployer(org.jreleaser.model.internal.deploy.maven.MavenDeployer deployer) {}

        @Override
        public void deploy(String s) {}
    }

    @Test
    void throwsExceptionWhenKeyExpired() {
        try (MockedStatic<SigningUtils> signingUtils = mockStatic(SigningUtils.class)) {
            signingUtils.when(() -> SigningUtils.getPublicKeyID(any())).thenReturn(Optional.of("ABCDEF"));
            signingUtils.when(() -> SigningUtils.getFingerprint(any())).thenReturn(Optional.of("ABCDEF123456"));
            // Set expiration date in the past
            signingUtils.when(() -> SigningUtils.getExpirationDateOfPublicKey(any()))
                .thenReturn(Optional.of(Instant.now().minusSeconds(3600)));


            org.jreleaser.model.internal.deploy.maven.MavenDeployer<?> mavenDeployer = mock(org.jreleaser.model.internal.deploy.maven.MavenDeployer.class);
            TestMavenDeployer deployer = new TestMavenDeployer(context, mavenDeployer);

            Throwable thrown = assertThrows(Exception.class, () -> {
                var method = AbstractMavenDeployer.class.getDeclaredMethod("verifyKeyIsValid");
                method.setAccessible(true);
                method.invoke(deployer);
            });
            Throwable cause = thrown;
            if (cause instanceof java.lang.reflect.InvocationTargetException && cause.getCause() != null) {
                cause = cause.getCause();
            }
            if (cause instanceof java.lang.reflect.InvocationTargetException && cause.getCause() != null) {
                cause = cause.getCause();
            }
            System.out.println("Actual exception: " + cause);
            System.out.println("Actual message: " + cause.getMessage());
            assertTrue(cause instanceof JReleaserException, "Expected JReleaserException but got: " + cause);
            assertTrue(cause.getMessage().contains("Unexpected error when signing key"), "Actual message: " + cause.getMessage());
        } catch (Exception e) {
            fail(e);
        }
    }
}
