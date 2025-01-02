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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
class JsonUtilsTest {
    @Test
    void mergeSameNode() throws IOException {
        // given:
        String input1 = "{\n" +
            "  \"aliases\" : {\n" +
            "    \"one\" : {\n" +
            "      \"script-ref\" : \"one.java\",\n" +
            "      \"description\" : \"one\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

        ObjectMapper objectMapper = new ObjectMapper();

        // when:
        JsonNode node1 = objectMapper.readTree(input1);
        JsonNode node2 = objectMapper.readTree(input1);
        JsonNode node3 = JsonUtils.merge(node1, node2);

        // then:
        assertThat(node3, notNullValue());
        assertThat(node3.toPrettyString(), equalTo(node1.toPrettyString()));
    }

    @Test
    void mergeDistinctNodes() throws IOException {
        // given:
        String input1 = "{\n" +
            "  \"aliases\" : {\n" +
            "    \"one\" : {\n" +
            "      \"script-ref\" : \"one.java\",\n" +
            "      \"description\" : \"one\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

        String input2 = "{\n" +
            "  \"aliases\" : {\n" +
            "    \"two\" : {\n" +
            "      \"script-ref\" : \"two.java\",\n" +
            "      \"description\" : \"two\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

        String output = "{\n" +
            "  \"aliases\" : {\n" +
            "    \"one\" : {\n" +
            "      \"script-ref\" : \"one.java\",\n" +
            "      \"description\" : \"one\"\n" +
            "    },\n" +
            "    \"two\" : {\n" +
            "      \"script-ref\" : \"two.java\",\n" +
            "      \"description\" : \"two\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

        ObjectMapper objectMapper = new ObjectMapper();

        // when:
        JsonNode node1 = objectMapper.readTree(input1);
        JsonNode node2 = objectMapper.readTree(input2);
        JsonNode node3 = JsonUtils.merge(node1, node2);

        // then:
        assertThat(node3, notNullValue());
        assertThat(node3.toPrettyString(), equalTo(objectMapper.readTree(output).toPrettyString()));
    }

    @Test
    void mergeExistingNodes() throws IOException {
        // given:
        String input1 = "{\n" +
            "  \"aliases\" : {\n" +
            "    \"one\" : {\n" +
            "      \"script-ref\" : \"one.java\",\n" +
            "      \"description\" : \"one\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

        String input2 = "{\n" +
            "  \"aliases\" : {\n" +
            "    \"one\" : {\n" +
            "      \"script-ref\" : \"one.java\",\n" +
            "      \"description\" : \"DESCRIPTION\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

        ObjectMapper objectMapper = new ObjectMapper();

        // when:
        JsonNode node1 = objectMapper.readTree(input1);
        JsonNode node2 = objectMapper.readTree(input2);
        JsonNode node3 = JsonUtils.merge(node1, node2);

        // then:
        assertThat(node3, notNullValue());
        assertThat(node3.toPrettyString(), equalTo(node2.toPrettyString()));
    }
}
