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
import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.domain.ObjectsListRowDto
import com.ritense.objectmanagement.repository.ObjectManagementRepository
import com.ritense.objecttypenapi.ObjecttypenApiPlugin
import com.ritense.plugin.domain.PluginConfigurationId
import com.ritense.plugin.service.PluginService
import java.util.UUID
import mu.KLogger
import mu.KotlinLogging
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ObjectManagementService(
    private val objectManagementRepository: ObjectManagementRepository,
    private val pluginService: PluginService
) {

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

    fun update(objectManagement: ObjectManagement): ObjectManagement =
        with(objectManagementRepository.findByTitle(objectManagement.title)) {
            if (this != null) {
                if (objectManagement.id != id) {
                    throw ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This title already exists. Please choose another title"
                    )
                }
            }
            objectManagementRepository.save(objectManagement)
        }

    fun getById(id: UUID): ObjectManagement? = objectManagementRepository.findByIdOrNull(id)

    fun getAll(): MutableList<ObjectManagement> = objectManagementRepository.findAll()

    fun deleteById(id: UUID) = objectManagementRepository.deleteById(id)

    fun getObjects(id: UUID, pageable: Pageable): PageImpl<ObjectsListRowDto> {
        val objectManagement = getById(id) ?: let {
            logger.info {
                "The requested Id is not configured as a objectnamagement configuration. " +
                    "The requested id was: $id"
            }
            throw NotFoundException()
        }

        val objectTypePluginInstance = pluginService
            .createInstance(
                PluginConfigurationId.existingId(objectManagement.objecttypenApiPluginConfigurationId)
            ) as ObjecttypenApiPlugin

        val objectenPluginInstance = pluginService
            .createInstance(
                PluginConfigurationId.existingId(objectManagement.objectenApiPluginConfigurationId)
            ) as ObjectenApiPlugin

        val objectsList = objectenPluginInstance.getObjectsByObjectTypeId(
            objectTypePluginInstance.url,
            objectenPluginInstance.url,
            objectManagement.objecttypeId,
            pageable
        )

        val objectsListDto = objectsList.results.map {
            ObjectsListRowDto(it.url.toString(), listOf(
                ObjectsListRowDto.ObjectsListItemDto("objectUrl", it.url),
                ObjectsListRowDto.ObjectsListItemDto("recordIndex", it.record.index ?: let {
                    logger.info {
                        "No index available"
                    }
                    throw throw NotFoundException()
                }),

            ))
        }

        return PageImpl(objectsListDto, pageable, objectsList.count.toLong())
    }

    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
    }
}