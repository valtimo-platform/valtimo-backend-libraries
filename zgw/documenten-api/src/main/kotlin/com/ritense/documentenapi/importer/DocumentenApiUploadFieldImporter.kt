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

package com.ritense.documentenapi.importer

import com.ritense.documentenapi.deployment.ZgwDocumentUploadFieldsDeploymentService
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.DOCUMENT_DEFINITION
import com.ritense.importer.ValtimoImportTypes.Companion.ZGW_DOCUMENT_UPLOAD_FIELD
import com.ritense.valtimo.changelog.service.ChangelogDeployer
import mu.KotlinLogging

class DocumentenApiUploadFieldImporter(
    private val deployer: ZgwDocumentUploadFieldsDeploymentService,
    private val changelogDeployer: ChangelogDeployer,
) : Importer {
    override fun type(): String = ZGW_DOCUMENT_UPLOAD_FIELD

    override fun dependsOn(): Set<String> {
        return setOf(DOCUMENT_DEFINITION)
    }

    override fun supports(fileName: String): Boolean = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        logger.info { "Importing ZGW document upload fields for file ${request.fileName}" }
        changelogDeployer.deploy(deployer, request.fileName, request.content.toString(Charsets.UTF_8))
    }

    private companion object {
        private val logger = KotlinLogging.logger {}
        val FILENAME_REGEX = """config/case/zgw-document-upload-fields/([^/]+)\.zgw-document-upload-fields\.json"""
            .toRegex()
    }
}