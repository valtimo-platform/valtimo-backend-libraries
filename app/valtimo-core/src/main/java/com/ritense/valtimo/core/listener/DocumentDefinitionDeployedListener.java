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

package com.ritense.valtimo.core.listener;

import com.ritense.document.domain.event.DocumentDefinitionDeployedEvent;
import com.ritense.processdocument.domain.impl.request.ProcessDocumentDefinitionRequest;
import com.ritense.processdocument.service.ProcessDocumentAssociationService;
import com.ritense.valtimo.domain.contexts.ContextProcess;
import com.ritense.valtimo.service.ContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentDefinitionDeployedListener {

    private final ProcessDocumentAssociationService processDocumentAssociationService;
    private final ContextService contextService;

    @EventListener(DocumentDefinitionDeployedEvent.class)
    public void handle(DocumentDefinitionDeployedEvent documentDefinitionDeployedEvent) {
        if (documentDefinitionDeployedEvent.documentDefinition().id().name().equals("leningen")) {
            var request = new ProcessDocumentDefinitionRequest(
                "lening-aanvragen",
                "leningen",
                true
            );
            processDocumentAssociationService.createProcessDocumentDefinition(request);
            contextService.findAll(Pageable.unpaged()).stream().forEach(context -> {
                final var loanProcessDemo = new ContextProcess("lening-aanvragen", true);
                context.addProcess(loanProcessDemo);
                final var bigExample = new ContextProcess("big-example", true);
                context.addProcess(bigExample);
                final var processVars = new ContextProcess("process-vars", true);
                context.addProcess(processVars);
                contextService.save(context);
            });
        }
        // Portal person
        if (documentDefinitionDeployedEvent.documentDefinition().id().name().equals("portal-person")) {
            var requestPerson = new ProcessDocumentDefinitionRequest(
                "portal-person",
                "portal-person",
                true
            );
            processDocumentAssociationService.createProcessDocumentDefinition(requestPerson);
        }

    }

}