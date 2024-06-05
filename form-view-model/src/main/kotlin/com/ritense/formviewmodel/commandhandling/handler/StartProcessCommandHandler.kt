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

package com.ritense.formviewmodel.commandhandling.handler

import com.ritense.commandhandling.CommandHandler
import com.ritense.formviewmodel.commandhandling.StartProcessCommand
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentAssociationService
import com.ritense.valtimo.service.CamundaProcessService

class StartProcessCommandHandler(
    private val camundaProcessService: CamundaProcessService,
    private val processDocumentAssociationService: ProcessDocumentAssociationService
) : CommandHandler<StartProcessCommand, Unit> {

    override fun execute(command: StartProcessCommand) {
        // Start process instance
        val processInstanceWithDefinition = camundaProcessService.startProcess(
            command.processDefinitionKey,
            command.businessKey,
            command.processVariables
        )
        // Link case instance to process instance
        val camundaProcessInstanceId = CamundaProcessInstanceId(
            processInstanceWithDefinition.processInstanceDto.id
        )
        processDocumentAssociationService.createProcessDocumentInstance(
            camundaProcessInstanceId.toString(),
            command.caseInstanceId,
            processInstanceWithDefinition.processDefinition.name
        )
    }

}