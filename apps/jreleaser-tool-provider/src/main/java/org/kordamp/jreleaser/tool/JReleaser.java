/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 Andres Almiray.
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
package org.kordamp.jreleaser.tool;

import org.kordamp.jreleaser.app.Main;

import java.io.PrintWriter;
import java.util.spi.ToolProvider;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class JReleaser implements ToolProvider {
    public String name() {
        return "jreleaser";
    }

    public int run(PrintWriter out, PrintWriter err, String... args) {
        return Main.run(out, err, args);
    }

    public static void main(String[] args) {
        Main.run(new PrintWriter(System.out), new PrintWriter(System.err), args);
    }
}
