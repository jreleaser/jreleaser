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
package org.jreleaser.maven.plugin;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
abstract class AbstractHttpUploader extends AbstractUploader implements HttpUploader {
    protected String uploadUrl;
    protected String downloadUrl;

    protected AbstractHttpUploader(String type) {
        super(type);
    }

    void setAll(AbstractHttpUploader uploader) {
        super.setAll(uploader);
        this.uploadUrl = uploader.uploadUrl;
        this.downloadUrl = uploader.downloadUrl;
    }

    @Override
    public String getUploadUrl() {
        return uploadUrl;
    }

    @Override
    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    @Override
    public String getDownloadUrl() {
        return downloadUrl;
    }

    @Override
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
