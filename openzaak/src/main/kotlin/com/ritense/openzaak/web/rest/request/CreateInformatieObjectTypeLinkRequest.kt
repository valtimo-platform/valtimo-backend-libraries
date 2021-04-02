package com.ritense.openzaak.web.rest.request

import java.net.URI

data class CreateInformatieObjectTypeLinkRequest(
    val documentDefinitionName: String,
    val zaakType: URI,
    val informatieObjectType: URI
)