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
package org.jreleaser.sdk.sdkman;

import org.jreleaser.model.api.JReleaserContext;

import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class DefaultSdkmanCommand extends AbstractSdkmanCommand {
    private DefaultSdkmanCommand(JReleaserContext context,
                                 String apiHost,
                                 int connectTimeout,
                                 int readTimeout,
                                 String consumerKey,
                                 String consumerToken,
                                 String candidate,
                                 String version,
                                 boolean dryrun) {
        super(context, apiHost, connectTimeout, readTimeout, consumerKey, consumerToken, candidate, version, dryrun);
    }

    @Override
    public void execute() throws SdkmanException {
        sdkman.setDefault(candidate, version);
    }

    public static Builder builder(JReleaserContext context) {
        return new Builder(context);
    }

    public static class Builder extends AbstractSdkmanCommand.Builder<Builder> {
        protected Builder(JReleaserContext context) {
            super(context);
        }

        public DefaultSdkmanCommand build() {
            requireNonBlank(apiHost, "'apiHost' must not be blank");
            requireNonBlank(consumerKey, "'consumerKey' must not be blank");
            requireNonBlank(consumerToken, "'consumerToken' must not be blank");
            requireNonBlank(candidate, "'candidate' must not be blank");
            requireNonBlank(version, "'version' must not be blank");

            return new DefaultSdkmanCommand(
                context,
                apiHost,
                connectTimeout,
                readTimeout,
                consumerKey,
                consumerToken,
                candidate,
                version,
                dryrun);
        }
    }
}
