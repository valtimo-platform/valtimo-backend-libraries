/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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
import com.ritense.objectenapi.client.ObjectsList
import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.domain.ObjectsListRowDto
import com.ritense.objectmanagement.domain.search.SearchWithConfigRequest
import com.ritense.objectmanagement.repository.ObjectManagementRepository
import com.ritense.objecttypenapi.ObjecttypenApiPlugin
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.service.PluginService
import com.ritense.search.domain.DataType
import com.ritense.search.domain.FieldType
import com.ritense.search.service.SearchFieldV2Service
import com.ritense.search.service.SearchListColumnService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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
    private val searchFieldV2Service: SearchFieldV2Service,
    private val searchListColumnService: SearchListColumnService
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
                objectManagementRepository.save(objectManagement.copy(id = this.id))
            } else {
                objectManagementRepository.save(objectManagement)
            }
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

        val searchFieldList = searchFieldV2Service.findAllByOwnerId(id.toString())!!

        val searchDtoList = searchFieldList.flatMap { searchField ->
            searchWithConfigRequest.otherFilters
                .filter { otherFilter -> otherFilter.key == searchField.key }
                .flatMap { otherFilter ->
                    if (searchField.fieldType != FieldType.RANGE) {
                        if (otherFilter.values.size > 1) {
                            throw IllegalArgumentException("The objects api does not support the multiselect options")
                        }
                        otherFilter.values.flatMap { value ->
                            mapToObjectSearchParameter(searchField.key, searchField.fieldType, value.value, searchField.dataType)
                        }
                    } else {
                        mapToObjectSearchParameter(
                            searchField.key,
                            searchField.fieldType,
                            listOf(otherFilter.rangeFrom?.value, otherFilter.rangeTo?.value),
                            searchField.dataType
                        )
                    }
                }
        }

        val searchString = ObjectSearchParameter.toQueryParameter(searchDtoList)

        val objectTypePluginInstance = getObjectTypenApiPlugin(objectManagement.objecttypenApiPluginConfigurationId)

        val objectenPluginInstance = getObjectenApiPlugin(objectManagement.objectenApiPluginConfigurationId)

        val objectsList = objectenPluginInstance.getObjectsByObjectTypeIdWithSearchParams(
            objectTypePluginInstance.url,
            objectManagement.objecttypeId,
            searchString,
            pageable
        )

        val objectsListDto = mapToObjectListRowDto(objectsList, id)

        return PageImpl(objectsListDto, pageable, objectsList.count.toLong())
    }

    private fun mapToObjectListRowDto(
        objectsList: ObjectsList,
        objectManagementId: UUID
    ): List<ObjectsListRowDto> {
        val listColumns = searchListColumnService.findByOwnerId(objectManagementId.toString())
        return objectsList.results.map {objects ->
            val listRowDto = listColumns?.map {
                listColumn ->
                ObjectsListRowDto.ObjectsListItemDto(
                    listColumn.key,
                    objects.record.data?.at(listColumn.path)
                )
            }
            ObjectsListRowDto(objects.uuid.toString(), listRowDto!!)
        }
    }

    private fun mapToObjectSearchParameter(
        key: String,
        fieldType: FieldType,
        value: Any,
        dataType: DataType
    ): List<ObjectSearchParameter> {
        return when (fieldType) {
            FieldType.TEXT_CONTAINS -> listOf(
                ObjectSearchParameter(
                    key,
                    Comparator.STRING_CONTAINS,
                    castValueToDataType(dataType, value)
                )
            )

            FieldType.RANGE -> {
                (value as List<Any?>).mapIndexed { index, rangeValue ->
                    if (rangeValue != null) {
                        ObjectSearchParameter(
                            key,
                            if(index == 0) Comparator.GREATER_THAN_OR_EQUAL_TO else Comparator.LOWER_THAN_OR_EQUAL_TO,
                            castValueToDataType(dataType, rangeValue)
                        )
                    } else {
                        null
                    }
                }.filterNotNull()
            }

            FieldType.MULTI_SELECT_DROPDOWN -> {
                throw IllegalArgumentException("The objects api does not support the multiselect options")
            }

            FieldType.SINGLE_SELECT_DROPDOWN -> listOf(
                ObjectSearchParameter(key, Comparator.EQUAL_TO, castValueToDataType(dataType, value))
            )

            FieldType.SINGLE -> listOf(
                ObjectSearchParameter(key, Comparator.STRING_CONTAINS, castValueToDataType(dataType, value))
            )
        }
    }

    fun castValueToDataType(dataType: DataType, value: Any): String {
        return when (dataType) {
            DataType.TEXT -> value as String
            DataType.NUMBER -> {
                value as Number
                return value.toString()
            }

            DataType.BOOLEAN -> {
                value as Boolean
                return value.toString()
            }

            DataType.DATE -> {
                value as LocalDate
                return value.toString()
            }
            DataType.DATE_TIME -> {
                value as LocalDateTime
                return value.toString()
            }
            DataType.TIME -> {
                value as LocalTime
                return value.toString()
            }
        }

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