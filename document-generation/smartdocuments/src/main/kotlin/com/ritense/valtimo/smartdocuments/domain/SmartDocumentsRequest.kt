package com.ritense.valtimo.smartdocuments.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class SmartDocumentsRequest(
    val customerData: Map<String, String>,
    @JsonProperty("SmartDocument") val smartDocument: SmartDocument,
) {

    data class SmartDocument(
        @JsonProperty("Selection") val selection: String,
        @JsonProperty("Template") val template: String,
        @JsonProperty("TemplateGroup") val templateGroup: String,
    )
}
