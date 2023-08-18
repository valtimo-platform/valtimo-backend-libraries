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
import java.time.LocalDate
import java.util.UUID

@Service
class ObjectManagementFacade(
    private val objectManagementRepository: ObjectManagementRepository,
    private val pluginService: PluginService
) {
    val logger = KotlinLogging.logger {}

    fun getObjectByUuid(objectName: String, uuid: UUID): ObjectWrapper {
        val accessObject = getAccessObject(objectName)

        return findObjectByUuid(accessObject = accessObject, uuid = uuid)
    }

    fun getObjectsByUuids(objectName: String, uuids: List<UUID>): ObjectsList {
        val accessObject = getAccessObject(objectName)
        val objects = mutableListOf<ObjectWrapper>()

        uuids.forEach(){
            objects.add(findObjectByUuid(accessObject = accessObject, uuid = it))
        }

        return ObjectsList(count = objects.size, results = objects)
    }

    private fun findObjectByUuid(accessObject: ObjectManagementAccessObject, uuid: UUID): ObjectWrapper {
        val objectUrl = URI.create("${accessObject.objectenApiPlugin.url}objects/$uuid")

        logger.trace { "Getting object $objectUrl" }

        return accessObject.objectenApiPlugin.getObject(objectUrl)
    }

    fun getObjectByUri(objectName: String, objectUrl: URI): ObjectWrapper {
        val accessObject = getAccessObject(objectName)

        return findObjectByUri(accessObject = accessObject, objectUrl = objectUrl)
    }

    fun getObjectsByUris(objectName: String, objectUrls: List<URI>): ObjectsList {
        val accessObject = getAccessObject(objectName)
        val objects = mutableListOf<ObjectWrapper>()

        objectUrls.forEach(){
            objects.add(findObjectByUri(accessObject = accessObject, objectUrl = it))
        }

        return ObjectsList(count = objects.size, results = objects)
    }

    private fun findObjectByUri(accessObject: ObjectManagementAccessObject, objectUrl: URI): ObjectWrapper {
        logger.trace { "Getting object $objectUrl" }

        return accessObject.objectenApiPlugin.getObject(objectUrl)
    }

    fun getObjectsPaged(
        objectName: String,
        searchString: String?,
        pageNumber: Int,
        pageSize: Int
    ): ObjectsList {
        val accessObject = getAccessObject(objectName)

        return findObjectsPaged(
            accessObject = accessObject,
            objectName = objectName,
            searchString = searchString,
            pageNumber = pageNumber,
            pageSize = pageSize
        )
    }

    // Please use this function with caution, as it could result in poor application performance.
    // It is advised to use getObjectsPaged() instead, where possible.
    fun getObjectsUnpaged(
        objectName: String,
        searchString: String?
    ): ObjectsList {
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

    private fun findObjectsPaged(
        accessObject: ObjectManagementAccessObject,
        objectName: String,
        searchString: String?,
        pageNumber: Int,
        pageSize: Int
    ): ObjectsList {
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

    private fun getAccessObject(objectName: String): ObjectManagementAccessObject {
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

    private data class ObjectManagementAccessObject(
        val objectManagement: ObjectManagement,
        val objectenApiPlugin: ObjectenApiPlugin,
        val objectTypenApiPlugin: ObjecttypenApiPlugin
    )
}
