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
package org.jreleaser.model.internal;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlFeatures;
import org.apache.commons.jexl3.JexlInfo;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.MapContext;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserException;
import org.jreleaser.mustache.Templates;

/**
 * @author Andres Almiray
 * @since 1.6.0
 */
public final class JReleaserScriptEvaluator {
    private final JexlEngine jexl;

    public JReleaserScriptEvaluator() {
        jexl = new JexlBuilder()
            .features(new JexlFeatures()
                .annotation(false)
                .importPragma(false)
                .lambda(false)
                .loops(false)
                .namespacePragma(false)
                .newInstance(false)
                .pragma(false)
                .pragmaAnywhere(false)
                .sideEffect(false)
                .sideEffectGlobal(false))
            .create();
    }

    public Object eval(JReleaserContext context, String scriptText) {
        try {
            scriptText = Templates.resolveTemplate(scriptText, context.props());
            JexlScript script = jexl.createScript(new JexlInfo("script", 0, 0), scriptText);
            JexlContext ctxt = new MapContext();
            ctxt.set("context", context.asImmutable());
            return script.execute(ctxt);
        } catch (JexlException.Parsing e) {
            throw new JReleaserException(RB.$("ERROR_script_parse", e.getMessage()));
        } catch (JexlException e) {
            throw new JReleaserException(RB.$("ERROR_script_execution", e.getMessage()));
        }
    }
}