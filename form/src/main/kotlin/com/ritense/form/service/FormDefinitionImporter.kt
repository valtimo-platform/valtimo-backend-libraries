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

package com.ritense.form.service

import com.ritense.form.autodeployment.FormDefinitionDeploymentService
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.CASE_DEFINITION
import com.ritense.importer.ValtimoImportTypes.Companion.FORM
import org.springframework.transaction.annotation.Transactional

@Transactional
class FormDefinitionImporter(
    private val formDefinitionDeploymentService: FormDefinitionDeploymentService
) : Importer {
    override fun type(): String = FORM

    override fun dependsOn(): Set<String> = setOf(CASE_DEFINITION)

    override fun supports(fileName: String): Boolean = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        val formDefinitionAsString = request.content.toString(Charsets.UTF_8)
        formDefinitionDeploymentService
            .deploy(
                fileNameWithoutPathAndExtension(request.fileName),
                formDefinitionAsString,
                request.caseDefinitionId,
                false
            )
    }

    private fun fileNameWithoutPathAndExtension(fileName: String): String {
        return fileName.substringAfterLast('/').substringBeforeLast('.')
    }

    companion object {
        val FILENAME_REGEX = """/form/([^/]+)\.json""".toRegex()
    }
}