package com.ritense.case.domain

import com.ritense.document.domain.search.SearchWithConfigRequest

data class CaseExportRequest(
    val caseDefinitionKey: String,
    val searchRequest: SearchWithConfigRequest,
)