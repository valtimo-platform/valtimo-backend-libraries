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

import com.ritense.smartdocuments.domain.DocumentFormatOption
import com.ritense.smartdocuments.plugin.SmartDocumentsPluginFactory
import com.ritense.smartdocuments.plugin.SmartDocumentsPluginGenerateDocumentProperties
import com.ritense.valtimo.contract.json.Mapper
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.bpm.engine.variable.value.TypedValue
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.Date

@RestController
@RequestMapping(value = ["/api/smart-documents/demo"])
class SmartDocumentsDemoResource(
    private val smartDocumentsPluginFactory: SmartDocumentsPluginFactory,
    private val runtimeService: RuntimeService,
) {

    @PostMapping(value = ["/generate"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun generateDocument(
        @RequestParam processInstanceId: String,
        @RequestParam pluginConfigurationKey: String,
        @RequestParam templateGroup: String,
        @RequestParam templateName: String,
        @RequestParam format: String,
        @RequestParam templatePlaceholders: Map<String, String>,
    ): ResponseEntity<Void> {
        val smartDocumentsPlugin = smartDocumentsPluginFactory.create(pluginConfigurationKey)
        val variables = runtimeService.getVariables(processInstanceId)
        val delegateTaskSmall = DelegateTaskSmall(processInstanceId, variables)
        val properties = Mapper.INSTANCE.get().writeValueAsString(
            SmartDocumentsPluginGenerateDocumentProperties(
                templateGroup,
                templateName,
                DocumentFormatOption.valueOf(format),
                templatePlaceholders
            )
        )
        smartDocumentsPlugin.generate(delegateTaskSmall, properties)
        return ResponseEntity.noContent().build()
    }
}

data class DelegateTaskSmall(
    private val processInstanceId: String,
    private val variables: MutableMap<String, Any>
) : DelegateTask {

    override fun getVariables() = variables
    override fun getExecution() = DelegateExecutionSmall(processInstanceId)

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
    override fun getBpmnModelInstance() = throw NotImplementedError()
    override fun getBpmnModelElementInstance() = throw NotImplementedError()
    override fun getProcessEngineServices() = throw NotImplementedError()
    override fun getProcessEngine() = throw NotImplementedError()
    override fun getId() = throw NotImplementedError()
    override fun getName() = throw NotImplementedError()
    override fun setName(name: String?) = throw NotImplementedError()
    override fun getDescription() = throw NotImplementedError()
    override fun setDescription(description: String?) = throw NotImplementedError()
    override fun getPriority() = throw NotImplementedError()
    override fun setPriority(priority: Int) = throw NotImplementedError()
    override fun getProcessInstanceId() = throw NotImplementedError()
    override fun getExecutionId() = throw NotImplementedError()
    override fun getProcessDefinitionId() = throw NotImplementedError()
    override fun getCaseInstanceId() = throw NotImplementedError()
    override fun getCaseExecutionId() = throw NotImplementedError()
    override fun getCaseDefinitionId() = throw NotImplementedError()
    override fun getCreateTime() = throw NotImplementedError()
    override fun getTaskDefinitionKey() = throw NotImplementedError()
    override fun getCaseExecution() = throw NotImplementedError()
    override fun getEventName() = throw NotImplementedError()
    override fun addCandidateUser(userId: String?) = throw NotImplementedError()
    override fun addCandidateUsers(candidateUsers: MutableCollection<String>?) = throw NotImplementedError()
    override fun addCandidateGroup(groupId: String?) = throw NotImplementedError()
    override fun addCandidateGroups(candidateGroups: MutableCollection<String>?) = throw NotImplementedError()
    override fun getOwner() = throw NotImplementedError()
    override fun setOwner(owner: String?) = throw NotImplementedError()
    override fun getAssignee() = throw NotImplementedError()
    override fun setAssignee(assignee: String?) = throw NotImplementedError()
    override fun getDueDate() = throw NotImplementedError()
    override fun setDueDate(dueDate: Date?) = throw NotImplementedError()
    override fun getDeleteReason() = throw NotImplementedError()
    override fun addUserIdentityLink(userId: String?, identityLinkType: String?) = throw NotImplementedError()
    override fun addGroupIdentityLink(groupId: String?, identityLinkType: String?) = throw NotImplementedError()
    override fun deleteCandidateUser(userId: String?) = throw NotImplementedError()
    override fun deleteCandidateGroup(groupId: String?) = throw NotImplementedError()
    override fun deleteUserIdentityLink(userId: String?, identityLinkType: String?) = throw NotImplementedError()
    override fun deleteGroupIdentityLink(groupId: String?, identityLinkType: String?) = throw NotImplementedError()
    override fun getCandidates() = throw NotImplementedError()
    override fun getTenantId() = throw NotImplementedError()
    override fun getFollowUpDate() = throw NotImplementedError()
    override fun setFollowUpDate(followUpDate: Date?) = throw NotImplementedError()
    override fun complete() = throw NotImplementedError()
}

data class DelegateExecutionSmall(
    private val processInstanceId: String,
) : DelegateExecution {

    override fun getProcessInstanceId() = processInstanceId

    override fun getVariableScopeKey() = throw NotImplementedError()
    override fun getVariables() = throw NotImplementedError()
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
    override fun resolveIncident(incidentId: String?) = throw NotImplementedError()}
