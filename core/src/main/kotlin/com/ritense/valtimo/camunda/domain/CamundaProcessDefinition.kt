/*
 *  Copyright 2015-2023 Ritense BV, the Netherlands.
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

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "ACT_RE_PROCDEF")
class CamundaProcessDefinition(

    @Id
    @Column(name = "ID_")
    val id: String,

    @Column(name = "REV_")
    val revision: Int?,

    @Column(name = "CATEGORY_")
    val category: String?,

    @Column(name = "NAME_")
    val name: String?,

    @Column(name = "KEY_")
    val key: String,

    @Column(name = "VERSION_")
    val version: Int,

    @Column(name = "DEPLOYMENT_ID_")
    val deploymentId: String?,

    @Column(name = "RESOURCE_NAME_")
    val resourceName: String?,

    @Column(name = "DGRM_RESOURCE_NAME_")
    val diagramResourceName: String?,

    @Column(name = "HAS_START_FORM_KEY_")
    val hasStartFormKey: Boolean?,

    @Column(name = "SUSPENSION_STATE_")
    val suspensionState: Int?,

    @Column(name = "TENANT_ID_")
    val tenantId: String?,

    @Column(name = "VERSION_TAG_")
    val versionTag: String?,

    @Column(name = "HISTORY_TTL_")
    val historyTimeToLive: Int?,

    @Column(name = "STARTABLE_")
    val isStartableInTasklist: Boolean

)