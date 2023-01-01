/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
package org.jreleaser.cli;

import org.jreleaser.cli.internal.Colorizer;
import org.jreleaser.model.JReleaserException;

import java.util.concurrent.Callable;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
abstract class AbstractCommand extends BaseCommand implements Callable<Integer> {
    protected abstract Main parent();

    @Override
    public Integer call() {
        setup();

        try {
            execute();
        } catch (HaltExecutionException e) {
            return 1;
        } catch (JReleaserException e) {
            Colorizer colorizer = new Colorizer(parent().out);
            String message = e.getMessage();
            colorizer.println(message);
            printDetails(e.getCause(), message, colorizer);
            return 1;
        } catch (Exception e) {
            e.printStackTrace(new Colorizer(parent().out));
            return 1;
        }

        return 0;
    }

    protected void setup() {
        Banner.display(parent().out);

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");
    }

    protected void printDetails(Throwable throwable, String message, Colorizer colorizer) {
        if (throwable == null) return;
        String myMessage = throwable.getMessage();
        if (!message.equals(myMessage)) {
            colorizer.println(myMessage);
        } else {
            printDetails(throwable.getCause(), message, colorizer);
        }
    }

    protected abstract void execute();
}
