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

package com.ritense.document.service;

import com.ritense.authorization.AuthorizationService;
import com.ritense.authorization.request.EntityAuthorizationRequest;
import com.ritense.document.domain.impl.JsonSchemaDocument;
import com.ritense.document.domain.impl.assignee.UnassignedDocumentCountDto;
import com.ritense.document.repository.DocumentRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static com.ritense.document.repository.impl.JsonSchemaDocumentSpecificationHelper.byDocumentDefinitionName;
import static com.ritense.document.repository.impl.JsonSchemaDocumentSpecificationHelper.byUnassigned;
import static com.ritense.document.service.JsonSchemaDocumentActionProvider.VIEW_LIST;

public class DocumentStatisticService {

    private final DocumentDefinitionService documentDefinitionService;
    private final DocumentRepository documentRepository;
    private final AuthorizationService authorizationService;

    public DocumentStatisticService(
        DocumentDefinitionService documentDefinitionService,
        DocumentRepository documentRepository,
        AuthorizationService authorizationService) {
        this.documentDefinitionService = documentDefinitionService;
        this.documentRepository = documentRepository;
        this.authorizationService = authorizationService;
    }

    public List<UnassignedDocumentCountDto> getUnassignedDocumentCountDtos() {
        final var authSpec = authorizationService.getAuthorizationSpecification(
            new EntityAuthorizationRequest<>(
                JsonSchemaDocument.class,
                VIEW_LIST
            ),
            null
        );
        return documentDefinitionService.findAll(Pageable.unpaged())
            .map(documentDefinition -> getUnassignedDocumentCountDto(documentDefinition.id().name(), authSpec))
            .toList();
    }

    private UnassignedDocumentCountDto getUnassignedDocumentCountDto(String documentDefinitionName, Specification authSpec) {
        long count = documentRepository.count(authSpec.and(byDocumentDefinitionName(documentDefinitionName).and(byUnassigned())));
        return new UnassignedDocumentCountDto(documentDefinitionName, count);
    }

}
