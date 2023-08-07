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
import org.springframework.boot.test.json.JacksonTester;

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
        jsonString = "{\n" +
            "\t\"documentId\": \"4bd8f762-0f83-42a6-8640-741b3f848752\",\n" +
            "\t\"content\": {},\n" +
            "\t\"versionBasedOn\": \"4bed17666ef48cf38080015e993ef25452a840a35e81c5d013c3dfcdd6098dd4\"\n" +
            "}";
    }

    @Test
    public void shouldParseJson() throws IOException {
        final var documentId = UUID;
        final var jsonData = objectMapper.createObjectNode();
        final var versionBasedOn = "4bed17666ef48cf38080015e993ef25452a840a35e81c5d013c3dfcdd6098dd4";
        final ModifyDocumentRequest request = new ModifyDocumentRequest(
            documentId,
            jsonData,
            versionBasedOn
        );
        assertThat(jacksonTester.parse(jsonString)).isEqualTo(request);
    }

    @Test
    public void shouldMarshalObjectToJson() throws IOException {
        final var documentId = UUID;
        final var jsonData = objectMapper.createObjectNode();
        final var versionBasedOn = "4bed17666ef48cf38080015e993ef25452a840a35e81c5d013c3dfcdd6098dd4";
        final ModifyDocumentRequest request = new ModifyDocumentRequest(
            documentId,
            jsonData,
            versionBasedOn
        );
        assertThat(jacksonTester.write(request)).isEqualTo(jsonString);
    }

}