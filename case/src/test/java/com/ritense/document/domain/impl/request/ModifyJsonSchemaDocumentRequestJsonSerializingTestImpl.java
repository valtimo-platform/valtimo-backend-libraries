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

package com.ritense.document.domain.impl.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.valtimo.contract.json.MapperSingleton;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

public class ModifyJsonSchemaDocumentRequestJsonSerializingTestImpl {

    private static final String UUID = "4bd8f762-0f83-42a6-8640-741b3f848752";
    private JacksonTester<ModifyDocumentRequest> jacksonTester;
    private String jsonString;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = MapperSingleton.INSTANCE.get();
        JacksonTester.initFields(this, objectMapper);
        jsonString = """
            {
            \t"documentId": "4bd8f762-0f83-42a6-8640-741b3f848752",
            \t"content": {}
            }""";
    }

    @Test
    public void shouldParseJson() throws IOException {
        final var jsonData = objectMapper.createObjectNode();
        final ModifyDocumentRequest request = new ModifyDocumentRequest(
            UUID,
            jsonData
        );
        ObjectContent<ModifyDocumentRequest> modifyDocumentRequestObjectContent = jacksonTester.parse(jsonString);
        assertThat(modifyDocumentRequestObjectContent.getObject()).isEqualTo(request);
    }

    @Test
    public void shouldMarshalObjectToJson() throws IOException {
        final var jsonData = objectMapper.createObjectNode();
        final ModifyDocumentRequest request = new ModifyDocumentRequest(
            UUID,
            jsonData
        );
        JsonContent<ModifyDocumentRequest> modifyDocumentRequestJsonContent = jacksonTester.write(request);
        JSONAssert.assertEquals(modifyDocumentRequestJsonContent.getJson(), jsonString, JSONCompareMode.STRICT);
    }
}