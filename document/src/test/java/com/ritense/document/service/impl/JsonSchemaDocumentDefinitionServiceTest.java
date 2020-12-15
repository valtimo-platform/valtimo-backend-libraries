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

package com.ritense.document.service.impl;

import com.ritense.document.BaseTest;
import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.impl.JsonSchema;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.exception.DocumentDefinitionNameMismatchException;
import com.ritense.document.repository.impl.JsonSchemaDocumentDefinitionRepository;
import com.ritense.document.service.DocumentDefinitionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JsonSchemaDocumentDefinitionServiceTest extends BaseTest {

    private DocumentDefinitionService documentDefinitionService;
    private JsonSchemaDocumentDefinitionRepository jsonSchemaDocumentDefinitionRepository;
    private ResourceLoader resourceLoader;
    private JsonSchemaDocumentDefinition definition;

    @BeforeEach
    public void setUp() {
        jsonSchemaDocumentDefinitionRepository = mock(JsonSchemaDocumentDefinitionRepository.class);
        resourceLoader = mock(DefaultResourceLoader.class);
        documentDefinitionService = mock(JsonSchemaDocumentDefinitionService.class);
        documentDefinitionService = spy(new JsonSchemaDocumentDefinitionService(resourceLoader, jsonSchemaDocumentDefinitionRepository));
        definition = definitionOf("person");
    }

    @Test
    @Disabled //TODO try to mock resource loading or refactor
    public void shouldDeployAll() {
        when(jsonSchemaDocumentDefinitionRepository.findAllByIdName(anyString())).thenReturn(Collections.emptyList());
        when(jsonSchemaDocumentDefinitionRepository.findFirstByIdNameOrderByIdVersionDesc(anyString())).thenReturn(Optional.empty());
        documentDefinitionService.deployAll();
        verify(documentDefinitionService, times(3)).deploy(any(JsonSchemaDocumentDefinition.class));
    }

    @Test
    public void shouldDeploy() {
        when(jsonSchemaDocumentDefinitionRepository.findFirstByIdNameOrderByIdVersionDesc(anyString())).thenReturn(Optional.empty());
        when(jsonSchemaDocumentDefinitionRepository.findById(any(JsonSchemaDocumentDefinitionId.class))).thenReturn(Optional.empty());

        documentDefinitionService.deploy(definition);

        verify(jsonSchemaDocumentDefinitionRepository, times(1)).saveAndFlush(definition);
    }

    @Test
    public void shouldReturnSaveOnceWhenDeployingUnchangedSchema() {
        when(jsonSchemaDocumentDefinitionRepository.findFirstByIdNameOrderByIdVersionDesc(anyString())).thenReturn(Optional.empty());
        when(jsonSchemaDocumentDefinitionRepository.findById(any(JsonSchemaDocumentDefinitionId.class))).thenReturn(Optional.empty())
            .thenReturn(Optional.of(definition));

        documentDefinitionService.deploy(definition);
        documentDefinitionService.deploy(definition);

        verify(jsonSchemaDocumentDefinitionRepository, times(1)).saveAndFlush(definition);
    }

    @Test
    public void shouldThrowExceptionWhenDeployingChangedSchema() {
        when(jsonSchemaDocumentDefinitionRepository.findFirstByIdNameOrderByIdVersionDesc(anyString())).thenReturn(Optional.empty());
        when(jsonSchemaDocumentDefinitionRepository.findById(any(JsonSchemaDocumentDefinitionId.class))).thenReturn(Optional.of(definition));

        final var definitionChanged = definitionOf("house");

        assertThrows(UnsupportedOperationException.class, () -> documentDefinitionService.deploy(definitionChanged));
    }

    @Test
    public void shouldThrowExceptionWhenDeployingNameMismatchedSchema() {
        when(jsonSchemaDocumentDefinitionRepository.findFirstByIdNameOrderByIdVersionDesc(anyString())).thenReturn(Optional.empty());
        when(jsonSchemaDocumentDefinitionRepository.findById(any(JsonSchemaDocumentDefinitionId.class))).thenReturn(Optional.of(definition));

        final var jsonSchemaDocumentDefinitionId = JsonSchemaDocumentDefinitionId.newId("person");
        final var otherJsonSchemaDocumentDefinitionId = JsonSchemaDocumentDefinitionId.newId("person2");
        final var jsonSchema = JsonSchema.fromResource(jsonSchemaDocumentDefinitionId.path());
        assertThrows(DocumentDefinitionNameMismatchException.class, () -> new JsonSchemaDocumentDefinition(otherJsonSchemaDocumentDefinitionId, jsonSchema));
    }

}