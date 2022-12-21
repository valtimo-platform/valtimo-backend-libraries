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

package com.ritense.objectmanagement.domain

import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "object-management")
data class ObjectManagement(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "objecten_api_plugin_configuration_id", nullable = false)
    val objecttypenApiPluginConfigurationId: UUID,

    @Column(name = "objecttype_id", nullable = false)
    val objecttypeId: UUID,

    @Column(name = "objecttypen_api_plugin_configuration_id", nullable = false)
    val objectenApiPluginConfigurationId: String,

    @Column(name = "show_data_in_menu", nullable = false)
    val showInDataMenu: Boolean = false,

    @Column(name = "form_definition_view")
    val formDefinitionView: String?,

    @Column(name = "form_definition_edit")
    val formDefinitionEdit: String?
)
