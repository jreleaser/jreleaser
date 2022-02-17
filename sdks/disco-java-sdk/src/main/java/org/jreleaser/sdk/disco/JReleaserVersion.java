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
package org.jreleaser.sdk.disco;

import java.util.ResourceBundle;

/**
 * @author Andres Almiray
 * @since 0.9.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class JReleaserVersion {
    private static final ResourceBundle bundle = ResourceBundle.getBundle(JReleaserVersion.class.getName());
    private static final String JRELEASER_VERSION = bundle.getString("jreleaser_version");

    public static String getPlainVersion() {
        return JRELEASER_VERSION;
    }
}
