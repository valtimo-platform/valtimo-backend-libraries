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

package com.ritense.objectmanagement.service

import com.fasterxml.jackson.databind.JsonNode
import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectenapi.client.ObjectRecord
import com.ritense.objectenapi.client.ObjectRequest
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objectenapi.client.ObjectsList
import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.repository.ObjectManagementRepository
import com.ritense.objecttypenapi.ObjecttypenApiPlugin
import com.ritense.plugin.service.PluginService
import mu.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.LocalDate
import java.util.UUID

class ObjectManagementFacade(
    private val objectManagementRepository: ObjectManagementRepository,
    private val pluginService: PluginService
) {
    fun getObjectByUuid(objectName: String, uuid: UUID): ObjectWrapper {
        logger.debug { "Get object by UUID objectName=$objectName uuid=$uuid" }
        val accessObject = getAccessObject(objectName)
        return findObjectByUuid(accessObject = accessObject, uuid = uuid)
    }

    fun getObjectsByUuids(objectName: String, uuids: List<UUID>): ObjectsList {
        logger.debug { "Get object by UUIDs objectName=$objectName uuids=$uuids" }
        val accessObject = getAccessObject(objectName)
        val objects = uuids.map { findObjectByUuid(accessObject = accessObject, uuid = it) }
        return ObjectsList(count = objects.size, results = objects)
    }

    fun getObjectByUri(objectName: String, objectUrl: URI): ObjectWrapper {
        logger.debug { "Get object by URI objectName=$objectName objectUrl=$objectUrl" }
        val accessObject = getAccessObject(objectName)
        return findObjectByUri(accessObject = accessObject, objectUrl = objectUrl)
    }

    fun getObjectsByUris(objectName: String, objectUrls: List<URI>): ObjectsList {
        logger.debug { "Get object by URIs objectName=$objectName objectUrls=$objectUrls" }
        val accessObject = getAccessObject(objectName)
        val objects = mutableListOf<ObjectWrapper>()
        objectUrls.forEach {
            objects.add(findObjectByUri(accessObject = accessObject, objectUrl = it))
        }
        return ObjectsList(count = objects.size, results = objects)
    }

    fun getObjectsPaged(
        objectName: String,
        searchString: String?,
        pageNumber: Int,
        ordering: String?,
        pageSize: Int
    ): ObjectsList {
        logger.debug {
            "get objects paged objectName=$objectName " +
                "searchString=$searchString, pageNumber=$pageNumber, pageSize=$pageSize"
        }
        val accessObject = getAccessObject(objectName)
        return findObjectsPaged(
            accessObject = accessObject,
            objectName = objectName,
            searchString = searchString,
            ordering = ordering,
            pageNumber = pageNumber,
            pageSize = pageSize
        )
    }

    // Please use this function with caution, as it could result in poor application performance.
    // It is advised to use getObjectsPaged() instead, where possible.
    fun getObjectsUnpaged(
        objectName: String,
        searchString: String?,
        ordering: String?
    ): ObjectsList {
        logger.debug { "get objects unpaged objectName=$objectName searchString=$searchString" }
        val accessObject = getAccessObject(objectName)

        var pageNumber = 0
        var totalResults = ObjectsList(
            results = listOf(),
            count = 0
        )

        do {
            val iterationResult = findObjectsPaged(
                accessObject = accessObject,
                objectName = objectName,
                searchString = searchString,
                ordering = ordering,
                pageNumber = pageNumber,
                pageSize = 500
            )

            var combinedResults = totalResults.results
            combinedResults += iterationResult.results
            totalResults = ObjectsList(
                results = combinedResults,
                count = combinedResults.size
            )

            pageNumber++
        } while (iterationResult.next != null)

        return totalResults
    }

    fun createObject(
        objectName: String,
        data: JsonNode,
        objectId: UUID? = null
    ): ObjectWrapper {
        logger.info { "Create object objectName=$objectName objectId=$objectId" }
        val accessObject = getAccessObject(objectName)
        val objectTypeUrl = accessObject.objectTypenApiPlugin.getObjectTypeUrlById(
            accessObject.objectManagement.objecttypeId
        )

        val objectRequest = ObjectRequest(
            objectId,
            objectTypeUrl,
            ObjectRecord(
                typeVersion = accessObject.objectManagement.objecttypeVersion,
                data = data,
                startAt = LocalDate.now()
            )
        )

        try {
            return accessObject.objectenApiPlugin.createObject(objectRequest)
        } catch (ex: RestClientResponseException) {
            throw Exception("Exception thrown while making a call to the Objects API. Response from the API: ${ex.responseBodyAsString}")
        }
    }

    fun updateObject(
        objectId: UUID,
        objectName: String,
        data: JsonNode,
    ): ObjectWrapper {
        logger.info { "Update object objectId=$objectId objectName=$objectName" }
        val accessObject = getAccessObject(objectName)
        val objectTypeUrl = accessObject.objectTypenApiPlugin.getObjectTypeUrlById(
            accessObject.objectManagement.objecttypeId
        )

        val objectRequest = ObjectRequest(
            objectTypeUrl,
            ObjectRecord(
                typeVersion = accessObject.objectManagement.objecttypeVersion,
                data = data,
                startAt = LocalDate.now()
            )
        )

        try {
            return accessObject.objectenApiPlugin
                .objectUpdate(
                    URI(
                        accessObject.objectenApiPlugin.url.toString() + "objects/" + objectId
                    ),
                    objectRequest
                )
        } catch (ex: RestClientResponseException) {
            throw Exception("Error while updating object ${objectId}. Response from Objects API: ${ex.responseBodyAsString}")
        }
    }

    fun deleteObject(
        objectName: String,
        objectId: UUID
    ): HttpStatus {
        val accessObject = getAccessObject(objectName)

        try {
            logger.trace { "Deleting object '$objectId' of type '${accessObject.objectManagement.objecttypeId}' from Objecten API using plugin ${accessObject.objectManagement.objectenApiPluginConfigurationId}" }
            return accessObject.objectenApiPlugin.deleteObject(
                URI("${accessObject.objectenApiPlugin.url}objects/$objectId")
            )
        } catch (ex: HttpClientErrorException) {
            throw IllegalStateException("Error while deleting object $objectId. Response from Objects API: ${ex.responseBodyAsString}", ex)
        }
    }

    private fun getAccessObject(objectName: String): ObjectManagementAccessObject {
        logger.debug { "Get access object objectName=$objectName" }
        val objectManagement = objectManagementRepository.findByTitle(objectName)
            ?: throw NoSuchElementException("Object type $objectName is not found in Object Management.")
        val objectenApiPlugin =
            pluginService.createInstance<ObjectenApiPlugin>(objectManagement.objectenApiPluginConfigurationId)
        val objectTypenApiPlugin =
            pluginService.createInstance<ObjecttypenApiPlugin>(objectManagement.objecttypenApiPluginConfigurationId)

        return ObjectManagementAccessObject(
            objectManagement,
            objectenApiPlugin,
            objectTypenApiPlugin
        )
    }

    private fun findObjectByUuid(accessObject: ObjectManagementAccessObject, uuid: UUID): ObjectWrapper {
        logger.debug { "Find object by uuid accessObject=$accessObject uuid=$uuid" }
        val objectUrl = UriComponentsBuilder
            .fromUri(accessObject.objectenApiPlugin.url)
            .pathSegment("objects")
            .pathSegment(uuid.toString())
            .build()
            .toUri()

        logger.trace { "Getting object $objectUrl" }
        return accessObject.objectenApiPlugin.getObject(objectUrl)
    }

    private fun findObjectByUri(accessObject: ObjectManagementAccessObject, objectUrl: URI): ObjectWrapper {
        logger.debug { "Getting object $objectUrl" }
        return accessObject.objectenApiPlugin.getObject(objectUrl)
    }

    private fun findObjectsPaged(
        accessObject: ObjectManagementAccessObject,
        objectName: String,
        searchString: String?,
        ordering: String? = "",
        pageNumber: Int,
        pageSize: Int
    ): ObjectsList {
        return if (!searchString.isNullOrBlank()) {
            logger.debug { "Getting object page for object type $objectName with search string $searchString" }

            accessObject.objectenApiPlugin.getObjectsByObjectTypeIdWithSearchParams(
                accessObject.objectTypenApiPlugin.url,
                accessObject.objectManagement.objecttypeId,
                searchString,
                ordering,
                PageRequest.of(pageNumber, pageSize)
            )
        } else {
            logger.debug { "Getting object page for object type $objectName" }

            accessObject.objectenApiPlugin.getObjectsByObjectTypeId(
                accessObject.objectTypenApiPlugin.url,
                accessObject.objectenApiPlugin.url,
                accessObject.objectManagement.objecttypeId,
                ordering,
                PageRequest.of(pageNumber, pageSize)
            )
        }
    }

    private data class ObjectManagementAccessObject(
        val objectManagement: ObjectManagement,
        val objectenApiPlugin: ObjectenApiPlugin,
        val objectTypenApiPlugin: ObjecttypenApiPlugin
    )

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
