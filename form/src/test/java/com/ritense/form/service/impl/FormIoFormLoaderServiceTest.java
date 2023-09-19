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

package com.ritense.form.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.ritense.document.domain.impl.JsonDocumentContent;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.service.DocumentSequenceGeneratorService;
import com.ritense.document.service.impl.JsonSchemaDocumentService;
import com.ritense.form.BaseTest;
import com.ritense.form.domain.FormIoFormDefinition;
import com.ritense.form.repository.FormDefinitionRepository;
import com.ritense.form.service.FormLoaderService;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import com.ritense.form.service.PrefillFormService;
import io.jsonwebtoken.lang.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FormIoFormLoaderServiceTest extends BaseTest {
    private FormLoaderService formLoaderService;
    private DocumentSequenceGeneratorService documentSequenceGeneratorService;
    private FormDefinitionRepository formDefinitionRepository;

    private PrefillFormService prefillFormService;

    @BeforeEach
    public void setUp() {
        documentSequenceGeneratorService = mock(DocumentSequenceGeneratorService.class);
        formDefinitionRepository = mock(FormDefinitionRepository.class);
        prefillFormService = mock(PrefillFormService.class);
        when(documentSequenceGeneratorService.next(any())).thenReturn(1L);
        formLoaderService = new FormIoFormLoaderService(formDefinitionRepository, prefillFormService);
        mockSpringContextHelper();
    }

    @Test
    public void shouldGetFormDefinition() throws IOException {
        final String formDefinitionName = "form-example";

        FormIoFormDefinition formIoFormDefinition = formDefinitionOf(formDefinitionName);
        when(formDefinitionRepository.findByName(eq(formDefinitionName))).thenReturn(Optional.of(formIoFormDefinition));

        final Optional<JsonNode> formDefinition = formLoaderService.getFormDefinitionByName(formDefinitionName);
        assertThat(formDefinition).isPresent();
    }

    @Test
    public void shouldGetFormDefinitionPreFilled() throws IOException {
        final Optional<JsonSchemaDocument> jsonSchemaDocument = documentOptional();

        final String formDefinitionName = "form-example";

        final var formIoFormDefinition = formDefinitionOf(formDefinitionName);
        when(formDefinitionRepository.findByName(eq(formDefinitionName))).thenReturn(Optional.of(formIoFormDefinition));
        when(prefillFormService.getPrefilledFormDefinition(any(), any())).then(invocation -> {
            formIoFormDefinition.preFill(
                Map.of(
                    "person.firstName", "John"
                )
            );
            return formIoFormDefinition;
        });
        final var formDefinition = formLoaderService.getFormDefinitionByNamePreFilled(
            formDefinitionName,
            jsonSchemaDocument.orElseThrow().id()
        );
        assertThat(formDefinition).isPresent();
        assertThat(formDefinition.get().get("components").get(0).get("defaultValue").asText()).isEqualTo("John");
    }

    @Test
    public void shouldGetFormDefinitionPreFilledWithNestedComponents() throws IOException {
        final Optional<JsonSchemaDocument> jsonSchemaDocument = documentOptional();

        String formDefinitionId = "form-example-nested-components";

        FormIoFormDefinition formIoFormDefinition = formDefinitionOf(formDefinitionId);
        when(formDefinitionRepository.findByName(eq(formDefinitionId))).thenReturn(Optional.of(formIoFormDefinition));
        when(prefillFormService.getPrefilledFormDefinition(any(), any())).then(invocation -> {
            formIoFormDefinition.preFill(
                Map.of(
                    "person.firstName", "John"
                )
            );
            return formIoFormDefinition;
        });
        final Optional<JsonNode> formDefinition = formLoaderService
            .getFormDefinitionByNamePreFilled(formDefinitionId, jsonSchemaDocument.orElseThrow().id());
        assertThat(formDefinition).isPresent();
        assertThat(formDefinition.get().get("components").get(3).get("components").get(0).get("components").get(0).get("defaultValue").asText())
            .isEqualTo("John");
    }

    private Optional<JsonSchemaDocument> documentOptional() {
        return JsonSchemaDocument.create(
            definition(),
            new JsonDocumentContent("{\n" +
                "\t\"person\" : {\n" +
                "\t\t\"firstName\" : \"John\"\n" +
                "\t}\n" +
                "}"
            ),
            "USERNAME",
            documentSequenceGeneratorService,
            null
        ).resultingDocument();
    }

}
