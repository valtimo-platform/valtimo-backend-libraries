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

package com.ritense.valtimo.formflow.importer

import com.ritense.formflow.service.FormFlowDeploymentService
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.FORM
import com.ritense.importer.ValtimoImportTypes.Companion.FORM_FLOW
import org.springframework.transaction.annotation.Transactional

@Transactional
class FormFlowDefinitionImporter(
    private val formFlowDeploymentService: FormFlowDeploymentService
) : Importer {
    override fun type() = FORM_FLOW

    override fun dependsOn() = setOf(FORM)

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        val formFlowKey = FILENAME_REGEX.matchEntire(request.fileName)!!.groupValues[1]
        formFlowDeploymentService.deploy(formFlowKey, request.content.toString(Charsets.UTF_8))
    }

    private companion object {
        val FILENAME_REGEX = """config/form-flow/([^/]+)\.json""".toRegex()
    }
}