/*
 *  Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.valtimo.changelog.service

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.valtimo.changelog.domain.ChangesetDeployer
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.transaction.annotation.Transactional
import java.util.TreeMap


@Transactional
class ChangelogDeployer(
    private val changelogService: ChangelogService,
    private val changesetDeployers: List<ChangesetDeployer>,
    private val environment: Environment,
) {
    // Create new objectmapper only used for md5 checksum sanitization to prevent config changes from impacting checksum
    private val objectMapper: ObjectMapper = JsonMapper
        .builder()
        .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
        .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
        .nodeFactory(SortingNodeFactory())
        .configure(SerializationFeature.INDENT_OUTPUT, false)
        .build()

    @EventListener(ApplicationReadyEvent::class)
    fun deployAll() {
        logger.info { "Running deployer" }

        changesetDeployers.asReversed().forEach { it.before() }

        changesetDeployers.forEach { changesetDeployer ->
            changelogService.loadResources(changesetDeployer.getPath()).forEach { resource ->
                val filename = changelogService.getFilename(resource)
                logger.info { "Running deployer changeset: $filename" }
                val resourceContent = resource.inputStream.bufferedReader().use { it.readText() }
                val resolvedResourceContent = resolveProperties(resourceContent)
                deploy(changesetDeployer, filename, resolvedResourceContent)
            }
        }
        logger.info { "Finished running deployer" }
    }

    fun deploy(changesetDeployer: ChangesetDeployer, filename: String, resourceContent: String) {
        try {
            changesetDeployer.getChangelogDetails(filename, resourceContent).forEach { changesetDetails ->
                // Parse as json to prevent whitespace changes from being detected as changes
                val reformattedContent = objectMapper.writeValueAsString(objectMapper.readTree(resourceContent))
                val md5sum = changelogService.computeMd5sum(reformattedContent)
                val legacyCheckSum = changelogService.computeMd5sum(changesetDetails.valueToChecksum)
                if (changelogService.isNewValidChangeset(changesetDetails.changesetId, md5sum, legacyCheckSum)) {
                    changesetDetails.deploy()
                    changelogService.saveChangeset(changesetDetails.changesetId, changesetDetails.key, filename, md5sum)
                }
            }
        } catch (e: Exception) {
            throw IllegalStateException("Failed to execute changelog: $filename", e)
        }
    }

    fun resolveProperties(content: String): String {
        var resolvedContent = content
        Regex("\\$\\{([^\\}]+)\\}").findAll(content)
            .map { it.groupValues }
            .forEach { (placeholder, placeholderValue) ->
                try {
                    val resolvedValue = environment.getProperty(placeholderValue)
                    if (!resolvedValue.isNullOrBlank()) {
                        resolvedContent = resolvedContent.replace(placeholder, resolvedValue)
                    }
                } catch (e: Exception) {
                    // ignored
                }
            }
        return resolvedContent
    }

    // Uses TreeMap when parsing to ObjectNode. Will sort keys alphabetically when serialized
    private class SortingNodeFactory : JsonNodeFactory() {
        override fun objectNode(): ObjectNode {
            return ObjectNode(this, TreeMap())
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }

}
