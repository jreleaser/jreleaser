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
package org.jreleaser.sdk.teams;

import org.apache.commons.io.IOUtils;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.model.Teams;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.model.announcer.spi.Announcer;
import org.jreleaser.util.Constants;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.jreleaser.util.MustacheUtils.passThrough;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class TeamsAnnouncer implements Announcer {
    private final JReleaserContext context;

    TeamsAnnouncer(JReleaserContext context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return org.jreleaser.model.Teams.NAME;
    }

    @Override
    public boolean isEnabled() {
        return context.getModel().getAnnounce().getTeams().isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        Teams teams = context.getModel().getAnnounce().getTeams();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(Constants.KEY_CHANGELOG, passThrough(convertLineEndings(context.getChangelog())));
        context.getModel().getRelease().getGitService().fillProps(props, context.getModel());
        String message = teams.getResolvedMessageTemplate(context, props);

        context.getLogger().info("message: {}", message);

        try {
            // create URL
            URL url = new URL(teams.getResolvedWebhook());
            // open connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // set options
            connection.setConnectTimeout(teams.getConnectTimeout() * 1000);
            connection.setReadTimeout(teams.getReadTimeout() * 1000);
            connection.setAllowUserInteraction(false);
            connection.setInstanceFollowRedirects(true);

            connection.setRequestMethod("POST");
            connection.addRequestProperty("Content-Type", "application/json; utf-8");
            connection.addRequestProperty("Accept", "application/json");
            connection.addRequestProperty("User-Agent", "JReleaser/" + JReleaserVersion.getPlainVersion());
            connection.setDoOutput(true);

            // write message
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = message.getBytes(UTF_8);
                os.write(input, 0, input.length);
            }

            // handle response
            int status = connection.getResponseCode();
            if (status >= 400) {
                String reason = connection.getResponseMessage();
                Reader reader = new InputStreamReader(connection.getErrorStream(), UTF_8);
                message = IOUtils.toString(reader);
                StringBuilder b = new StringBuilder("Teams webhook replied with: ")
                    .append(status);
                if (isNotBlank(reason)) {
                    b.append(" reason: ")
                        .append(reason)
                        .append(",");
                }
                if (isNotBlank(message)) {
                    b.append(message);
                }
                throw new AnnounceException(b.toString());
            }
        } catch (IOException e) {
            throw new AnnounceException(e);
        }
    }

    public static String convertLineEndings(String str) {
        return str.replaceAll("\\n", "\\\\n\\\\n");
    }
}
