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
package org.jreleaser.sdk.nexus2;

import org.jreleaser.logging.JReleaserLogger;
// Use fully qualified names to avoid confusion between SDK and model classes
import org.jreleaser.model.spi.deploy.DeployException;
import org.jreleaser.sdk.nexus2.api.StagingProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class Nexus2MavenDeployerTest {
    private org.jreleaser.sdk.nexus2.Nexus2MavenDeployer deployer;
    private Nexus2 nexus2;
    private org.jreleaser.model.internal.JReleaserContext context;
    private org.jreleaser.model.api.JReleaserContext apiContext;
    private JReleaserLogger logger;
    private org.jreleaser.model.internal.deploy.maven.Nexus2MavenDeployer modelDeployer;

    @BeforeEach
    void setUp() {
        context = mock(org.jreleaser.model.internal.JReleaserContext.class);
        apiContext = mock(org.jreleaser.model.api.JReleaserContext.class);
        logger = mock(JReleaserLogger.class);
        when(context.getLogger()).thenReturn(logger);
        when(apiContext.getLogger()).thenReturn(logger);
        // Stub apiContext.getModel().getEnvironment() to avoid NPE in Nexus2 constructor
        org.jreleaser.model.api.JReleaserModel model = mock(org.jreleaser.model.api.JReleaserModel.class);
        when(apiContext.getModel()).thenReturn(model);
        org.jreleaser.model.api.environment.Environment env = mock(org.jreleaser.model.api.environment.Environment.class);
        when(model.getEnvironment()).thenReturn(env);
        modelDeployer = mock(org.jreleaser.model.internal.deploy.maven.Nexus2MavenDeployer.class);
        deployer = new org.jreleaser.sdk.nexus2.Nexus2MavenDeployer(context);
        deployer.setDeployer(modelDeployer);
    }

    @Test
    void testCreateStagingRepositoryLogsUrl() throws Exception {
        String groupId = "com.example";
        String stagingProfileId = "profile123";
        String stagingRepositoryId = "repo456";
        String apiHost = "https://nexus.example.com/";



        // Use a test subclass of Nexus2 with required constructor args
        class TestNexus2 extends org.jreleaser.sdk.nexus2.Nexus2 {
            public TestNexus2() {
                super(apiContext, apiHost, "user", "pass", 1, 1, true, 1, 1);
            }
            @Override
            public String createStagingRepository(String profileId, String groupIdParam) {
                assertEquals(stagingProfileId, profileId);
                assertEquals(groupId, groupIdParam);
                return stagingRepositoryId;
            }
            @Override
            public StagingProfileRepository getStagingRepository(String repoId) {
                assertEquals(stagingRepositoryId, repoId);
                return mockRepo(repoId);
            }
            @Override
            public String getApiHost() {
                return apiHost;
            }
        }
        nexus2 = new TestNexus2();

        // Call the method under test
        String result = invokeCreateStagingRepository(deployer, nexus2, groupId, stagingProfileId);

        assertEquals(stagingRepositoryId, result);
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        verify(logger, atLeastOnce()).info(logCaptor.capture());
        // The actual log message uses a placeholder {0}, so check for that
        boolean foundPlaceholderLog = logCaptor.getAllValues().stream().anyMatch(msg -> msg.contains("Staging repository URL: {0}"));
        assertTrue(foundPlaceholderLog, "Expected log message with staging repository URL placeholder: Staging repository URL: {0}");
    }

    // Helper to access the private method via reflection
    private String invokeCreateStagingRepository(org.jreleaser.sdk.nexus2.Nexus2MavenDeployer deployer, Nexus2 nexus2, String groupId, String stagingProfileId) throws DeployException {
        try {
            var m = org.jreleaser.sdk.nexus2.Nexus2MavenDeployer.class.getDeclaredMethod("createStagingRepository", Nexus2.class, String.class, String.class);
            m.setAccessible(true);
            return (String) m.invoke(deployer, nexus2, groupId, stagingProfileId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private StagingProfileRepository mockRepo(String repoId) {
        return new StagingProfileRepository() {
            @Override
            public String getRepositoryId() {
                return repoId;
            }
        };
    }
}
