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
