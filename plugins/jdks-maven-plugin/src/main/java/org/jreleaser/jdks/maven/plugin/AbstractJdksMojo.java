/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.jdks.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.jreleaser.util.Errors;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.3.0
 */
abstract class AbstractJdksMojo extends AbstractSetupMojo {

    protected static final int MINIMUM_CONNECT_TIMEOUT = 20;

    protected static final int MAXIMUM_CONNECT_TIMEOUT = 300;

    protected static final int MINIMUM_READ_TIMEOUT = 60;

    protected static final int MAXIMUM_READ_TIMEOUT = 300;

    @Parameter(required = true)
    protected List<Jdk> jdks;

    @Parameter(property = "jdks.setup.connect.timeout", defaultValue = "" + MINIMUM_CONNECT_TIMEOUT)
    protected int connectTimeout;

    @Parameter(property = "jdks.setup.read.timeout", defaultValue = "" + MINIMUM_READ_TIMEOUT)
    protected int readTimeout;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Banner.display(project, getLog());

        if (null == jdks || jdks.isEmpty()) return;
        validate();

        doExecute();
    }

    protected abstract void doExecute() throws MojoExecutionException;

    protected void validate() throws MojoFailureException {
        if (connectTimeout < MINIMUM_CONNECT_TIMEOUT) {
            connectTimeout = MINIMUM_CONNECT_TIMEOUT;
            getLog().warn("Connect timeout cannot be less than " + MINIMUM_CONNECT_TIMEOUT + " seconds. Defaulting to " + connectTimeout + " seconds.");
        } else if (connectTimeout > MAXIMUM_CONNECT_TIMEOUT) {
            connectTimeout = MAXIMUM_CONNECT_TIMEOUT;
            getLog().warn("Connect timeout cannot be greater than " + MAXIMUM_CONNECT_TIMEOUT + " seconds. Defaulting to " + connectTimeout + " seconds.");
        }
        if (readTimeout < MINIMUM_READ_TIMEOUT) {
            readTimeout = MINIMUM_READ_TIMEOUT;
            getLog().warn("Read timeout cannot be less than " + MINIMUM_READ_TIMEOUT + " seconds. Defaulting to " + readTimeout + " seconds.");
        } else if (readTimeout > MAXIMUM_READ_TIMEOUT) {
            readTimeout = MAXIMUM_READ_TIMEOUT;
            getLog().warn("Read timeout cannot be greater than " + MAXIMUM_READ_TIMEOUT + " seconds. Defaulting to " + readTimeout + " seconds.");
        }

        if (null == jdks || jdks.isEmpty()) return;

        Errors errors = new Errors();
        jdks.forEach(jdk -> jdk.validate(errors));

        if (errors.hasErrors()) {
            StringWriter s = new StringWriter();
            PrintWriter w = new PrintWriter(s, true);
            errors.logErrors(w);
            throw new MojoFailureException(s.toString());
        }
    }
}
