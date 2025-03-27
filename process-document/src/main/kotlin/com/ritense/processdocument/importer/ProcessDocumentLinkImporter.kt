/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.processdocument.importer

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.service.DocumentDefinitionService
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.PROCESS_DEFINITION
import com.ritense.importer.ValtimoImportTypes.Companion.PROCESS_DOCUMENT_LINK
import com.ritense.processdocument.domain.ProcessDefinitionCaseDefinitionId
import com.ritense.processdocument.domain.ProcessDefinitionId
import com.ritense.processdocument.domain.ProcessDocumentDefinitionRequest
import com.ritense.processdocument.domain.config.ProcessDocumentLinkConfigItem
import com.ritense.processdocument.service.ProcessDefinitionCaseDefinitionService
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import mu.KLogger
import mu.KotlinLogging
import org.camunda.bpm.engine.RepositoryService
import org.springframework.transaction.annotation.Transactional

@Transactional
class ProcessDocumentLinkImporter(
    private val processDefinitionCaseDefinitionService: ProcessDefinitionCaseDefinitionService,
    private val documentDefinitionService: DocumentDefinitionService,
    private val objectMapper: ObjectMapper,
    private val repositoryService: RepositoryService
) : Importer {

    override fun type() = PROCESS_DOCUMENT_LINK

    override fun dependsOn() = setOf(PROCESS_DEFINITION)

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        val documentDefinitionName = FILENAME_REGEX.matchEntire(request.fileName)!!.groupValues[1]
        deploy(request.caseDefinitionId!!, documentDefinitionName, request.content.toString(Charsets.UTF_8))
    }

    @Throws(JsonProcessingException::class)
    fun deploy(caseDefinitionId: CaseDefinitionId, documentDefinitionName: String, content: String) {
        val processDocumentLinkConfigItems: List<ProcessDocumentLinkConfigItem> = getJson(content)
        val documentDefinitionOptional =
            documentDefinitionService.findByNameAndCaseDefinitionId(documentDefinitionName, caseDefinitionId)
        if (documentDefinitionOptional.isPresent()) {
            processDocumentLinkConfigItems.forEach { item ->
                createProcessDocumentLink(
                    caseDefinitionId,
                    documentDefinitionName,
                    item
                )
            }
        }
    }

    private fun createProcessDocumentLink(
        caseDefinitionId: CaseDefinitionId,
        documentDefinitionName: String,
        item: ProcessDocumentLinkConfigItem
    ) {
        val processDefinition = repositoryService
            .createProcessDefinitionQuery()
            .processDefinitionKey(item.processDefinitionKey)
            .versionTag("$caseDefinitionId")
            .singleResult()

        val request = ProcessDocumentDefinitionRequest(
            ProcessDefinitionId(processDefinition.id),
            caseDefinitionId,
            item.canInitializeDocument,
            item.startableByUser
        )

        runWithoutAuthorization<Any?> {
            //get document definition
            val existingAssociation = processDefinitionCaseDefinitionService.findById(
                ProcessDefinitionCaseDefinitionId(ProcessDefinitionId(item.processDefinitionKey), caseDefinitionId)
            )

            if (existingAssociation != null) {
                if (!item.equalsProcessDocumentDefinition(existingAssociation)) {
                    logger.info(
                        "Updating process-document-links from {}.json",
                        documentDefinitionName
                    )
                    processDefinitionCaseDefinitionService.deleteProcessDocumentDefinition(
                        request.processDefinitionId,
                        request.caseDefinitionId
                    )
                    processDefinitionCaseDefinitionService.createProcessDocumentDefinition(request)
                }
            } else {
                logger.info(
                    "Deploying process-document-links from {}.json",
                    documentDefinitionName
                )
                processDefinitionCaseDefinitionService.createProcessDocumentDefinition(request)
            }
            null
        }
    }

    private fun getJson(rawJson: String): List<ProcessDocumentLinkConfigItem> {
        return objectMapper.readValue<List<ProcessDocumentLinkConfigItem>>(rawJson)
    }

    companion object {
        private val FILENAME_REGEX = """/process-document-link/([^/]+)\.json""".toRegex()
        private val logger: KLogger = KotlinLogging.logger {}
    }
}