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
import com.fasterxml.jackson.databind.node.ValueNode
import com.ritense.objectsapi.opennotificaties.OpenNotificatieConnector
import com.ritense.objectsapi.opennotificaties.OpenNotificatieService
import com.ritense.objectsapi.opennotificaties.OpenNotificationEvent
import com.ritense.objectsapi.taak.resolve.ValueResolverService
import com.ritense.processdocument.domain.impl.CamundaProcessInstanceId
import com.ritense.valtimo.contract.json.Mapper
import com.ritense.valtimo.service.BpmnModelService
import com.ritense.valtimo.service.CamundaTaskService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.VariableScope
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties
import org.springframework.context.event.EventListener
import java.net.URI

class TaakObjectListener(
    private val openNotificatieService: OpenNotificatieService,
    private val camundaTaskService: CamundaTaskService,
    private val valueResolverService: ValueResolverService,
    private val bpmnModelService: BpmnModelService,
    private val runtimeService: RuntimeService,
) {

    @EventListener(OpenNotificationEvent::class)
    fun notificationReceived(event: OpenNotificationEvent) {
        if (event.notification.kanaal == OpenNotificatieConnector.OBJECTEN_KANAAL_NAME
            && event.notification.isEditNotification()
        ) {
            val connector = openNotificatieService.findConnector(event.connectorId, event.authorizationKey)

            // check if the created object is the right kind based on the name of the type of the created object.
            // This is the only way to do so until other information becomes available or we retrieve every object that is created
            if (connector is TaakObjectConnector
                && event.notification.getObjectTypeName()?.equals(connector.getObjectsApiConnector().getProperties().objectType.title)?: false
            ) {
                val taakObjectId = event.notification.getObjectId()
                val taakObject = connector.getTaakObject(taakObjectId)
                if (taakObject.status != TaakObjectStatus.ingediend) {
                    return
                }
                saveDataAndCompleteTask(taakObject)

                connector.deleteTaakObject(taakObjectId)
            }
        }
    }

    private fun saveDataAndCompleteTask(taakObject: TaakObjectDto) {
        val task = camundaTaskService.findTaskById(taakObject.verwerkerTaakId.toString())
        if (taakObject.data != null && taakObject.data.isNotEmpty()) {
            val taakObjectData = Mapper.INSTANCE.get().valueToTree<JsonNode>(taakObject.data)
            loadTaakObjectDocuments(taakObjectData)
            handleTaakObjectData(taakObjectData, task)
        }
        camundaTaskService.completeTaskWithoutFormData(taakObject.verwerkerTaakId.toString())
    }

    private fun loadTaakObjectDocuments(taakObjectData: JsonNode) {
        val filesNode = taakObjectData.at(JsonPointer.valueOf("/documenten"))
        if (filesNode.isArray) {
            openNotificatieService.createOpenzaakResources(filesNode.map { URI(it.textValue()) })
        }
    }

    private fun handleTaakObjectData(taakObjectData: JsonNode, task: Task) {
        val resolvedValues = getResolvedValues(task, taakObjectData)
        if (resolvedValues.isNotEmpty()) {
            valueResolverService.handleValues(
                CamundaProcessInstanceId(task.processInstanceId),
                getVariableScope(task),
                resolvedValues
            )
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
            .filter { it.camundaName.startsWith(prefix = "taskResult:", ignoreCase = true) }
            .associateBy(
                { it.camundaName.substringAfter(delimiter = ":") },
                { getValue(data, it.camundaValue) }
            )
    }

    private fun getValue(data: JsonNode, path: String): Any {
        val valueNode = data.at(JsonPointer.valueOf(path)) as ValueNode
        return Mapper.INSTANCE.get().treeToValue(valueNode, Object::class.java)
    }

    private fun getVariableScope(task: Task): VariableScope {
        return runtimeService.createProcessInstanceQuery()
            .processInstanceId(task.processInstanceId)
            .singleResult() as VariableScope
    }
}