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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

class AbstractMavenDeployerTest {
    @Test
    void throwsExceptionWhenNoExpirationDate() {
        try (MockedStatic<SigningUtils> signingUtils = mockStatic(SigningUtils.class)) {
            signingUtils.when(() -> SigningUtils.getPublicKeyID(any())).thenReturn(Optional.of("ABCDEF"));
            signingUtils.when(() -> SigningUtils.getFingerprint(any())).thenReturn(Optional.of("ABCDEF123456"));
            // Set expiration date to Instant.EPOCH
            signingUtils.when(() -> SigningUtils.getExpirationDateOfPublicKey(any()))
                .thenReturn(Optional.of(Instant.EPOCH));

            org.jreleaser.model.internal.deploy.maven.MavenDeployer<org.jreleaser.model.api.deploy.maven.MavenDeployer> mavenDeployer =
                mock(org.jreleaser.model.internal.deploy.maven.MavenDeployer.class);
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
            assertTrue(cause instanceof JReleaserException, "Expected JReleaserException but got: " + cause);
            String expectedMessage = org.jreleaser.bundle.RB.$("signing.public.key.no.expiration.date", "ABCDEF");
            assertTrue(cause.getMessage().contains(expectedMessage), "Actual message: " + cause.getMessage());
        } catch (Exception e) {
            fail(e);
        }
    }
    @Test
    void throwsExceptionWhenPublicKeyIdNotFound() {
        try (MockedStatic<SigningUtils> signingUtils = mockStatic(SigningUtils.class)) {
            signingUtils.when(() -> SigningUtils.getPublicKeyID(any())).thenReturn(Optional.empty());
            signingUtils.when(() -> SigningUtils.getFingerprint(any())).thenReturn(Optional.of("ABCDEF123456"));
            signingUtils.when(() -> SigningUtils.getExpirationDateOfPublicKey(any())).thenReturn(Optional.of(Instant.now().plusSeconds(3600)));

            org.jreleaser.model.internal.deploy.maven.MavenDeployer<org.jreleaser.model.api.deploy.maven.MavenDeployer> mavenDeployer =
                mock(org.jreleaser.model.internal.deploy.maven.MavenDeployer.class);
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
            assertTrue(cause instanceof JReleaserException, "Expected JReleaserException but got: " + cause);
            String expectedMessage = org.jreleaser.bundle.RB.$("ERROR_public_key_not_found");
            assertTrue(cause.getMessage().contains(expectedMessage), "Actual message: " + cause.getMessage());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void throwsExceptionWhenFingerprintNotFound() {
        try (MockedStatic<SigningUtils> signingUtils = mockStatic(SigningUtils.class)) {
            signingUtils.when(() -> SigningUtils.getPublicKeyID(any())).thenReturn(Optional.of("ABCDEF"));
            signingUtils.when(() -> SigningUtils.getFingerprint(any())).thenReturn(Optional.empty());
            signingUtils.when(() -> SigningUtils.getExpirationDateOfPublicKey(any())).thenReturn(Optional.of(Instant.now().plusSeconds(3600)));

            org.jreleaser.model.internal.deploy.maven.MavenDeployer<org.jreleaser.model.api.deploy.maven.MavenDeployer> mavenDeployer =
                mock(org.jreleaser.model.internal.deploy.maven.MavenDeployer.class);
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
            assertTrue(cause instanceof JReleaserException, "Expected JReleaserException but got: " + cause);
            String expectedMessage = org.jreleaser.bundle.RB.$("ERROR_public_key_not_found");
            assertTrue(cause.getMessage().contains(expectedMessage), "Actual message: " + cause.getMessage());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void throwsExceptionWhenSigningUtilsThrows() {
        try (MockedStatic<SigningUtils> signingUtils = mockStatic(SigningUtils.class)) {
            signingUtils.when(() -> SigningUtils.getPublicKeyID(any())).thenThrow(new org.jreleaser.model.api.signing.SigningException("fail"));

            org.jreleaser.model.internal.deploy.maven.MavenDeployer<org.jreleaser.model.api.deploy.maven.MavenDeployer> mavenDeployer =
                mock(org.jreleaser.model.internal.deploy.maven.MavenDeployer.class);
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
            assertTrue(cause instanceof JReleaserException, "Expected JReleaserException but got: " + cause);
            String expectedMessage = org.jreleaser.bundle.RB.$("ERROR_public_key_not_found");
            assertTrue(cause.getMessage().contains(expectedMessage), "Actual message: " + cause.getMessage());
        } catch (Exception e) {
            fail(e);
        }
    }
    private JReleaserContext context;

    @BeforeEach
    void setup() {
        context = mock(JReleaserContext.class, RETURNS_DEEP_STUBS);
        when(context.getLogger()).thenReturn(mock(JReleaserLogger.class));
    }


    static class TestMavenDeployer extends AbstractMavenDeployer<org.jreleaser.model.api.deploy.maven.MavenDeployer, org.jreleaser.model.internal.deploy.maven.MavenDeployer<org.jreleaser.model.api.deploy.maven.MavenDeployer>> {
        private final org.jreleaser.model.internal.deploy.maven.MavenDeployer<org.jreleaser.model.api.deploy.maven.MavenDeployer> mavenDeployer;

        TestMavenDeployer(JReleaserContext context, org.jreleaser.model.internal.deploy.maven.MavenDeployer<org.jreleaser.model.api.deploy.maven.MavenDeployer> mavenDeployer) {
            super(context);
            this.mavenDeployer = mavenDeployer;
        }

        @Override
        public org.jreleaser.model.internal.deploy.maven.MavenDeployer<org.jreleaser.model.api.deploy.maven.MavenDeployer> getDeployer() {
            return mavenDeployer;
        }

        @Override
        public String getType() {
            return "test";
        }

        public void setDeployer(org.jreleaser.model.internal.deploy.maven.MavenDeployer<org.jreleaser.model.api.deploy.maven.MavenDeployer> deployer) {}

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


            org.jreleaser.model.internal.deploy.maven.MavenDeployer<org.jreleaser.model.api.deploy.maven.MavenDeployer> mavenDeployer =
                mock(org.jreleaser.model.internal.deploy.maven.MavenDeployer.class);
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
            assertTrue(cause instanceof JReleaserException, "Expected JReleaserException but got: " + cause);
            assertTrue(cause.getMessage().contains("Signing key ABCDEF expired at"), "Actual message: " + cause.getMessage());
        } catch (Exception e) {
            fail(e);
        }
    }
}
