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

package com.ritense.zaakdetails.documentobjectenapisync

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "document_objecten_api_sync")
data class DocumentObjectenApiSync(

    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "document_definition_name")
    val documentDefinitionName: String,

    @Column(name = "document_definition_version")
    val documentDefinitionVersion: Long,

    @Column(name = "object_management_configuration_id")
    val objectManagementConfigurationId: UUID,

    @Column(name = "enabled")
    val enabled: Boolean = true
)
