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
package org.jreleaser.sdk.ftp;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.Ftp;
import org.jreleaser.model.FtpDownloader;
import org.jreleaser.model.FtpUploader;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.TimeoutAware;
import org.jreleaser.model.downloader.spi.DownloadException;
import org.jreleaser.model.uploader.spi.UploadException;

import java.io.IOException;

import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public class FtpUtils {
    private FtpUtils() {
        // noop
    }

    public static FTPClient open(JReleaserContext context, FtpDownloader downloader) throws DownloadException {
        try {
            return ftpClient(context, downloader);
        } catch (IOException e) {
            throw new DownloadException(RB.$("ERROR_unexpected_download_from", downloader.getName()), e);
        }
    }

    public static FTPClient open(JReleaserContext context, FtpUploader uploader) throws UploadException {
        if (context.isDryrun()) return null;

        try {
            return ftpClient(context, uploader);
        } catch (IOException e) {
            throw new UploadException(RB.$("ERROR_unexpected_upload_to", uploader.getName()), e);
        }
    }

    private static <T extends Ftp & TimeoutAware> FTPClient ftpClient(JReleaserContext context, T ftp) throws IOException {
        FTPClient client = new FTPClient();
        client.setConnectTimeout(ftp.getConnectTimeout() * 1000);
        client.setSoTimeout(ftp.getReadTimeout() * 1000);

        client.addProtocolCommandListener(new FtpCommandListener(context));

        client.connect(ftp.getResolvedHost(), ftp.getResolvedPort());
        int reply = client.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            client.disconnect();
            throw new IOException(RB.$("ERROR_unexpected_error"));
        }

        String username = ftp.getResolvedUsername();
        if (isBlank(username)) {
            username = "anonymous";
        }

        client.login(username, ftp.getResolvedPassword());
        reply = client.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            client.disconnect();
            throw new IOException(RB.$("ERROR_login"));
        }

        return client;
    }

    public static void close(FtpUploader uploader, FTPClient ftp) throws UploadException {
        try {
            if (null != ftp) ftp.disconnect();
        } catch (IOException e) {
            throw new UploadException(RB.$("ERROR_disconnect", uploader.getName()), e);
        }
    }

    public static void close(FtpDownloader downloader, FTPClient ftp) throws DownloadException {
        try {
            ftp.disconnect();
        } catch (IOException e) {
            throw new DownloadException(RB.$("ERROR_disconnect", downloader.getName()), e);
        }
    }

    private static class FtpCommandListener implements ProtocolCommandListener {
        private static final String LOGIN = "LOGIN";

        private final JReleaserContext context;

        public FtpCommandListener(JReleaserContext context) {
            this.context = context;
        }

        @Override
        public void protocolCommandSent(ProtocolCommandEvent event) {
            StringBuilder msg = new StringBuilder("> ");

            String cmd = event.getCommand();
            if ("PASS".equalsIgnoreCase(cmd) || "USER".equalsIgnoreCase(cmd)) {
                msg.append(cmd).append(" *******");
            } else {
                if (LOGIN.equalsIgnoreCase(cmd)) {
                    String m = event.getMessage();
                    m = m.substring(0, msg.indexOf(LOGIN) + LOGIN.length());
                    msg.append(m).append(" *******");
                } else {
                    msg.append(event.getMessage().trim());
                }
            }

            context.getLogger().debug(msg.toString());
        }

        @Override
        public void protocolReplyReceived(ProtocolCommandEvent event) {
            context.getLogger().debug("< " + event.getMessage().trim());
            if (FTPReply.isNegativeTransient(event.getReplyCode()) ||
                FTPReply.isNegativePermanent(event.getReplyCode())) {
                throw new IllegalStateException(event.getMessage());
            }
        }
    }
}
