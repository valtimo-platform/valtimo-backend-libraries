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

package com.ritense.document.service.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ritense.BaseTest;
import com.ritense.authorization.AuthorizationService;
import com.ritense.document.domain.impl.JsonSchema;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.exception.DocumentDefinitionNameMismatchException;
import com.ritense.document.repository.impl.JsonSchemaDocumentDefinitionRepository;
import jakarta.validation.ValidationException;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

class JsonSchemaDocumentDefinitionServiceTest extends BaseTest {

    private JsonSchemaDocumentDefinitionService documentDefinitionService;
    private JsonSchemaDocumentDefinitionRepository jsonSchemaDocumentDefinitionRepository;
    private ResourceLoader resourceLoader;
    private JsonSchemaDocumentDefinition definition;

    @BeforeEach
    public void setUp() {
        jsonSchemaDocumentDefinitionRepository = mock(JsonSchemaDocumentDefinitionRepository.class);
        resourceLoader = mock(DefaultResourceLoader.class);
        documentDefinitionService = spy(new JsonSchemaDocumentDefinitionService(
            resourceLoader,
            jsonSchemaDocumentDefinitionRepository,
            mock(AuthorizationService.class)
        ));
        definition = definitionOfForUnitTests("person");
    }

    @Test
    void shouldStore() {
        when(jsonSchemaDocumentDefinitionRepository.findFirstByIdNameOrderByIdCaseDefinitionIdVersionTagDesc(anyString())).thenReturn(
            Optional.empty());
        when(jsonSchemaDocumentDefinitionRepository.findById(any(JsonSchemaDocumentDefinitionId.class))).thenReturn(
            Optional.empty());

        documentDefinitionService.store(definition);

        verify(jsonSchemaDocumentDefinitionRepository, times(1)).saveAndFlush(definition);
    }

    @Test
    void shouldThrowExceptionWhenDeployingNameMismatchedSchema() {
        when(jsonSchemaDocumentDefinitionRepository.findFirstByIdNameOrderByIdCaseDefinitionIdVersionTagDesc(anyString())).thenReturn(
            Optional.empty());
        when(jsonSchemaDocumentDefinitionRepository.findById(any(JsonSchemaDocumentDefinitionId.class))).thenReturn(
            Optional.of(definition));

        final var jsonSchemaDocumentDefinitionId = JsonSchemaDocumentDefinitionId.of("person", mock());
        final var otherJsonSchemaDocumentDefinitionId = JsonSchemaDocumentDefinitionId.of("person2", mock());
        final var jsonSchema = JsonSchema.fromResourceUri(path(jsonSchemaDocumentDefinitionId.name()));
        assertThrows(
            DocumentDefinitionNameMismatchException.class,
            () -> new JsonSchemaDocumentDefinition(otherJsonSchemaDocumentDefinitionId, jsonSchema)
        );
    }

    @Test
    void shouldValidateProperty() {
        String documentDefinitionName = "name";
        documentDefinitionService.removeDocumentDefinition(documentDefinitionName);

        verify(jsonSchemaDocumentDefinitionRepository, times(1)).deleteByIdName(documentDefinitionName);
    }

    @Test
    void shouldValidateJsonPathInDefinitionWithReference() {
        var definition = definitionOfForUnitTests("combined-schema-additional-property-example");
        assertTrue(documentDefinitionService.isValidJsonPath(definition, "$.address.streetName"));
        assertFalse(documentDefinitionService.isValidJsonPath(definition, "$.address.nonExistent"));
    }

    @Test
    void shouldValidateJsonPathInDefinitionWithReferenceToOtherFile() {
        var definition = definitionOfForUnitTests("referenced");
        assertTrue(documentDefinitionService.isValidJsonPath(definition, "$.address.streetName"));
        assertFalse(documentDefinitionService.isValidJsonPath(definition, "$.address.nonExistent"));
    }

    @Test
    void shouldValidateJsonPathWithArray() {
        var definition = definitionOfForUnitTests("array-example");
        assertTrue(documentDefinitionService.isValidJsonPath(definition, "$.files[0].id"));
        assertTrue(documentDefinitionService.isValidJsonPath(definition, "$.files.[0].id"));
        assertTrue(documentDefinitionService.isValidJsonPath(definition, "$.['files'][0]['id']"));
        assertTrue(documentDefinitionService.isValidJsonPath(definition, "$.['files'].[0].['id']"));
        assertFalse(documentDefinitionService.isValidJsonPath(definition, "$.files[0].nonExistent"));
    }

    @Test
    void shouldValidateJsonPathWithFunctions() {
        var definition = definitionOfForUnitTests("array-example");
        assertTrue(documentDefinitionService.isValidJsonPath(definition, "$.files.length()"));
        assertTrue(documentDefinitionService.isValidJsonPath(definition, "$.files.someDatabaseSpecificFunction()"));
        assertFalse(documentDefinitionService.isValidJsonPath(definition, "$.files.missingBracket("));
    }

    @Test
    void shouldValidateJsonPathWithWildcard() {
        var definition = definitionOfForUnitTests("array-example");
        assertTrue(documentDefinitionService.isValidJsonPath(definition, "$.files[*].id"));
        assertFalse(documentDefinitionService.isValidJsonPath(definition, "$.nonExistent[*].id"));
    }

    @Test
    void shouldValidateJsonPointerWithoutAdditionalProperties() {
        var definitionName = "combined-schema-additional-property-example";
        mockDefinition(definitionName);
        documentDefinitionService.validateJsonPointer(definitionName, "/address/streetName");
        var exception = assertThrows(
            ValidationException.class, () ->
                documentDefinitionService.validateJsonPointer(definitionName, "/address/nonExistent")
        );
        assertEquals(
            "JsonPointer '/address/nonExistent' doesn't point to any property inside document definition 'combined-schema-additional-property-example'",
            exception.getMessage()
        );
    }

    @Test
    void shouldValidateJsonPointerWithAdditionalProperties() {
        var definitionName = "allows-additional-properties";
        mockDefinition(definitionName);
        documentDefinitionService.validateJsonPointer(definitionName, "/address/streetName");
        documentDefinitionService.validateJsonPointer(definitionName, "/address/nonExistent");
        var exception = assertThrows(
            ValidationException.class, () ->
                documentDefinitionService.validateJsonPointer(definitionName, "/nonExistent")
        );
        assertEquals(
            "JsonPointer '/nonExistent' doesn't point to any property inside document definition 'allows-additional-properties'",
            exception.getMessage()
        );
    }

    @Test
    void shouldGetPropertyNamesFromReferencedNestedObject() {
        var definitionName = "combined-schema-additional-property-example";
        var definition = mockDefinition(definitionName);

        var names = documentDefinitionService.getPropertyNames(definition);

        Collections.sort(names);
        assertArrayEquals(
            names.toArray(), new String[]{
                "/address/city",
                "/address/country",
                "/address/number",
                "/address/province",
                "/address/streetName"
            }
        );
    }

    public JsonSchemaDocumentDefinition mockDefinition(String definitionName) {
        var definition = definitionOfForUnitTests(definitionName);
        when(jsonSchemaDocumentDefinitionRepository.findFirstByIdNameOrderByIdCaseDefinitionIdVersionTagDesc(
            definitionName))
            .thenReturn(Optional.of(definition));
        return definition;
    }

    public URI path(String name) {
        return URI.create(String.format("config/unit-test/document/definition/%s.json", name + ".schema"));
    }
}
