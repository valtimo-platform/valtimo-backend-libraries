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

package com.ritense.document.importer

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.search.SearchConfigurationDto
import com.ritense.document.exception.SearchFieldConfigurationDeploymentException
import com.ritense.document.service.SearchFieldService
import com.ritense.importer.ImportRequest
import com.ritense.importer.Importer
import com.ritense.importer.ValtimoImportTypes.Companion.DOCUMENT_DEFINITION
import com.ritense.importer.ValtimoImportTypes.Companion.SEARCH
import com.ritense.logging.withLoggingContext
import mu.KotlinLogging
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.transaction.annotation.Transactional
import java.io.IOException

@Transactional
class SearchFieldImporter(
    private val resourceLoader: ResourceLoader,
    private val searchFieldService: SearchFieldService,
    private val objectMapper: ObjectMapper,
) : Importer {
    override fun type() = SEARCH

    override fun dependsOn() = setOf(DOCUMENT_DEFINITION)

    override fun supports(fileName: String) = fileName.matches(FILENAME_REGEX)

    override fun import(request: ImportRequest) {
        withLoggingContext("caseDefinitionKey" to request.caseDefinitionId!!.key) {
            deploy(
                request,
                request.content.toString(Charsets.UTF_8)
            )
        }
    }

    @Throws(IOException::class)
    fun deploy(request: ImportRequest, searchConfigurationJson: String) {
        validate(searchConfigurationJson)

        val searchConfiguration: SearchConfigurationDto = objectMapper.readValue(
            searchConfigurationJson,
            SearchConfigurationDto::class.java
        )

        val caseDefinitionId = request.caseDefinitionId!!

        try {
            runWithoutAuthorization<Any?> {
                // TODO: Do we want to do this, or only delete when the to be deployed case definition is newer than the
                // last deployed case definition? Or something else entirely?
                searchFieldService.deleteSearchFields(caseDefinitionId.key)
                val searchConfigurationFields = searchConfiguration.toEntity(caseDefinitionId.key)
                searchFieldService.createSearchConfiguration(searchConfigurationFields, caseDefinitionId)
                logger.info(
                    "Deployed search configuration for case - {}",
                    caseDefinitionId.key
                )
                null
            }
        } catch (e: Exception) {
            throw SearchFieldConfigurationDeploymentException(caseDefinitionId.key, e)
        }
    }

    @Throws(IOException::class)
    private fun validate(searchJson: String) {
        val configurationJsonObject = JSONObject(JSONTokener(searchJson))

        val schema = SchemaLoader.load(JSONObject(JSONTokener(loadSearchSchemaResource().inputStream)))
        schema.validate(configurationJsonObject)
    }

    private fun loadSearchSchemaResource(): Resource {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResource(SEARCH_SCHEMA_PATH)
    }

    private companion object {
        val FILENAME_REGEX = """/search/([^/]*)\.json""".toRegex()
        const val SEARCH_SCHEMA_PATH = "classpath:config/search/schema/search.schema.json"
        val logger = KotlinLogging.logger {}
    }
}