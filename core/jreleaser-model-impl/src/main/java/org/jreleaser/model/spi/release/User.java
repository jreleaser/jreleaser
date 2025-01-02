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
package org.jreleaser.model.spi.release;

import java.util.Objects;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class User {
    private final String username;
    private final String email;
    private final String url;

    public User(String username, String email, String url) {
        this.username = username;
        this.email = email;
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getUrl() {
        return url;
    }

    public String asLink(String input) {
        return "[" + input + "](" + url + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username) &&
            email.equals(user.email) &&
            url.equals(user.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email, url);
    }

    @Override
    public String toString() {
        return "User[" +
            "username='" + username + '\'' +
            ", email='" + email + '\'' +
            ", url='" + url + '\'' +
            "]";
    }
}
