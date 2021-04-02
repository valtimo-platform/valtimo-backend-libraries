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
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinition;
import com.ritense.document.service.result.UndeployDocumentDefinitionResult;
import com.ritense.document.service.result.UndeployDocumentDefinitionResultFailed;
import com.ritense.document.service.result.UndeployDocumentDefinitionResultSucceeded;
import com.ritense.valtimo.contract.event.UndeployDocumentDefinitionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UndeployJsonSchemaDocumentDefinitionServiceTest extends BaseTest {

    private UndeployJsonSchemaDocumentDefinitionService undeployJsonSchemaDocumentDefinitionService;
    private JsonSchemaDocumentDefinitionService documentDefinitionService;
    private JsonSchemaDocumentService documentService;
    private ApplicationEventPublisher applicationEventPublisher;
    private final String documentDefinitionName = "name";
    private UndeployDocumentDefinitionEvent undeployDocumentDefinitionEvent;
    private JsonSchemaDocumentDefinition documentDefinition;

    @BeforeEach
    void setUp() {
        undeployDocumentDefinitionEvent = new UndeployDocumentDefinitionEvent(documentDefinitionName);
        documentDefinition = mock(JsonSchemaDocumentDefinition.class);
        documentDefinitionService = mock(JsonSchemaDocumentDefinitionService.class);
        documentService = mock(JsonSchemaDocumentService.class);
        applicationEventPublisher = mock(ApplicationEventPublisher.class);

        undeployJsonSchemaDocumentDefinitionService = new UndeployJsonSchemaDocumentDefinitionService(
            documentDefinitionService,
            documentService,
            applicationEventPublisher
        );
    }

    @Test
    void undeploySuccess() {
        when(documentDefinitionService.findLatestByName(eq(documentDefinitionName))).thenReturn(Optional.of(documentDefinition));
        when(documentDefinition.isReadOnly()).thenReturn(false);

        UndeployDocumentDefinitionResult result = undeployJsonSchemaDocumentDefinitionService.undeploy(documentDefinitionName);

        verify(documentService, times(1)).removeDocuments(eq(documentDefinitionName));
        verify(documentDefinitionService, times(1)).removeDocumentDefinition(eq(documentDefinitionName));
        verify(applicationEventPublisher, times(1)).publishEvent(eq(undeployDocumentDefinitionEvent));

        assertThat(result).isInstanceOf(UndeployDocumentDefinitionResultSucceeded.class);
    }

    @Test
    void undeployResultFailedDueToThrownException() {
        when(documentDefinitionService.findLatestByName(eq(documentDefinitionName))).thenReturn(Optional.of(documentDefinition));
        when(documentDefinition.isReadOnly()).thenReturn(false);

        doThrow(IllegalArgumentException.class).when(documentDefinitionService).removeDocumentDefinition(eq(documentDefinitionName));

        UndeployDocumentDefinitionResult result = undeployJsonSchemaDocumentDefinitionService.undeploy(documentDefinitionName);

        verify(documentService, times(1)).removeDocuments(eq(documentDefinitionName));
        verify(documentDefinitionService, times(1)).removeDocumentDefinition(eq(documentDefinitionName));
        verify(applicationEventPublisher, times(0)).publishEvent(eq(undeployDocumentDefinitionEvent));

        assertThat(result).isInstanceOf(UndeployDocumentDefinitionResultFailed.class);
    }

    @Test
    void undeployFailedDueToReadOnly() {
        when(documentDefinitionService.findLatestByName(eq(documentDefinitionName))).thenReturn(Optional.of(documentDefinition));
        when(documentDefinition.isReadOnly()).thenReturn(true);

        UndeployDocumentDefinitionResult result = undeployJsonSchemaDocumentDefinitionService.undeploy(documentDefinitionName);

        verify(documentService, times(0)).removeDocuments(eq(documentDefinitionName));
        verify(documentDefinitionService, times(0)).removeDocumentDefinition(eq(documentDefinitionName));
        verify(applicationEventPublisher, times(0)).publishEvent(eq(undeployDocumentDefinitionEvent));

        assertThat(result).isInstanceOf(UndeployDocumentDefinitionResultFailed.class);
    }
}