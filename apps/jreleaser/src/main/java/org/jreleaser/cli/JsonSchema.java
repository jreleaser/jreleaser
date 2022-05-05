/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.generator.impl.DefinitionKey;
import com.github.victools.jsonschema.generator.naming.DefaultSchemaDefinitionNamingStrategy;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.util.JReleaserException;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Collections.singletonMap;

/**
 * @author Andres Almiray
 * @since 0.10.0
 */
@CommandLine.Command(name = "json-schema")
public class JsonSchema extends AbstractCommand {
    @CommandLine.ParentCommand
    Main parent;

    @Override
    protected Main parent() {
        return parent;
    }

    protected void execute() {
        Map<String, String> mappings = new LinkedHashMap<>();
        mappings.put("Map<String, Object>", "Properties");
        mappings.put("Map<String, String>", "StringProperties");
        mappings.put("Map<String, Webhook>", "WebhookMap");
        mappings.put("Map<String, Archive>", "ArchiveMap");
        mappings.put("Map<String, Jlink>", "JlinkMap");
        mappings.put("Map<String, Jpackage>", "JpackageMap");
        mappings.put("Map<String, NativeImage>", "NativeImageMap");
        mappings.put("Map<String, Distribution>", "DistributionMap");
        mappings.put("Map<String, DockerSpec>", "DockerSpecMap");
        mappings.put("Map<String, Artifactory>", "ArtifactoryMap");
        mappings.put("Map<String, Http>", "HttpMap");
        mappings.put("Map<String, S3>", "S3Map");
        mappings.put("Map<String, AzureArtifacts>", "AzureArtifactsMap");

        try {
            SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2019_09, OptionPreset.PLAIN_JSON);
            configBuilder.getObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            configBuilder.with(Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT);
            configBuilder.with(Option.DEFINITION_FOR_MAIN_SCHEMA);
            configBuilder.with(Option.DEFINITIONS_FOR_ALL_OBJECTS);
            JacksonModule jacksonModule = new JacksonModule();
            configBuilder.with(jacksonModule);
            configBuilder.forTypesInGeneral()
                .withDescriptionResolver(scope -> scope.getType().getErasedType() == JReleaserModel.class ?
                    String.format("JReleaser %s", JReleaserVersion.getPlainVersion()) : null)
                .withPatternPropertiesResolver(scope -> {
                    if (scope.getType().isInstanceOf(Map.class)) {
                        ResolvedType type = scope.getTypeParameterFor(Map.class, 1);
                        if (type.getErasedType() != String.class && type.getErasedType() != Object.class) {
                            return singletonMap("^[a-zA-Z-]+$", type);
                        }
                    }
                    return null;
                })
                .withAdditionalPropertiesResolver(scope -> {
                    if (scope.getType().isInstanceOf(Map.class)) {
                        ResolvedType type = scope.getTypeParameterFor(Map.class, 1);
                        if (type.getErasedType() == String.class || type.getErasedType() == Object.class) {
                            return scope.getTypeParameterFor(Map.class, 0);
                        }
                    }
                    return null;
                })
                .withDefinitionNamingStrategy(new DefaultSchemaDefinitionNamingStrategy() {
                    @Override
                    public String getDefinitionNameForKey(DefinitionKey key, SchemaGenerationContext context) {
                        String definitionNameForKey = super.getDefinitionNameForKey(key, context);
                        return mappings.getOrDefault(definitionNameForKey, definitionNameForKey);
                    }
                });

            SchemaGeneratorConfig config = configBuilder.build();
            SchemaGenerator generator = new SchemaGenerator(config);
            JsonNode jsonSchema = generator.generateSchema(JReleaserModel.class);

            String fileName = String.format("jreleaser-%s-schema.json", JReleaserVersion.getPlainVersion());
            Path schemaPath = Paths.get(fileName);
            String json = configBuilder.getObjectMapper().writeValueAsString(jsonSchema);
            Files.write(schemaPath, json.getBytes(), CREATE, WRITE, TRUNCATE_EXISTING);
            parent().out.println("Schema written to " + schemaPath.toAbsolutePath());
        } catch (Exception e) {
            throw new JReleaserException($("ERROR_unexpected_error"), e);
        }
    }
}
