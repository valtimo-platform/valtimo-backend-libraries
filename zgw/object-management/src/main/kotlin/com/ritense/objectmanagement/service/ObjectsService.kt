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
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Service
class ObjectsService(
    private val objectManagementRepository: ObjectManagementRepository,
    private val pluginService: PluginService
) {
    val logger = KotlinLogging.logger {}

    private val accessObjects: MutableMap<String, ObjectsApiAccessObject> = mutableMapOf()
    private val accessObjectTimeToLiveInSeconds = 300L

    // This function will clear the cache of Objects API access objects.
    // It should be called whenever a change to an ObjectManagement record is saved.
    fun clearCache() {
        accessObjects.clear()
    }

    fun getObjectByUuid(objectName: String, uuid: UUID): ObjectWrapper {
        val accessObject = getAccessObject(objectName)
        val objectUrl = URI.create("${accessObject.objectenApiPlugin.url}objects/$uuid")

        logger.trace { "Getting object $objectUrl" }

        return accessObject.objectenApiPlugin.getObject(objectUrl)
    }

    fun getObjectsByUuids(objectName: String, uuids: List<UUID>): ObjectsList {
        val objects = mutableListOf<ObjectWrapper>()

        uuids.forEach(){
            objects.add(getObjectByUuid(objectName, it))
        }

        return ObjectsList(count = objects.size, results = objects)
    }

    fun getObjectByUri(objectName: String, objectUrl: URI): ObjectWrapper {
        val accessObject = getAccessObject(objectName)

        logger.trace { "Getting object $objectUrl" }

        return accessObject.objectenApiPlugin.getObject(objectUrl)
    }

    fun getObjectsByUris(objectName: String, objectUrls: List<URI>): ObjectsList {
        val objects = mutableListOf<ObjectWrapper>()

        objectUrls.forEach(){
            objects.add(getObjectByUri(objectName, it))
        }

        return ObjectsList(count = objects.size, results = objects)
    }

    fun getObjectsPaged(
        objectName: String,
        searchString: String?,
        pageNumber: Int,
        pageSize: Int
    ): ObjectsList {
        val accessObject = getAccessObject(objectName)

        return if (!searchString.isNullOrBlank()) {
            logger.trace { "Getting object page for object type $objectName with search string $searchString" }

            accessObject.objectenApiPlugin.getObjectsByObjectTypeIdWithSearchParams(
                accessObject.objectTypenApiPlugin.url,
                accessObject.objectManagement.objecttypeId,
                searchString,
                PageRequest.of(pageNumber, pageSize)
            )
        } else {
            logger.trace { "Getting object page for object type $objectName" }

            accessObject.objectenApiPlugin.getObjectsByObjectTypeId(
                accessObject.objectTypenApiPlugin.url,
                accessObject.objectenApiPlugin.url,
                accessObject.objectManagement.objecttypeId,
                PageRequest.of(pageNumber, pageSize)
            )
        }
    }

    // Please use this function with caution, as it could result in poor application performance.
    // It is advised to use getObjectsPaged() instead, where possible.
    fun getObjectsUnpaged(
        objectName: String,
        searchString: String?
    ): ObjectsList {
        var pageNumber = 0
        var totalResults = ObjectsList(
            results = listOf(),
            count = 0
        )

        do {
            val iterationResult = getObjectsPaged(objectName, searchString, pageNumber, 500)

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
        data: JsonNode
    ): ObjectWrapper {
        val accessObject = getAccessObject(objectName)
        val objectTypeUrl =
            accessObject.objectTypenApiPlugin.getObjectTypeUrlById(accessObject.objectManagement.objecttypeId)

        val objectRequest = ObjectRequest(
            objectTypeUrl,
            ObjectRecord(
                typeVersion = accessObject.objectManagement.objecttypeVersion,
                data = data,
                startAt = LocalDate.now()
            )
        )

        try {
            logger.trace { "Creating object $objectRequest" }

            return accessObject.objectenApiPlugin.createObject(objectRequest)
        } catch (ex: WebClientResponseException) {
            throw Exception("Exception thrown while making a call to the Objects API. Response from the API: ${ex.responseBodyAsString}")
        }
    }

    private fun getAccessObject(objectName: String): ObjectsApiAccessObject {
        if (!accessObjects.containsKey(objectName) || isAccessObjectTimeToLiveExpired(objectName)) {
            initializeAccessObject(objectName)
        }

        return accessObjects.getValue(objectName)
    }

    private fun isAccessObjectTimeToLiveExpired(objectName: String): Boolean {
        return accessObjects.getValue(objectName).createdTime < Instant.now().minusSeconds(accessObjectTimeToLiveInSeconds)
    }

    private fun initializeAccessObject(objectName: String) {
        val objectManagement = objectManagementRepository.findByTitle(objectName)
            ?: throw NoSuchElementException("Object type $objectName is not found in Object Management.")
        val objectenApiPlugin =
            pluginService.createInstance<ObjectenApiPlugin>(objectManagement.objectenApiPluginConfigurationId)
        val objectTypenApiPlugin =
            pluginService.createInstance<ObjecttypenApiPlugin>(objectManagement.objecttypenApiPluginConfigurationId)

        val accessObject = ObjectsApiAccessObject(
            objectManagement,
            objectenApiPlugin,
            objectTypenApiPlugin,
            Instant.now()
        )

        accessObjects.put(objectName, accessObject)
    }

    private data class ObjectsApiAccessObject(
        val objectManagement: ObjectManagement,
        val objectenApiPlugin: ObjectenApiPlugin,
        val objectTypenApiPlugin: ObjecttypenApiPlugin,
        val createdTime: Instant
    )
}
