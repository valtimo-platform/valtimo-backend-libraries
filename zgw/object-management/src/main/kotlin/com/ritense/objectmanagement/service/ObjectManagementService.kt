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

import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.repository.ObjectManagementRepository
import java.net.URL
import java.util.UUID
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Transactional(readOnly = true)
class ObjectManagementService(
    private val objectManagementRepository: ObjectManagementRepository
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

    fun findByObjectTypeId(id: String) = objectManagementRepository.findByObjecttypeId(id)
}