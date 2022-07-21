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
package com.ritense.smartdocuments.web.rest

import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.domain.PluginProcessLink
import com.ritense.plugin.domain.PluginProcessLinkId
import com.ritense.plugin.service.PluginService
import com.ritense.smartdocuments.domain.DocumentFormatOption
import com.ritense.valtimo.contract.json.Mapper
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.variable.value.TypedValue
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(value = ["/api/smart-documents/demo"])
class SmartDocumentsDemoResource(
    private val pluginService: PluginService,
    private val runtimeService: RuntimeService,
) {

    @PostMapping(value = ["/generate"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun generateDocument(
        @RequestParam processInstanceId: String,
        @RequestParam pluginConfigurationId: UUID,
        @RequestParam templateGroup: String,
        @RequestParam templateName: String,
        @RequestParam format: String,
        @RequestParam resultingDocumentLocation: String,
        @RequestParam templatePlaceholders: Map<String, String>,
    ): ResponseEntity<Void> {
        val variables = runtimeService.getVariables(processInstanceId)
        val delegateExecutionSmall = DelegateExecutionSmall(processInstanceId, variables)
        val objectMapper = Mapper.INSTANCE.get()
        val properties = objectMapper.readTree(objectMapper.writeValueAsString(
            SmartDocumentsPluginGenerateDocumentProperties(
                templateGroup,
                templateName,
                DocumentFormatOption.valueOf(format),
                resultingDocumentLocation,
                templatePlaceholders
            )
        ))
        val processLink = PluginProcessLink(
            id = PluginProcessLinkId.newId(),
            processDefinitionId = "generateProcess",
            activityId = "generate",
            actionProperties = properties,
            pluginConfigurationId = PluginConfigurationId.existingId(pluginConfigurationId),
            pluginActionDefinitionKey = "generate-document"
        )
        pluginService.invoke(delegateExecutionSmall, processLink)

        return ResponseEntity.noContent().build()
    }
}

data class SmartDocumentsPluginGenerateDocumentProperties(
    val templateGroup: String,
    val templateName: String,
    val format: DocumentFormatOption,
    val resultingDocumentLocation: String,
    val templatePlaceholders: Map<String, String>
)

data class DelegateExecutionSmall(
    private val processInstanceId: String,
    private val variables: MutableMap<String, Any>,
) : DelegateExecution {

    override fun getProcessInstanceId() = processInstanceId
    override fun getVariables() = variables

    override fun getVariableScopeKey() = throw NotImplementedError()
    override fun getVariablesTyped() = throw NotImplementedError()
    override fun getVariablesTyped(deserializeValues: Boolean) = throw NotImplementedError()
    override fun getVariablesLocal() = throw NotImplementedError()
    override fun getVariablesLocalTyped() = throw NotImplementedError()
    override fun getVariablesLocalTyped(deserializeValues: Boolean) = throw NotImplementedError()
    override fun getVariable(variableName: String?) = throw NotImplementedError()
    override fun getVariableLocal(variableName: String?) = throw NotImplementedError()
    override fun <T : TypedValue?> getVariableTyped(variableName: String?) = throw NotImplementedError()
    override fun <T : TypedValue?> getVariableTyped(variableName: String?, deserializeValue: Boolean) =
        throw NotImplementedError()

    override fun <T : TypedValue?> getVariableLocalTyped(variableName: String?) = throw NotImplementedError()
    override fun <T : TypedValue?> getVariableLocalTyped(variableName: String?, deserializeValue: Boolean) =
        throw NotImplementedError()

    override fun getVariableNames() = throw NotImplementedError()
    override fun getVariableNamesLocal() = throw NotImplementedError()
    override fun setVariable(variableName: String?, value: Any?, activityId: String?) = throw NotImplementedError()
    override fun setVariable(variableName: String?, value: Any?) = throw NotImplementedError()
    override fun setVariableLocal(variableName: String?, value: Any?) = throw NotImplementedError()
    override fun setVariables(variables: MutableMap<String, out Any>?) = throw NotImplementedError()
    override fun setVariablesLocal(variables: MutableMap<String, out Any>?) = throw NotImplementedError()
    override fun hasVariables() = throw NotImplementedError()
    override fun hasVariablesLocal() = throw NotImplementedError()
    override fun hasVariable(variableName: String?) = throw NotImplementedError()
    override fun hasVariableLocal(variableName: String?) = throw NotImplementedError()
    override fun removeVariable(variableName: String?) = throw NotImplementedError()
    override fun removeVariableLocal(variableName: String?) = throw NotImplementedError()
    override fun removeVariables(variableNames: MutableCollection<String>?) = throw NotImplementedError()
    override fun removeVariables() = throw NotImplementedError()
    override fun removeVariablesLocal(variableNames: MutableCollection<String>?) = throw NotImplementedError()
    override fun removeVariablesLocal() = throw NotImplementedError()
    override fun getId() = throw NotImplementedError()
    override fun getEventName() = throw NotImplementedError()
    override fun getBusinessKey() = throw NotImplementedError()
    override fun getBpmnModelInstance() = throw NotImplementedError()
    override fun getBpmnModelElementInstance() = throw NotImplementedError()
    override fun getProcessEngineServices() = throw NotImplementedError()
    override fun getProcessEngine() = throw NotImplementedError()
    override fun getProcessBusinessKey() = throw NotImplementedError()
    override fun setProcessBusinessKey(businessKey: String?) = throw NotImplementedError()
    override fun getProcessDefinitionId() = throw NotImplementedError()
    override fun getParentId() = throw NotImplementedError()
    override fun getCurrentActivityId() = throw NotImplementedError()
    override fun getCurrentActivityName() = throw NotImplementedError()
    override fun getActivityInstanceId() = throw NotImplementedError()
    override fun getParentActivityInstanceId() = throw NotImplementedError()
    override fun getCurrentTransitionId() = throw NotImplementedError()
    override fun getProcessInstance() = throw NotImplementedError()
    override fun getSuperExecution() = throw NotImplementedError()
    override fun isCanceled() = throw NotImplementedError()
    override fun getTenantId() = throw NotImplementedError()
    override fun createIncident(incidentType: String?, configuration: String?) = throw NotImplementedError()
    override fun createIncident(incidentType: String?, configuration: String?, message: String?) =
        throw NotImplementedError()

    override fun resolveIncident(incidentId: String?) = throw NotImplementedError()
}
