/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
package org.jreleaser.tools;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.MustacheFactory;
import org.jreleaser.model.Artifact;
import org.jreleaser.model.Distribution;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Tool;
import org.jreleaser.util.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.jreleaser.util.StringUtils.capitalize;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
final class ProcessorUtils {
    private ProcessorUtils() {
        //noop
    }

    static <T extends Tool> boolean verifyAndAddArtifacts(Logger logger,
                                                          Map<String, Object> context,
                                                          JReleaserModel model,
                                                          Distribution distribution,
                                                          String artifactExtension,
                                                          T tool) throws ToolProcessingException {
        List<Artifact> artifacts = distribution.getArtifacts().stream()
            .filter(artifact -> artifact.getPath().endsWith(artifactExtension))
            .collect(Collectors.toList());

        if (artifacts.size() == 0) {
            // we can't proceed
            logger.warn("No suitable {} artifacts found in distribution {} to be packaged with ",
                artifactExtension, distribution.getName(), capitalize(tool.getToolName()));
            return false;
        }

        for (int i = 0; i < artifacts.size(); i++) {
            Artifact artifact = artifacts.get(i);
            String classifier = isNotBlank(artifact.getOsClassifier()) ? capitalize(artifact.getOsClassifier()) : "";
            String artifactFileName = Paths.get(artifact.getPath()).getFileName().toString();
            context.put("artifact" + classifier + "JavaVersion", artifact.getJavaVersion());
            context.put("artifact" + classifier + "FileName", artifactFileName);
            context.put("artifact" + classifier + "Hash", artifact.getHash());
            Map<String, Object> newContext = new LinkedHashMap<>(context);
            newContext.put("artifactFileName", artifactFileName);
            String artifactUrl = applyTemplate(new StringReader(model.getRelease().getGitService().getDownloadUrlFormat()), newContext, "downloadUrl");
            context.put("artifact" + classifier + "Url", artifactUrl);

            if (0 == i) {
                context.put("distributionUrl", artifactUrl);
                context.put("distributionSha256", artifact.getHash());
                context.put("distributionJavaVersion", artifact.getJavaVersion());
            }
        }

        return true;
    }

    static String applyTemplate(Reader reader, Map<String, Object> context, String toolName) {
        StringWriter input = new StringWriter();
        MustacheFactory mf = new MyMustacheFactory();
        Mustache mustache = mf.compile(reader, toolName);
        mustache.execute(input, context);
        input.flush();
        return input.toString();
    }

    static String applyTemplate(Reader reader, Map<String, Object> context) {
        return applyTemplate(reader, context, UUID.randomUUID().toString());
    }

    private static class MyMustacheFactory extends DefaultMustacheFactory {
        @Override
        public void encode(String value, Writer writer) {
            if (value.startsWith("!!") && value.endsWith("!!")) {
                try {
                    writer.write(value.substring(2, value.length() - 2));
                } catch (IOException e) {
                    throw new MustacheException("Failed to write value: " + value, e);
                }
            } else {
                super.encode(value, writer);
            }
        }
    }
}
