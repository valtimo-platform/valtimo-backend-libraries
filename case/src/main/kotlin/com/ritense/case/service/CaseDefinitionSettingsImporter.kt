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

package com.ritense.case.service

import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.CASE_DEFINITION_SETTINGS
import com.ritense.importer.ValtimoImportTypes.Companion.DOCUMENT_DEFINITION
import com.ritense.logging.withLoggingContext

class CaseDefinitionSettingsImporter(
    private val deploymentService: CaseDefinitionDeploymentService
) : Importer {
    override fun type() = CASE_DEFINITION_SETTINGS

    override fun dependsOn() = setOf(DOCUMENT_DEFINITION)

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        val caseDefinitionName = FILENAME_REGEX.matchEntire(request.fileName)!!.groupValues[1]
        withLoggingContext("jsonSchemaDocumentName" to caseDefinitionName) {
            deploymentService.deploy(caseDefinitionName, request.content.toString(Charsets.UTF_8), true)
        }
    }

    private companion object {
        val FILENAME_REGEX = """config/case/definition/([^/]+)\.json""".toRegex()
    }
}