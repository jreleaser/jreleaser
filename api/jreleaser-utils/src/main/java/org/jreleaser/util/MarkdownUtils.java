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
package org.jreleaser.util;

import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.CoreTextContentNodeRenderer;
import org.commonmark.renderer.text.TextContentNodeRendererContext;
import org.commonmark.renderer.text.TextContentRenderer;
import org.commonmark.renderer.text.TextContentWriter;

/**
 * @author Andres Almiray
 * @author George Grigalashvili
 * @author Christian Kleinboelting
 * @since 1.10.0
 */
public final class MarkdownUtils {
    private MarkdownUtils() {
        // noop
    }

    private static class MyCoreTextContentNodeRenderer extends CoreTextContentNodeRenderer {
        private final TextContentWriter textContent;

        public MyCoreTextContentNodeRenderer(TextContentNodeRendererContext context) {
            super(context);
            this.textContent = context.getWriter();
        }

        @Override
        public void visit(Link link) {
            rewriteLink(link, link.getTitle(), link.getDestination());
        }

        private void rewriteLink(Node node, String title, String destination) {
            boolean hasChild = node.getFirstChild() != null;
            boolean hasTitle = title != null && !title.equals(destination);
            boolean hasDestination = destination != null && !destination.equals("");

            if (hasChild) {
                textContent.write('[');
                visitChildren(node);
                textContent.write(']');
                if (hasTitle || hasDestination) {
                    textContent.write('(');
                }
            }

            if (hasTitle) {
                textContent.write(title);
                if (hasDestination) {
                    textContent.colon();
                    textContent.whitespace();
                }
            }

            if (hasDestination) {
                textContent.write(destination);
            }

            if (hasChild && (hasTitle || hasDestination)) {
                textContent.write(')');
            }
        }
    }

    public static Parser createMarkdownParser() {
        Parser.Builder builder = Parser.builder();
        ((AutolinkExtension) AutolinkExtension.create()).extend(builder);
        return builder.build();
    }

    public static TextContentRenderer createTextContentRenderer() {
        return TextContentRenderer.builder()
            .nodeRendererFactory(MyCoreTextContentNodeRenderer::new)
            .build();
    }
}
