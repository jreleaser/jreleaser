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

import org.jreleaser.util.Env;

import java.util.Map;

import static org.jreleaser.util.Constants.HIDE;
import static org.jreleaser.util.Constants.UNSET;
import static org.jreleaser.util.MustacheUtils.applyTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
public class Artifactory extends AbstractUploader {
    public static final String NAME = "artifactory";

    private String target;
    private String username;
    private String password;
    private String token;

    public Artifactory() {
        super(NAME);
    }

    void setAll(Artifactory artifactory) {
        super.setAll(artifactory);
        this.username = artifactory.username;
        this.password = artifactory.password;
        this.target = artifactory.target;
        this.token = artifactory.token;
    }

    @Override
    public String getPrefix() {
        return NAME;
    }

    public String getResolvedTarget(JReleaserContext context) {
        Map<String, Object> props = context.props();
        props.putAll(getResolvedExtraProperties());
        return applyTemplate(target, props);
    }

    public String getResolvedUsername() {
        return Env.resolve("ARTIFACTORY_" + Env.toVar(name) + "_USERNAME", username);
    }

    public String getResolvedPassword() {
        return Env.resolve("ARTIFACTORY_" + Env.toVar(name) + "_PASSWORD", password);
    }

    public String getResolvedToken() {
        return Env.resolve("ARTIFACTORY_" + Env.toVar(name) + "_TOKEN", token);
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

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    protected void asMap(Map<String, Object> props, boolean full) {
        props.put("target", target);
        props.put("username", isNotBlank(getResolvedUsername()) ? HIDE : UNSET);
        props.put("password", isNotBlank(getResolvedPassword()) ? HIDE : UNSET);
        props.put("token", isNotBlank(getResolvedToken()) ? HIDE : UNSET);
    }
}
