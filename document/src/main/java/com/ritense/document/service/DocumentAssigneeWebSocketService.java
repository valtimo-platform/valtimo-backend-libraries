/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.document.service;

import com.ritense.document.domain.impl.assignee.UnassignedDocumentCountDto;
import com.ritense.document.event.DocumentAssigneeChangedEvent;
import com.ritense.document.repository.DocumentRepository;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

public class DocumentAssigneeWebSocketService {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final DocumentDefinitionService documentDefinitionService;
    private final DocumentRepository documentRepository;

    public DocumentAssigneeWebSocketService(
        SimpMessagingTemplate simpMessagingTemplate,
        DocumentDefinitionService documentDefinitionService,
        DocumentRepository documentRepository
    ) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.documentDefinitionService = documentDefinitionService;
        this.documentRepository = documentRepository;
    }

    @EventListener(DocumentAssigneeChangedEvent.class)
    public void handleDocumentAssigneeChangedEvent() {
        sendUnassignedDocumentCountList();
    }

    @EventListener(SessionSubscribeEvent.class)
    public void handleSessionSubscribeEvent(SessionSubscribeEvent sessionSubscribeEvent) {
        sendUnassignedDocumentCountList();
    }

    private void sendUnassignedDocumentCountList() {
        var unassignedDocumentCountList = documentDefinitionService.findForUser(true, Pageable.unpaged())
            .map(documentDefinition -> getUnassignedDocumentCountDto(documentDefinition.id().name()))
            .toList();

        simpMessagingTemplate.convertAndSend(unassignedDocumentCountList);
    }

    private UnassignedDocumentCountDto getUnassignedDocumentCountDto(String documentDefinitionName) {
        return new UnassignedDocumentCountDto(
            documentDefinitionName,
            documentRepository.countByDocumentDefinitionIdNameAndAssigneeId(documentDefinitionName, null)
        );
    }

}
