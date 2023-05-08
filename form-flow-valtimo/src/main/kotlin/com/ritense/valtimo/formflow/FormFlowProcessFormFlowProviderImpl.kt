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

package com.ritense.valtimo.formflow

import com.ritense.formflow.service.FormFlowService
import com.ritense.formlink.domain.FormAssociation
import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormFlowIdLink
import com.ritense.formlink.repository.ProcessFormAssociationRepository
import com.ritense.formlink.service.CreateFormFlowRequest
import com.ritense.formlink.service.CreateFormFlowResponse
import com.ritense.formlink.service.FormLinkNewProcessFormFlowProvider

@Deprecated("Will be removed with form associations in version 12.0. Use process links instead")
class FormLinkNewProcessFormFlowProviderImpl(
    private val formFlowService: FormFlowService,
    private val processFormAssociationRepository: ProcessFormAssociationRepository
): FormLinkNewProcessFormFlowProvider {
    override fun createFormFlow(processDefinitionKey: String, request: CreateFormFlowRequest): CreateFormFlowResponse {
        val additionalProperties = getAdditionalProperties(processDefinitionKey, request)
        val formFlowId = getFormAssociation(processDefinitionKey).formLink.formFlowId
        val formFlowDefinition = formFlowService.findDefinition(formFlowId)!!
        val formFlowInstance = formFlowService.save(formFlowDefinition.createInstance(additionalProperties))
        return CreateFormFlowResponse(formFlowInstance.id.id)
    }

    private fun getAdditionalProperties(processDefinitionKey: String, request: CreateFormFlowRequest): Map<String, Any> {
        val additionalProperties = mutableMapOf(
            "processDefinitionKey" to processDefinitionKey,
        )

        request.documentId?.let {
            additionalProperties["documentId"] = it.toString()
        }
        request.documentDefinitionName?.let {
            additionalProperties["documentDefinitionName"] = it
        }

        return additionalProperties
    }

    private fun getFormAssociation(processDefinitionKey: String): FormAssociation {
        val formAssociation = processFormAssociationRepository.findStartEventAssociation(processDefinitionKey)
            ?: throw IllegalStateException("No form association found. Process: '${processDefinitionKey}'")


        if (formAssociation.formLink !is BpmnElementFormFlowIdLink) {
            throw IllegalStateException("Found form association is not of type 'BpmnElementFormFlowIdLink'. Type: ${formAssociation.formLink.javaClass.simpleName}, process: '${processDefinitionKey}'")
        }

        return formAssociation
    }
}