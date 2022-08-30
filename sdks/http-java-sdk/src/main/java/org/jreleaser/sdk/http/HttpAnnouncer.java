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
package org.jreleaser.sdk.http;

import feign.form.FormData;
import org.jreleaser.model.Http;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.model.announcer.spi.Announcer;
import org.jreleaser.model.uploader.spi.UploadException;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.util.Constants;
import org.jreleaser.util.MustacheUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.Templates.resolveTemplate;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
public class HttpAnnouncer implements Announcer {
    private final JReleaserContext context;

    HttpAnnouncer(JReleaserContext context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return org.jreleaser.model.HttpAnnouncers.NAME;
    }

    @Override
    public boolean isEnabled() {
        return context.getModel().getAnnounce().getConfiguredHttp().isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        Map<String, org.jreleaser.model.HttpAnnouncer> http = context.getModel().getAnnounce().getHttp();

        for (Map.Entry<String, org.jreleaser.model.HttpAnnouncer> e : http.entrySet()) {
            if (e.getValue().isEnabled()) {
                context.getLogger().setPrefix("http." + e.getKey());
                try {
                    announce(e.getValue());
                } catch (AnnounceException x) {
                    context.getLogger().warn(x.getMessage().trim());
                } finally {
                    context.getLogger().restorePrefix();
                }
            }
        }
    }

    public void announce(org.jreleaser.model.HttpAnnouncer announcer) throws AnnounceException {
        String payload = "";
        if (isNotBlank(announcer.getPayload())) {
            payload = announcer.getResolvedPayload(context);
        } else {
            Map<String, Object> props = context.props();
            props.put(Constants.KEY_CHANGELOG, MustacheUtils.passThrough(context.getChangelog()));
            context.getModel().getRelease().getGitService().fillProps(props, context.getModel());
            payload = announcer.getResolvedPayloadTemplate(context, props);
        }

        String resolvedUrl = announcer.getResolvedUrl(context);
        context.getLogger().info("url: {}", resolvedUrl);
        context.getLogger().info("payload: {}", payload);

        if (context.isDryrun()) return;

        String username = announcer.getResolvedUsername();
        String password = announcer.getResolvedPassword();

        if (!context.isDryrun()) {
            try {
                Map<String, String> headers = new LinkedHashMap<>();
                switch (announcer.resolveAuthorization()) {
                    case NONE:
                        break;
                    case BASIC:
                        String auth = username + ":" + password;
                        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
                        auth = new String(encodedAuth);
                        headers.put("Authorization", "Basic " + auth);
                        break;
                    case BEARER:
                        headers.put("Authorization", "Bearer " + password);
                        break;
                }

                resolveHeaders(announcer, headers);

                FormData data = ClientUtils.toFormData(
                    "payload",
                    headers.computeIfAbsent("Content-Type", k -> "text/plain"),
                    payload);

                if (announcer.getMethod() == Http.Method.POST) {
                    ClientUtils.postFile(context.getLogger(),
                        resolvedUrl,
                        announcer.getConnectTimeout(),
                        announcer.getReadTimeout(),
                        data,
                        headers);
                } else {
                    ClientUtils.putFile(context.getLogger(),
                        resolvedUrl,
                        announcer.getConnectTimeout(),
                        announcer.getReadTimeout(),
                        data,
                        headers);
                }
            } catch (IOException | UploadException e) {
                context.getLogger().trace(e);
                throw new AnnounceException(e.getMessage(), e);
            }
        }
    }

    private void resolveHeaders(org.jreleaser.model.HttpAnnouncer announcer, Map<String, String> headers) {
        Map<String, Object> props = context.props();
        announcer.getHeaders().forEach((k, v) -> {
            String value = resolveTemplate(v, props);
            if (isNotBlank(value)) headers.put(k, value);
        });
    }
}
