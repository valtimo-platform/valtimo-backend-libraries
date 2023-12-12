/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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
import com.ritense.valtimo.service.CamundaProcessService
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream

@Transactional
class CamundaProcessDefinitionImporter(
    private val camundaProcessService: CamundaProcessService
) : Importer {
    override fun type() = "processdefinition"

    override fun dependsOn(): Set<String> = emptySet()

    override fun supports(fileName: String): Boolean {
        return fileName.startsWith(PATH)
            && fileName.substringAfterLast('.') == EXTENSION
    }

    override fun import(request: ImportRequest) {
        camundaProcessService.deploy(fileNameWithoutPath(request.fileName), ByteArrayInputStream(request.content))
    }

    private fun fileNameWithoutPath(fileName: String): String {
        return fileName.substringAfterLast('/')
    }

    private companion object {
            private const val PATH = "bpmn/"
            private const val EXTENSION = "bpmn"
    }
}