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
package org.jreleaser.sdk.s3;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Artifact;
import org.jreleaser.model.internal.upload.S3Uploader;
import org.jreleaser.model.spi.upload.UploadException;
import org.jreleaser.sdk.commons.AbstractArtifactUploader;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.endpoints.Endpoint;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.endpoints.S3EndpointParams;
import software.amazon.awssdk.services.s3.endpoints.S3EndpointProvider;
import software.amazon.awssdk.services.s3.model.AccessControlPolicy;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetBucketAclRequest;
import software.amazon.awssdk.services.s3.model.Grant;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.Owner;
import software.amazon.awssdk.services.s3.model.Permission;
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.Type;

import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class S3ArtifactUploader extends AbstractArtifactUploader<org.jreleaser.model.api.upload.S3Uploader, S3Uploader> {
    private S3Uploader uploader;

    public S3ArtifactUploader(JReleaserContext context) {
        super(context);
    }

    @Override
    public S3Uploader getUploader() {
        return uploader;
    }

    @Override
    public void setUploader(S3Uploader uploader) {
        this.uploader = uploader;
    }

    @Override
    public String getType() {
        return org.jreleaser.model.api.upload.S3Uploader.TYPE;
    }

    @Override
    public void upload(String name) throws UploadException {
        Set<Artifact> artifacts = collectArtifacts();
        if (artifacts.isEmpty()) {
            context.getLogger().info(RB.$("artifacts.no.match"));
        }

        String bucketName = uploader.getBucket();

        S3Client s3 = createS3Client();
        String ownerId = null;

        if (!context.isDryrun()) {
            try {
                context.getLogger().debug(RB.$("s3.bucket.check"), bucketName);
                if (!doesBucketExist(s3, bucketName)) {
                    createBucket(s3, bucketName);
                }

                Owner owner = s3.getBucketAcl(GetBucketAclRequest.builder()
                    .bucket(bucketName)
                    .build()).owner();
                if (owner != null) {
                    ownerId = owner.id();
                }
            } catch (SdkException e) {
                context.getLogger().trace(e);
                throw new UploadException(RB.$("ERROR_unexpected_upload2"), e);
            }
        }

        for (Artifact artifact : artifacts) {
            Path path = artifact.getEffectivePath(context);
            try {
                context.getLogger().info(" - {}", path.getFileName());

                String bucketPath = uploader.getResolvedPath(context, artifact);
                context.getLogger().debug("   {}", bucketPath);

                if (!context.isDryrun()) {
                    context.getLogger().debug(RB.$("s3.object.check"), bucketName, bucketPath);
                    if (doesObjectExist(s3, bucketName, bucketPath)) {
                        deleteObject(s3, bucketName, bucketPath);
                    }

                    putObject(s3, ownerId, bucketName, bucketPath, path);
                }
            } catch (SdkException e) {
                context.getLogger().trace(e);
                throw new UploadException(RB.$("ERROR_unexpected_upload", context.relativizeToBasedir(path)), e);
            }
        }
    }

    private S3Client createS3Client() {
        S3ClientBuilder builder = S3Client.builder()
            .httpClientBuilder(ApacheHttpClient.builder());

        if (isNotBlank(uploader.getAccessKeyId()) &&
            isNotBlank(uploader.getSecretKey()) &&
            isNotBlank(uploader.getSessionToken())) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                AwsSessionCredentials.create(uploader.getAccessKeyId(),
                    uploader.getSecretKey(),
                    uploader.getSessionToken())));
        } else if (isNotBlank(uploader.getAccessKeyId()) &&
            isNotBlank(uploader.getSecretKey())) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(uploader.getAccessKeyId(),
                    uploader.getSecretKey())));
        }

        if (isBlank(uploader.getEndpoint())) {
            builder.region(Region.of(uploader.getRegion()));
        } else {
            builder.endpointProvider(new MyS3EndpointProvider(
                uploader.getEndpoint(), Region.of(uploader.getRegion())));
        }

        builder.overrideConfiguration(confBuilder -> {
            uploader.getHeaders().forEach(confBuilder::putHeader);
            confBuilder.apiCallAttemptTimeout(Duration.of(uploader.getConnectTimeout(), ChronoUnit.SECONDS));
        });

        return builder.build();
    }

    private boolean doesBucketExist(S3Client s3, String bucketName) {
        try {
            return s3.headBucket(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build())
                .sdkHttpResponse().isSuccessful();
        } catch (NoSuchBucketException e) {
            return false;
        }
    }

    private void createBucket(S3Client s3, String bucketName) throws SdkException {
        context.getLogger().debug(RB.$("s3.bucket.create"), bucketName);
        s3.createBucket(CreateBucketRequest.builder()
            .bucket(bucketName)
            .build());

        context.getLogger().debug(RB.$("s3.bucket.create.wait"), bucketName);
        s3.waiter().waitUntilBucketExists(HeadBucketRequest.builder()
            .bucket(bucketName)
            .build());
    }

    private void deleteObject(S3Client s3, String bucketName, String bucketPath) throws SdkException {
        context.getLogger().debug(RB.$("s3.object.delete"), bucketName, bucketPath);
        s3.deleteObject(DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(bucketPath)
            .build());

        context.getLogger().debug(RB.$("s3.object.delete.wait"), bucketName, bucketPath);
        s3.waiter().waitUntilObjectNotExists(HeadObjectRequest.builder()
            .bucket(bucketName)
            .key(bucketPath)
            .build());
    }

    private boolean doesObjectExist(S3Client s3, String bucketName, String bucketPath) {
        try {
            return s3.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(bucketPath)
                    .build())
                .sdkHttpResponse().isSuccessful();
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    private void putObject(S3Client s3, String ownerId, String bucketName, String bucketPath, Path path) throws SdkException {
        context.getLogger().debug(RB.$("s3.object.write"), bucketName, bucketPath);
        s3.putObject(PutObjectRequest.builder()
            .bucket(bucketName)
            .key(bucketPath)
            .build(), path);

        if (ownerId != null) {
            List<Grant> grantList = new ArrayList<>();
            grantList.add(Grant.builder()
                .grantee(builder -> builder.id(ownerId)
                    .type(Type.CANONICAL_USER))
                .permission(Permission.FULL_CONTROL)
                .build());
            grantList.add(Grant.builder()
                .grantee(builder -> builder
                    .uri("http://acs.amazonaws.com/groups/global/AllUsers")
                    .type(Type.GROUP))
                .permission(Permission.READ)
                .build());

            AccessControlPolicy acl = AccessControlPolicy.builder()
                .owner(builder -> builder.id(ownerId))
                .grants(grantList)
                .build();

            PutObjectAclRequest putAclReq = PutObjectAclRequest.builder()
                .bucket(bucketName)
                .key(bucketPath)
                .accessControlPolicy(acl)
                .build();

            context.getLogger().debug(RB.$("s3.object.acl"), bucketName, bucketPath);
            s3.putObjectAcl(putAclReq);
        }
    }

    public static class MyS3EndpointProvider implements S3EndpointProvider {
        private final S3EndpointProvider delegate = S3EndpointProvider.defaultProvider();
        private final String endpoint;
        private final Region region;

        public MyS3EndpointProvider(String endpoint, Region region) {
            this.endpoint = endpoint;
            this.region = region;
        }

        @Override
        public CompletableFuture<Endpoint> resolveEndpoint(S3EndpointParams endpointParams) {
            S3EndpointParams.Builder builder = S3EndpointParams.builder();
            builder.accelerate(endpointParams.accelerate());
            builder.bucket(endpointParams.bucket());
            builder.useFips(endpointParams.useFips());
            builder.useDualStack(endpointParams.useDualStack());
            builder.forcePathStyle(endpointParams.forcePathStyle());
            builder.useGlobalEndpoint(endpointParams.useGlobalEndpoint());
            builder.useObjectLambdaEndpoint(endpointParams.useObjectLambdaEndpoint());
            builder.disableAccessPoints(endpointParams.disableAccessPoints());
            builder.disableMultiRegionAccessPoints(endpointParams.disableMultiRegionAccessPoints());
            builder.useArnRegion(endpointParams.useArnRegion());
            builder.endpoint(endpoint);
            builder.region(region);
            return delegate.resolveEndpoint(builder.build());
        }
    }
}
