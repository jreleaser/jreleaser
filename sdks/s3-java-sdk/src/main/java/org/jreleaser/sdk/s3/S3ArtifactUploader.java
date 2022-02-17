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
package org.jreleaser.sdk.s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.S3;
import org.jreleaser.model.uploader.spi.UploadException;
import org.jreleaser.sdk.commons.AbstractArtifactUploader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardOpenOption.READ;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class S3ArtifactUploader extends AbstractArtifactUploader<S3> {
    private static final Tika TIKA = new Tika();
    private S3 uploader;

    public S3ArtifactUploader(JReleaserContext context) {
        super(context);
    }

    @Override
    public S3 getUploader() {
        return uploader;
    }

    @Override
    public void setUploader(S3 uploader) {
        this.uploader = uploader;
    }

    @Override
    public String getType() {
        return S3.TYPE;
    }

    @Override
    public void upload(String name) throws UploadException {
        List<Artifact> artifacts = collectArtifacts();
        if (artifacts.isEmpty()) {
            context.getLogger().info(RB.$("artifacts.no.match"));
        }

        String bucketName = uploader.getResolvedBucket();

        AmazonS3 s3 = createS3Client();

        // does the bucket exist?
        context.getLogger().debug(RB.$("s3.bucket.check"), bucketName);
        if (!s3.doesBucketExistV2(bucketName)) {
            // create the bucket
            context.getLogger().debug(RB.$("s3.bucket.create"), bucketName);
            s3.createBucket(bucketName);
        }

        for (Artifact artifact : artifacts) {
            Path path = artifact.getEffectivePath(context);
            context.getLogger().info(" - {}", path.getFileName());

            try {
                String bucketPath = uploader.getResolvedPath(context, artifact);
                context.getLogger().debug("   {}", bucketPath);

                if (!context.isDryrun()) {
                    context.getLogger().debug(RB.$("s3.object.check"), bucketName, bucketPath);
                    if (s3.doesObjectExist(bucketName, bucketPath)) {
                        context.getLogger().debug(RB.$("s3.object.create"), bucketName, bucketPath);
                        s3.deleteObject(bucketName, bucketPath);
                    }

                    ObjectMetadata meta = new ObjectMetadata();
                    meta.setContentType(MediaType.parse(TIKA.detect(path)).toString());
                    meta.setContentLength(Files.size(path));

                    context.getLogger().debug(RB.$("s3.object.write"), bucketName, bucketPath);
                    try (InputStream is = Files.newInputStream(path, READ)) {
                        s3.putObject(new PutObjectRequest(bucketName, bucketPath, is, meta));
                    }

                    context.getLogger().debug(RB.$("s3.object.acl"), bucketName, bucketPath);
                    AccessControlList acl = s3.getObjectAcl(bucketName, bucketPath);
                    acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
                    s3.setObjectAcl(bucketName, bucketPath, acl);
                }
            } catch (IOException e) {
                context.getLogger().trace(e);
                throw new UploadException(RB.$("ERROR_unexpected_upload", context.relativizeToBasedir(path)), e);
            }
        }
    }

    private AmazonS3 createS3Client() throws UploadException {
        try {
            AmazonS3ClientBuilder s3Builder = AmazonS3ClientBuilder.standard();
            if (isNotBlank(uploader.getResolvedAccessKeyId()) &&
                isNotBlank(uploader.getResolvedSecretKey()) &&
                isNotBlank(uploader.getResolvedSessionToken())) {
                s3Builder.withCredentials(new AWSStaticCredentialsProvider(
                    new BasicSessionCredentials(uploader.getResolvedAccessKeyId(),
                        uploader.getResolvedSecretKey(),
                        uploader.getResolvedSessionToken())));
            } else if (isNotBlank(uploader.getResolvedAccessKeyId()) &&
                isNotBlank(uploader.getResolvedSecretKey())) {
                s3Builder.withCredentials(new AWSStaticCredentialsProvider(
                    new BasicAWSCredentials(uploader.getResolvedAccessKeyId(),
                        uploader.getResolvedSecretKey())));
            }

            Map<String, String> headers = uploader.getHeaders();
            if (headers != null) {
                ClientConfiguration clientConfiguration = new ClientConfiguration();
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    if (header.getKey() != null && header.getValue() != null) {
                        clientConfiguration.addHeader(header.getKey(), header.getValue());
                    }
                }
                s3Builder.setClientConfiguration(clientConfiguration);
            }

            if (isBlank(uploader.getResolvedEndpoint())) {
                s3Builder.withRegion(uploader.getResolvedRegion());
            } else {
                s3Builder.withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(uploader.getResolvedEndpoint(),
                        uploader.getResolvedRegion()));
            }

            s3Builder.getClientConfiguration()
                .setConnectionTimeout(uploader.getConnectTimeout() * 1000);

            return s3Builder.build();
        } catch (SdkClientException e) {
            context.getLogger().trace(e);
            throw new UploadException(RB.$("ERROR_unexpected_s3_client_config"), e);
        }
    }
}
