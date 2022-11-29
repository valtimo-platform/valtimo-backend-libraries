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
import com.ritense.document.repository.DocumentRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class DocumentStatisticService {

    private final DocumentDefinitionService documentDefinitionService;
    private final DocumentRepository documentRepository;

    public DocumentStatisticService(
        DocumentDefinitionService documentDefinitionService,
        DocumentRepository documentRepository
    ) {
        this.documentDefinitionService = documentDefinitionService;
        this.documentRepository = documentRepository;
    }

    public List<UnassignedDocumentCountDto> getUnassignedDocumentCountDtos() {
        return documentDefinitionService.findForUser(true, Pageable.unpaged())
            .map(documentDefinition -> getUnassignedDocumentCountDto(documentDefinition.id().name()))
            .toList();
    }

    private UnassignedDocumentCountDto getUnassignedDocumentCountDto(String documentDefinitionName) {
        long count = documentRepository.countByDocumentDefinitionIdNameAndAssigneeId(documentDefinitionName, null);
        return new UnassignedDocumentCountDto(documentDefinitionName, count);
    }

}
