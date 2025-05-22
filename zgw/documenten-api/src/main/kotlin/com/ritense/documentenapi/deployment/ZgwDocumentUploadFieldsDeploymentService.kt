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

package com.ritense.documentenapi.deployment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.authorization.annotation.RunWithoutAuthorization
import com.ritense.documentenapi.domain.DocumentenApiUploadField
import com.ritense.documentenapi.domain.DocumentenApiUploadFieldId
import com.ritense.documentenapi.repository.DocumentenApiUploadFieldRepository
import com.ritense.documentenapi.service.DocumentenApiService
import com.ritense.valtimo.changelog.domain.ChangesetDeployer
import com.ritense.valtimo.changelog.domain.ChangesetDetails
import com.ritense.valtimo.changelog.service.ChangelogService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import mu.KLogger
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@SkipComponentScan
class ZgwDocumentUploadFieldsDeploymentService(
    private val objectMapper: ObjectMapper,
    private val documentenApiUploadFieldRepository: DocumentenApiUploadFieldRepository,
    private val documentenApiService: DocumentenApiService,
    private val changelogService: ChangelogService,
    private val clearTables: Boolean
) : ChangesetDeployer {
    override fun getPath() = "classpath*:**/*.zgw-document-upload-fields.json"

    override fun before() {
        if (clearTables) {
            logger.info { "clearTables: Clearing all documenten api upload fields" }
            documentenApiUploadFieldRepository.deleteAll()
            changelogService.deleteChangesetsByKey(KEY)
        }
    }

    @RunWithoutAuthorization
    override fun getChangelogDetails(filename: String, content: String): List<ChangesetDetails> {
        val changeset = objectMapper.readValue<ZgwDocumentUploadFieldsChangeset>(content)
        return listOf(
            ChangesetDetails(
                changesetId = changeset.changesetId,
                valueToChecksum = changeset.caseDefinitions,
                key = KEY,
                deploy = { deploy(changeset.caseDefinitions) }
            )
        )
    }

    private fun deploy(caseDefinitions: List<ZgwDocumentCaseDefinitionUploadFields>) {
        runWithoutAuthorization {
            caseDefinitions.forEach {
                it.fields.forEach { field ->
                    documentenApiService.updateUploadField(
                        DocumentenApiUploadField(
                            id = DocumentenApiUploadFieldId(it.key, field.key),
                            defaultValue = field.defaultValue,
                            visible = field.visible,
                            readonly = field.readonly,
                        )
                    )
                }
            }
        }
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
        private const val KEY = "case-documenten-api-upload-fields"
    }
}