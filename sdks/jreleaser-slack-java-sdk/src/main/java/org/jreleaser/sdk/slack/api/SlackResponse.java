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
package org.jreleaser.sdk.slack.api;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class SlackResponse {
    private boolean ok;
    private String warning;
    private String error;
    private String needed;
    private String provided;
    private String deprecatedArgument;

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getNeeded() {
        return needed;
    }

    public void setNeeded(String needed) {
        this.needed = needed;
    }

    public String getProvided() {
        return provided;
    }

    public void setProvided(String provided) {
        this.provided = provided;
    }

    public String getDeprecatedArgument() {
        return deprecatedArgument;
    }

    public void setDeprecatedArgument(String deprecatedArgument) {
        this.deprecatedArgument = deprecatedArgument;
    }

    @Override
    public String toString() {
        return "MessageResponse[" +
            "ok=" + ok +
            ", warning='" + warning + '\'' +
            ", error='" + error + '\'' +
            ", needed='" + needed + '\'' +
            ", provided='" + provided + '\'' +
            ", deprecatedArgument='" + deprecatedArgument + '\'' +
            "]";
    }
}
