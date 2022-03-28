package com.ritense.formflow.domain

import java.util.UUID
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class FormFlowInstanceId(
    @Column(name = "form_flow_instance_id")
    val id: UUID
) : AbstractId<FormFlowInstanceId>() {

}