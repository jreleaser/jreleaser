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
package org.jreleaser.maven.plugin.internal;

import org.jreleaser.model.internal.JReleaserModelPrinter;

import java.io.PrintWriter;

import static org.apache.maven.shared.utils.logging.MessageUtils.buffer;
import static org.apache.maven.shared.utils.logging.MessageUtils.isColorEnabled;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class MavenJReleaserModelPrinter extends JReleaserModelPrinter {
    public MavenJReleaserModelPrinter(PrintWriter out) {
        super(out);
    }

    @Override
    protected String color(String color, String input) {
        switch (color) {
            case "cyan":
                return cyan(input);
            case "blue":
                return blue(input);
            case "yellow":
                return yellow(input);
            case "red":
                return red(input);
            case "green":
                return green(input);
            case "magenta":
                return magenta(input);
            case "black":
                return black(input);
            case "white":
                return white(input);
            default:
                return input;
        }
    }

    private String black(CharSequence s) {
        return buffer().a(!isColorEnabled() ? s : "\u001B[30m" + s + "\u001b[0m").build();
    }

    private String red(CharSequence s) {
        return buffer().a(!isColorEnabled() ? s : "\u001B[31m" + s + "\u001b[0m").build();
    }

    private String green(CharSequence s) {
        return buffer().a(!isColorEnabled() ? s : "\u001B[32m" + s + "\u001b[0m").build();
    }

    private String yellow(CharSequence s) {
        return buffer().a(!isColorEnabled() ? s : "\u001B[33m" + s + "\u001b[0m").build();
    }

    private String blue(CharSequence s) {
        return buffer().a(!isColorEnabled() ? s : "\u001B[34m" + s + "\u001b[0m").build();
    }

    private String magenta(CharSequence s) {
        return buffer().a(!isColorEnabled() ? s : "\u001B[35m" + s + "\u001b[0m").build();
    }

    private String cyan(CharSequence s) {
        return buffer().a(!isColorEnabled() ? s : "\u001B[36m" + s + "\u001b[0m").build();
    }

    private String white(CharSequence s) {
        return buffer().a(!isColorEnabled() ? s : "\u001B[37m" + s + "\u001b[0m").build();
    }

    private String erase(CharSequence s) {
        return buffer().a(!isColorEnabled() ? s : "\u001b[2K" + s).build();
    }
}
