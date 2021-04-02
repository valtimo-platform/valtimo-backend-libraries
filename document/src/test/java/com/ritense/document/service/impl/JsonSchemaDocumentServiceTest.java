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
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.repository.impl.JsonSchemaDocumentRepository;
import com.ritense.resource.service.ResourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JsonSchemaDocumentServiceTest extends BaseTest {

    private JsonSchemaDocumentService jsonSchemaDocumentService;
    private JsonSchemaDocumentRepository documentRepository;
    private JsonSchemaDocumentDefinitionService documentDefinitionService;
    private JsonSchemaDocumentDefinitionSequenceGeneratorService documentSequenceGeneratorService;
    private ResourceService resourceService;
    private JsonSchemaDocument jsonSchemaDocument;

    private final String documentDefinitionName = "name";

    @BeforeEach
    void init() {
        documentRepository = mock(JsonSchemaDocumentRepository.class);
        documentDefinitionService = mock(JsonSchemaDocumentDefinitionService.class);
        documentSequenceGeneratorService = mock(JsonSchemaDocumentDefinitionSequenceGeneratorService.class);
        resourceService = mock(ResourceService.class);
        jsonSchemaDocument = mock(JsonSchemaDocument.class);

        jsonSchemaDocumentService = new JsonSchemaDocumentService(
            documentRepository,
            documentDefinitionService,
            documentSequenceGeneratorService,
            resourceService);
    }

    @Test
    void shouldRemoveDocuments() {
        PageImpl<JsonSchemaDocument> jsonSchemaDocuments = new PageImpl<>(List.of(this.jsonSchemaDocument));

        when(documentRepository.findAllByDocumentDefinitionIdName(eq(Pageable.unpaged()), eq(documentDefinitionName)))
            .thenReturn(jsonSchemaDocuments);

        jsonSchemaDocumentService.removeDocuments(documentDefinitionName);

        verify(documentRepository, times(1)).saveAll(eq(jsonSchemaDocuments.toList()));
        verify(documentRepository, times(1)).deleteAll(eq(jsonSchemaDocuments.toList()));
        verify(documentSequenceGeneratorService, times(1)).deleteSequenceRecordBy(eq(documentDefinitionName));
    }

    @Test
    void shouldNotRemoveDocumentsBecauseTheyDontExist() {
        PageImpl<JsonSchemaDocument> jsonSchemaDocuments = new PageImpl<>(Collections.emptyList());

        when(documentRepository.findAllByDocumentDefinitionIdName(eq(Pageable.unpaged()), eq(documentDefinitionName)))
            .thenReturn(jsonSchemaDocuments);

        jsonSchemaDocumentService.removeDocuments(documentDefinitionName);

        verify(documentRepository, times(0)).saveAll(eq(jsonSchemaDocuments.toList()));
        verify(documentRepository, times(0)).deleteAll(eq(jsonSchemaDocuments.toList()));
        verify(documentSequenceGeneratorService, times(0)).deleteSequenceRecordBy(eq(documentDefinitionName));
    }
}