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

package com.ritense.form.service

import com.ritense.form.autodeployment.FormDefinitionDeploymentService
import com.ritense.importer.Importer
import com.ritense.importer.ImportRequest

class FormDefinitionImporter(
    val formDefinitionDeploymentService: FormDefinitionDeploymentService
) : Importer {
    override fun type(): String = "form"

    override fun dependsOn(): Set<String> = emptySet()

    override fun supports(fileName: String): Boolean {
        return fileName.substringBeforeLast('/') == PATH
            && fileName.substringAfterLast('.') == EXTENSION
    }

    override fun import(request: ImportRequest) {
        val formDefinitionAsString = request.content.toString(Charsets.UTF_8)
        formDefinitionDeploymentService
            .deploy(
                fileNameWithoutPathAndExtension(request.fileName),
                formDefinitionAsString
            )
    }

    private fun fileNameWithoutPathAndExtension(fileName: String): String {
        return fileName.substringBeforeLast('.').substringAfterLast('/')
    }

    companion object {
        private const val PATH = "config/form/"
        private const val EXTENSION = "json"
    }
}