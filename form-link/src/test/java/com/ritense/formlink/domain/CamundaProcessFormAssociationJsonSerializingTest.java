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

package com.ritense.formlink.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.formlink.BaseTest;
import com.ritense.formlink.domain.impl.formassociation.FormAssociations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JacksonTester;
import java.io.IOException;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

public class CamundaProcessFormAssociationJsonSerializingTest extends BaseTest {

    private static final String UUID_STRING_ID = "4bd8f762-0f83-42a6-8640-741b3f848752";
    private static final String UUID_STRING_FORM_ASSOCIATION_ID = "e407d9a3-5611-48d8-bb91-1f45af5a9967";
    private static final String UUID_STRING_FORM_ID = "4bd8f762-0f83-42a6-8640-741b3f848754";
    private static final String JSON_STRING_VALUE = "[{\n" +
        "\t\"className\": \"com.ritense.formlink.domain.impl.formassociation.UserTaskFormAssociation\",\n" +
        "\t\"id\": \"e407d9a3-5611-48d8-bb91-1f45af5a9967\",\n" +
        "\t\"formLink\": {\n" +
        "\t\t\"className\": \"com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormIdLink\",\n" +
        "\t\t\"id\": \"user-task-id\",\n" +
        "\t\t\"formId\": \"4bd8f762-0f83-42a6-8640-741b3f848754\"\n" +
        "\t}\n" +
        "}]";
    private static final String JSON_WITH_IS_PUBLIC_STRING_VALUE = "[{\n" +
        "\t\"className\": \"com.ritense.formlink.domain.impl.formassociation.UserTaskFormAssociation\",\n" +
        "\t\"id\": \"e407d9a3-5611-48d8-bb91-1f45af5a9967\",\n" +
        "\t\"formLink\": {\n" +
        "\t\t\"className\": \"com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormIdLink\",\n" +
        "\t\t\"id\": \"user-task-id\",\n" +
        "\t\t\"isPublic\": \"false\",\n" +
        "\t\t\"formId\": \"4bd8f762-0f83-42a6-8640-741b3f848754\"\n" +
        "\t}\n" +
        "}]";

    private JacksonTester<FormAssociations> jacksonTester;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        JacksonTester.initFields(this, objectMapper);
    }

    @Test
    public void shouldParseJson() throws IOException {
        final var camundaProcessFormAssociation = processFormAssociation(
            UUID.fromString(UUID_STRING_ID),
            UUID.fromString(UUID_STRING_FORM_ASSOCIATION_ID),
            UUID.fromString(UUID_STRING_FORM_ID)
        );
        assertThat(jacksonTester.parse(JSON_STRING_VALUE)).isEqualTo(camundaProcessFormAssociation.getFormAssociations());
    }

    @Test
    public void shouldParseJsonWithIsPublic() throws IOException {
        final var camundaProcessFormAssociation = processFormAssociation(
            UUID.fromString(UUID_STRING_ID),
            UUID.fromString(UUID_STRING_FORM_ASSOCIATION_ID),
            UUID.fromString(UUID_STRING_FORM_ID)
        );

        assertThat(jacksonTester.parse(JSON_WITH_IS_PUBLIC_STRING_VALUE)).isEqualTo(camundaProcessFormAssociation.getFormAssociations());
    }

    @Test
    public void shouldMarshalObjectToJson() throws IOException {
        final var camundaProcessFormAssociation = processFormAssociation(
            UUID.fromString(UUID_STRING_ID),
            UUID.fromString(UUID_STRING_FORM_ASSOCIATION_ID),
            UUID.fromString(UUID_STRING_FORM_ID)
        );
        assertThat(jacksonTester.write(camundaProcessFormAssociation.getFormAssociations())).isEqualToJson(JSON_STRING_VALUE);
    }
}