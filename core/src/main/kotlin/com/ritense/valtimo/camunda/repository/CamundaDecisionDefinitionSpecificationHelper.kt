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

package com.ritense.valtimo.camunda.repository

import com.ritense.valtimo.camunda.domain.CamundaDecisionDefinition
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.service.CamundaProcessService.CAMUNDA_CASE_DEFINITION_VERSION_TAG_PREFIX
import org.springframework.data.jpa.domain.Specification

class CamundaDecisionDefinitionSpecificationHelper {

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
        const val DEQ_REQ_ID: String = "decisionRequirementsId"
        const val DEQ_REQ_KEY: String = "decisionRequirementsKey"
        const val TENANT_ID: String = "tenantId"
        const val HISTORY_TTL: String = "historyTimeToLive"
        const val VERSION_TAG: String = "versionTag"

        @JvmStatic
        fun byId(id: String) = Specification<CamundaDecisionDefinition> { root, _, cb ->
            cb.equal(root.get<Any>(ID), id)
        }

        @JvmStatic
        fun byKey(decisionDefinitionKey: String) = Specification<CamundaDecisionDefinition> { root, _, cb ->
            cb.equal(root.get<Any>(KEY), decisionDefinitionKey)
        }

        @JvmStatic
        fun byVersion(version: Int) = Specification<CamundaDecisionDefinition> { root, _, cb ->
            cb.equal(root.get<Any>(VERSION), version)
        }

        @JvmStatic
        fun byVersionTag(versionTag: String?) = Specification<CamundaDecisionDefinition> { root, _, cb ->
            if (versionTag == null) {
                root.get<Any>(VERSION_TAG).isNull
            } else {
                cb.equal(root.get<Any>(VERSION_TAG), versionTag)
            }
        }

        @JvmStatic
        fun byLatestVersion() = Specification<CamundaDecisionDefinition> { root, query, cb ->
            val sub = query.subquery(Long::class.java)
            val subRoot = sub.from(CamundaDecisionDefinition::class.java)
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
        fun byLinkedToCaseDefinitionId(caseDefinitionId: CaseDefinitionId) = Specification { root, query, cb ->
            byVersionTag(CAMUNDA_CASE_DEFINITION_VERSION_TAG_PREFIX + caseDefinitionId)
                .toPredicate(root, query, cb)
        }

        @JvmStatic
        fun byLatestVersionAndLinkedToCaseDefinitionId(caseDefinitionId: CaseDefinitionId) = Specification { root, query, cb ->
            byLatestVersionTag(CAMUNDA_CASE_DEFINITION_VERSION_TAG_PREFIX + caseDefinitionId)
                .toPredicate(root, query, cb)
        }

        @JvmStatic
        fun byLatestVersionTag(versionTag: String?) = Specification { root, query, cb ->
            val sub = query.subquery(Long::class.java)
            val subRoot = sub.from(CamundaDecisionDefinition::class.java)
            sub.select(cb.max(subRoot.get(VERSION)))
            sub.where(
                cb.and(
                    cb.equal(subRoot.get<Any>(KEY), root.get<Any>(KEY)),
                    byVersionTag(versionTag).toPredicate(root, query, cb),
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
        fun byNotLinkedToCaseDefinition() = Specification<CamundaDecisionDefinition> { root, _, cb ->
            cb.or(
                cb.isNull(root.get<Any>(VERSION_TAG)),
                cb.not(
                    cb.equal(
                        cb.function(
                            "left",
                            String::class.java,
                            root.get<String>(VERSION_TAG),
                            cb.literal(3)
                        ),
                        "CD:"
                    )
                )
            )
        }
    }
}