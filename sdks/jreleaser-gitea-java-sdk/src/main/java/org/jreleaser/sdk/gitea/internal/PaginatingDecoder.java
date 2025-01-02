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
package org.jreleaser.sdk.gitea.internal;

import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;
import org.jreleaser.sdk.commons.feign.DelegatingFeignDecoder;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author Andres Almiray
 * @since 1.2.0
 */
public class PaginatingDecoder extends DelegatingFeignDecoder {
    public PaginatingDecoder(Decoder delegate) {
        super(delegate);
    }

    @Override
    public Object decode(Response response, Type type) throws IOException, FeignException {
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            if (pt.getRawType().getTypeName().equals(Page.class.getName())) {
                return new Page<>(response.headers(), super.decode(response, pt.getActualTypeArguments()[0]));
            }
        }
        return super.decode(response, type);
    }
}
