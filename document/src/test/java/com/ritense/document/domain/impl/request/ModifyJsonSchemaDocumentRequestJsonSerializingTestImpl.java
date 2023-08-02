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

package com.ritense.document.domain.impl.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;

import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;

public class ModifyJsonSchemaDocumentRequestJsonSerializingTestImpl {

    private static final String UUID = "4bd8f762-0f83-42a6-8640-741b3f848752";
    private JacksonTester<ModifyDocumentRequest> jacksonTester;
    private String jsonString;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        JacksonTester.initFields(this, objectMapper);
        jsonString = """
            {
            \t"documentId": "4bd8f762-0f83-42a6-8640-741b3f848752",
            \t"content": {},
            \t"versionBasedOn": "4bed17666ef48cf38080015e993ef25452a840a35e81c5d013c3dfcdd6098dd4"
            }""";
    }

    @Test
    public void shouldParseJson() throws IOException {
        final var jsonData = objectMapper.createObjectNode();
        final var versionBasedOn = "4bed17666ef48cf38080015e993ef25452a840a35e81c5d013c3dfcdd6098dd4";
        final ModifyDocumentRequest request = new ModifyDocumentRequest(
            UUID,
            jsonData,
            versionBasedOn
        );
        ObjectContent<ModifyDocumentRequest> modifyDocumentRequestObjectContent = jacksonTester.parse(jsonString);
        assertThat(modifyDocumentRequestObjectContent.getObject()).isEqualTo(request);
    }

    @Test
    public void shouldMarshalObjectToJson() throws IOException {
        final var jsonData = objectMapper.createObjectNode();
        final var versionBasedOn = "4bed17666ef48cf38080015e993ef25452a840a35e81c5d013c3dfcdd6098dd4";
        final ModifyDocumentRequest request = new ModifyDocumentRequest(
            UUID,
            jsonData,
            versionBasedOn
        );
        JsonContent<ModifyDocumentRequest> modifyDocumentRequestJsonContent = jacksonTester.write(request);
        JSONAssert.assertEquals(modifyDocumentRequestJsonContent.getJson(), jsonString, JSONCompareMode.STRICT);
    }
}