package com.ritense.processdocument.event

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import com.ritense.valtimo.contract.audit.AuditEvent
import com.ritense.valtimo.contract.audit.AuditMetaData
import com.ritense.valtimo.contract.audit.view.AuditView
import com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty
import com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull
import java.time.LocalDateTime
import java.util.UUID

class BesluitAddedEvent : AuditMetaData, AuditEvent {

    private var identificatie: String
    private var businessKey: String

    @JsonCreator
    constructor(
        id: UUID?,
        origin: String?,
        occurredOn: LocalDateTime?,
        user: String?,
        identificatie: String,
        businessKey: String
    ) : super(id, origin, occurredOn, user) {

        assertArgumentNotNull(identificatie, "identificatie is required")
        assertArgumentNotEmpty(identificatie, "identification cannot be empty")
        assertArgumentNotNull(businessKey, "businessKey is required")
        assertArgumentNotEmpty(businessKey,"businessKey cannot be empty")
        this.identificatie = identificatie
        this.businessKey = businessKey
    }

    @JsonProperty
    @JsonView(AuditView.Public::class)
    fun getIdentificatie(): String {
        return identificatie
    }

    fun getBusinessKey(): String {
        return businessKey
    }

    override fun getDocumentId(): UUID {
       return UUID.fromString(businessKey)
    }
}