package com.ritense.case.service.exception

class NoExportPermissionException(caseDefinitionKey: String) : RuntimeException( "No permission found to export case '$caseDefinitionKey'.")