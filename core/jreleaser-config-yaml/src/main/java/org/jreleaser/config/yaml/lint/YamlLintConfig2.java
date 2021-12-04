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

import com.github.sbaudoin.yamllint.YamlLintConfig;
import com.github.sbaudoin.yamllint.YamlLintConfigException;
import com.github.sbaudoin.yamllint.rules.Rule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Andres Almiray
 * @since 0.9.1
 */
public class YamlLintConfig2 extends YamlLintConfig {
    public YamlLintConfig2(CharSequence content) throws YamlLintConfigException {
        super(content);
    }

    public YamlLintConfig2(URL file) throws IOException, YamlLintConfigException {
        super(file);
    }

    public YamlLintConfig2(InputStream in) throws YamlLintConfigException {
        super(in);
    }

    @Override
    public List<Rule> getEnabledRules(File file) {
        List<Rule> rules = new ArrayList<>();
        for (Map.Entry<String, Object> entry : ruleConf.entrySet()) {
            Rule rule = RuleFactory.instance.getRule(entry.getKey());
            if (rule != null && entry.getValue() != null && (file == null || !rule.ignores(file))) {
                rules.add(rule);
            }
        }
        return rules;
    }

    @Override
    protected void validate() throws YamlLintConfigException {
        for (Map.Entry<String, Object> entry : ruleConf.entrySet()) {
            String id = entry.getKey();
            Rule rule = RuleFactory.instance.getRule(id);
            if (rule == null) {
                throw getInvalidConfigException(String.format("no such rule: \"%s\"", id));
            }

            Map<String, Object> newConf = validateRuleConf(rule, entry.getValue());
            ruleConf.put(id, newConf);
        }
    }

    /**
     * Returns a {@code YamlLintConfigException} with the message "invalid config: %passed_message%"
     *
     * @param message a message that describes the configuration error
     * @return a {@code YamlLintConfigException} with the passed message
     */
    private static YamlLintConfigException getInvalidConfigException(String message) {
        return getInvalidConfigException(null, message, null);
    }

    /**
     * Returns a {@code YamlLintConfigException} with the message "invalid%specifier% config: %passed_message%"
     *
     * @param specifier a string to be passed after 'invalid'. Pass {@code null} if you do not want any specifier.
     * @param message   a message that describes the configuration error
     * @param e         an optional (may be {@code null}) {@code Throwable} to be set as the ancestor of the returned exception
     * @return a {@code YamlLintConfigException} with the passed message
     */
    private static YamlLintConfigException getInvalidConfigException(String specifier, String message, Throwable e) {
        String m = String.format("invalid%s config: %s", (specifier == null) ? "" : (" " + specifier), message);
        if (e == null) {
            return new YamlLintConfigException(m);
        } else {
            return new YamlLintConfigException(m, e);
        }
    }
}
