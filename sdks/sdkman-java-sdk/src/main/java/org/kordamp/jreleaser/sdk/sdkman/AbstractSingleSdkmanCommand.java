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
package org.kordamp.jreleaser.sdk.sdkman;

import okhttp3.Request;
import okhttp3.Response;

import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractSingleSdkmanCommand extends AbstractSdkmanCommand {
    protected AbstractSingleSdkmanCommand(String consumerKey,
                                          String consumerToken,
                                          String candidate,
                                          String version,
                                          String apiHost,
                                          boolean https) {
        super(consumerKey,
                consumerToken,
                candidate,
                version,
                apiHost,
                https);
    }

    protected abstract Request createRequest(Map<String, String> payload);

    @Override
    public void execute() throws SdkmanException {
        try {
            Response resp = execCall(createRequest(getPayload()));
            int statusCode = resp.code();
            if (statusCode < 200 || statusCode >= 300) {
                throw new IllegalStateException("Server returned error " + resp.message());
            }
        } catch (Exception e) {
            throw new SdkmanException("Sdk vendor operation failed", e);
        }
    }
}
