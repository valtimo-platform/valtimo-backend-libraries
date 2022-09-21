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

package com.ritense.processdocument.service.impl;

import com.ritense.processdocument.domain.impl.DocumentDefinitionProcess;
import com.ritense.processdocument.domain.impl.DocumentDefinitionProcessLink;
import com.ritense.processdocument.domain.impl.DocumentDefinitionProcessLinkId;
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessLinkResponse;
import com.ritense.processdocument.domain.impl.request.DocumentDefinitionProcessRequest;
import com.ritense.processdocument.repository.DocumentDefinitionProcessLinkRepository;
import com.ritense.processdocument.service.DocumentDefinitionProcessLinkService;
import org.camunda.bpm.engine.RepositoryService;

public class DocumentDefinitionProcessLinkServiceImpl implements DocumentDefinitionProcessLinkService {

    private final DocumentDefinitionProcessLinkRepository documentDefinitionProcessLinkRepository;
    private final RepositoryService repositoryService;

    public DocumentDefinitionProcessLinkServiceImpl(DocumentDefinitionProcessLinkRepository repo,
                                                    RepositoryService repositoryService) {
        this.documentDefinitionProcessLinkRepository = repo;
        this.repositoryService = repositoryService;
    }

    @Override
    public DocumentDefinitionProcess getDocumentDefinitionProcess(String documentDefinitionName) {
        var link= documentDefinitionProcessLinkRepository.findByIdDocumentDefinitionName(documentDefinitionName);

        if (link.isPresent()) {
            var processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(link.get().getId().processDefinitionKey())
                .latestVersion()
                .singleResult();

            return new DocumentDefinitionProcess(processDefinition.getKey(), processDefinition.getName());
        }

        return null;
    }

    @Override
    public DocumentDefinitionProcessLinkResponse saveDocumentDefinitionProcess(
        String documentDefinitionName,
        DocumentDefinitionProcessRequest request) {

        var currentLink = getDocumentDefinitionProcess(documentDefinitionName);

        if (currentLink != null) {
            // If there is already a link set for this document definition then delete the current link
            // before storing the new one
            deleteDocumentDefinitionProcess(documentDefinitionName);
        }

        var processDefinition = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(request.getProcessDefinitionKey())
            .latestVersion()
            .singleResult();

        var link = new DocumentDefinitionProcessLink(
            DocumentDefinitionProcessLinkId.newId(
                documentDefinitionName,
                request.getProcessDefinitionKey()
            )
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
