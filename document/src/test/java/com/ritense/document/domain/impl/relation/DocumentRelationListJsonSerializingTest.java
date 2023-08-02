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

package com.ritense.document.domain.impl.relation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.document.domain.impl.JsonSchemaDocumentId;
import com.ritense.document.domain.relation.DocumentRelationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

public class DocumentRelationListJsonSerializingTest {

    private static final String UUID_STRING = "91e750e1-53ab-4922-9979-6a2dacd009cf";
    private JacksonTester<Set<JsonSchemaDocumentRelation>> jacksonTester;
    private String jsonString;

    @BeforeEach
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        JacksonTester.initFields(this, objectMapper);
        jsonString = """
            [{
            \t"id": "91e750e1-53ab-4922-9979-6a2dacd009cf",
            \t"relationType": "NEXT"
            }]""";
    }

    @Test
    public void shouldParseJson() throws IOException {
        final JsonSchemaDocumentRelation relationship = new JsonSchemaDocumentRelation(
            JsonSchemaDocumentId.newId(UUID.fromString(UUID_STRING)),
            DocumentRelationType.NEXT
        );
        final Set<JsonSchemaDocumentRelation> relationships = Set.of(relationship);
        ObjectContent<Set<JsonSchemaDocumentRelation>> jsonSchemaDocumentRelationObjectContent = jacksonTester.parse(jsonString);
        assertThat(jsonSchemaDocumentRelationObjectContent.getObject()).isEqualTo(relationships);
    }

    @Test
    public void shouldMarshalObjectToJson() throws IOException {
        final JsonSchemaDocumentRelation relationship = new JsonSchemaDocumentRelation(
            JsonSchemaDocumentId.newId(UUID.fromString(UUID_STRING)),
            DocumentRelationType.NEXT
        );
        final Set<JsonSchemaDocumentRelation> relationships = Set.of(relationship);
        JsonContent<Set<JsonSchemaDocumentRelation>> jsonContent = jacksonTester.write(relationships);
        JSONAssert.assertEquals(jsonContent.getJson(), jsonString, JSONCompareMode.STRICT);
    }
}