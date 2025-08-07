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
package org.jreleaser.model.internal.signing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jreleaser.model.internal.common.AbstractModelObject;
import org.jreleaser.model.internal.common.Domain;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.model.Constants.HIDE;
import static org.jreleaser.model.Constants.UNSET;
import static org.jreleaser.util.StringUtils.isNotBlank;

public final class SignTool extends AbstractModelObject<SignTool> implements Domain {
    private static final long serialVersionUID = 3947764654343954453L;

    private String certificateFile;
    private String password;
    private String timestampUrl;
    private String algorithm;
    private String description;
    private String executable;

    @JsonIgnore
    private final org.jreleaser.model.api.signing.SignTool immutable = new org.jreleaser.model.api.signing.SignTool() {
        private static final long serialVersionUID = -3215654774433643453L;

        @Override
        public String getCertificateFile() {
            return certificateFile;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getTimestampUrl() {
            return timestampUrl;
        }

        @Override
        public String getAlgorithm() {
            return algorithm;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getExecutable() {
            return executable;
        }

        @Override
        public Map<String, Object> asMap(boolean full) {
            return SignTool.this.asMap(full);
        }
    };

    public org.jreleaser.model.api.signing.SignTool asImmutable() {
        return immutable;
    }

    @Override
    public void merge(SignTool source) {
        super.merge(source);
        this.certificateFile = merge(this.certificateFile, source.certificateFile);
        this.password = merge(this.password, source.password);
        this.timestampUrl = merge(this.timestampUrl, source.timestampUrl);
        this.algorithm = merge(this.algorithm, source.algorithm);
        this.description = merge(this.description, source.description);
        this.executable = merge(this.executable, source.executable);
    }

    public String getCertificateFile() {
        return certificateFile;
    }

    public void setCertificateFile(String certificateFile) {
        this.certificateFile = certificateFile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTimestampUrl() {
        return timestampUrl;
    }

    public void setTimestampUrl(String timestampUrl) {
        this.timestampUrl = timestampUrl;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    @Override
    public Map<String, Object> asMap(boolean full) {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("certificateFile", certificateFile);
        props.put("password", isNotBlank(password) ? HIDE : UNSET);
        props.put("timestampUrl", timestampUrl);
        props.put("algorithm", algorithm);
        props.put("description", description);
        props.put("executable", executable);
        return props;
    }
}