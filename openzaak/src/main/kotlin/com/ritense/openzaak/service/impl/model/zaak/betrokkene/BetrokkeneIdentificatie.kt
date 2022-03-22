package com.ritense.openzaak.service.impl.model.zaak.betrokkene

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "betrokkeneIdentificatie"
)
@JsonSubTypes(
    value = [
        JsonSubTypes.Type(value = RolNatuurlijkPersoon::class),
        JsonSubTypes.Type(value = RolNietNatuurlijkPersoon::class)
    ]
)
sealed class BetrokkeneIdentificatie()