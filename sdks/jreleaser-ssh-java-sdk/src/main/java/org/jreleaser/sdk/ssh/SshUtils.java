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
package org.jreleaser.sdk.ssh;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.FingerprintVerifier;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.PasswordUtils;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Ssh;
import org.jreleaser.model.internal.download.SshDownloader;
import org.jreleaser.model.internal.upload.SshUploader;
import org.jreleaser.model.spi.download.DownloadException;
import org.jreleaser.model.spi.upload.UploadException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public class SshUtils {
    private SshUtils() {
        // noop
    }

    public static SSHClient createSSHClient(JReleaserContext context, SshUploader<?> uploader) throws UploadException {
        if (context.isDryrun()) return null;

        try {
            SSHClient client = sshClient(context, uploader);
            client.setConnectTimeout(uploader.getConnectTimeout() * 1000);
            client.setTimeout(uploader.getReadTimeout() * 1000);
            return client;
        } catch (IOException e) {
            throw new UploadException(RB.$("ERROR_unexpected_upload_to", uploader.getName()), e);
        }
    }

    public static SSHClient createSSHClient(JReleaserContext context, SshDownloader<?> downloader) throws DownloadException {
        if (context.isDryrun()) return null;

        try {
            SSHClient client = sshClient(context, downloader);
            client.setConnectTimeout(downloader.getConnectTimeout() * 1000);
            client.setTimeout(downloader.getReadTimeout() * 1000);
            return client;
        } catch (IOException e) {
            throw new DownloadException(RB.$("ERROR_unexpected_download_from", downloader.getName()), e);
        }
    }

    private static SSHClient sshClient(JReleaserContext context, Ssh ssh) throws IOException {
        SSHClient client = new SSHClient();

        Path defaultKnownHostsFilePath = Paths.get(System.getProperty("user.home")).resolve(".ssh/known_hosts");

        if (isNotBlank(ssh.getKnownHostsFile())) {
            Path knownHostsFilePath = context.getBasedir().resolve(ssh.getKnownHostsFile());
            if (Files.exists(knownHostsFilePath)) {
                client.loadKnownHosts(knownHostsFilePath.toFile());
            } else {
                if (!Files.exists(defaultKnownHostsFilePath)) {
                    Files.createDirectories(defaultKnownHostsFilePath.getParent());
                    Files.createFile(defaultKnownHostsFilePath);
                }
                client.loadKnownHosts();
            }
        } else {
            if (!Files.exists(defaultKnownHostsFilePath)) {
                Files.createDirectories(defaultKnownHostsFilePath.getParent());
                Files.createFile(defaultKnownHostsFilePath);
            }
            client.loadKnownHosts();
        }

        String publicKey = ssh.getPublicKey();
        String privateKey = ssh.getPrivateKey();
        String passphrase = ssh.getPassphrase();
        String fingerprint = ssh.getFingerprint();

        if (isNotBlank(publicKey) && isNotBlank(privateKey)) {
            PasswordFinder passwordFinder = null;
            if (isNotBlank(passphrase)) {
                passwordFinder = PasswordUtils.createOneOff(passphrase.toCharArray());
            }
            client.loadKeys(privateKey, publicKey, passwordFinder);
        }

        if (isNotBlank(fingerprint)) {
            client.addHostKeyVerifier(FingerprintVerifier.getInstance(fingerprint));
        }

        if (Boolean.getBoolean("jreleaser.disableSshVerification")) {
            context.getLogger().warn(RB.$("warn_ssh_disabled"));
            client.addHostKeyVerifier(new PromiscuousVerifier());
        }

        client.connect(ssh.getHost(), ssh.getPort());
        client.authPassword(ssh.getUsername(), ssh.getPassword());
        return client;
    }

    public static SFTPClient createSFTPClient(SshUploader<?> uploader, SSHClient ssh) throws UploadException {
        if (null == ssh) return null;

        try {
            return ssh.newSFTPClient();
        } catch (IOException e) {
            throw new UploadException(RB.$("ERROR_unexpected_upload_to", uploader.getName()), e);
        }
    }

    public static SFTPClient createSFTPClient(SshDownloader<?> downloader, SSHClient ssh) throws DownloadException {
        if (null == ssh) return null;

        try {
            return ssh.newSFTPClient();
        } catch (IOException e) {
            throw new DownloadException(RB.$("ERROR_unexpected_download_from", downloader.getName()), e);
        }
    }

    public static void createDirectories(JReleaserContext context, SshUploader<?> uploader, SSHClient ssh, Path path) throws UploadException {
        try (Session session = ssh.startSession()) {
            Session.Command cmd = session.exec("mkdir -p " + path.toAbsolutePath());
            cmd.join(uploader.getReadTimeout(), TimeUnit.SECONDS);
        } catch (SSHException e) {
            context.getLogger().trace(e);
            throw new UploadException(RB.$("ERROR_ssh_mkdir", path), e);
        }
    }

    public static void disconnect(SshUploader<?> uploader, SSHClient ssh) throws UploadException {
        try {
            if (null != ssh) ssh.disconnect();
        } catch (IOException e) {
            throw new UploadException(RB.$("ERROR_disconnect", uploader.getName()), e);
        }
    }

    public static void disconnect(SshDownloader<?> downloader, SSHClient ssh) throws DownloadException {
        try {
            if (null != ssh) ssh.disconnect();
        } catch (IOException e) {
            throw new DownloadException(RB.$("ERROR_disconnect", downloader.getName()), e);
        }
    }

    public static void close(SshUploader<?> uploader, SFTPClient sftp) throws UploadException {
        try {
            if (null != sftp) sftp.close();
        } catch (IOException e) {
            throw new UploadException(RB.$("ERROR_disconnect", uploader.getName()), e);
        }
    }

    public static void close(SshDownloader<?> downloader, SFTPClient sftp) throws DownloadException {
        try {
            if (null != sftp) sftp.close();
        } catch (IOException e) {
            throw new DownloadException(RB.$("ERROR_disconnect", downloader.getName()), e);
        }
    }
}
