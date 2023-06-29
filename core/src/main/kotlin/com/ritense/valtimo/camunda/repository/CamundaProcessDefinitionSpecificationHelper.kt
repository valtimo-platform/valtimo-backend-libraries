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

import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState
import org.springframework.data.jpa.domain.Specification

class CamundaProcessDefinitionSpecificationHelper {

    companion object {

        const val ID: String = "id"
        const val REVISION: String = "revision"
        const val CATEGORY: String = "category"
        const val NAME: String = "name"
        const val KEY: String = "key"
        const val VERSION: String = "version"
        const val DEPLOYMENT_ID: String = "deploymentId"
        const val RESOURCE_NAME: String = "resourceName"
        const val DIAGRAM_RESOURCE_NAME: String = "diagramResourceName"
        const val HAS_START_FORM_KEY: String = "hasStartFormKey"
        const val SUSPENSION_STATE: String = "suspensionState"
        const val TENANT_ID: String = "tenantId"
        const val VERSION_TAG: String = "versionTag"
        const val HISTORY_TIME_TO_LIVE: String = "historyTimeToLive"
        const val IS_STARTABLE_IN_TASK_LIST: String = "isStartableInTasklist"

        @JvmStatic
        fun query() = Specification<CamundaProcessDefinition> { _, _, cb ->
            cb.equal(cb.literal(1), 1)
        }

        @JvmStatic
        fun byId(id: String) = Specification<CamundaProcessDefinition> { root, _, cb ->
            cb.equal(root.get<Any>(ID), id)
        }

        @JvmStatic
        fun byKey(processDefinitionKey: String) = Specification<CamundaProcessDefinition> { root, _, cb ->
            cb.equal(root.get<Any>(KEY), processDefinitionKey)
        }

        @JvmStatic
        fun byVersion(version: Int) = Specification<CamundaProcessDefinition> { root, _, cb ->
            cb.equal(root.get<Any>(VERSION), version)
        }

        @JvmStatic
        fun byLatestVersion() = Specification<CamundaProcessDefinition> { root, query, cb ->
            val sub = query.subquery(Long::class.java)
            val subRoot = sub.from(CamundaProcessDefinition::class.java)
            sub.select(cb.max(subRoot.get(VERSION)))
            sub.where(
                cb.and(
                    cb.equal(subRoot.get<Any>(KEY), root.get<Any>(KEY)),
                    cb.or(
                        cb.equal(subRoot.get<Any>(TENANT_ID), root.get<Any>(TENANT_ID)),
                        cb.and(subRoot.get<Any>(TENANT_ID).isNull, root.get<Any>(TENANT_ID).isNull)
                    )
                )
            )
            sub.groupBy(subRoot.get<Any>(TENANT_ID), subRoot.get<Any>(KEY))
            cb.equal(root.get<Any>(VERSION), sub)
        }

        @JvmStatic
        fun byActive() = Specification<CamundaProcessDefinition> { root, _, cb ->
            cb.equal(root.get<Any>(SUSPENSION_STATE), SuspensionState.ACTIVE.stateCode)
        }

    }
}