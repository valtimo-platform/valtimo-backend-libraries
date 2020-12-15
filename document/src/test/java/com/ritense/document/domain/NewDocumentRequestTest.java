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

package com.ritense.document.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.document.domain.impl.request.NewDocumentRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NewDocumentRequestTest {

    @Test
    public void shouldHaveEqualGetters() throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode jsonData = objectMapper.readTree("{\"key\":123,\"somethingelse\":\"henk\",\"nested\":{\"henk\":\"jan\"}}");
        final String definitionName = "some-name";

        var newDocRequest = new NewDocumentRequest(definitionName, jsonData);

        assertThat(jsonData).isEqualTo(newDocRequest.content());
        assertThat(definitionName).isEqualTo(newDocRequest.documentDefinitionName());
    }
}