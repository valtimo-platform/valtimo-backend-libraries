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

package com.ritense.processdocument.service.impl;

import static com.ritense.document.service.JsonSchemaDocumentDefinitionActionProvider.VIEW;

import com.ritense.authorization.AuthorizationContext;
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService;
import com.ritense.processdocument.domain.impl.DocumentDefinitionProcess;
import com.ritense.processdocument.domain.impl.DocumentDefinitionProcessLink;
import com.ritense.processdocument.domain.impl.DocumentDefinitionProcessLinkId;
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessLinkResponse;
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessRequest;
import com.ritense.processdocument.repository.DocumentDefinitionProcessLinkRepository;
import com.ritense.processdocument.service.DocumentDefinitionProcessLinkService;
import com.ritense.valtimo.camunda.service.CamundaRepositoryService;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DocumentDefinitionProcessLinkServiceImpl implements DocumentDefinitionProcessLinkService {

    private final DocumentDefinitionProcessLinkRepository documentDefinitionProcessLinkRepository;
    private final CamundaRepositoryService repositoryService;

    private final JsonSchemaDocumentDefinitionService documentDefinitionService;

    public DocumentDefinitionProcessLinkServiceImpl(
        DocumentDefinitionProcessLinkRepository repo,
        CamundaRepositoryService repositoryService,
        JsonSchemaDocumentDefinitionService documentDefinitionService
    ) {
        this.documentDefinitionProcessLinkRepository = repo;
        this.repositoryService = repositoryService;
        this.documentDefinitionService = documentDefinitionService;
    }

    @Override
    public DocumentDefinitionProcess getDocumentDefinitionProcess(String documentDefinitionName) {
        documentDefinitionService.requirePermission(documentDefinitionName, VIEW);
        var link = documentDefinitionProcessLinkRepository.findByIdDocumentDefinitionName(documentDefinitionName);

        if (link.isPresent()) {
            var processDefinition = AuthorizationContext
                .runWithoutAuthorization(
                    () -> repositoryService.findLatestProcessDefinition(link.get().getId().getProcessDefinitionKey())
                );

            return new DocumentDefinitionProcess(processDefinition.getKey(), processDefinition.getName());
        }

        return null;
    }

    @Override
    public List<DocumentDefinitionProcess> getDocumentDefinitionProcessList(String documentDefinitionName) {
        documentDefinitionService.requirePermission(documentDefinitionName, VIEW);
        var links = documentDefinitionProcessLinkRepository.findAllByIdDocumentDefinitionName(documentDefinitionName);

        return links.stream().map(link -> {
            var processDefinition = AuthorizationContext
                .runWithoutAuthorization(
                    () -> repositoryService.findLatestProcessDefinition(link.getId().getProcessDefinitionKey())
                );

            return new DocumentDefinitionProcess(processDefinition.getKey(), processDefinition.getName());
        }).toList();
    }

    @Override
    public Optional<DocumentDefinitionProcessLink> getDocumentDefinitionProcessLink(String documentDefinitionName, String type) {
        documentDefinitionService.requirePermission(documentDefinitionName, VIEW);
        return documentDefinitionProcessLinkRepository.findByIdDocumentDefinitionNameAndType(documentDefinitionName, type);
    }

    @Override
    public DocumentDefinitionProcessLinkResponse saveDocumentDefinitionProcess(
        String documentDefinitionName,
        DocumentDefinitionProcessRequest request
    ) {

        var processDefinition = AuthorizationContext
            .runWithoutAuthorization(
                () -> repositoryService.findLatestProcessDefinition(request.getProcessDefinitionKey())
            );

        if (processDefinition == null) {
            throw new IllegalArgumentException("Unknown process definition with key: " + request.getProcessDefinitionKey());
        }

        var currentLink = documentDefinitionProcessLinkRepository.findByIdDocumentDefinitionNameAndType(documentDefinitionName, request.getLinkType());
        if (currentLink.isPresent()) {
            // If there is already a link set for this document definition then delete the current link
            // before storing the new one
            documentDefinitionProcessLinkRepository.deleteByIdDocumentDefinitionNameAndType(documentDefinitionName, request.getLinkType());
        }

        currentLink = getDocumentDefinitionProcessLink(documentDefinitionName, request.getProcessDefinitionKey());
        if (currentLink.isPresent()) {
            // If there is already a link set for this document definition then delete the current link
            // before storing the new one
            documentDefinitionProcessLinkRepository.deleteByIdDocumentDefinitionNameAndIdProcessDefinitionKey(
                documentDefinitionName,
                request.getProcessDefinitionKey()
            );
        }

        var link = new DocumentDefinitionProcessLink(
            DocumentDefinitionProcessLinkId.newId(
                documentDefinitionName,
                request.getProcessDefinitionKey()
            ),
            request.getLinkType()
        );

        documentDefinitionProcessLinkRepository.save(link);

        return new DocumentDefinitionProcessLinkResponse(
            processDefinition.getKey(),
            processDefinition.getName()
        );
    }

    @Override
    public void deleteDocumentDefinitionProcess(String documentDefinitionName) {
        documentDefinitionProcessLinkRepository.deleteByIdDocumentDefinitionName(documentDefinitionName);
    }
}
