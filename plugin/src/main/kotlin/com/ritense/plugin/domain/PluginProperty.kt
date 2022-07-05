package com.ritense.plugin.domain

import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "plugin_property")
class PluginProperty(
    @Id
    @EmbeddedId
    val id: PluginPropertyId,
    @Column(name = "title")
    val title: String,
    @Column(name = "required")
    val required: Boolean,
    @Column(name = "field_name")
    val fieldName: String,
    @Column(name = "field_type")
    val fieldType: String
)