/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.document.domain.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JacksonTester;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaDocumentDefinitionIdJsonSerializingTest {

    private static final String DEFINITION_STRING = "aDefinition";
    private JacksonTester<JsonSchemaDocumentDefinitionId> jacksonTester;
    private static final String JSON_STRING_VALUE = "{\"name\":\"aDefinition\",\"version\":1}";
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        JacksonTester.initFields(this, objectMapper);
    }

    @Test
    public void shouldParseJson() throws IOException {
        final JsonSchemaDocumentDefinitionId definitionId = JsonSchemaDocumentDefinitionId.newId(DEFINITION_STRING);
        assertThat(jacksonTester.parse(JSON_STRING_VALUE)).isEqualTo(definitionId);
    }

    @Test
    public void shouldMarshalObjectToJson() throws IOException {
        final JsonSchemaDocumentDefinitionId definitionId = JsonSchemaDocumentDefinitionId.newId(DEFINITION_STRING);
        assertThat(jacksonTester.write(definitionId)).isEqualToJson(JSON_STRING_VALUE);
    }

}