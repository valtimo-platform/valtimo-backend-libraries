package com.ritense.plugin.domain

import com.fasterxml.jackson.databind.JsonNode
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import org.hibernate.annotations.Type

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
    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(name = "action_properties", columnDefinition = "JSON")
    val actionProperties: JsonNode,
    @Column(name = "plugin_configuration_id")
    val pluginConfigurationKey: String,
    @Column(name = "plugin_action_definition_key")
    val pluginActionDefinitionKey: String
) : ProcessLink