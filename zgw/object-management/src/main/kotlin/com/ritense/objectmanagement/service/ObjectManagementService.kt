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
import com.ritense.objectenapi.client.Comparator.EQUAL_TO
import com.ritense.objectenapi.client.Comparator.GREATER_THAN_OR_EQUAL_TO
import com.ritense.objectenapi.client.Comparator.LOWER_THAN_OR_EQUAL_TO
import com.ritense.objectenapi.client.Comparator.STRING_CONTAINS
import com.ritense.objectenapi.client.ObjectSearchParameter
import com.ritense.objectenapi.client.ObjectWrapper
import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.domain.ObjectsListRowDto
import com.ritense.objectmanagement.domain.search.SearchRequestValue
import com.ritense.objectmanagement.domain.search.SearchWithConfigFilter
import com.ritense.objectmanagement.domain.search.SearchWithConfigRequest
import com.ritense.objectmanagement.repository.ObjectManagementRepository
import com.ritense.objecttypenapi.ObjecttypenApiPlugin
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.service.PluginService
import com.ritense.search.domain.DataType.BOOLEAN
import com.ritense.search.domain.DataType.DATE
import com.ritense.search.domain.DataType.DATETIME
import com.ritense.search.domain.DataType.NUMBER
import com.ritense.search.domain.DataType.TEXT
import com.ritense.search.domain.DataType.TIME
import com.ritense.search.domain.FieldType.MULTI_SELECT_DROPDOWN
import com.ritense.search.domain.FieldType.RANGE
import com.ritense.search.domain.FieldType.SINGLE
import com.ritense.search.domain.FieldType.SINGLE_SELECT_DROPDOWN
import com.ritense.search.domain.FieldType.TEXT_CONTAINS
import com.ritense.search.domain.SearchFieldV2
import com.ritense.search.service.SearchFieldV2Service
import com.ritense.search.service.SearchListColumnService
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.UUID

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
            val result = objectManagementRepository.save(objectManagement)
            return result
        }

    @Transactional
    fun update(objectManagement: ObjectManagement): ObjectManagement =
        with(objectManagementRepository.findByTitle(objectManagement.title)) {
            val result = if (this != null && objectManagement.id != id) {
                objectManagementRepository.save(objectManagement.copy(id = this.id))
            } else {
                objectManagementRepository.save(objectManagement)
            }
            return result
        }

    fun getById(id: UUID): ObjectManagement? = objectManagementRepository.findByIdOrNull(id)

    fun getByTitle(title: String): ObjectManagement? = objectManagementRepository.findByTitle(title)

    fun getAll(): List<ObjectManagement> = objectManagementRepository.findAll()

    @Transactional
    fun deleteById(id: UUID) {
        objectManagementRepository.deleteById(id)
    }

    fun getObjects(id: UUID, pageable: Pageable): PageImpl<ObjectsListRowDto> {
        val objectManagement = getById(id) ?: let {
            throw IllegalArgumentException("The requested Id is not configured as a object management configuration. The requested id was: $id")
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
        val objectManagement = getById(id)
            ?: throw IllegalStateException("The requested Id is not configured as a object management configuration. The requested id was: $id")

        val searchFieldList = searchFieldV2Service.findAllByOwnerId(id.toString())!!

        val searchDtoList = searchFieldList.flatMap { searchField ->
            searchWithConfigRequest.otherFilters
                .filter { otherFilter -> otherFilter.key == searchField.key }
                .flatMap { otherFilter -> mapToObjectSearchParameters(searchField, otherFilter) }
        }

        val objectsList = getObjectsWithSearchParams(objectManagement, searchDtoList, pageable)

        val objectsListDto = mapToObjectListRowDto(objectsList.toList(), id)

        return PageImpl(objectsListDto, pageable, objectsList.totalElements)
    }

    fun getObjectsWithSearchParams(
        objectManagement: ObjectManagement,
        searchParameters: List<ObjectSearchParameter>,
        pageable: Pageable
    ): PageImpl<ObjectWrapper> {
        val searchString = ObjectSearchParameter.toQueryParameter(searchParameters)

        val objectTypePluginInstance = getObjectTypenApiPlugin(objectManagement.objecttypenApiPluginConfigurationId)

        val objectenPluginInstance = getObjectenApiPlugin(objectManagement.objectenApiPluginConfigurationId)

        val objectsList = objectenPluginInstance.getObjectsByObjectTypeIdWithSearchParams(
            objectTypePluginInstance.url,
            objectManagement.objecttypeId,
            searchString,
            pageable
        )

        return PageImpl(objectsList.results, pageable, objectsList.count.toLong())
    }

    private fun mapToObjectListRowDto(
        objectsList: List<ObjectWrapper>,
        objectManagementId: UUID
    ): List<ObjectsListRowDto> {
        val listColumns = searchListColumnService.findByOwnerId(objectManagementId.toString())
        return objectsList.map { objectApiObject ->
            val listRowDto = listColumns?.map { listColumn ->
                if (!listColumn.path.startsWith("object:/") && !listColumn.path.startsWith("/")) {
                    throw IllegalArgumentException("Unknown list column path prefix in: '${listColumn.path}'")
                }

                ObjectsListRowDto.ObjectsListItemDto(
                    listColumn.key,
                    objectApiObject.record.data?.at(listColumn.path.substringAfter(":"))
                )
            }
            ObjectsListRowDto(objectApiObject.uuid.toString(), listRowDto!!)
        }
    }

    private fun mapToObjectSearchParameters(
        searchField: SearchFieldV2,
        otherFilter: SearchWithConfigFilter,
    ): List<ObjectSearchParameter> {
        if (otherFilter.values.size > 1) {
            throw IllegalArgumentException("The objects api does not support the multiselect options")
        }

        return when (searchField.fieldType) {
            RANGE -> {
                val searchGte = mapToObjectSearchParameter(
                    searchField,
                    GREATER_THAN_OR_EQUAL_TO,
                    otherFilter.rangeFrom
                )
                val searchLte = mapToObjectSearchParameter(
                    searchField,
                    LOWER_THAN_OR_EQUAL_TO,
                    otherFilter.rangeTo
                )
                listOfNotNull(searchGte, searchLte)
            }

            SINGLE ->
                otherFilter.values.mapNotNull { value ->
                    if (searchField.dataType == TEXT || searchField.dataType == BOOLEAN) {
                        // Note: Implementations assume that TEXT + SINGLE should do a STRING_CONTAINS search
                        // Note: Searching for BOOLEAN types in the Objects API only works when using STRING_CONTAINS
                        mapToObjectSearchParameter(searchField, STRING_CONTAINS, value)
                    } else {
                        mapToObjectSearchParameter(searchField, EQUAL_TO, value)
                    }
                }

            TEXT_CONTAINS ->
                otherFilter.values.mapNotNull { value ->
                    mapToObjectSearchParameter(searchField, STRING_CONTAINS, value)
                }

            SINGLE_SELECT_DROPDOWN ->
                otherFilter.values.mapNotNull { value ->
                    mapToObjectSearchParameter(searchField, EQUAL_TO, value)
                }

            MULTI_SELECT_DROPDOWN ->
                throw IllegalArgumentException("The objects api does not support the multiselect options")

            else ->
                throw IllegalArgumentException("Unknown search field type '${searchField.fieldType}'")
        }
    }

    private fun mapToObjectSearchParameter(
        searchField: SearchFieldV2,
        comparator: Comparator,
        value: SearchRequestValue?
    ): ObjectSearchParameter? {
        return if (value?.value == null) {
            null
        } else {
            ObjectSearchParameter(
                mapToObjectApiPath(searchField.path),
                comparator,
                castValueToDataType(searchField, value)
            )
        }
    }

    private fun mapToObjectApiPath(jsonPointerPath: String): String {
        if (!jsonPointerPath.startsWith("object:/") && !jsonPointerPath.startsWith("/")) {
            throw IllegalArgumentException("Unknown search path prefix in: '${jsonPointerPath}'")
        }
        return jsonPointerPath
            .substringAfter("object:")
            .substringAfter("/")
            .replace("/", "__")
    }

    private fun castValueToDataType(searchField: SearchFieldV2, searchRequestValue: SearchRequestValue): String {
        val value = searchRequestValue.value!!
        return when (searchField.dataType) {
            TEXT -> value as String
            NUMBER -> if (value is String) value else (value as Number).toString()
            BOOLEAN -> if (value is String) value else (value as Boolean).toString()
            DATE -> parseDate(value)
            DATETIME -> parseDatetime(value, searchField)
            TIME -> parseTime(value, searchField)
        }
    }

    private fun parseDate(value: Any): String {
        return if (value is String) {
            LocalDate.parse(value).toString()
        } else {
            (value as LocalDate).toString()
        }
    }

    private fun parseDatetime(value: Any, searchField: SearchFieldV2): String {
        return if (value is String) {
            val dateTimeValue = ZonedDateTime.parse(value)
            if (searchField.fieldType == RANGE) {
                // Note: Objects API doesn't support field type 'RANGE' with data type 'DATETIME'
                dateTimeValue.toLocalDate().toString()
            } else {
                dateTimeValue.toString()
            }
        } else {
            (value as ZonedDateTime).toString()
        }
    }

    private fun parseTime(value: Any, searchField: SearchFieldV2): String {
        return if (searchField.fieldType == RANGE) {
            throw IllegalStateException("Objects API doesn't support field type 'RANGE' with data type 'TIME'")
        } else if (value is String) {
            LocalTime.parse(value).toString()
        } else {
            (value as LocalTime).toString()
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
}