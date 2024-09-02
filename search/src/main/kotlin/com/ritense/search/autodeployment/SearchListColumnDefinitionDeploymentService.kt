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
package com.ritense.search.autodeployment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.search.domain.SearchListColumn
import com.ritense.search.domain.SearchListColumnConfigurationAutoDeploymentFinishedEvent
import com.ritense.search.service.SearchListColumnService
import com.ritense.valtimo.contract.annotation.SkipComponentScan
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.stereotype.Service
import java.io.IOException

@Service
@SkipComponentScan
class SearchListColumnDefinitionDeploymentService(
    private val resourceLoader: ResourceLoader,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val searchListColumnService: SearchListColumnService,
    private val objectMapper: ObjectMapper
) {
    @EventListener(ApplicationReadyEvent::class)
    @Order(Ordered.LOWEST_PRECEDENCE)
    fun deployAllFromResourceFiles() {
        logger.info("Deploying all search list column configurations from {}", PATH)
        val resources = loadResources()

        val searchListColumnList = resources.map { resource ->
            require(resource != null)
            try {
                val searchListColumn = objectMapper.readValue<SearchListColumn>(resource.inputStream)

                if (
                    searchListColumnService.findById(searchListColumn.id).isEmpty
                ) {
                    searchListColumnService.create(searchListColumn)
                } else {
                    searchListColumnService.update(searchListColumn.ownerId, searchListColumn.key, searchListColumn)
                }.also {
                    logger.info("Deployed search list column configuration {}", searchListColumn.id)
                }
            } catch (e: IOException) {
                throw RuntimeException("Error while deploying search list column configurations", e)
            }
        }

        applicationEventPublisher.publishEvent(
            SearchListColumnConfigurationAutoDeploymentFinishedEvent().searchListColumnAutoDeploymentFinishedEvent(
                searchListColumnList
            )
        )
    }

    private fun loadResources(): Array<Resource?> {
        return try {
            ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(PATH)
        } catch (ioe: IOException) {
            throw RuntimeException("Failed to load resources from $PATH", ioe)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        const val PATH = "classpath*:config/search-list-column/*.json"
    }
}
