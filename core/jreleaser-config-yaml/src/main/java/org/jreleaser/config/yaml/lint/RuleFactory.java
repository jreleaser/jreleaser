/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
package org.jreleaser.config.yaml.lint;

import com.github.sbaudoin.yamllint.rules.Rule;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Andres Almiray
 * @since 0.9.1
 */
public class RuleFactory {
    /**
     * The instance that holds this singleton
     */
    public static final RuleFactory instance = new RuleFactory();
    private final Map<String, Rule> rules = new HashMap<>();

    /**
     * Hide default constructor
     */
    private RuleFactory() {
        ServiceLoader.load(Rule.class, getClass().getClassLoader())
            .forEach(rule -> rules.put(rule.getId(), rule));
    }

    /**
     * Returns a matching rule by id
     *
     * @param id the ID of the rule to be returned
     * @return the rule corresponding to the passed ID or <code>null</code> if not found
     */
    public Rule getRule(String id) {
        if (rules.containsKey(id)) {
            return rules.get(id);
        }

        return null;
    }
}
