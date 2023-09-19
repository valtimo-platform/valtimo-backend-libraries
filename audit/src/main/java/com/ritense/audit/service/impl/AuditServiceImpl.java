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

package com.ritense.audit.service.impl;

import com.ritense.audit.domain.AuditRecord;
import com.ritense.audit.domain.AuditRecordId;
import com.ritense.audit.exception.AuditRecordNotFoundException;
import com.ritense.audit.repository.AuditRecordRepository;
import com.ritense.audit.service.AuditService;
import com.ritense.authorization.Action;
import com.ritense.authorization.AuthorizationContext;
import com.ritense.authorization.AuthorizationService;
import com.ritense.authorization.request.EntityAuthorizationRequest;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.snapshot.JsonSchemaDocumentSnapshot;
import com.ritense.document.service.DocumentService;
import com.ritense.document.service.JsonSchemaDocumentActionProvider;
import com.ritense.valtimo.contract.audit.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Transactional
public class AuditServiceImpl implements AuditService {

    private final AuditRecordRepository<AuditRecord> auditRecordRepository;
    private final AuthorizationService authorizationService;
    private final DocumentService documentService;

    public AuditServiceImpl(
        AuditRecordRepository<AuditRecord> auditRecordRepository,
        AuthorizationService authorizationService,
        DocumentService documentService
    ) {
        this.auditRecordRepository = auditRecordRepository;
        this.authorizationService = authorizationService;
        this.documentService = documentService;
    }

    @Override
    public AuditRecord findById(AuditRecordId auditRecordId) {
        denyAuthorization();
        return auditRecordRepository
            .findById(auditRecordId)
            .orElseThrow(() -> new AuditRecordNotFoundException("AuditRecord not found for " + auditRecordId));
    }

    @Override
    public Page<AuditRecord> findByEventAndDocumentId(List<Class<? extends AuditEvent>> eventTypes, UUID documentId, Pageable pageable) {
        var document = AuthorizationContext.runWithoutAuthorization(() -> documentService.get(documentId.toString()));

        authorizationService.requirePermission(
            new EntityAuthorizationRequest(
                JsonSchemaDocument.class,
                JsonSchemaDocumentActionProvider.VIEW,
                document
            )
        );

        return auditRecordRepository.findByEventAndDocumentId(eventTypes, documentId, pageable);
    }

    @Override
    public List<AuditRecord> findByEventAndOccurredBetween(Class<? extends AuditEvent> eventType, LocalDateTime from, LocalDateTime until, Pageable pageable) {
        denyAuthorization();
        return auditRecordRepository.findByEventAndOccurredBetween(eventType.getName(), from, until, pageable);
    }

    @Override
    public Page<AuditRecord> findByProperty(String key, Object value, Pageable pageable) {
        denyAuthorization();
        return auditRecordRepository.findAuditRecordsByProperty(key, value, pageable);
    }

    @Override
    public List<AuditRecord> findByEventTypeAndProperty(Class<? extends AuditEvent> eventType, String key, Object value) {
        denyAuthorization();
        return auditRecordRepository.findAuditRecordsByEventAndProperty(eventType.getName(), key, value);
    }

    @Override
    public void deleteAllBefore(LocalDateTime date) {
        auditRecordRepository.deleteAllBefore(date);
    }

    private void denyAuthorization() {
        authorizationService.requirePermission(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocumentSnapshot.class,
                Action.deny()
            )
        );
    }

}