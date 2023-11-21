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

package com.ritense.document.service

import com.ritense.document.domain.DocumentDefinition
import com.ritense.document.domain.impl.Mapper
import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.valtimo.contract.domain.ExportFile
import java.io.ByteArrayOutputStream

open class JsonSchemaDocumentDefinitionExportService(
    private val documentDefinitionService: JsonSchemaDocumentDefinitionService
) {

    open fun export(id: DocumentDefinition.Id): Set<ExportFile> {
        val documentDefinition = documentDefinitionService.findBy(id).orElseThrow()

        val baos = ByteArrayOutputStream()

        MAPPER.writerWithDefaultPrettyPrinter().writeValue(baos, documentDefinition.schema.asJson())

        val exportFile = ExportFile(
            PATH.format(documentDefinition.id.name()),
            baos.toByteArray()
        )

        return setOf(exportFile)
    }

    companion object {
        const val PATH = "config/document/definition/%s.schema.json"
        private val MAPPER = Mapper.INSTANCE.get()

    }
}