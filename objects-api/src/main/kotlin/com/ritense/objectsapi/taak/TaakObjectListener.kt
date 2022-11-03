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

package com.ritense.objectsapi.taak

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.ritense.document.domain.Document
import com.ritense.document.domain.impl.JsonSchemaRelatedFile
import com.ritense.document.service.DocumentService
import com.ritense.objectsapi.opennotificaties.OpenNotificatieConnector
import com.ritense.objectsapi.opennotificaties.OpenNotificatieService
import com.ritense.objectsapi.opennotificaties.OpenNotificationEvent
import com.ritense.openzaak.service.ZaakService
import com.ritense.processdocument.domain.ProcessInstanceId
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.resource.service.OpenZaakService
import com.ritense.valtimo.contract.json.Mapper
import com.ritense.valtimo.service.BpmnModelService
import com.ritense.valtimo.service.CamundaTaskService
import com.ritense.valueresolver.ValueResolverService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.VariableScope
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties
import org.springframework.context.event.EventListener
import java.net.MalformedURLException
import java.net.URI

class TaakObjectListener(
    private val openNotificatieService: OpenNotificatieService,
    private val camundaTaskService: CamundaTaskService,
    private val valueResolverService: ValueResolverService,
    private val bpmnModelService: BpmnModelService,
    private val runtimeService: RuntimeService,
    private val documentService: DocumentService,
    private val processDocumentService: ProcessDocumentService,
    private val zaakService: ZaakService,
    private val openZaakService: OpenZaakService,
) {

    @EventListener(OpenNotificationEvent::class)
    fun notificationReceived(event: OpenNotificationEvent) {
        if (event.notification.kanaal == OpenNotificatieConnector.OBJECTEN_KANAAL_NAME
            && event.notification.isUpdateNotification()
        ) {
            val connector = openNotificatieService.findConnector(event.connectorId, event.authorizationKey)

            if (connector is TaakObjectConnector && connector.getObjectsApiConnector().getProperties()
                    .objectType.url == event.notification.getObjectTypeUrl()
            ) {
                val taakObjectId = event.notification.getObjectId()
                val taakObjectRecord = connector.getTaakObjectRecord(taakObjectId)
                val taakObject = taakObjectRecord.record.data
                if (taakObject.status != TaakObjectStatus.ingediend) {
                    return
                }
                saveDataAndCompleteTask(taakObject)

                connector.modifyTaakObjectStatusVerwerkt(taakObjectRecord)
            }
        }
    }

    private fun saveDataAndCompleteTask(taakObject: TaakObjectDto) {
        val task = camundaTaskService.findTaskById(taakObject.verwerkerTaakId.toString())
        if (taakObject.verzondenData != null && taakObject.verzondenData.isNotEmpty()) {
            val processInstanceId = CamundaProcessInstanceId(task.processInstanceId)
            val variableScope = getVariableScope(task)
            val taakObjectData = Mapper.INSTANCE.get().valueToTree<JsonNode>(taakObject.verzondenData)
            val resolvedValues = getResolvedValues(task, taakObjectData)
            loadTaakObjectDocuments(processInstanceId, variableScope, taakObjectData)
            handleTaakObjectData(processInstanceId, variableScope, resolvedValues)
        }
        camundaTaskService.completeTaskWithoutFormData(taakObject.verwerkerTaakId.toString())
    }

    private fun loadTaakObjectDocuments(
        processInstanceId: ProcessInstanceId,
        variableScope: VariableScope,
        taakObjectData: JsonNode
    ) {
        val documentId = processDocumentService.getDocumentId(processInstanceId, variableScope)
        getDocumentenUris(taakObjectData).forEach { createResourceAndAssignToDocument(it, documentId) }
    }

    private fun getDocumentenUris(taakObjectData: JsonNode): List<URI> {
        val documentPathsNode = taakObjectData.at(JsonPointer.valueOf("/documenten"))
        if (documentPathsNode.isMissingNode || documentPathsNode.isNull) {
            return emptyList()
        }
        if (!documentPathsNode.isArray) {
            throw RuntimeException("Not an array: '/verzonden_data/documenten'")
        }
        val documentenUris = mutableListOf<URI>()
        for (documentPathNode in documentPathsNode) {
            val documentUrlNode = taakObjectData.at(JsonPointer.valueOf(documentPathNode.textValue()))
            if (!documentUrlNode.isMissingNode && !documentUrlNode.isNull) {
                try {
                    if (documentUrlNode.isTextual) {
                        documentenUris.add(URI(documentUrlNode.textValue()))
                    } else if (documentUrlNode.isArray) {
                        documentUrlNode.forEach { documentenUris.add(URI(it.textValue())) }
                    } else {
                        throw RuntimeException("Invalid URL in '/verzonden_data/documenten'. ${documentUrlNode.toPrettyString()}")
                    }
                } catch (e: MalformedURLException) {
                    throw RuntimeException("Malformed URL in: '/verzonden_data/documenten'", e)
                }
            }
        }
        return documentenUris
    }

    private fun createResourceAndAssignToDocument(file: URI, documentId: Document.Id) {
        val informatieObject = zaakService.getInformatieObject(file)
        val resource = openZaakService.store(informatieObject)
        val relatedFile = JsonSchemaRelatedFile.from(resource).withCreatedBy(informatieObject.auteur)
        documentService.assignRelatedFile(documentId, relatedFile)
    }

    private fun handleTaakObjectData(
        processInstanceId: ProcessInstanceId,
        variableScope: VariableScope,
        resolvedValues: Map<String, Any>
    ) {
        if (resolvedValues.isNotEmpty()) {
            valueResolverService.handleValues(processInstanceId.toString(), variableScope, resolvedValues)
        }
    }

    /**
     * @param task with extensions-property: [ taskResult:doc:add:/streetName  to  "/persoonsData/adres/straatnaam" ]
     * @param data {"persoonsData":{"adres":{"straatnaam":"Funenpark"}}}
     * @return mapOf(doc:add:/streetName to "Funenpark")
     */
    private fun getResolvedValues(task: Task, data: JsonNode): Map<String, Any> {
        return bpmnModelService.getTask(task).extensionElements.elements
            .filterIsInstance<CamundaProperties>()
            .single()
            .camundaProperties
            .filter { it.camundaName != null && it.camundaValue != null }
            .filter { it.camundaName.startsWith(prefix = "taskResult:", ignoreCase = true) }
            .associateBy(
                { it.camundaName.substringAfter(delimiter = ":") },
                { getValue(data, it.camundaValue, it.camundaName, task) }
            )
    }

    private fun getValue(data: JsonNode, path: String, camundaName: String, task: Task): Any {
        val valueNode = data.at(JsonPointer.valueOf(path))
        if (valueNode.isMissingNode) {
            throw RuntimeException("Failed to do '$camundaName' for task '${task.taskDefinitionKey}'. Missing data on path '$path'")
        }
        return Mapper.INSTANCE.get().treeToValue(valueNode, Object::class.java)
    }

    private fun getVariableScope(task: Task): VariableScope {
        return runtimeService.createProcessInstanceQuery()
            .processInstanceId(task.processInstanceId)
            .singleResult() as VariableScope
    }
}
