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

import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.domain.impl.sequence.JsonSchemaDocumentDefinitionSequenceRecord;
import com.ritense.document.repository.impl.JsonSchemaDocumentDefinitionSequenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JsonSchemaDocumentDefinitionSequenceGeneratorServiceTest {

    private JsonSchemaDocumentDefinitionSequenceRepository documentDefinitionSequenceRepository;
    private JsonSchemaDocumentDefinitionSequenceGeneratorService sequenceGeneratorService;

    @BeforeEach
    public void setUp() {
        documentDefinitionSequenceRepository = mock(JsonSchemaDocumentDefinitionSequenceRepository.class);
        sequenceGeneratorService = new JsonSchemaDocumentDefinitionSequenceGeneratorService(documentDefinitionSequenceRepository);
    }

    @Test
    public void shouldGetNewSequenceWithInitialValueOf1() {
        final var id = JsonSchemaDocumentDefinitionId.newId("Some-Name");

        when(documentDefinitionSequenceRepository.findByDefinitionName(id.name())).thenReturn(Optional.empty());

        final long nextSequence = sequenceGeneratorService.next(id);

        assertThat(nextSequence).isEqualTo(1);
    }

    @Test
    public void shouldGetUpdatedSequenceWithValueOf2() {
        final var id = JsonSchemaDocumentDefinitionId.existingId("Some-Name", 1);

        when(documentDefinitionSequenceRepository.findByDefinitionName(id.name()))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(new JsonSchemaDocumentDefinitionSequenceRecord(id)));

        sequenceGeneratorService.next(id);
        final long nextSequence = sequenceGeneratorService.next(id);

        assertThat(nextSequence).isEqualTo(2);
    }

}