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
package org.jreleaser.model;

import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Scoop extends AbstractTool {
    public static final String TOOL_NAME = "scoop";

    private String checkverUrl;
    private String autoupdateUrl;

    public Scoop() {
        super(TOOL_NAME);
    }

    void setAll(Scoop scoop) {
        super.setAll(scoop);
        this.checkverUrl = scoop.checkverUrl;
        this.autoupdateUrl = scoop.autoupdateUrl;
    }

    public String getCheckverUrl() {
        return checkverUrl;
    }

    public void setCheckverUrl(String checkverUrl) {
        this.checkverUrl = checkverUrl;
    }

    public String getAutoupdateUrl() {
        return autoupdateUrl;
    }

    public void setAutoupdateUrl(String autoupdateUrl) {
        this.autoupdateUrl = autoupdateUrl;
    }

    @Override
    protected void asMap(Map<String, Object> props) {
        props.put("checkverUrl", checkverUrl);
        props.put("autoupdateUrl", autoupdateUrl);
    }
}
