/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.valtimo.contract.case_.CaseDefinitionId;
import com.ritense.valtimo.contract.json.MapperSingleton;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

class JsonSchemaDocumentDefinitionIdJsonSerializingTest {

    private static final String DEFINITION_STRING = "aDefinition";
    private JacksonTester<JsonSchemaDocumentDefinitionId> jacksonTester;
    private static final String JSON_STRING_VALUE = "{\"name\":\"aDefinition\",\"caseDefinitionId\":{\"key\":\"key\", \"versionTag\":\"1.0.0\"}}";

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = MapperSingleton.INSTANCE.get();
        JacksonTester.initFields(this, objectMapper);
    }

    @Test
    void shouldParseJson() throws IOException {
        final JsonSchemaDocumentDefinitionId definitionId = JsonSchemaDocumentDefinitionId.of(DEFINITION_STRING, CaseDefinitionId.of("key", "1.0.0"));
        ObjectContent<JsonSchemaDocumentDefinitionId> jsonSchemaDocumentDefinitionIdObjectContent = this.jacksonTester.parse(JSON_STRING_VALUE);
        assertThat(jsonSchemaDocumentDefinitionIdObjectContent.getObject()).isEqualTo(definitionId);
    }

    @Test
    void shouldMarshalObjectToJson() throws IOException {
        final JsonSchemaDocumentDefinitionId definitionId = JsonSchemaDocumentDefinitionId.of(DEFINITION_STRING, CaseDefinitionId.of("key", "1.0.0"));
        JsonContent<JsonSchemaDocumentDefinitionId> jsonSchemaDocumentDefinitionIdJsonContent = this.jacksonTester.write(definitionId);
        JSONAssert.assertEquals(jsonSchemaDocumentDefinitionIdJsonContent.getJson(), JSON_STRING_VALUE, JSONCompareMode.STRICT);
    }
}