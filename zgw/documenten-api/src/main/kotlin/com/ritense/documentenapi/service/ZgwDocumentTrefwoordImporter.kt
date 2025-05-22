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

package com.ritense.documentenapi.service

import com.ritense.documentenapi.deployment.ZgwDocumentTrefwoordDeploymentService
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.DOCUMENT_DEFINITION
import com.ritense.valtimo.changelog.service.ChangelogDeployer
import mu.KotlinLogging
import org.springframework.transaction.annotation.Transactional

@Transactional
class ZgwDocumentTrefwoordImporter(
    private val zgwDocumentTrefwoordDeploymentService: ZgwDocumentTrefwoordDeploymentService,
    private val changelogDeployer: ChangelogDeployer
) : Importer {
    override fun type() = "zgw-document-trefwoord"

    override fun dependsOn() = setOf(DOCUMENT_DEFINITION)

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        logger.info { "Importing ZGW document trefwoorden for file ${request.fileName}" }
        changelogDeployer.deploy(zgwDocumentTrefwoordDeploymentService, request.fileName, request.content.toString(Charsets.UTF_8))
    }

    private companion object {
        private val logger = KotlinLogging.logger {}
        val FILENAME_REGEX = """config/case/trefwoorden/([^/]+)\.zgw-document-trefwoorden\.json""".toRegex()
    }
}