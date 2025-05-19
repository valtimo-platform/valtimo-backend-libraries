/*
 *  Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.valtimo.camunda.domain

import com.ritense.valtimo.contract.case_.CaseDefinitionId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "ACT_RE_DECISION_DEF")
class CamundaDecisionDefinition(

    @Id
    @Column(name = "ID_", insertable = false, updatable = false)
    val id: String,

    @Column(name = "REV_", insertable = false, updatable = false)
    val revision: Int?,

    @Column(name = "CATEGORY_", insertable = false, updatable = false)
    val category: String?,

    @Column(name = "NAME_", insertable = false, updatable = false)
    val name: String?,

    @Column(name = "KEY_", insertable = false, updatable = false)
    val key: String,

    @Column(name = "VERSION_", insertable = false, updatable = false)
    val version: Int,

    @Column(name = "DEPLOYMENT_ID_", insertable = false, updatable = false)
    val deploymentId: String?,

    @Column(name = "RESOURCE_NAME_", insertable = false, updatable = false)
    val resourceName: String?,

    @Column(name = "DGRM_RESOURCE_NAME_", insertable = false, updatable = false)
    val diagramResourceName: String?,

    @Column(name = "DEC_REQ_ID_", insertable = false, updatable = false)
    val decisionRequirementsId: String?,

    @Column(name = "DEC_REQ_KEY_", insertable = false, updatable = false)
    val decisionRequirementsKey: String?,

    @Column(name = "TENANT_ID_", insertable = false, updatable = false)
    val tenantId: String?,

    @Column(name = "HISTORY_TTL_", insertable = false, updatable = false)
    val historyTimeToLive: Int?,

    @Column(name = "VERSION_TAG_", insertable = false, updatable = false)
    val versionTag: String?,

    ) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CamundaDecisionDefinition) return false

        if (id != other.id) return false
        if (revision != other.revision) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (revision ?: 0)
        return result
    }

    fun getCaseDefinitionId(): CaseDefinitionId? {
        return if (versionTag != null && versionTag.startsWith("CD:")) {
            val caseDefinitionIdAsArray = versionTag.substringAfter("CD:").split(":")
            CaseDefinitionId.of(caseDefinitionIdAsArray[0], caseDefinitionIdAsArray[1])
        } else {
            null
        }
    }


}