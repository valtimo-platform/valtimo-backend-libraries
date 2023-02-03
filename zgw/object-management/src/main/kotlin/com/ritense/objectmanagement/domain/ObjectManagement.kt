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

package com.ritense.objectmanagement.domain

import com.ritense.objectenapi.management.ObjectManagementInfo
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "object_management_configuration")
data class ObjectManagement(
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    override val id: UUID = UUID.randomUUID(),

    @Column(name = "title", nullable = false, unique = true)
    override val title: String,

    @Column(name = "objecttypen_api_plugin_configuration_id", nullable = false)
    override val objecttypenApiPluginConfigurationId: UUID,

    @Column(name = "objecttype_id", nullable = false)
    override val objecttypeId: String,

    @Column(name = "object_type_version")
    override val objecttypeVersion: Int = 0,

    @Column(name = "objecten_api_plugin_configuration_id", nullable = false)
    override val objectenApiPluginConfigurationId: UUID,

    @Column(name = "show_data_in_menu", nullable = false)
    override val showInDataMenu: Boolean = false,

    @Column(name = "form_definition_view")
    override val formDefinitionView: String? = null,

    @Column(name = "form_definition_edit")
    override val formDefinitionEdit: String? = null
) : ObjectManagementInfo
