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

package com.ritense.valtimo.camunda.repository

class CamundaProcessInstanceSpecificationHelper {

    companion object {

        const val ID: String = "id"
        const val REVISION: String = "revision"
        const val ROOT_PROCESS_INSTANCE: String = "rootProcessInstance"
        const val PROCESS_INSTANCE: String = "processInstance"
        const val BUSINESS_KEY: String = "businessKey"
        const val PARENT: String = "parent"
        const val PROCESS_DEFINITION: String = "processDefinition"
        const val SUPER_EXECUTION: String = "superExecution"
        const val SUPER_CASE_EXECUTION_ID: String = "superCaseExecutionId"
        const val CASE_INSTANCE_ID: String = "caseInstanceId"
        const val ACTIVITY_ID: String = "activityId"
        const val ACTIVITY_INSTANCE_ID: String = "activityInstanceId"
        const val ACTIVE: String = "active"
        const val CONCURRENT: String = "concurrent"
        const val SCOPE: String = "scope"
        const val EVENT_SCOPE: String = "eventScope"
        const val SUSPENSION_STATE: String = "suspensionState"
        const val CACHED_ENTITY_STATE: String = "cachedEntityState"
        const val SEQUENCE_COUNTER: String = "sequenceCounter"
        const val TENANT_ID: String = "tenantId"
        const val VARIABLES: String = "variableInstances"

    }
}