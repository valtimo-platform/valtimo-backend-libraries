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

package com.ritense.document.service.impl;

import com.ritense.document.domain.DocumentDefinition;
import com.ritense.document.domain.impl.JsonSchemaDocumentDefinitionId;
import com.ritense.document.domain.impl.sequence.JsonSchemaDocumentDefinitionSequenceRecord;
import com.ritense.document.repository.DocumentDefinitionSequenceRepository;
import com.ritense.document.service.DocumentSequenceGeneratorService;
import org.hibernate.exception.LockAcquisitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class JsonSchemaDocumentDefinitionSequenceGeneratorService implements DocumentSequenceGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaDocumentDefinitionSequenceGeneratorService.class);
    private final DocumentDefinitionSequenceRepository<JsonSchemaDocumentDefinitionSequenceRecord> documentDefinitionSequenceRepository;

    public JsonSchemaDocumentDefinitionSequenceGeneratorService(DocumentDefinitionSequenceRepository<JsonSchemaDocumentDefinitionSequenceRecord> documentDefinitionSequenceRepository) {
        this.documentDefinitionSequenceRepository = documentDefinitionSequenceRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    @Retryable(
        value = {LockAcquisitionException.class, CannotAcquireLockException.class},
        maxAttempts = 5,
        backoff = @Backoff(delay = 500, maxDelay = 5000)
    )
    public long next(DocumentDefinition.Id documentDefinitionId) {
        final var optionalSequence = documentDefinitionSequenceRepository
            .findByDefinitionName(documentDefinitionId.name());

        JsonSchemaDocumentDefinitionSequenceRecord sequenceRecord;
        if (optionalSequence.isPresent()) {
            sequenceRecord = optionalSequence.get();
            sequenceRecord.increment();
            logger.debug("Updating sequence record for - {} - next {}", documentDefinitionId, sequenceRecord.sequence());
        } else {
            sequenceRecord = new JsonSchemaDocumentDefinitionSequenceRecord(
                JsonSchemaDocumentDefinitionId.existingId(documentDefinitionId)
            );
            logger.debug(
                "Creating new sequence record for - {} - initial sequence {}",
                documentDefinitionId,
                sequenceRecord.sequence()
            );
        }
        documentDefinitionSequenceRepository.saveAndFlush(sequenceRecord);
        return sequenceRecord.sequence();
    }

    @Transactional
    @Override
    public void deleteSequenceRecordBy(String documentDefinitionName) {
        documentDefinitionSequenceRepository.deleteByDocumentDefinitionName(documentDefinitionName);
    }

}