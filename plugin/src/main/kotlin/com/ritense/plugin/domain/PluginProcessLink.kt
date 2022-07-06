package com.ritense.plugin.domain

import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "plugin_process_link")
data class PluginProcessLink(
    @Id
    @Embedded
    override val id: PluginProcessLinkId,
    @Column(name = "process_definition_id", updatable = false)
    override val processDefinitionId: String,
    @Column(name = "activity_id", updatable = false)
    override val activityId: String,
    @Column(name = "action_properties")
    val actionProperties: String,
    @Column(name = "plugin_configuration_key")
    val pluginConfigurationKey: String,
    @Column(name = "plugin_action_definition_key")
    val pluginActionDefinitionKey: String
) : ProcessLink