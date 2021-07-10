/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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

import org.jreleaser.util.Env;

import java.util.Map;

import static org.jreleaser.util.Constants.HIDE;
import static org.jreleaser.util.Constants.UNSET;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class Artifactory extends AbstractUploader {
    public static final String TYPE = "artifactory";

    private String username;
    private String password;
    private Authorization authorization;

    public Artifactory() {
        super(TYPE);
    }

    void setAll(Artifactory artifactory) {
        super.setAll(artifactory);
        this.username = artifactory.username;
        this.password = artifactory.password;
        this.authorization = artifactory.authorization;
    }

    public Authorization resolveAuthorization() {
        if (null == authorization) {
            authorization = Authorization.BEARER;
        }

        return authorization;
    }

    public String getResolvedUsername() {
        return Env.resolve("ARTIFACTORY_" + Env.toVar(name) + "_USERNAME", username);
    }

    public String getResolvedPassword() {
        return Env.resolve("ARTIFACTORY_" + Env.toVar(name) + "_PASSWORD", password);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Deprecated
    public String getTarget() {
        System.out.println("artifactory.target has been deprecated since 0.6.0 and will be removed in the future. Use artifactory.uploadUrl instead");
        return getUploadUrl();
    }

    @Deprecated
    public void setTarget(String target) {
        System.out.println("artifactory.target has been deprecated since 0.6.0 and will be removed in the future. Use artifactory.uploadUrl instead");
        setUploadUrl(target);
    }

    public Authorization getAuthorization() {
        return authorization;
    }

    public void setAuthorization(Authorization authorization) {
        this.authorization = authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = Authorization.of(authorization);
    }

    @Override
    protected void asMap(Map<String, Object> props, boolean full) {
        props.put("authorization", authorization);
        props.put("username", isNotBlank(getResolvedUsername()) ? HIDE : UNSET);
        props.put("password", isNotBlank(getResolvedPassword()) ? HIDE : UNSET);
    }
}
