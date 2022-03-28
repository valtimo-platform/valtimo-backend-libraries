package com.ritense.formflow.domain

import org.hibernate.annotations.Type
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.OneToMany
import javax.persistence.OrderBy

@Embeddable
class FormFlowInstanceContext(
    @OneToMany(mappedBy = "form_flow_step_instance", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("formFlowStepInstance.order ASC")
    val history: MutableList<FormFlowStepInstance>,
    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonType")
    @Column(name = "additional_properties", columnDefinition = "json", nullable = false)
    val additionalProperties: Map<String, Any>
)