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

package com.ritense.objectmanagement.web.rest

import com.ritense.objectmanagement.domain.ObjectManagement
import com.ritense.objectmanagement.domain.ObjectsDto
import com.ritense.objectmanagement.service.ObjectManagementService
import java.util.UUID
import javax.validation.Valid
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/api/v1/object/management/configuration", produces = [MediaType.APPLICATION_JSON_VALUE])
class ObjectManagementResource(
    private val objectManagementService: ObjectManagementService
) {

    @PostMapping
    fun create(@Valid @RequestBody objectManagement: ObjectManagement): ResponseEntity<ObjectManagement> =
        ResponseEntity.ok(objectManagementService.create(objectManagement))

    @PutMapping
    fun update(@Valid @RequestBody objectManagement: ObjectManagement): ResponseEntity<ObjectManagement> =
        ResponseEntity.ok(objectManagementService.update(objectManagement))

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): ResponseEntity<ObjectManagement?> =
        ResponseEntity.ok(objectManagementService.getById(id))

    @GetMapping
    fun getAll(): ResponseEntity<MutableList<ObjectManagement>> = ResponseEntity.ok(objectManagementService.getAll())

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Any> {
        objectManagementService.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/object")
    fun getObjects(
        @PathVariable id: UUID,
        @PageableDefault pageable: Pageable
    ): ResponseEntity<PageImpl<ObjectsDto>> =
        ResponseEntity.ok(objectManagementService.getObjects(id, pageable))
}
