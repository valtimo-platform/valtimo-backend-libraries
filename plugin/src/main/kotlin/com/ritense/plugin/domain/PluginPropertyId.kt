package com.ritense.plugin.domain

import com.ritense.valtimo.contract.domain.AbstractId
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.ManyToOne

@Embeddable
class PluginPropertyId(
    @Column(name = "plugin_property_key")
    val key: String,
    @ManyToOne(targetEntity = PluginDefinition::class, fetch = FetchType.LAZY)
    @JoinColumns(
        JoinColumn(name = "plugin_definition_key", referencedColumnName = "plugin_definition_key"),
    )
    var pluginDefinition: PluginDefinition
): AbstractId<PluginPropertyId>()