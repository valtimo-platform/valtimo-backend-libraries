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

package com.ritense.objectmanagement.service

import com.ritense.objectenapi.ObjectenApiPlugin
import com.ritense.objectenapi.client.Comparator
import com.ritense.objectenapi.client.ObjectSearchParameter
import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.domain.ObjectsListRowDto
import com.ritense.objectmanagement.domain.search.SearchWithConfigRequest
import com.ritense.objectmanagement.repository.ObjectManagementRepository
import com.ritense.objecttypenapi.ObjecttypenApiPlugin
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.service.PluginService
import com.ritense.search.domain.FieldType
import com.ritense.search.service.SearchFieldV2Service
import java.util.UUID
import mu.KLogger
import mu.KotlinLogging
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Transactional(readOnly = true)
class ObjectManagementService(
    private val objectManagementRepository: ObjectManagementRepository,
    private val pluginService: PluginService,
    private val searchFieldV2Service: SearchFieldV2Service
) {

    @Transactional
    fun create(objectManagement: ObjectManagement): ObjectManagement =
        with(objectManagementRepository.findByTitle(objectManagement.title)) {
            if (this != null) {
                throw ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This title already exists. Please choose another title"
                )
            }
            objectManagementRepository.save(objectManagement)
        }

    @Transactional
    fun update(objectManagement: ObjectManagement): ObjectManagement =
        with(objectManagementRepository.findByTitle(objectManagement.title)) {
            if (this != null && objectManagement.id != id) {
                throw ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This title already exists. Please choose another title"
                )
            }
            objectManagementRepository.save(objectManagement)
        }

    fun getById(id: UUID): ObjectManagement? = objectManagementRepository.findByIdOrNull(id)

    fun getAll(): List<ObjectManagement> = objectManagementRepository.findAll()

    @Transactional
    fun deleteById(id: UUID) = objectManagementRepository.deleteById(id)

    fun getObjects(id: UUID, pageable: Pageable): PageImpl<ObjectsListRowDto> {
        val objectManagement = getById(id) ?: let {
            logger.info {
                "The requested Id is not configured as a objectnamagement configuration. " +
                    "The requested id was: $id"
            }
            throw NotFoundException()
        }

        val objectTypePluginInstance = getObjectTypenApiPlugin(objectManagement.objecttypenApiPluginConfigurationId)

        val objectenPluginInstance = getObjectenApiPlugin(objectManagement.objectenApiPluginConfigurationId)

        val objectsList = objectenPluginInstance.getObjectsByObjectTypeId(
            objectTypePluginInstance.url,
            objectenPluginInstance.url,
            objectManagement.objecttypeId,
            pageable
        )

        val objectsListDto = objectsList.results.map {
            ObjectsListRowDto(
                it.url.toString(), listOf(
                    ObjectsListRowDto.ObjectsListItemDto("objectUrl", it.url),
                    ObjectsListRowDto.ObjectsListItemDto("recordIndex", it.record.index),

                    )
            )
        }

        return PageImpl(objectsListDto, pageable, objectsList.count.toLong())
    }

    fun getObjectsWithSearchParams(
        searchWithConfigRequest: SearchWithConfigRequest,
        id: UUID,
        pageable: Pageable
    ): PageImpl<ObjectsListRowDto> {
        val objectManagement = getById(id) ?: let {
            logger.info {
                "The requested Id is not configured as a objectnamagement configuration. " +
                    "The requested id was: $id"
            }
            throw NotFoundException()
        }

        val searchFieldList = searchFieldV2Service.findAllByOwnerId(id.toString())

        val searchDtoList = listOf<ObjectSearchParameter>()

        searchFieldList?.forEach {
            searchWithConfigRequest.otherFilters.forEach { searchWithConfigFilter ->
                if (searchWithConfigFilter.key == it.key) {
                    val values = searchWithConfigFilter.values
                    if (values.size > 1) {
                        values.forEach { value ->
                            searchDtoList + mapToObjectSearchParameter(it.key, it.fieldType, value.toString())
                        }
                    }
                    if (values.size == 1) {
                        searchDtoList + mapToObjectSearchParameter(it.key, it.fieldType, values[0].toString())
                    }
                }
            }
        }

        val searchString = concatenateObjectSearchParameter(searchDtoList)

        val objectTypePluginInstance = getObjectTypenApiPlugin(objectManagement.objecttypenApiPluginConfigurationId)

        val objectenPluginInstance = getObjectenApiPlugin(objectManagement.objectenApiPluginConfigurationId)

        val objectsList = objectenPluginInstance.getObjectsByObjectTypeIdWithSearchParams(
            objectTypePluginInstance.url,
            objectenPluginInstance.url,
            objectManagement.objecttypeId,
            searchString,
            pageable
        )

        val objectsListDto = objectsList.results.map {
            ObjectsListRowDto(
                it.url.toString(), listOf(
                    ObjectsListRowDto.ObjectsListItemDto("objectUrl", it.url),
                    ObjectsListRowDto.ObjectsListItemDto("recordIndex", it.record.index),

                    )
            )
        }

        return PageImpl(objectsListDto, pageable, objectsList.count.toLong())
    }

    private fun mapToObjectSearchParameter(key: String, fieldType: FieldType, value: Any): List<ObjectSearchParameter> {
        return when (fieldType) {
            FieldType.TEXT_CONTAINS -> listOf(ObjectSearchParameter(key, Comparator.STRING_CONTAINS, value.toString()))
            FieldType.RANGE -> listOf(
                ObjectSearchParameter(key, Comparator.GREATER_THAN_OR_EQUAL_TO, value.toString()),
                ObjectSearchParameter(key, Comparator.LOWER_THAN_OR_EQUAL_TO, value.toString())
            )

            FieldType.MULTI_SELECT_DROPDOWN -> {
                val returnList = listOf<ObjectSearchParameter>()
                val list = listOf<String>()
                list + value as List<String>
                list.forEach {
                    returnList + ObjectSearchParameter(key, Comparator.EQUAL_TO, it)
                }
                return returnList
            }
            FieldType.SINGLE_SELECT_DROPDOWN -> listOf(
                ObjectSearchParameter(key, Comparator.EQUAL_TO, value.toString())
            )
            FieldType.SINGLE -> listOf(
                ObjectSearchParameter(key, Comparator.STRING_CONTAINS, value.toString())
            )
        }
    }

    private fun concatenateObjectSearchParameter(searchParameterList: List<ObjectSearchParameter>): String {
        var searchList = ""
        searchParameterList.forEach {
            if (searchList == "") {
                searchList.plus(it.toQueryParameter())
            } else {
                searchList.plus(",${it.toQueryParameter()}")
            }
        }
        return searchList
    }

    private fun getObjectenApiPlugin(id: UUID) = pluginService
        .createInstance(
            PluginConfigurationId.existingId(id)
        ) as ObjectenApiPlugin

    private fun getObjectTypenApiPlugin(id: UUID) = pluginService
        .createInstance(
            PluginConfigurationId.existingId(id)
        ) as ObjecttypenApiPlugin

    fun findByObjectTypeId(id: String) = objectManagementRepository.findByObjecttypeId(id)


    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}