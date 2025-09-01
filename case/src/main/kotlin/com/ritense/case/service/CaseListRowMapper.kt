package com.ritense.case.service

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.ritense.case.domain.CaseListColumn
import com.ritense.case.web.rest.dto.CaseListRowDto
import com.ritense.document.domain.impl.JsonSchemaDocument

class CaseListRowMapper {

    fun toCaseListRowDto(
        jsonSchemaDocument: JsonSchemaDocument,
        caseListColumns: List<CaseListColumn>
    ): CaseListRowDto {
        val items = caseListColumns.map { listColumn ->
            val value = when {
                listColumn.path.startsWith(PREFIX_DOC_PROPERTY) -> fromDocumentContent(
                    jsonSchemaDocument.content().asJson(),
                    toJsonPointer(listColumn.path.removePrefix(PREFIX_DOC_PROPERTY))
                )

                listColumn.path.startsWith(PREFIX_CASE_PROPERTY) -> fromCaseMetaData(
                    jsonSchemaDocument, listColumn.path.removePrefix(PREFIX_CASE_PROPERTY)
                )

                else -> null
            }
            CaseListRowDto.CaseListItemDto(listColumn.id.key, value)
        }
        return CaseListRowDto(jsonSchemaDocument.id().toString(), items)
    }

    fun fromDocumentContent(documentContent: JsonNode, path: JsonPointer): Any? {
        return documentContent
            .at(path)
            .toValue()
    }

    private fun JsonNode.toValue(): Any? =
        if (isMissingNode || isNull) null
        else jacksonObjectMapper().treeToValue(this)

    private fun toJsonPointer(path: String): JsonPointer {
        return JsonPointer.valueOf(
            (if (path.startsWith("/")) path
            else "/$path").replace('.', '/')
        )
    }

    private fun fromCaseMetaData(document: JsonSchemaDocument, key: String): Any? = when (key) {
        "id" -> document.id().id.toString()
        "createdOn" -> document.createdOn()
        "modifiedOn" -> document.modifiedOn().orElse(null)
        "createdBy" -> document.createdBy()
        "assigneeFullName" -> document.assigneeFullName()
        "assigneeId" -> document.assigneeId()
        "caseTags" -> document.caseTags().map { it.title }
        "documentDefinitionId" -> document.definitionId().toString()
        "documentDefinitionId.name" -> document.definitionId().name()
        "internalStatus" -> document.internalStatus()
        "sequence" -> document.sequence()
        "version" -> document.version()
        else -> null
    }

    companion object {
        private const val PREFIX_CASE_PROPERTY = "case:"
        private const val PREFIX_DOC_PROPERTY = "doc:"
    }
}