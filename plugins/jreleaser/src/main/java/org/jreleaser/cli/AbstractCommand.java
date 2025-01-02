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
package org.jreleaser.cli;

import org.jreleaser.cli.internal.Colorizer;
import org.jreleaser.model.JReleaserException;
import picocli.CommandLine;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@CommandLine.Command
public abstract class AbstractCommand<C extends IO> extends BaseCommand implements Callable<Integer> {
    @CommandLine.ParentCommand
    private C parent;

    protected C parent() {
        return parent;
    }

    @Override
    public Integer call() {
        setup();
        checkArgsForDeprecations();

        try {
            execute();
        } catch (HaltExecutionException e) {
            return 1;
        } catch (JReleaserException e) {
            Colorizer colorizer = new Colorizer(parent().getOut());
            String message = e.getMessage();
            colorizer.println(message);
            printDetails(e.getCause(), message, colorizer);
            return 1;
        } catch (Exception e) {
            e.printStackTrace(new Colorizer(parent().getOut()));
            return 1;
        }

        return 0;
    }

    protected void setup() {
        Banner.display(parent().getErr());

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");
    }

    protected void checkArgsForDeprecations() {
        Set<DeprecatedArg> candidates = new TreeSet<>();
        collectCandidateDeprecatedArgs(candidates);
        Map<String, DeprecatedArg> groupedCandidates = candidates.stream()
            .collect(Collectors.toMap(DeprecatedArg::getDeprecated, e -> e));

        Set<DeprecatedArg> args = new TreeSet<>();

        CommandLine.ParseResult pr = spec.commandLine().getParseResult();
        List<CommandLine.Model.OptionSpec> options = spec.options();
        for (CommandLine.Model.OptionSpec opt : options) {
            String optName = opt.shortestName();
            if (groupedCandidates.containsKey(optName) &&
                pr.expandedArgs().contains(optName) &&
                pr.hasMatchedOption(optName)) {
                args.add(groupedCandidates.get(optName));
            }
        }

        for (DeprecatedArg arg : args) {
            parent().getErr().println($("deprecated.arg", arg.deprecated, arg.since, arg.replacement));
        }
    }

    protected void collectCandidateDeprecatedArgs(Set<DeprecatedArg> args) {
        // noop
    }

    protected void printDetails(Throwable throwable, String message, Colorizer colorizer) {
        if (null == throwable) return;
        String myMessage = throwable.getMessage();
        if (!message.equals(myMessage)) {
            colorizer.println(myMessage);
        } else {
            printDetails(throwable.getCause(), message, colorizer);
        }
    }

    protected abstract void execute();

    public static final class DeprecatedArg implements Comparable<DeprecatedArg> {
        private final String deprecated;
        private final String replacement;
        private final String since;

        public DeprecatedArg(String deprecated, String replacement, String since) {
            this.deprecated = deprecated;
            this.replacement = replacement;
            this.since = since;
        }

        public String getDeprecated() {
            return deprecated;
        }

        public String getReplacement() {
            return replacement;
        }

        public String getSince() {
            return since;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeprecatedArg that = (DeprecatedArg) o;
            return deprecated.equals(that.deprecated) && replacement.equals(that.replacement) && since.equals(that.since);
        }

        @Override
        public int hashCode() {
            return Objects.hash(deprecated, replacement, since);
        }

        @Override
        public int compareTo(DeprecatedArg o) {
            return Comparator.comparing(DeprecatedArg::getDeprecated)
                .compare(this, o);
        }
    }
}
