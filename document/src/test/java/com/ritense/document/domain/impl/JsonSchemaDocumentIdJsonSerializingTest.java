/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaDocumentIdJsonSerializingTest {

    private static final String UUID_STRING = "4bd8f762-0f83-42a6-8640-741b3f848752";
    private static final String JSON_STRING_VALUE = "\"4bd8f762-0f83-42a6-8640-741b3f848752\"";

    private JacksonTester<JsonSchemaDocumentId> jacksonTester;

    @BeforeEach
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        JacksonTester.initFields(this, objectMapper);
    }

    @Test
    public void shouldParseJson() throws IOException {
        var documentId = JsonSchemaDocumentId.existingId(UUID.fromString(UUID_STRING));
        ObjectContent<JsonSchemaDocumentId> jsonSchemaDocumentIdObjectContent = jacksonTester.parse(JSON_STRING_VALUE);
        assertThat(jsonSchemaDocumentIdObjectContent.getObject()).isEqualTo(documentId);
    }

    @Test
    public void shouldMarshalObjectToJson() throws IOException {
        final JsonSchemaDocumentId documentId = JsonSchemaDocumentId.existingId(UUID.fromString(UUID_STRING));
        JsonContent<JsonSchemaDocumentId> jsonSchemaDocumentIdJsonContent = jacksonTester.write(documentId);
        JSONAssert.assertEquals(jsonSchemaDocumentIdJsonContent.getJson(), JSON_STRING_VALUE, JSONCompareMode.STRICT);
    }
}