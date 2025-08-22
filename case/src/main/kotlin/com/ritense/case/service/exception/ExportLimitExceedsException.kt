package com.ritense.case.service.exception

class ExportLimitExceedsException(caseDefinitionKey: String) : RuntimeException(
    "Export failed for case '$caseDefinitionKey': the number of cases exceeds the maximum limit of 10,000. Please refine your search criteria."
)