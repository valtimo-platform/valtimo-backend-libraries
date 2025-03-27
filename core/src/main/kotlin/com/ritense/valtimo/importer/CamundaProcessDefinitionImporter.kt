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

package com.ritense.valtimo.importer

import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.CASE_DEFINITION
import com.ritense.importer.ValtimoImportTypes.Companion.PROCESS_DEFINITION
import com.ritense.valtimo.service.CamundaProcessService
import org.springframework.transaction.annotation.Transactional

@Transactional
class CamundaProcessDefinitionImporter(
    private val camundaProcessService: CamundaProcessService
) : Importer {
    override fun type() = PROCESS_DEFINITION

    override fun dependsOn(): Set<String> = setOf(CASE_DEFINITION)

    override fun supports(fileName: String): Boolean {
        return fileName.startsWith(PATH)
            && fileName.substringAfterLast('.') == EXTENSION
    }

    override fun import(request: ImportRequest) {
        request.content.inputStream().use {
            camundaProcessService.deploy(request.caseDefinitionId, fileNameWithoutPath(request.fileName), it)
        }
    }

    private fun fileNameWithoutPath(fileName: String): String {
        return fileName.substringAfterLast('/')
    }

    private companion object {
            private const val PATH = "/bpmn/"
            private const val EXTENSION = "bpmn"
    }
}