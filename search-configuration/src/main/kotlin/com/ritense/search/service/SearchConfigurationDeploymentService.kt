/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.search.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.search.domain.SearchConfigurationDto
import mu.KotlinLogging
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StreamUtils
import java.io.InputStream
import java.nio.charset.StandardCharsets

open class SearchConfigurationDeploymentService(
    private val resourceLoader: ResourceLoader,
    private val searchConfigurationService: SearchConfigurationService,
    private val objectMapper: ObjectMapper,
) {

    @Transactional
    @EventListener(ApplicationReadyEvent::class)
    open fun deployAll() {
        logger.info("Deploy all search configurations")
        try {
            loadSearchConfigurationsResources().forEach { resource ->
                deploy(resource)
            }
        } catch (e: Exception) {
            throw RuntimeException("Error deploying search configurations", e)
        }
    }

    private fun deploy(searchResource: Resource) {
        if (searchResource.filename != null) {
            deploy(searchResource.filename!!.substringBeforeLast("."), searchResource.inputStream)
        }
    }

    private fun deploy(searchConfigurationName: String, searchConfigurationJson: InputStream) {
        deploy(searchConfigurationName, StreamUtils.copyToString(searchConfigurationJson, StandardCharsets.UTF_8))
    }

    private fun deploy(searchConfigurationName: String, searchConfigurationJson: String) {
        validate(searchConfigurationJson)

        val searchConfiguration = objectMapper.readValue(searchConfigurationJson, SearchConfigurationDto::class.java)

        try {
            searchConfigurationService.saveConfiguration(searchConfiguration.toEntity(searchConfigurationName))
            logger.info("Deployed search configuration - {}", searchConfigurationName)
        } catch (e: Exception) {
            throw RuntimeException("Failed to deploy search configuration $searchConfigurationName", e)
        }
    }

    private fun validate(searchJson: String) {
        val configurationJsonObject = JSONObject(JSONTokener(searchJson))

        val schema = SchemaLoader.load(JSONObject(JSONTokener(loadSearchSchemaResource().inputStream)))
        schema.validate(configurationJsonObject)
    }


    private fun loadSearchSchemaResource(): Resource {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResource(SEARCH_SCHEMA_PATH)
    }

    private fun loadSearchConfigurationsResources(): Array<Resource> {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(SEARCH_CONFIGURATIONS_PATH)
    }

    companion object {
        private const val SEARCH_SCHEMA_PATH = "classpath:config/search/schema/search.schema.json"
        private const val SEARCH_CONFIGURATIONS_PATH = "classpath:config/search/*.json"
        val logger = KotlinLogging.logger {}
    }
}
