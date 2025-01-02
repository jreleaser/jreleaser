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
package org.jreleaser.engine.schema;

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
import org.jreleaser.model.JReleaserException;
import org.jreleaser.model.JReleaserVersion;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.model.internal.announce.Announce;
import org.jreleaser.model.internal.announce.HttpAnnouncer;
import org.jreleaser.model.internal.announce.WebhookAnnouncer;
import org.jreleaser.model.internal.download.AbstractSshDownloader;
import org.jreleaser.model.internal.download.FtpDownloader;
import org.jreleaser.model.internal.download.HttpDownloader;
import org.jreleaser.model.internal.upload.AbstractSshUploader;
import org.jreleaser.model.internal.upload.ArtifactoryUploader;
import org.jreleaser.model.internal.upload.FtpUploader;
import org.jreleaser.model.internal.upload.HttpUploader;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Collections.singletonMap;
import static org.jreleaser.bundle.RB.$;

/**
 * @author Andres Almiray
 * @since 1.4.0
 */
public final class JsonSchemaGenerator {
    private JsonSchemaGenerator() {
        // noop
    }

    public static void generate(PrintWriter out) {
        Map<String, String> mappings = new LinkedHashMap<>();
        mappings.put("Map<String, Object>", "Properties");
        mappings.put("Map<String, String>", "StringProperties");
        mappings.put("Map<String, WebhookAnnouncer>", "WebhookAnnouncerMap");
        mappings.put("Map<String, HttpAnnouncer>", "HttpAnnouncerMap");
        mappings.put("Map<String, ArchiveAssembler>", "ArchiveAssemblerMap");
        mappings.put("Map<String, JavaArchiveAssembler>", "JavaArchiveAssemblerMap");
        mappings.put("Map<String, JlinkAssembler>", "JlinkAssemblerMap");
        mappings.put("Map<String, JpackageAssembler>", "JpackageAssemblerMap");
        mappings.put("Map<String, NativeImageAssembler>", "NativeImageAssemblerMap");
        mappings.put("Map<String, Distribution>", "DistributionMap");
        mappings.put("Map<String, DockerSpec>", "DockerSpecMap");
        mappings.put("Map<String, JibSpec>", "JibSpecMap");
        mappings.put("Map<String, ArtifactoryUploader>", "ArtifactoryUploaderMap");
        mappings.put("Map<String, GiteaUploader>", "GiteaUploaderMap");
        mappings.put("Map<String, GitlabUploader>", "GitlabUploaderMap");
        mappings.put("Map<String, FtpUploader>", "FtpUploaderMap");
        mappings.put("Map<String, HttpUploader>", "HttpUploaderMap");
        mappings.put("Map<String, SftpUploader>", "SftpUploaderMap");
        mappings.put("Map<String, ScpUploader>", "ScpUploaderMap");
        mappings.put("Map<String, S3Uploader>", "S3UploaderMap");
        mappings.put("Map<String, FtpDownloader>", "FtpDownloaderMap");
        mappings.put("Map<String, HttpDownloader>", "HttpDownloaderMap");
        mappings.put("Map<String, SftpDownloader>", "SftpDownloaderMap");
        mappings.put("Map<String, ScpDownloader>", "ScpDownloaderMap");
        mappings.put("Map<String, Extension>", "ExtensionMap");
        mappings.put("Map<String, ArtifactoryMavenDeployer>", "ArtifactoryMavenDeployerMap");
        mappings.put("Map<String, AzureMavenDeployer>", "AzureMavenDeployerMap");
        mappings.put("Map<String, GiteaMavenDeployer>", "GiteaMavenDeployerMap");
        mappings.put("Map<String, GithubMavenDeployer>", "GithubMavenDeployerMap");
        mappings.put("Map<String, GitlabMavenDeployer>", "GitlabMavenDeployerMap");
        mappings.put("Map<String, Nexus2MavenDeployer>", "Nexus2MavenDeployerMap");
        mappings.put("Map<String, MavenCentralMavenDeployer>", "MavenCentralMavenDeployerMap");
        mappings.put("Map<String, SwidTag>", "SwidTagMap");

        try {
            SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_7, OptionPreset.PLAIN_JSON);
            configBuilder.getObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            configBuilder.with(Option.SCHEMA_VERSION_INDICATOR);
            configBuilder.with(Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT);
            configBuilder.with(Option.DEFINITION_FOR_MAIN_SCHEMA);
            configBuilder.with(Option.DEFINITIONS_FOR_ALL_OBJECTS);
            configBuilder.with(Option.MAP_VALUES_AS_ADDITIONAL_PROPERTIES);
            configBuilder.with(Option.GETTER_METHODS);
            configBuilder.with(Option.NONSTATIC_NONVOID_NONGETTER_METHODS);
            configBuilder.with(Option.FIELDS_DERIVED_FROM_ARGUMENTFREE_METHODS);
            configBuilder.with(Option.STRICT_TYPE_INFO);
            JacksonModule jacksonModule = new JacksonModule();
            configBuilder.with(jacksonModule);
            configBuilder.forTypesInGeneral()
                .withDescriptionResolver(scope -> scope.getType().getErasedType() == JReleaserModel.class ?
                    String.format("JReleaser %s", JReleaserVersion.getPlainVersion()) : null)
                .withPatternPropertiesResolver(scope -> {
                    if (scope.getType().isInstanceOf(Map.class)) {
                        ResolvedType type = scope.getTypeParameterFor(Map.class, 1);
                        if (type.getErasedType() != String.class && type.getErasedType() != Object.class) {
                            return singletonMap("^[a-zA-Z][a-zA-Z0-9-]*[a-zA-Z0-9]?$", type);
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
            configBuilder.forMethods()
                .withIgnoreCheck(method -> {
                    if (method.isVoid() || !method.getName().startsWith("get") || method.getArgumentCount() != 0) {
                        return true;
                    }
                    Class<?> declaringType = method.getDeclaringType().getErasedType();
                    return declaringType != Announce.class &&
                        declaringType != HttpAnnouncer.class &&
                        declaringType != HttpDownloader.class &&
                        declaringType != HttpUploader.class &&
                        declaringType != ArtifactoryUploader.class &&
                        declaringType != FtpDownloader.class &&
                        declaringType != FtpUploader.class &&
                        declaringType != AbstractSshDownloader.class &&
                        declaringType != AbstractSshUploader.class &&
                        declaringType != WebhookAnnouncer.class;
                });

            SchemaGeneratorConfig config = configBuilder.build();
            SchemaGenerator generator = new SchemaGenerator(config);
            JsonNode jsonSchema = generator.generateSchema(JReleaserModel.class);

            String fileName = String.format("jreleaser-schema-%s.json", JReleaserVersion.getPlainVersion());
            Path schemaPath = Paths.get(fileName);
            String json = configBuilder.getObjectMapper().writeValueAsString(jsonSchema);
            Files.write(schemaPath, json.getBytes(UTF_8), CREATE, WRITE, TRUNCATE_EXISTING);
            out.println("Schema written to " + schemaPath.toAbsolutePath());
        } catch (Exception e) {
            throw new JReleaserException($("ERROR_unexpected_error"), e);
        }
    }
}
