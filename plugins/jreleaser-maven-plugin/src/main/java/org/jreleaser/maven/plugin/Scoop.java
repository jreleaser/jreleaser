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
package org.jreleaser.maven.plugin;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Scoop extends AbstractRepositoryTool {
    private final Bucket bucket = new Bucket();
    private String checkverUrl;
    private String autoupdateUrl;

    void setAll(Scoop scoop) {
        super.setAll(scoop);
        this.checkverUrl = scoop.checkverUrl;
        this.autoupdateUrl = scoop.autoupdateUrl;
        setBucket(scoop.bucket);
    }

    public String getCheckverUrl() {
        return checkverUrl;
    }

    public void setCheckverUrl(String checkverUrl) {
        this.checkverUrl = checkverUrl;
    }

    public String getAutoupdateUrl() {
        return autoupdateUrl;
    }

    public void setAutoupdateUrl(String autoupdateUrl) {
        this.autoupdateUrl = autoupdateUrl;
    }

    public Bucket getBucket() {
        return bucket;
    }

    public void setBucket(Bucket bucket) {
        this.bucket.setAll(bucket);
    }

    @Override
    public boolean isSet() {
        return super.isSet() ||
            isNotBlank(checkverUrl) ||
            isNotBlank(autoupdateUrl) ||
            bucket.isSet();
    }
}
