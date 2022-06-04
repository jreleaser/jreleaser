/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
import static org.jreleaser.util.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public class Mastodon extends AbstractAnnouncer<Mastodon> {
    public static final String NAME = "mastodon";
    public static final String MASTODON_ACCESS_TOKEN = "MASTODON_ACCESS_TOKEN";

    private String host;
    private String accessToken;
    private String status;

    public Mastodon() {
        super(NAME);
    }

    @Override
    public void merge(Mastodon mastodon) {
        freezeCheck();
        super.merge(mastodon);
        this.host = merge(this.host, mastodon.host);
        this.accessToken = merge(this.accessToken, mastodon.accessToken);
        this.status = merge(this.status, mastodon.status);
    }

    public String getResolvedStatus(JReleaserContext context) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, getResolvedExtraProperties());
        context.getModel().getRelease().getGitService().fillProps(props, context.getModel());
        return resolveTemplate(status, props);
    }

    public String getResolvedAccessToken() {
        return Env.env(MASTODON_ACCESS_TOKEN, accessToken);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        freezeCheck();
        this.host = host;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        freezeCheck();
        this.accessToken = accessToken;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        freezeCheck();
        this.status = status;
    }

    @Override
    protected void asMap(Map<String, Object> props, boolean full) {
        props.put("host", host);
        props.put("accessToken", isNotBlank(getResolvedAccessToken()) ? HIDE : UNSET);
        props.put("status", status);
    }
}
