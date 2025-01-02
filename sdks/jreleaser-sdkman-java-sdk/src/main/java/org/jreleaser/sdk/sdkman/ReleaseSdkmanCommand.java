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

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ReleaseSdkmanCommand extends AbstractSdkmanCommand {
    private final Map<String, String> platforms = new LinkedHashMap<>();

    private ReleaseSdkmanCommand(JReleaserContext context,
                                 String apiHost,
                                 int connectTimeout,
                                 int readTimeout,
                                 String consumerKey,
                                 String consumerToken,
                                 String candidate,
                                 String version,
                                 boolean dryrun,
                                 Map<String, String> platforms) {
        super(context, apiHost, connectTimeout, readTimeout, consumerKey, consumerToken, candidate, version, dryrun);
        this.platforms.putAll(platforms);
    }

    @Override
    public void execute() throws SdkmanException {
        sdkman.release(candidate, version, platforms);
    }

    public static Builder builder(JReleaserContext context) {
        return new Builder(context);
    }

    public static class Builder extends AbstractSdkmanCommand.Builder<Builder> {
        private final Map<String, String> platforms = new LinkedHashMap<>();
        private String url;

        protected Builder(JReleaserContext context) {
            super(context);
        }

        /**
         * The URL from where the candidate version can be downloaded
         */
        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * Platform to downloadable URL mappings.
         * Supported platforms are:
         * <ul>
         * <li>MAC_OSX</li>
         * <li>WINDOWS_64</li>
         * <li>LINUX_64</li>
         * <li>LINUX_32</li>
         * </ul>
         * Example:
         * <pre>
         *     "MAC_OSX"   :"https://host/micronaut-x.y.z-macosx.zip"
         *     "LINUX_64"  :"https://host/micronaut-x.y.z-linux64.zip"
         *     "WINDOWS_64":"https://host/micronaut-x.y.z-win.zip"
         * </pre>
         */
        public Builder platforms(Map<String, String> platforms) {
            this.platforms.putAll(platforms);
            return this;
        }

        public Builder platform(String platform, String url) {
            this.platforms.put(
                requireNonBlank(platform, "'platform' must not be blank").trim(),
                requireNonBlank(url, "'url' must not be blank").trim());
            return this;
        }

        public ReleaseSdkmanCommand build() {
            requireNonBlank(apiHost, "'apiHost' must not be blank");
            requireNonBlank(consumerKey, "'consumerKey' must not be blank");
            requireNonBlank(consumerToken, "'consumerToken' must not be blank");
            requireNonBlank(candidate, "'candidate' must not be blank");
            requireNonBlank(version, "'version' must not be blank");

            // url is required if platforms is empty
            if ((platforms.isEmpty()) && isBlank(url)) {
                throw new IllegalArgumentException("Missing url");
            }
            if (isNotBlank(url)) {
                platforms.put("UNIVERSAL", url);
            }

            return new ReleaseSdkmanCommand(
                context,
                apiHost,
                connectTimeout,
                readTimeout,
                consumerKey,
                consumerToken,
                candidate,
                version,
                dryrun,
                platforms);
        }
    }
}
