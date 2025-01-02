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
package org.jreleaser.sdk.http;

import feign.form.FormData;
import org.jreleaser.bundle.RB;
import org.jreleaser.extensions.api.workflow.WorkflowListenerException;
import org.jreleaser.model.Http;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.announce.HttpAnnouncers;
import org.jreleaser.model.api.hooks.ExecutionEvent;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.announce.Announcer;
import org.jreleaser.model.spi.upload.UploadException;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.sdk.commons.ClientUtils;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.3.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class HttpAnnouncer implements Announcer<org.jreleaser.model.api.announce.HttpAnnouncers> {
    private final JReleaserContext context;
    private final org.jreleaser.model.internal.announce.HttpAnnouncers https;

    public HttpAnnouncer(JReleaserContext context) {
        this.context = context;
        this.https = context.getModel().getAnnounce().getConfiguredHttp();
    }

    @Override
    public HttpAnnouncers getAnnouncer() {
        return https.asImmutable();
    }

    @Override
    public String getName() {
        return org.jreleaser.model.api.announce.HttpAnnouncers.TYPE;
    }

    @Override
    public boolean isEnabled() {
        return https.isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        Map<String, org.jreleaser.model.internal.announce.HttpAnnouncer> http = https.getHttp();

        for (Map.Entry<String, org.jreleaser.model.internal.announce.HttpAnnouncer> e : http.entrySet()) {
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

    public void announce(org.jreleaser.model.internal.announce.HttpAnnouncer announcer) throws AnnounceException {
        String payload = "";
        if (isNotBlank(announcer.getPayload())) {
            payload = announcer.getResolvedPayload(context);
        } else {
            TemplateContext props = context.props();
            context.getModel().getRelease().getReleaser().fillProps(props, context.getModel());
            payload = announcer.getResolvedPayloadTemplate(context, props);
        }

        String resolvedUrl = announcer.getResolvedUrl(context);
        context.getLogger().info("url: {}", resolvedUrl);
        context.getLogger().debug("payload: {}", payload);

        if (context.isDryrun()) return;

        fireAnnouncerEvent(ExecutionEvent.before(JReleaserCommand.ANNOUNCE.toStep()), announcer);

        String username = announcer.getUsername();
        String password = announcer.getPassword();

        try {
            Map<String, String> headers = new LinkedHashMap<>();
            switch (announcer.resolveAuthorization()) {
                case NONE:
                    break;
                case BASIC:
                    String auth = username + ":" + password;
                    byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(UTF_8));
                    auth = new String(encodedAuth, UTF_8);
                    headers.put("Authorization", "Basic " + auth);
                    break;
                case BEARER:
                    headers.put("Authorization", announcer.getBearerKeyword() + " " + password);
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

            fireAnnouncerEvent(ExecutionEvent.success(JReleaserCommand.ANNOUNCE.toStep()), announcer);
        } catch (UploadException e) {
            fireAnnouncerEvent(ExecutionEvent.failure(JReleaserCommand.ANNOUNCE.toStep(), e), announcer);

            context.getLogger().trace(e);
            throw new AnnounceException(e.getMessage(), e);
        }
    }

    private void resolveHeaders(org.jreleaser.model.internal.announce.HttpAnnouncer announcer, Map<String, String> headers) {
        TemplateContext props = context.props();
        announcer.getHeaders().forEach((k, v) -> {
            String value = resolveTemplate(v, props);
            if (isNotBlank(value)) headers.put(k, value);
        });
    }

    private void fireAnnouncerEvent(ExecutionEvent event, org.jreleaser.model.internal.announce.HttpAnnouncer http) {
        try {
            context.fireAnnounceStepEvent(event, http.asImmutable());
        } catch (WorkflowListenerException e) {
            context.getLogger().error(RB.$("listener.failure", e.getListener().getClass().getName()));
            context.getLogger().trace(e);
        }
    }
}
